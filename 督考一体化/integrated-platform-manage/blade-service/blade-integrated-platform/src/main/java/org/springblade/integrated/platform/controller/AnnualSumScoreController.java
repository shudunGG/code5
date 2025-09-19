package org.springblade.integrated.platform.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.excel.SyAnnualExcel;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 考核评价-年度首页 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/20 22:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("AnnualSumScore")
@Api(value = "考核评价-年度首页", tags = "考核评价-年度首页控制层代码")
public class AnnualSumScoreController extends BladeController {

	@Autowired
	private IAnnualSumScoreService iAnnualSumScoreService;

	@Autowired
	private IAnnualEvaluationService iAnnualEvaluationService;
	private final ILeaderAppriseService iLeaderAppriseService;
	//加分项服务类
	private final IScoreAddService iScoreAddService;
	//减分项服务类
	private final IScoreMinusService iScoreMinusService;
	private final IQuarterlySumScoreService iQuarterlySumScoreService;
	private final IAppriseDeptService iAppriseDeptService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserClient userClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详细信息
	 */
	@GetMapping("/detailApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-App年度首页详细信息", notes = "传入 annualSumScore")
	public R<Map<String, Map<String, Object>>> detailApp(AnnualSumScore annualSumScore) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(currentUser.getRoleId()).toString();
		//sql查询条件
		QueryWrapper<AnnualSumScore> queryWrapper = new QueryWrapper<AnnualSumScore>();
		queryWrapper.select(
			" check_classify,check_classify_name,\n" +
				"       responsible_unit_id,responsible_unit_name,\n" +
				"       appraise_deptid,serv_name,annual_year,\n" +
				"       sum(zzsxjs_score) as zzsxjs_score,\n" +
				"       sum(ldnl_score) as ldnl_score,\n" +
				"       sum(ddjs_score) as ddjs_score,\n" +
				"       sum(xqgzlfz_score) as xqgzlfz_score,\n" +
				"       sum(szgzlfz_score) as szgzlfz_score,\n" +
				"       sum(add_score) as add_score,\n" +
				"       sum(minus_score) as minus_score,\n" +
				"       sum(jdpj_score) as jdpj_score,\n" +
				"       sum(leader_score) as leader_score,\n" +
				"       sum(annual_sum_score) as annual_sum_score");
		//【季度评价得分（jdpj_score）存的就是总分，所以不需要相加】
		queryWrapper.eq(annualSumScore.getAnnualYear()!=null && !Objects.equals(annualSumScore.getAnnualYear(), ""),"annual_year",annualSumScore.getAnnualYear());
		queryWrapper.eq(annualSumScore.getCheckClassify()!=null && !Objects.equals(annualSumScore.getCheckClassify(), ""),"check_classify",annualSumScore.getCheckClassify());
		queryWrapper.eq(StringUtils.isNotEmpty(annualSumScore.getResponsibleUnitId()),"responsible_unit_id",annualSumScore.getResponsibleUnitId());
		//没有打分详情角色的账号要发布过后才能查看分数
		if (!roleNames.contains(isLookRole)) {
			queryWrapper.apply(" is_send = 1");
		}
		queryWrapper.groupBy("responsible_unit_name");//,"annual_sum_score"
		queryWrapper.orderByDesc("annual_sum_score");
		List<AnnualSumScore> detail = iAnnualSumScoreService.list(queryWrapper);

		//当这个值为0的时候表示年度的分数没有发布，列表不展示季度的分数
		int isShowQuarterScore = detail.size();

		for (int i = 0; i < detail.size(); i++) {
			//TODO 领导评价得分 总分 × 20%
			double leaderScore = 0.0;
			QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper =new QueryWrapper<>();
			leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
			leaderAppriseQueryWrapper.eq(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			leaderAppriseQueryWrapper.eq("evaluation_type","1");
			leaderAppriseQueryWrapper.like("create_time",annualSumScore.getAnnualYear());
			leaderAppriseQueryWrapper.groupBy("apprise_rolename");
			List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
			try {
				if (leaderApprise.size() > 0) {
					for (LeaderApprise leaderApprise1 : leaderApprise) {
						leaderScore += Double.parseDouble(leaderApprise1.getScore());
					}
				}
				leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
				leaderScore = leaderScore * 0.2;
			} catch (NullPointerException nullPointerException) {
				leaderScore = 0.0;
				System.out.println("领导评价得分为空！");
			}

			leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
			detail.get(i).setLeaderScore(leaderScore);

			//TODO 加分项总得分
			double addScore = 0.0;
			QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
			queryWrapperAdd.select(" * ");
			queryWrapperAdd.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			queryWrapperAdd.like("create_time",annualSumScore.getAnnualYear());
			queryWrapperAdd.eq("check_way","2");
			queryWrapperAdd.apply("isok=1");
			List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
			if (scoreAddList.size() > 0) {
				for (ScoreAdd scoreAdd : scoreAddList) {
					addScore += Double.parseDouble(scoreAdd.getScore());
				}
			}
			//加分项
			//addScore = addScore * 0.1;
			addScore = Double.parseDouble(String.format("%.2f", addScore));
			detail.get(i).setAddScore(addScore);

			//TODO 减分项
			double minusScore = 0.0;
			QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
			queryWrapperMinus.select(" * ");
			queryWrapperMinus.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			queryWrapperMinus.like("create_time",annualSumScore.getAnnualYear());
			queryWrapperMinus.eq("check_way","2");
			List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
			if (scoreMinusList.size() > 0) {
				for (ScoreMinus scoreMinus : scoreMinusList) {
					minusScore += Double.parseDouble(scoreMinus.getScore());
				}
			}
			//minusScore = minusScore * 0.1;
			minusScore = Double.parseDouble(String.format("%.2f", minusScore));
			detail.get(i).setMinusScore(minusScore);

			//TODO 季度评价得分，获取今年【每季度10%】的分数
			double quarterlySumScore = 0.0;
			QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
			queryWrapper1.select(" check_classify,check_classify_name,\n" +
				"       responsible_unit_id,responsible_unit_name,\n" +
				"        serv_name,stage,stage_year,\n" +
				"       sum(quarterly_sum_score) as quarterly_sum_score ");
			queryWrapper1.eq(annualSumScore.getAnnualYear()!=null && !Objects.equals(annualSumScore.getAnnualYear(), ""),"stage_year",annualSumScore.getAnnualYear());
			queryWrapper1.eq(annualSumScore.getCheckClassify()!=null && !Objects.equals(annualSumScore.getCheckClassify(), ""),"check_classify",annualSumScore.getCheckClassify());
			queryWrapper1.eq(detail.get(i).getResponsibleUnitId()!=null,"responsible_unit_id",detail.get(i).getResponsibleUnitId());
			queryWrapper1.groupBy("stage");
			List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);

			if (quarterlySumScoreList.size() > 0) {
				for (int j = 0; j < quarterlySumScoreList.size(); j++) {
					//TODO 领导评价得分 总分 × 20%
					double jdleaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 =new QueryWrapper<>();
					leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
					leaderAppriseQueryWrapper1.eq("evaluation_type","2");
					leaderAppriseQueryWrapper1.like("apprise_year",annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
					//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
					List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
					try {
						if (leaderApprise2.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise2) {
								jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
						jdleaderScore = jdleaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						jdleaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
					quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


					//TODO 加分项总得分
					double jdaddScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
					queryWrapperAdda.select(" * ");
					queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
					queryWrapperAdda.like("create_time",annualSumScore.getAnnualYear());
					queryWrapperAdda.eq("check_way","1");
					queryWrapperAdda.apply("isok=1");
					List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
					if (scoreAddLista.size() > 0) {
						for (int jj = 0; jj < scoreAddLista.size(); jj++) {
							jdaddScore+=Double.parseDouble(scoreAddLista.get(jj).getScore());
						}
					}
					//jdaddScore = jdaddScore * 0.1;
					jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
					quarterlySumScoreList.get(j).setAddScore(jdaddScore);

					//TODO 减分项
					double jdminusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
					queryWrapperMinusa.select(" * ");
					queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
					queryWrapperMinusa.like("create_time",annualSumScore.getAnnualYear());
					queryWrapperMinusa.eq("check_way","1");
					List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
					if (scoreMinusLista.size() > 0) {
						for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
							jdminusScore+=Double.parseDouble(scoreMinusLista.get(jj).getScore());
						}
					}
					//jdminusScore = jdminusScore * 0.1;
					jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
					quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

					/*//TODO 督察督办分数
					double dcdbScore = 0.0;
					//把所有县区的单位查出来
					List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1").getData();
					String deptIds = "";
					if (gDeptids.size() > 0) {
						for (int k = 0; k < gDeptids.size(); k++) {
							deptIds+=gDeptids.get(k).getId()+",";
						}
					}
					String deptId = quarterlySumScoreList.get(j).getResponsibleUnitId();
					//如果当前单位的id属于县区，那就查找子部门的分数
					String deptId1 = deptId;
					if (deptIds.contains(deptId)) {
						R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
						List<Dept> deptChildRData = deptChildR.getData();
						if (deptChildRData.size() > 0) {
							for (int jj = 0; jj < deptChildRData.size(); jj++) {
								deptId1+= "," +deptChildRData.get(jj).getId();
							}
						}
					}
					String[] deptIdss = deptId1.split(",");
					QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
					dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
					//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
					dcdbqueryWrapper.in("dept_id",deptIdss);
					dcdbqueryWrapper.likeRight("create_time",annualSumScore.getAnnualYear());
					dcdbqueryWrapper.groupBy("dept_id,serv_code");

					List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
					int size = 0;
					if (supervisionScores.size() > 0) {
						for (int jj = 0; jj < supervisionScores.size(); jj++) {
							LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
								.select(SupervisionInfo::getFlowStatus)
								.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
							SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
							if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
								dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
								size++;
							}
						}
					}
					//根据supervisionScores长度计算dcdbScore的平均分
					if (supervisionScores.size() > 0) {
						if (size > 0) {
							dcdbScore = dcdbScore / size;
						}
					}
					//将督查督办得分的10%算到季度评价中
					dcdbScore = dcdbScore * 0.1;
					dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
					if(dcdbScore < 10.0) {
						if(dcdbScore < 0) {
							quarterlySumScoreList.get(j).setDcdbScore(0.0);
						}else {
							quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
						}
					}else {
						quarterlySumScoreList.get(j).setDcdbScore(10.0);
					}*/


					//TODO 保留两位小数
					quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
					quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
					quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

					//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
					double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore; //+ quarterlySumScoreList.get(j).getDcdbScore()
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));
					quarterlySumScore +=sumScore;
				}
			}

			//计算出今年季度的分数
			quarterlySumScore = quarterlySumScore * 0.1;
			quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
			//向年度得分表中添加季度得分
			if (isShowQuarterScore == 0) {
				detail.get(i).setJdpjScore(0.0);
			} else {
				detail.get(i).setJdpjScore(quarterlySumScore);
			}

			//保留两位小数
			detail.get(i).setZzsxjsScore(Double.parseDouble(String.format("%.2f", detail.get(i).getZzsxjsScore())));
			detail.get(i).setLdnlScore(Double.parseDouble(String.format("%.2f", detail.get(i).getLdnlScore())));
			detail.get(i).setDdjsScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDdjsScore())));
			detail.get(i).setSzgzlfzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getSzgzlfzScore())));
			detail.get(i).setXqgzlfzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getXqgzlfzScore())));

			//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
			double sumScore = detail.get(i).getAnnualSumScore() + detail.get(i).getJdpjScore() + leaderScore + addScore - minusScore;
			sumScore = Double.parseDouble(String.format("%.2f", sumScore));
			detail.get(i).setAnnualSumScore(sumScore);
		}

		//TODO 把其他未评价的部门添加进去
		//拿到detail中所有的【责任单位】
		String[] strings = new String[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			strings[i] = detail.get(i).getResponsibleUnitName();
		}
		//去重
		List<String> list1 = new ArrayList<String>();
		for (String v : strings) {
			if (!list1.contains(v)) {
				list1.add(v);
			}
		}
		//获取当前年份
		int year = DateTime.now().year();
		int year1 = Integer.parseInt(annualSumScore.getAnnualYear());
		//判断查询的年份是否小于当前年份
		if (year1 >= year) {
			//获取分组后的单位id
			R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000",annualSumScore.getCheckClassify(), annualSumScore.getAnnualYear());
			List<Dept> depts = Rdeptids.getData();
			if (depts.size() > 0 && StringUtils.isEmpty(annualSumScore.getResponsibleUnitId())) {
				for (int i = 0; i < depts.size(); i++) {
					String deptId = depts.get(i).getId().toString();
					String deptName = depts.get(i).getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "1");
					leaderAppriseQueryWrapper.like("create_time", annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper.groupBy("apprise_rolename");
					List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
					try {
						if (leaderApprise.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise) {
								leaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
						leaderScore = leaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						leaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));

					//TODO 加分项总得分
					double addScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
					queryWrapperAdd.select(" * ");
					queryWrapperAdd.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperAdd.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperAdd.eq("check_way", "2");
					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (ScoreAdd scoreAdd : scoreAddList) {
							addScore += Double.parseDouble(scoreAdd.getScore());
						}
					}
					//加分项
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperMinus.eq("check_way", "2");
					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (ScoreMinus scoreMinus : scoreMinusList) {
							minusScore += Double.parseDouble(scoreMinus.getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

					//TODO 季度评价得分，获取今年【每季度10%】的分数
					double quarterlySumScore = 0.0;
					QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
					queryWrapper1.select(" check_classify,check_classify_name,\n" +
						"       responsible_unit_id,responsible_unit_name,\n" +
						"        serv_name,stage,stage_year,\n" +
						"       sum(quarterly_sum_score) as quarterly_sum_score ");
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getAnnualYear()), "stage_year", annualSumScore.getAnnualYear());
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getCheckClassify()), "check_classify", annualSumScore.getCheckClassify());
					queryWrapper1.eq(StringUtils.isNotBlank(deptId), "responsible_unit_id", deptId);
					queryWrapper1.groupBy("stage");
					List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
					if (quarterlySumScoreList.size() > 0) {
						for (int j = 0; j < quarterlySumScoreList.size(); j++) {
							//TODO 领导评价得分 总分 × 20%
							double jdleaderScore = 0.0;
							QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
							leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
							leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
							leaderAppriseQueryWrapper1.eq("evaluation_type", "2");
							leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
							leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
							//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
							List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
							try {
								if (leaderApprise2.size() > 0) {
									for (LeaderApprise leaderApprise1 : leaderApprise2) {
										jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
									}
								}
								jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
								jdleaderScore = jdleaderScore * 0.2;
							} catch (NullPointerException nullPointerException) {
								jdleaderScore = 0.0;
								System.out.println("领导评价得分为空！");
							}

							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


							//TODO 加分项总得分
							double jdaddScore = 0.0;
							QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
							queryWrapperAdda.select(" * ");
							queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
							queryWrapperAdda.eq("check_way", "1");
							queryWrapperAdda.apply("isok=1");
							List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
							if (scoreAddLista.size() > 0) {
								for (int jj = 0; jj < scoreAddLista.size(); jj++) {
									jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
								}
							}
							//jdaddScore = jdaddScore * 0.1;
							jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
							quarterlySumScoreList.get(j).setAddScore(jdaddScore);

							//TODO 减分项
							double jdminusScore = 0.0;
							QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
							queryWrapperMinusa.select(" * ");
							queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
							queryWrapperMinusa.eq("check_way", "1");
							List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
							if (scoreMinusLista.size() > 0) {
								for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
									jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
								}
							}
							//jdminusScore = jdminusScore * 0.1;
							jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
							quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

							/*//TODO 督察督办分数
							double dcdbScore = 0.0;
							//把所有县区的单位查出来
							List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
							String deptIds = "";
							if (gDeptids.size() > 0) {
								for (int k = 0; k < gDeptids.size(); k++) {
									deptIds += gDeptids.get(k).getId() + ",";
								}
							}
							String deptIdsss = quarterlySumScoreList.get(j).getResponsibleUnitId();
							//如果当前单位的id属于县区，那就查找子部门的分数
							String deptId1 = deptIdsss;
							if (deptIds.contains(deptIdsss)) {
								R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
								List<Dept> deptChildRData = deptChildR.getData();
								if (deptChildRData.size() > 0) {
									for (int jj = 0; jj < deptChildRData.size(); jj++) {
										deptId1 += "," + deptChildRData.get(jj).getId();
									}
								}
							}
							String[] deptIdss = deptId1.split(",");
							QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
							dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
							//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
							dcdbqueryWrapper.in("dept_id", deptIdss);
							dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
							dcdbqueryWrapper.groupBy("dept_id,serv_code");

							List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
							int size = 0;
							if (supervisionScores.size() > 0) {
								for (int jj = 0; jj < supervisionScores.size(); jj++) {
									LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
										.select(SupervisionInfo::getFlowStatus)
										.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
									SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
									if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
										dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
										size++;
									}
								}
							}
							//根据supervisionScores长度计算dcdbScore的平均分
							if (supervisionScores.size() > 0) {
								if (size > 0) {
									dcdbScore = dcdbScore / size;
								}
							}
							//将督查督办得分的10%算到季度评价中
							dcdbScore = dcdbScore * 0.1;
							dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
							if (dcdbScore < 10.0) {
								if (dcdbScore < 0) {
									quarterlySumScoreList.get(j).setDcdbScore(0.0);
								} else {
									quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
								}
							} else {
								quarterlySumScoreList.get(j).setDcdbScore(10.0);
							}*/


							//TODO 保留两位小数
							quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
							quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
							quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

							//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
							double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore;// + quarterlySumScoreList.get(j).getDcdbScore()
							sumScore = Double.parseDouble(String.format("%.2f", sumScore));
							quarterlySumScore += sumScore;
						}
					}
					else {
						//TODO 领导评价得分 总分 × 20%
						double jdleaderScore = 0.0;
						QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
						leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
						leaderAppriseQueryWrapper1.eq( "dept_id", deptId);
						leaderAppriseQueryWrapper1.eq("evaluation_type", "2");
						leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
						leaderAppriseQueryWrapper1.groupBy("apprise_rolename");;
						//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
						List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
						try {
							if (leaderApprise2.size() > 0) {
								for (LeaderApprise leaderApprise1 : leaderApprise2) {
									jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
								}
							}
							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							jdleaderScore = jdleaderScore * 0.2;
						} catch (NullPointerException nullPointerException) {
							jdleaderScore = 0.0;
							System.out.println("领导评价得分为空！");
						}

						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));


						//TODO 加分项总得分
						double jdaddScore = 0.0;
						QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
						queryWrapperAdda.select(" * ");
						queryWrapperAdda.like( "dept_id", deptId);
						queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperAdda.eq("check_way", "1");
						queryWrapperAdda.apply("isok=1");
						List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
						if (scoreAddLista.size() > 0) {
							for (int jj = 0; jj < scoreAddLista.size(); jj++) {
								jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
							}
						}
						//jdaddScore = jdaddScore * 0.1;
						jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));

						//TODO 减分项
						double jdminusScore = 0.0;
						QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
						queryWrapperMinusa.select(" * ");
						queryWrapperMinusa.like( "dept_id", deptId);
						queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperMinusa.eq("check_way", "1");
						List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
						if (scoreMinusLista.size() > 0) {
							for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
								jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
							}
						}
						//jdminusScore = jdminusScore * 0.1;
						jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));

						/*//TODO 督察督办分数
						double dcdbScore = 0.0;
						//把所有县区的单位查出来
						List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
						String deptIds = "";
						if (gDeptids.size() > 0) {
							for (int k = 0; k < gDeptids.size(); k++) {
								deptIds += gDeptids.get(k).getId() + ",";
							}
						}
						String deptIdsss = deptId;
						//如果当前单位的id属于县区，那就查找子部门的分数
						String deptId1 = deptIdsss;
						if (deptIds.contains(deptIdsss)) {
							R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
							List<Dept> deptChildRData = deptChildR.getData();
							if (deptChildRData.size() > 0) {
								for (int jj = 0; jj < deptChildRData.size(); jj++) {
									deptId1 += "," + deptChildRData.get(jj).getId();
								}
							}
						}
						String[] deptIdss = deptId1.split(",");
						QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
						dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
						//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
						dcdbqueryWrapper.in("dept_id", deptIdss);
						dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
						dcdbqueryWrapper.groupBy("dept_id,serv_code");

						List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
						int size = 0;
						if (supervisionScores.size() > 0) {
							for (int jj = 0; jj < supervisionScores.size(); jj++) {
								LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
									.select(SupervisionInfo::getFlowStatus)
									.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
								SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
								if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
									dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
									size++;
								}
							}
						}
						//根据supervisionScores长度计算dcdbScore的平均分
						if (supervisionScores.size() > 0) {
							if (size > 0) {
								dcdbScore = dcdbScore / size;
							}
						}
						//将督查督办得分的10%算到季度评价中
						dcdbScore = dcdbScore * 0.1;
						dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
						if (dcdbScore < 10.0) {
							if (dcdbScore < 0) {
								dcdbScore = 0.0;
							}
						} else {
							dcdbScore = 10.0;
						}*/

						//TODO 计算总分 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
						double sumScore = jdleaderScore + jdaddScore - jdminusScore;// + dcdbScore
						sumScore = Double.parseDouble(String.format("%.2f", sumScore));
						quarterlySumScore = sumScore;
					}
					//计算出今年季度的分数
					quarterlySumScore = quarterlySumScore * 0.1;
					quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
					//判断年度得分是否发布，未发布的话就不计算总分
					if (isShowQuarterScore == 0) {
						quarterlySumScore = 0.0;
					}
					//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
					double sumScore = quarterlySumScore + leaderScore + addScore - minusScore;
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					if (list1.size() > 0) {
						if (!list1.toString().contains(deptName)) {
							AnnualSumScore annualSumScore1 = new AnnualSumScore();
							annualSumScore1.setResponsibleUnitId(deptId);
							annualSumScore1.setResponsibleUnitName(deptName);
							annualSumScore1.setZzsxjsScore(0.0);
							annualSumScore1.setLdnlScore(0.0);
							annualSumScore1.setDdjsScore(0.0);
							annualSumScore1.setXqgzlfzScore(0.0);
							annualSumScore1.setSzgzlfzScore(0.0);
							annualSumScore1.setAddScore(addScore);
							annualSumScore1.setMinusScore(minusScore);
							annualSumScore1.setLeaderScore(leaderScore);
							annualSumScore1.setJdpjScore(quarterlySumScore);
							annualSumScore1.setAnnualType(0);
							detail.add(annualSumScore1);
						}
					}
					else if (list1.size() == 0) {
						AnnualSumScore annualSumScore1 = new AnnualSumScore();
						annualSumScore1.setResponsibleUnitId(deptId);
						annualSumScore1.setResponsibleUnitName(deptName);
						annualSumScore1.setZzsxjsScore(0.0);
						annualSumScore1.setLdnlScore(0.0);
						annualSumScore1.setDdjsScore(0.0);
						annualSumScore1.setXqgzlfzScore(0.0);
						annualSumScore1.setSzgzlfzScore(0.0);
						annualSumScore1.setAddScore(addScore);
						annualSumScore1.setMinusScore(minusScore);
						annualSumScore1.setLeaderScore(leaderScore);
						annualSumScore1.setJdpjScore(quarterlySumScore);
						annualSumScore1.setAnnualSumScore(sumScore);
						annualSumScore1.setAnnualType(0);
						detail.add(annualSumScore1);
					}
				}
			}
			else if(StringUtils.isNotEmpty(annualSumScore.getResponsibleUnitId())){
				Dept dept = SysCache.getDept(Long.valueOf(annualSumScore.getResponsibleUnitId()));
				if (dept != null) {
					String deptId = dept.getId().toString();
					String deptName = dept.getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "1");
					leaderAppriseQueryWrapper.like("create_time", annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper.groupBy("apprise_rolename");
					List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
					try {
						if (leaderApprise.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise) {
								leaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
						leaderScore = leaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						leaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));

					//TODO 加分项总得分
					double addScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
					queryWrapperAdd.select(" * ");
					queryWrapperAdd.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperAdd.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperAdd.eq("check_way", "2");
					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (ScoreAdd scoreAdd : scoreAddList) {
							addScore += Double.parseDouble(scoreAdd.getScore());
						}
					}
					//加分项
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperMinus.eq("check_way", "2");
					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (ScoreMinus scoreMinus : scoreMinusList) {
							minusScore += Double.parseDouble(scoreMinus.getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

					//TODO 季度评价得分，获取今年【每季度10%】的分数
					double quarterlySumScore = 0.0;
					QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
					queryWrapper1.select(" check_classify,check_classify_name,\n" +
						"       responsible_unit_id,responsible_unit_name,\n" +
						"        serv_name,stage,stage_year,\n" +
						"       sum(quarterly_sum_score) as quarterly_sum_score ");
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getAnnualYear()), "stage_year", annualSumScore.getAnnualYear());
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getCheckClassify()), "check_classify", annualSumScore.getCheckClassify());
					queryWrapper1.eq(StringUtils.isNotBlank(deptId), "responsible_unit_id", deptId);
					queryWrapper1.groupBy("stage");
					List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
					if (quarterlySumScoreList.size() > 0) {
						for (int j = 0; j < quarterlySumScoreList.size(); j++) {
							//TODO 领导评价得分 总分 × 20%
							double jdleaderScore = 0.0;
							QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 =new QueryWrapper<>();
							leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
							leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
							leaderAppriseQueryWrapper1.eq("evaluation_type","2");
							leaderAppriseQueryWrapper1.like("apprise_year",annualSumScore.getAnnualYear());
							leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
							//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
							List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
							try {
								if (leaderApprise2.size() > 0) {
									for (LeaderApprise leaderApprise1 : leaderApprise2) {
										jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
									}
								}
								jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
								jdleaderScore = jdleaderScore * 0.2;
							} catch (NullPointerException nullPointerException) {
								jdleaderScore = 0.0;
								System.out.println("领导评价得分为空！");
							}

							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


							//TODO 加分项总得分
							double jdaddScore = 0.0;
							QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
							queryWrapperAdda.select(" * ");
							queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperAdda.like("create_time",annualSumScore.getAnnualYear());
							queryWrapperAdda.eq("check_way","1");
							queryWrapperAdda.apply("isok=1");
							List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
							if (scoreAddLista.size() > 0) {
								for (int jj = 0; jj < scoreAddLista.size(); jj++) {
									jdaddScore+=Double.parseDouble(scoreAddLista.get(jj).getScore());
								}
							}
							//jdaddScore = jdaddScore * 0.1;
							jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
							quarterlySumScoreList.get(j).setAddScore(jdaddScore);

							//TODO 减分项
							double jdminusScore = 0.0;
							QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
							queryWrapperMinusa.select(" * ");
							queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperMinusa.like("create_time",annualSumScore.getAnnualYear());
							queryWrapperMinusa.eq("check_way","1");
							List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
							if (scoreMinusLista.size() > 0) {
								for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
									jdminusScore+=Double.parseDouble(scoreMinusLista.get(jj).getScore());
								}
							}
							//jdminusScore = jdminusScore * 0.1;
							jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
							quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

							/*//TODO 督察督办分数
							double dcdbScore = 0.0;
							//把所有县区的单位查出来
							List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1").getData();
							String deptIds = "";
							if (gDeptids.size() > 0) {
								for (int k = 0; k < gDeptids.size(); k++) {
									deptIds+=gDeptids.get(k).getId()+",";
								}
							}
							String deptIdsss = quarterlySumScoreList.get(j).getResponsibleUnitId();
							//如果当前单位的id属于县区，那就查找子部门的分数
							String deptId1 = deptIdsss;
							if (deptIds.contains(deptIdsss)) {
								R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
								List<Dept> deptChildRData = deptChildR.getData();
								if (deptChildRData.size() > 0) {
									for (int jj = 0; jj < deptChildRData.size(); jj++) {
										deptId1+= "," +deptChildRData.get(jj).getId();
									}
								}
							}
							String[] deptIdss = deptId1.split(",");
							QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
							dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
							//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
							dcdbqueryWrapper.in("dept_id",deptIdss);
							dcdbqueryWrapper.likeRight("create_time",annualSumScore.getAnnualYear());
							dcdbqueryWrapper.groupBy("dept_id,serv_code");

							List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
							int size = 0;
							if (supervisionScores.size() > 0) {
								for (int jj = 0; jj < supervisionScores.size(); jj++) {
									LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
										.select(SupervisionInfo::getFlowStatus)
										.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
									SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
									if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
										dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
										size++;
									}
								}
							}
							//根据supervisionScores长度计算dcdbScore的平均分
							if (supervisionScores.size() > 0) {
								if (size > 0) {
									dcdbScore = dcdbScore / size;
								}
							}
							//将督查督办得分的10%算到季度评价中
							dcdbScore = dcdbScore * 0.1;
							dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
							if(dcdbScore < 10.0) {
								if(dcdbScore < 0) {
									quarterlySumScoreList.get(j).setDcdbScore(0.0);
								}else {
									quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
								}
							}else {
								quarterlySumScoreList.get(j).setDcdbScore(10.0);
							}*/


							//TODO 保留两位小数
							quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
							quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
							quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

							//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
							double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore; //+ quarterlySumScoreList.get(j).getDcdbScore()
							sumScore = Double.parseDouble(String.format("%.2f", sumScore));
							quarterlySumScore +=sumScore;
						}
					}
					else {
						//TODO 领导评价得分 总分 × 20%
						double jdleaderScore = 0.0;
						QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
						leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
						leaderAppriseQueryWrapper1.eq( "dept_id", deptId);
						leaderAppriseQueryWrapper1.eq("evaluation_type", "2");
						leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
						leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
						//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
						List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
						try {
							if (leaderApprise2.size() > 0) {
								for (LeaderApprise leaderApprise1 : leaderApprise2) {
									jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
								}
							}
							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							jdleaderScore = jdleaderScore * 0.2;
						} catch (NullPointerException nullPointerException) {
							jdleaderScore = 0.0;
							System.out.println("领导评价得分为空！");
						}

						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));


						//TODO 加分项总得分
						double jdaddScore = 0.0;
						QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
						queryWrapperAdda.select(" * ");
						queryWrapperAdda.like( "dept_id", deptId);
						queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperAdda.eq("check_way", "1");
						queryWrapperAdda.apply("isok=1");
						List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
						if (scoreAddLista.size() > 0) {
							for (int jj = 0; jj < scoreAddLista.size(); jj++) {
								jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
							}
						}
						//jdaddScore = jdaddScore * 0.1;
						jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));

						//TODO 减分项
						double jdminusScore = 0.0;
						QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
						queryWrapperMinusa.select(" * ");
						queryWrapperMinusa.like( "dept_id", deptId);
						queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperMinusa.eq("check_way", "1");
						List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
						if (scoreMinusLista.size() > 0) {
							for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
								jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
							}
						}
						//jdminusScore = jdminusScore * 0.1;
						jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));

						/*//TODO 督察督办分数
						double dcdbScore = 0.0;
						//把所有县区的单位查出来
						List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
						String deptIds = "";
						if (gDeptids.size() > 0) {
							for (int k = 0; k < gDeptids.size(); k++) {
								deptIds += gDeptids.get(k).getId() + ",";
							}
						}
						String deptIdsss = deptId;
						//如果当前单位的id属于县区，那就查找子部门的分数
						String deptId1 = deptIdsss;
						if (deptIds.contains(deptIdsss)) {
							R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
							List<Dept> deptChildRData = deptChildR.getData();
							if (deptChildRData.size() > 0) {
								for (int jj = 0; jj < deptChildRData.size(); jj++) {
									deptId1 += "," + deptChildRData.get(jj).getId();
								}
							}
						}
						String[] deptIdss = deptId1.split(",");
						QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
						dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
						//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
						dcdbqueryWrapper.in("dept_id", deptIdss);
						dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
						dcdbqueryWrapper.groupBy("dept_id,serv_code");

						List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
						int size = 0;
						if (supervisionScores.size() > 0) {
							for (int jj = 0; jj < supervisionScores.size(); jj++) {
								LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
									.select(SupervisionInfo::getFlowStatus)
									.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
								SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
								if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
									dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
									size++;
								}
							}
						}
						//根据supervisionScores长度计算dcdbScore的平均分
						if (supervisionScores.size() > 0) {
							if (size > 0) {
								dcdbScore = dcdbScore / size;
							}
						}
						//将督查督办得分的10%算到季度评价中
						dcdbScore = dcdbScore * 0.1;
						dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
						if (dcdbScore < 10.0) {
							if (dcdbScore < 0) {
								dcdbScore = 0.0;
							}
						} else {
							dcdbScore = 10.0;
						}*/

						//TODO 计算总分 领导评价得分 + 加分项 - 减分项 + 督查督办得分
						double sumScore =  jdleaderScore + jdaddScore - jdminusScore;// + dcdbScore
						sumScore = Double.parseDouble(String.format("%.2f", sumScore));
						quarterlySumScore = sumScore;
					}
					//计算出今年季度的分数
					quarterlySumScore = quarterlySumScore * 0.1;
					quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
					//判断年度得分是否发布，未发布的话就不计算总分
					if (isShowQuarterScore == 0) {
						quarterlySumScore = 0.0;
					}
					//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
					double sumScore = quarterlySumScore + leaderScore + addScore - minusScore;
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					AnnualSumScore annualSumScore1 = new AnnualSumScore();
					annualSumScore1.setResponsibleUnitId(deptId);
					annualSumScore1.setResponsibleUnitName(deptName);
					annualSumScore1.setZzsxjsScore(0.0);
					annualSumScore1.setLdnlScore(0.0);
					annualSumScore1.setDdjsScore(0.0);
					annualSumScore1.setXqgzlfzScore(0.0);
					annualSumScore1.setSzgzlfzScore(0.0);
					annualSumScore1.setAddScore(addScore);
					annualSumScore1.setMinusScore(minusScore);
					annualSumScore1.setLeaderScore(leaderScore);
					annualSumScore1.setJdpjScore(quarterlySumScore);
					annualSumScore1.setAnnualSumScore(sumScore);
					annualSumScore1.setAnnualType(0);
					detail.add(annualSumScore1);
				}
			}
		}

		//给总得分排名次
		double[] annualscore = new double[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			annualscore[i] = detail.get(i).getAnnualSumScore();
		}
		//去重
		List<Double> list = new ArrayList<Double>();
		for (double v : annualscore) {
			if (!list.contains(v)) {
				list.add(v);
			}
		}

		//先升序排列
		Collections.sort(list);
		//然后降序排列，得到list[0]最大
		Collections.reverse(list);
		//把所有的排名都拿到再进行进一步筛选
		Map<String, Map<String, Object>> map = new HashMap();
		for (int i = 0; i < list.size(); i++) {
			for (AnnualSumScore sumScore : detail) {
				Map<String, Object> map1 = new HashMap();
				if (sumScore.getAnnualSumScore().equals(list.get(i))) {
					//排序
					sumScore.setAnnualType(i + 1);
					//总分
					map1.put("sumScore",sumScore.getAnnualSumScore());
					//排名
					map1.put("ranking",sumScore.getAnnualType());
					map.put(sumScore.getResponsibleUnitId().toString(),map1);
				}
			}
		}
		//筛选出当前责任单位的排名和分数
		Map<String, Map<String, Object>> map2 = null;
		if (annualSumScore.getResponsibleUnitId()!=null) {
			String rid = annualSumScore.getResponsibleUnitId().toString();
			map2 = new HashMap<>();
			if (map.containsKey(rid)) {
				map2.put(rid,map.get(rid));
			};
		}

		if (map2 != null) {
			return R.data(map2);
		} else {
			return R.data(map);
		}

	}

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-年度首页详细信息", notes = "传入 annualSumScore")
	public R<List<AnnualSumScore>> detail(AnnualSumScore annualSumScore){
		R<List<AnnualSumScore>> rList = SyDetail(annualSumScore);
		return rList;
	}

	/**
	 * 详细信息
	 */
	@PostMapping("/detailApplication")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-年度首页详细信息", notes = "传入 annualSumScore")
	public R detailApplication(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("详细信息-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			AnnualSumScore annualSumScore = objectMapper.convertValue(jsonParams, AnnualSumScore.class);
			List<AnnualSumScore> rList = SyDetail(annualSumScore).getData();
			JSONArray jsonArray = objectMapper.convertValue(rList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 导出详细信息
	 */
	@GetMapping("/exportDetail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-导出年度首页详细信息", notes = "传入 annualSumScore")
	public void exportDetail(AnnualSumScore annualSumScore, HttpServletResponse response){
		R<List<AnnualSumScore>> rList = SyDetail(annualSumScore);
		List<AnnualSumScore> list = rList.getData();
		List<SyAnnualExcel> syAnnualExcels = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SyAnnualExcel syAnnualExcel = new SyAnnualExcel();
			syAnnualExcel.setZrdw(list.get(i).getResponsibleUnitName());
			syAnnualExcel.setGzlfz(String.valueOf(list.get(i).getGzlfzScore()));
			syAnnualExcel.setZzsxjs(String.valueOf(list.get(i).getZzsxjsScore()));
			syAnnualExcel.setLdnl(String.valueOf(list.get(i).getLdnlScore()));
			syAnnualExcel.setDdjs(String.valueOf(list.get(i).getDdjsScore()));
			syAnnualExcel.setJiaFen(String.valueOf(list.get(i).getAddScore()));
			syAnnualExcel.setJianFen(String.valueOf(list.get(i).getMinusScore()));
			syAnnualExcel.setLdpj(String.valueOf(list.get(i).getLeaderScore()));
			syAnnualExcel.setJdpjdf(String.valueOf(list.get(i).getJdpjScore()));
			syAnnualExcel.setZf(String.valueOf(list.get(i).getAnnualSumScore()));
			syAnnualExcel.setPm(String.valueOf(list.get(i).getAnnualType()));
			syAnnualExcels.add(syAnnualExcel);
		}
		String fileName = annualSumScore.getCheckClassifyName() + "-" + annualSumScore.getAnnualYear() + "-" + "得分详情";
		ExcelUtil.export(response, fileName , "季度评价得分详情", syAnnualExcels, SyAnnualExcel.class);
	}

	public R<List<AnnualSumScore>> SyDetail(AnnualSumScore annualSumScore) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(currentUser.getRoleId()).toString();

		//当前角色是否包含 打分详情 或者 考核绩效发布
		boolean flag = roleNames.contains(isLookRole) || roleNames.contains("考核绩效发布");

		//sql查询条件
		QueryWrapper<AnnualSumScore> queryWrapper = new QueryWrapper<AnnualSumScore>();
		queryWrapper.select(
			" check_classify,check_classify_name,\n" +
				"       is_send,\n" +
				"       responsible_unit_id,responsible_unit_name,\n" +
				"       appraise_deptid,serv_name,annual_year,\n" +
				"       sum(zzsxjs_score) as zzsxjs_score,\n" +
				"       sum(ldnl_score) as ldnl_score,\n" +
				"       sum(ddjs_score) as ddjs_score,\n" +
				"       sum(xqgzlfz_score) as xqgzlfz_score,\n" +
				"       sum(szgzlfz_score) as szgzlfz_score,\n" +
				"       sum(add_score) as add_score,\n" +
				"       sum(minus_score) as minus_score,\n" +
				"       sum(jdpj_score) as jdpj_score,\n" +
				"       sum(leader_score) as leader_score,\n" +
				"       sum(annual_sum_score) as annual_sum_score");
		//【季度评价得分（jdpj_score）存的就是总分，所以不需要相加】
		queryWrapper.eq(annualSumScore.getAnnualYear()!=null && !Objects.equals(annualSumScore.getAnnualYear(), ""),"annual_year",annualSumScore.getAnnualYear());
		queryWrapper.eq(annualSumScore.getCheckClassify()!=null && !Objects.equals(annualSumScore.getCheckClassify(), ""),"check_classify",annualSumScore.getCheckClassify());
		queryWrapper.eq(StringUtils.isNotEmpty(annualSumScore.getResponsibleUnitId()),"responsible_unit_id",annualSumScore.getResponsibleUnitId());
		//没有打分详情角色的账号要发布过后才能查看分数
		if (!roleNames.contains(isLookRole)) {
			queryWrapper.apply(" is_send = 1");
		}
		queryWrapper.groupBy("responsible_unit_name");//,"annual_sum_score"
		queryWrapper.orderByDesc("annual_sum_score");
		List<AnnualSumScore> detail = iAnnualSumScoreService.list(queryWrapper);

		int isShowQuarterScore = detail.size();
		for (int i = 0; i < detail.size(); i++) {
			//TODO 领导评价得分 总分 × 20%
			double leaderScore = 0.0;
			QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper =new QueryWrapper<>();
			leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
			leaderAppriseQueryWrapper.eq(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			leaderAppriseQueryWrapper.eq("evaluation_type","1");

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {leaderAppriseQueryWrapper.eq("is_send","1");}

			leaderAppriseQueryWrapper.like("create_time",annualSumScore.getAnnualYear());
			leaderAppriseQueryWrapper.groupBy("apprise_rolename");
			List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
			try {
				if (leaderApprise.size() > 0) {
					for (LeaderApprise leaderApprise1 : leaderApprise) {
						leaderScore += Double.parseDouble(leaderApprise1.getScore());
					}
				}
				leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
				leaderScore = leaderScore * 0.2;
			} catch (NullPointerException nullPointerException) {
				leaderScore = 0.0;
				System.out.println("领导评价得分为空！");
			}

			leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
			detail.get(i).setLeaderScore(leaderScore);

			//TODO 加分项总得分
			double addScore = 0.0;
			QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
			queryWrapperAdd.select(" * ");
			queryWrapperAdd.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			queryWrapperAdd.like("create_time",annualSumScore.getAnnualYear());
			queryWrapperAdd.eq("check_way","2");

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {queryWrapperAdd.eq("is_send","1");}

			queryWrapperAdd.apply("isok=1");
			List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
			if (scoreAddList.size() > 0) {
				for (ScoreAdd scoreAdd : scoreAddList) {
					addScore += Double.parseDouble(scoreAdd.getScore());
				}
			}
			//加分项
			//addScore = addScore * 0.1;
			addScore = Double.parseDouble(String.format("%.2f", addScore));
			detail.get(i).setAddScore(addScore);

			//TODO 减分项
			double minusScore = 0.0;
			QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
			queryWrapperMinus.select(" * ");
			queryWrapperMinus.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			queryWrapperMinus.like("create_time",annualSumScore.getAnnualYear());
			queryWrapperMinus.eq("check_way","2");

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {queryWrapperMinus.eq("is_send","1");}

			List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
			if (scoreMinusList.size() > 0) {
				for (ScoreMinus scoreMinus : scoreMinusList) {
					minusScore += Double.parseDouble(scoreMinus.getScore());
				}
			}
			//minusScore = minusScore * 0.1;
			minusScore = Double.parseDouble(String.format("%.2f", minusScore));
			detail.get(i).setMinusScore(minusScore);

			//TODO 季度评价得分，获取今年【每季度10%】的分数
			double quarterlySumScore = 0.0;
			QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
			queryWrapper1.select(" check_classify,check_classify_name,\n" +
				"       responsible_unit_id,responsible_unit_name,\n" +
				"        serv_name,stage,stage_year,\n" +
				"       sum(quarterly_sum_score) as quarterly_sum_score ");
			queryWrapper1.eq(annualSumScore.getAnnualYear()!=null && !Objects.equals(annualSumScore.getAnnualYear(), ""),"stage_year",annualSumScore.getAnnualYear());
			queryWrapper1.eq(annualSumScore.getCheckClassify()!=null && !Objects.equals(annualSumScore.getCheckClassify(), ""),"check_classify",annualSumScore.getCheckClassify());
			queryWrapper1.eq(detail.get(i).getResponsibleUnitId()!=null,"responsible_unit_id",detail.get(i).getResponsibleUnitId());

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {queryWrapper1.eq("is_send","1");}

			queryWrapper1.groupBy("stage");
			List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);

			if (quarterlySumScoreList.size() > 0) {
				for (int j = 0; j < quarterlySumScoreList.size(); j++) {
					//TODO 领导评价得分 总分 × 20%
					double jdleaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 =new QueryWrapper<>();
					leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
					leaderAppriseQueryWrapper1.eq("evaluation_type","2");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {leaderAppriseQueryWrapper1.eq("is_send","1");}

					leaderAppriseQueryWrapper1.like("apprise_year",annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
					//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
					List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
					try {
						if (leaderApprise2.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise2) {
								jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
						jdleaderScore = jdleaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						jdleaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
					quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


					//TODO 加分项总得分
					double jdaddScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
					queryWrapperAdda.select(" * ");
					queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
					queryWrapperAdda.like("create_time",annualSumScore.getAnnualYear());
					queryWrapperAdda.eq("check_way","1");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperAdda.eq("is_send","1");}

					queryWrapperAdda.apply("isok=1");
					List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
					if (scoreAddLista.size() > 0) {
						for (int jj = 0; jj < scoreAddLista.size(); jj++) {
							jdaddScore+=Double.parseDouble(scoreAddLista.get(jj).getScore());
						}
					}
					//jdaddScore = jdaddScore * 0.1;
					jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
					quarterlySumScoreList.get(j).setAddScore(jdaddScore);

					//TODO 减分项
					double jdminusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
					queryWrapperMinusa.select(" * ");
					queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
					queryWrapperMinusa.like("create_time",annualSumScore.getAnnualYear());
					queryWrapperMinusa.eq("check_way","1");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperMinusa.eq("is_send","1");}

					List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
					if (scoreMinusLista.size() > 0) {
						for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
							jdminusScore+=Double.parseDouble(scoreMinusLista.get(jj).getScore());
						}
					}
					//jdminusScore = jdminusScore * 0.1;
					jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
					quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

					/*//TODO 督察督办分数
					double dcdbScore = 0.0;
					//把所有县区的单位查出来
					List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1").getData();
					String deptIds = "";
					if (gDeptids.size() > 0) {
						for (int k = 0; k < gDeptids.size(); k++) {
							deptIds+=gDeptids.get(k).getId()+",";
						}
					}
					String deptId = quarterlySumScoreList.get(j).getResponsibleUnitId();
					//如果当前单位的id属于县区，那就查找子部门的分数
					String deptId1 = deptId;
					if (deptIds.contains(deptId)) {
						R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
						List<Dept> deptChildRData = deptChildR.getData();
						if (deptChildRData.size() > 0) {
							for (int jj = 0; jj < deptChildRData.size(); jj++) {
								deptId1+= "," +deptChildRData.get(jj).getId();
							}
						}
					}
					String[] deptIdss = deptId1.split(",");
					QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
					dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
					//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
					dcdbqueryWrapper.in("dept_id",deptIdss);
					dcdbqueryWrapper.likeRight("create_time",annualSumScore.getAnnualYear());
					dcdbqueryWrapper.groupBy("dept_id,serv_code");

					List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
					int size = 0;
					if (supervisionScores.size() > 0) {
						for (int jj = 0; jj < supervisionScores.size(); jj++) {
							LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
								.select(SupervisionInfo::getFlowStatus)
								.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
							SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
							if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
								dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
								size++;
							}
						}
					}
					//根据supervisionScores长度计算dcdbScore的平均分
					if (supervisionScores.size() > 0) {
						if (size > 0) {
							dcdbScore = dcdbScore / size;
						}
					}
					//将督查督办得分的10%算到季度评价中
					dcdbScore = dcdbScore * 0.1;
					dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
					if(dcdbScore < 10.0) {
						if(dcdbScore < 0) {
							quarterlySumScoreList.get(j).setDcdbScore(0.0);
						}else {
							quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
						}
					}else {
						quarterlySumScoreList.get(j).setDcdbScore(10.0);
					}*/


					//TODO 保留两位小数
					quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
					quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
					quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

					//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
					double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore;// + quarterlySumScoreList.get(j).getDcdbScore()
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));
					quarterlySumScore +=sumScore;
				}
			}

			//计算出今年季度的分数
			quarterlySumScore = quarterlySumScore * 0.1;
			quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
			//判断年度得分是否发布，未发布的话就不计算总分
			if (isShowQuarterScore == 0) {
				quarterlySumScore = 0.0;
			}
			detail.get(i).setJdpjScore(quarterlySumScore);

			//保留两位小数
			detail.get(i).setZzsxjsScore(Double.parseDouble(String.format("%.2f", detail.get(i).getZzsxjsScore())));
			detail.get(i).setLdnlScore(Double.parseDouble(String.format("%.2f", detail.get(i).getLdnlScore())));
			detail.get(i).setDdjsScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDdjsScore())));
			detail.get(i).setSzgzlfzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getSzgzlfzScore())));
			detail.get(i).setXqgzlfzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getXqgzlfzScore())));

			//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
			double sumScore = detail.get(i).getAnnualSumScore() + quarterlySumScore + leaderScore + addScore - minusScore;
			sumScore = Double.parseDouble(String.format("%.2f", sumScore));
			detail.get(i).setAnnualSumScore(sumScore);
		}

		//TODO 把其他未评价的部门添加进去
		//拿到detail中所有的【责任单位】
		String[] strings = new String[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			strings[i] = detail.get(i).getResponsibleUnitName();
		}
		//去重
		List<String> list1 = new ArrayList<String>();
		for (String v : strings) {
			if (!list1.contains(v)) {
				list1.add(v);
			}
		}
		//获取当前年份
		int year = DateTime.now().year();
		int year1 = Integer.parseInt(annualSumScore.getAnnualYear());
		//判断查询的年份是否小于当前年份
		/*if (year1 >= year) {*/
			//获取分组后的单位id
			R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000",annualSumScore.getCheckClassify(), annualSumScore.getAnnualYear());
			List<Dept> depts = Rdeptids.getData();
			if (depts.size() > 0 && StringUtils.isEmpty(annualSumScore.getResponsibleUnitId())) {
				for (int i = 0; i < depts.size(); i++) {
					String deptId = depts.get(i).getId().toString();
					String deptName = depts.get(i).getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "1");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {leaderAppriseQueryWrapper.eq("is_send","1");}

//					leaderAppriseQueryWrapper.like("create_time", annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper.like("apprise_year", annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper.groupBy("apprise_rolename");
					List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
					try {
						if (leaderApprise.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise) {
								leaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
						leaderScore = leaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						leaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));

					//TODO 加分项总得分
					double addScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
					queryWrapperAdd.select(" * ");
					queryWrapperAdd.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperAdd.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperAdd.eq("check_way", "2");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperAdd.eq("is_send","1");}

					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (ScoreAdd scoreAdd : scoreAddList) {
							addScore += Double.parseDouble(scoreAdd.getScore());
						}
					}
					//加分项
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperMinus.eq("check_way", "2");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperMinus.eq("is_send","1");}

					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (ScoreMinus scoreMinus : scoreMinusList) {
							minusScore += Double.parseDouble(scoreMinus.getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

					//TODO 季度评价得分，获取今年【每季度10%】的分数
					double quarterlySumScore = 0.0;
					QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
					queryWrapper1.select(" check_classify,check_classify_name,\n" +
						"       responsible_unit_id,responsible_unit_name,\n" +
						"        serv_name,stage,stage_year,\n" +
						"       sum(quarterly_sum_score) as quarterly_sum_score ");
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getAnnualYear()), "stage_year", annualSumScore.getAnnualYear());
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getCheckClassify()), "check_classify", annualSumScore.getCheckClassify());
					queryWrapper1.eq(StringUtils.isNotBlank(deptId), "responsible_unit_id", deptId);
					queryWrapper1.groupBy("stage");
					List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
					if (quarterlySumScoreList.size() > 0) {
						for (int j = 0; j < quarterlySumScoreList.size(); j++) {
							//TODO 领导评价得分 总分 × 20%
							double jdleaderScore = 0.0;
							QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
							leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
							leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
							leaderAppriseQueryWrapper1.eq("evaluation_type", "2");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {leaderAppriseQueryWrapper1.eq("is_send","1");}

							leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
							leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
							//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
							List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
							try {
								if (leaderApprise2.size() > 0) {
									for (LeaderApprise leaderApprise1 : leaderApprise2) {
										jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
									}
								}
								jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
								jdleaderScore = jdleaderScore * 0.2;
							} catch (NullPointerException nullPointerException) {
								jdleaderScore = 0.0;
								System.out.println("领导评价得分为空！");
							}

							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


							//TODO 加分项总得分
							double jdaddScore = 0.0;
							QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
							queryWrapperAdda.select(" * ");
							queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
							queryWrapperAdda.eq("check_way", "1");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {queryWrapperAdda.eq("is_send","1");}

							queryWrapperAdda.apply("isok=1");
							List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
							if (scoreAddLista.size() > 0) {
								for (int jj = 0; jj < scoreAddLista.size(); jj++) {
									jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
								}
							}
							//jdaddScore = jdaddScore * 0.1;
							jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
							quarterlySumScoreList.get(j).setAddScore(jdaddScore);

							//TODO 减分项
							double jdminusScore = 0.0;
							QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
							queryWrapperMinusa.select(" * ");
							queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId() != null, "dept_id", quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
							queryWrapperMinusa.eq("check_way", "1");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {queryWrapperMinusa.eq("is_send","1");}

							List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
							if (scoreMinusLista.size() > 0) {
								for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
									jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
								}
							}
							//jdminusScore = jdminusScore * 0.1;
							jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
							quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

							/*//TODO 督察督办分数
							double dcdbScore = 0.0;
							//把所有县区的单位查出来
							List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
							String deptIds = "";
							if (gDeptids.size() > 0) {
								for (int k = 0; k < gDeptids.size(); k++) {
									deptIds += gDeptids.get(k).getId() + ",";
								}
							}
							String deptIdsss = quarterlySumScoreList.get(j).getResponsibleUnitId();
							//如果当前单位的id属于县区，那就查找子部门的分数
							String deptId1 = deptIdsss;
							if (deptIds.contains(deptIdsss)) {
								R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
								List<Dept> deptChildRData = deptChildR.getData();
								if (deptChildRData.size() > 0) {
									for (int jj = 0; jj < deptChildRData.size(); jj++) {
										deptId1 += "," + deptChildRData.get(jj).getId();
									}
								}
							}
							String[] deptIdss = deptId1.split(",");
							QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
							dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
							//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
							dcdbqueryWrapper.in("dept_id", deptIdss);
							dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
							dcdbqueryWrapper.groupBy("dept_id,serv_code");

							List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
							int size = 0;
							if (supervisionScores.size() > 0) {
								for (int jj = 0; jj < supervisionScores.size(); jj++) {
									LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
										.select(SupervisionInfo::getFlowStatus)
										.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
									SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
									if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
										dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
										size++;
									}
								}
							}
							//根据supervisionScores长度计算dcdbScore的平均分
							if (supervisionScores.size() > 0) {
								if (size > 0) {
									dcdbScore = dcdbScore / size;
								}
							}
							//将督查督办得分的10%算到季度评价中
							dcdbScore = dcdbScore * 0.1;
							dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
							if (dcdbScore < 10.0) {
								if (dcdbScore < 0) {
									quarterlySumScoreList.get(j).setDcdbScore(0.0);
								} else {
									quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
								}
							} else {
								quarterlySumScoreList.get(j).setDcdbScore(10.0);
							}*/


							//TODO 保留两位小数
							quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
							quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
							quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

							//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分
							double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore;// + quarterlySumScoreList.get(j).getDcdbScore()
							sumScore = Double.parseDouble(String.format("%.2f", sumScore));
							quarterlySumScore += sumScore;
						}
					}
					else {
						//TODO 领导评价得分 总分 × 20%
						double jdleaderScore = 0.0;
						QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
						leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
						leaderAppriseQueryWrapper1.eq( "dept_id", deptId);
						leaderAppriseQueryWrapper1.eq("evaluation_type", "2");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {leaderAppriseQueryWrapper1.eq("is_send","1");}

						leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
						leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
						//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
						List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
						try {
							if (leaderApprise2.size() > 0) {
								for (LeaderApprise leaderApprise1 : leaderApprise2) {
									jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
								}
							}
							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							jdleaderScore = jdleaderScore * 0.2;
						} catch (NullPointerException nullPointerException) {
							jdleaderScore = 0.0;
							System.out.println("领导评价得分为空！");
						}

						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));


						//TODO 加分项总得分
						double jdaddScore = 0.0;
						QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
						queryWrapperAdda.select(" * ");
						queryWrapperAdda.like( "dept_id", deptId);
						queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperAdda.eq("check_way", "1");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {queryWrapperAdda.eq("is_send","1");}

						queryWrapperAdda.apply("isok=1");
						List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
						if (scoreAddLista.size() > 0) {
							for (int jj = 0; jj < scoreAddLista.size(); jj++) {
								jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
							}
						}
						//jdaddScore = jdaddScore * 0.1;
						jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));

						//TODO 减分项
						double jdminusScore = 0.0;
						QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
						queryWrapperMinusa.select(" * ");
						queryWrapperMinusa.like( "dept_id", deptId);
						queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperMinusa.eq("check_way", "1");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {queryWrapperMinusa.eq("is_send","1");}

						List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
						if (scoreMinusLista.size() > 0) {
							for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
								jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
							}
						}
						//jdminusScore = jdminusScore * 0.1;
						jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));

						/*//TODO 督察督办分数
						double dcdbScore = 0.0;
						//把所有县区的单位查出来
						List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
						String deptIds = "";
						if (gDeptids.size() > 0) {
							for (int k = 0; k < gDeptids.size(); k++) {
								deptIds += gDeptids.get(k).getId() + ",";
							}
						}
						String deptIdsss = deptId;
						//如果当前单位的id属于县区，那就查找子部门的分数
						String deptId1 = deptIdsss;
						if (deptIds.contains(deptIdsss)) {
							R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
							List<Dept> deptChildRData = deptChildR.getData();
							if (deptChildRData.size() > 0) {
								for (int jj = 0; jj < deptChildRData.size(); jj++) {
									deptId1 += "," + deptChildRData.get(jj).getId();
								}
							}
						}
						String[] deptIdss = deptId1.split(",");
						QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
						dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
						//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
						dcdbqueryWrapper.in("dept_id", deptIdss);
						dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
						dcdbqueryWrapper.groupBy("dept_id,serv_code");

						List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
						int size = 0;
						if (supervisionScores.size() > 0) {
							for (int jj = 0; jj < supervisionScores.size(); jj++) {
								LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
									.select(SupervisionInfo::getFlowStatus)
									.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
								SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
								if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
									dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
									size++;
								}
							}
						}
						//根据supervisionScores长度计算dcdbScore的平均分
						if (supervisionScores.size() > 0) {
							if (size > 0) {
								dcdbScore = dcdbScore / size;
							}
						}
						//将督查督办得分的10%算到季度评价中
						dcdbScore = dcdbScore * 0.1;
						dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
						if (dcdbScore < 10.0) {
							if (dcdbScore < 0) {
								dcdbScore = 0.0;
							}
						} else {
							dcdbScore = 10.0;
						}*/

						//TODO 计算总分 领导评价得分 + 加分项 - 减分项 + 督查督办得分
						double sumScore = jdleaderScore + jdaddScore - jdminusScore;// + dcdbScore
						sumScore = Double.parseDouble(String.format("%.2f", sumScore));
						quarterlySumScore = sumScore;
					}
					//计算出今年季度的分数
					quarterlySumScore = quarterlySumScore * 0.1;
					quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
					//判断年度得分是否发布，未发布的话就不计算总分
					if (isShowQuarterScore == 0) {
						quarterlySumScore = 0.0;
					}
					//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
					double sumScore = quarterlySumScore + leaderScore + addScore - minusScore;
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					if (list1.size() > 0) {
						if (!list1.toString().contains(deptName)) {
							AnnualSumScore annualSumScore1 = new AnnualSumScore();
							annualSumScore1.setResponsibleUnitId(deptId);
							annualSumScore1.setResponsibleUnitName(deptName);
							annualSumScore1.setZzsxjsScore(0.0);
							annualSumScore1.setLdnlScore(0.0);
							annualSumScore1.setDdjsScore(0.0);
							annualSumScore1.setXqgzlfzScore(0.0);
							annualSumScore1.setSzgzlfzScore(0.0);
							annualSumScore1.setAddScore(addScore);
							annualSumScore1.setMinusScore(minusScore);
							annualSumScore1.setLeaderScore(leaderScore);
							annualSumScore1.setJdpjScore(quarterlySumScore);
							annualSumScore1.setAnnualSumScore(sumScore);
							annualSumScore1.setAnnualType(0);
							detail.add(annualSumScore1);
						}
					}
					else if (list1.size() == 0) {
						AnnualSumScore annualSumScore1 = new AnnualSumScore();
						annualSumScore1.setResponsibleUnitId(deptId);
						annualSumScore1.setResponsibleUnitName(deptName);
						annualSumScore1.setZzsxjsScore(0.0);
						annualSumScore1.setLdnlScore(0.0);
						annualSumScore1.setDdjsScore(0.0);
						annualSumScore1.setXqgzlfzScore(0.0);
						annualSumScore1.setSzgzlfzScore(0.0);
						annualSumScore1.setAddScore(addScore);
						annualSumScore1.setMinusScore(minusScore);
						annualSumScore1.setLeaderScore(leaderScore);
						annualSumScore1.setJdpjScore(quarterlySumScore);
						annualSumScore1.setAnnualSumScore(sumScore);
						annualSumScore1.setAnnualType(0);
						detail.add(annualSumScore1);
					}
				}
			}
			else if(StringUtils.isNotEmpty(annualSumScore.getResponsibleUnitId())){
				Dept dept = SysCache.getDept(Long.valueOf(annualSumScore.getResponsibleUnitId()));
				if (dept != null) {
					String deptId = dept.getId().toString();
					String deptName = dept.getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "1");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {leaderAppriseQueryWrapper.eq("is_send","1");}

					leaderAppriseQueryWrapper.like("create_time", annualSumScore.getAnnualYear());
					leaderAppriseQueryWrapper.groupBy("apprise_rolename");
					List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
					try {
						if (leaderApprise.size() > 0) {
							for (LeaderApprise leaderApprise1 : leaderApprise) {
								leaderScore += Double.parseDouble(leaderApprise1.getScore());
							}
						}
						leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
						leaderScore = leaderScore * 0.2;
					} catch (NullPointerException nullPointerException) {
						leaderScore = 0.0;
						System.out.println("领导评价得分为空！");
					}

					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));

					//TODO 加分项总得分
					double addScore = 0.0;
					QueryWrapper<ScoreAdd> queryWrapperAdd = new QueryWrapper<>();
					queryWrapperAdd.select(" * ");
					queryWrapperAdd.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperAdd.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperAdd.eq("check_way", "2");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperAdd.eq("is_send","1");}

					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (ScoreAdd scoreAdd : scoreAddList) {
							addScore += Double.parseDouble(scoreAdd.getScore());
						}
					}
					//加分项
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", annualSumScore.getAnnualYear());
					queryWrapperMinus.eq("check_way", "2");

					//判断是否包含打分详情或者考核绩效发布
					if(!flag) {queryWrapperMinus.eq("is_send","1");}

					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (ScoreMinus scoreMinus : scoreMinusList) {
							minusScore += Double.parseDouble(scoreMinus.getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

					//TODO 季度评价得分，获取今年【每季度10%】的分数
					double quarterlySumScore = 0.0;
					QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
					queryWrapper1.select(" check_classify,check_classify_name,\n" +
						"       responsible_unit_id,responsible_unit_name,\n" +
						"        serv_name,stage,stage_year,\n" +
						"       sum(quarterly_sum_score) as quarterly_sum_score ");
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getAnnualYear()), "stage_year", annualSumScore.getAnnualYear());
					queryWrapper1.eq(StringUtils.isNotBlank(annualSumScore.getCheckClassify()), "check_classify", annualSumScore.getCheckClassify());
					queryWrapper1.eq(StringUtils.isNotBlank(deptId), "responsible_unit_id", deptId);
					queryWrapper1.groupBy("stage");
					List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
					if (quarterlySumScoreList.size() > 0) {
						for (int j = 0; j < quarterlySumScoreList.size(); j++) {
							//TODO 领导评价得分 总分 × 20%
							double jdleaderScore = 0.0;
							QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 =new QueryWrapper<>();
							leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
							leaderAppriseQueryWrapper1.eq(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId().toString());
							leaderAppriseQueryWrapper1.eq("evaluation_type","2");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {leaderAppriseQueryWrapper1.eq("is_send","1");}

							leaderAppriseQueryWrapper1.like("apprise_year",annualSumScore.getAnnualYear());
							leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
							//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
							List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
							try {
								if (leaderApprise2.size() > 0) {
									for (LeaderApprise leaderApprise1 : leaderApprise2) {
										jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
									}
								}
								jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
								jdleaderScore = jdleaderScore * 0.2;
							} catch (NullPointerException nullPointerException) {
								jdleaderScore = 0.0;
								System.out.println("领导评价得分为空！");
							}

							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							quarterlySumScoreList.get(j).setLeaderScore(jdleaderScore);


							//TODO 加分项总得分
							double jdaddScore = 0.0;
							QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
							queryWrapperAdda.select(" * ");
							queryWrapperAdda.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperAdda.like("create_time",annualSumScore.getAnnualYear());
							queryWrapperAdda.eq("check_way","1");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {queryWrapperAdda.eq("is_send","1");}

							queryWrapperAdda.apply("isok=1");
							List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
							if (scoreAddLista.size() > 0) {
								for (int jj = 0; jj < scoreAddLista.size(); jj++) {
									jdaddScore+=Double.parseDouble(scoreAddLista.get(jj).getScore());
								}
							}
							//jdaddScore = jdaddScore * 0.1;
							jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));
							quarterlySumScoreList.get(j).setAddScore(jdaddScore);

							//TODO 减分项
							double jdminusScore = 0.0;
							QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
							queryWrapperMinusa.select(" * ");
							queryWrapperMinusa.like(quarterlySumScoreList.get(j).getResponsibleUnitId()!=null,"dept_id",quarterlySumScoreList.get(j).getResponsibleUnitId());
							queryWrapperMinusa.like("create_time",annualSumScore.getAnnualYear());
							queryWrapperMinusa.eq("check_way","1");

							//判断是否包含打分详情或者考核绩效发布
							if(!flag) {queryWrapperMinusa.eq("is_send","1");}

							List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
							if (scoreMinusLista.size() > 0) {
								for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
									jdminusScore+=Double.parseDouble(scoreMinusLista.get(jj).getScore());
								}
							}
							//jdminusScore = jdminusScore * 0.1;
							jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));
							quarterlySumScoreList.get(j).setMinusScore(jdminusScore);

							/*//TODO 督察督办分数
							double dcdbScore = 0.0;
							//把所有县区的单位查出来
							List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1").getData();
							String deptIds = "";
							if (gDeptids.size() > 0) {
								for (int k = 0; k < gDeptids.size(); k++) {
									deptIds+=gDeptids.get(k).getId()+",";
								}
							}
							String deptIdsss = quarterlySumScoreList.get(j).getResponsibleUnitId();
							//如果当前单位的id属于县区，那就查找子部门的分数
							String deptId1 = deptIdsss;
							if (deptIds.contains(deptIdsss)) {
								R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
								List<Dept> deptChildRData = deptChildR.getData();
								if (deptChildRData.size() > 0) {
									for (int jj = 0; jj < deptChildRData.size(); jj++) {
										deptId1+= "," +deptChildRData.get(jj).getId();
									}
								}
							}
							String[] deptIdss = deptId1.split(",");
							QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
							dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
							//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
							dcdbqueryWrapper.in("dept_id",deptIdss);
							dcdbqueryWrapper.likeRight("create_time",annualSumScore.getAnnualYear());
							dcdbqueryWrapper.groupBy("dept_id,serv_code");

							List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
							int size = 0;
							if (supervisionScores.size() > 0) {
								for (int jj = 0; jj < supervisionScores.size(); jj++) {
									LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
										.select(SupervisionInfo::getFlowStatus)
										.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
									SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
									if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
										dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
										size++;
									}
								}
							}
							//根据supervisionScores长度计算dcdbScore的平均分
							if (supervisionScores.size() > 0) {
								if (size > 0) {
									dcdbScore = dcdbScore / size;
								}
							}
							//将督查督办得分的10%算到季度评价中
							dcdbScore = dcdbScore * 0.1;
							dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
							if(dcdbScore < 10.0) {
								if(dcdbScore < 0) {
									quarterlySumScoreList.get(j).setDcdbScore(0.0);
								}else {
									quarterlySumScoreList.get(j).setDcdbScore(dcdbScore);
								}
							}else {
								quarterlySumScoreList.get(j).setDcdbScore(10.0);
							}*/


							//TODO 保留两位小数
							quarterlySumScoreList.get(j).setDjgzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDjgzScore())));
							quarterlySumScoreList.get(j).setGzsjScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getGzsjScore())));
							quarterlySumScoreList.get(j).setDflzScore(Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(j).getDflzScore())));

							//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
							double sumScore = quarterlySumScoreList.get(j).getQuarterlySumScore() + jdleaderScore + jdaddScore - jdminusScore;// + quarterlySumScoreList.get(j).getDcdbScore()
							sumScore = Double.parseDouble(String.format("%.2f", sumScore));
							quarterlySumScore +=sumScore;
						}
					} else {
						//TODO 领导评价得分 总分 × 20%
						double jdleaderScore = 0.0;
						QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper1 = new QueryWrapper<>();
						leaderAppriseQueryWrapper1.select(" *,ROUND(AVG(score),2) as avgScore ");
						leaderAppriseQueryWrapper1.eq( "dept_id", deptId);
						leaderAppriseQueryWrapper1.eq("evaluation_type", "2");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {leaderAppriseQueryWrapper1.eq("is_send","1");}

						leaderAppriseQueryWrapper1.like("apprise_year", annualSumScore.getAnnualYear());
						leaderAppriseQueryWrapper1.groupBy("apprise_rolename");
						//leaderAppriseQueryWrapper1.apply("apprise_quarter=" + quarterNum);
						List<LeaderApprise> leaderApprise2 = iLeaderAppriseService.list(leaderAppriseQueryWrapper1);
						try {
							if (leaderApprise2.size() > 0) {
								for (LeaderApprise leaderApprise1 : leaderApprise2) {
									jdleaderScore += Double.parseDouble(leaderApprise1.getScore());
								}
							}
							jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));
							jdleaderScore = jdleaderScore * 0.2;
						} catch (NullPointerException nullPointerException) {
							jdleaderScore = 0.0;
							System.out.println("领导评价得分为空！");
						}

						jdleaderScore = Double.parseDouble(String.format("%.2f", jdleaderScore));


						//TODO 加分项总得分
						double jdaddScore = 0.0;
						QueryWrapper<ScoreAdd> queryWrapperAdda = new QueryWrapper<>();
						queryWrapperAdda.select(" * ");
						queryWrapperAdda.like( "dept_id", deptId);
						queryWrapperAdda.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperAdda.eq("check_way", "1");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {queryWrapperAdda.eq("is_send","1");}

						queryWrapperAdda.apply("isok=1");
						List<ScoreAdd> scoreAddLista = iScoreAddService.list(queryWrapperAdda);
						if (scoreAddLista.size() > 0) {
							for (int jj = 0; jj < scoreAddLista.size(); jj++) {
								jdaddScore += Double.parseDouble(scoreAddLista.get(jj).getScore());
							}
						}
						//jdaddScore = jdaddScore * 0.1;
						jdaddScore = Double.parseDouble(String.format("%.2f", jdaddScore));

						//TODO 减分项
						double jdminusScore = 0.0;
						QueryWrapper<ScoreMinus> queryWrapperMinusa = new QueryWrapper<>();
						queryWrapperMinusa.select(" * ");
						queryWrapperMinusa.like( "dept_id", deptId);
						queryWrapperMinusa.like("create_time", annualSumScore.getAnnualYear());
						queryWrapperMinusa.eq("check_way", "1");

						//判断是否包含打分详情或者考核绩效发布
						if(!flag) {queryWrapperMinusa.eq("is_send","1");}

						List<ScoreMinus> scoreMinusLista = iScoreMinusService.list(queryWrapperMinusa);
						if (scoreMinusLista.size() > 0) {
							for (int jj = 0; jj < scoreMinusLista.size(); jj++) {
								jdminusScore += Double.parseDouble(scoreMinusLista.get(jj).getScore());
							}
						}
						//jdminusScore = jdminusScore * 0.1;
						jdminusScore = Double.parseDouble(String.format("%.2f", jdminusScore));

						/*//TODO 督察督办分数
						double dcdbScore = 0.0;
						//把所有县区的单位查出来
						List<Dept> gDeptids = sysClient.getDeptByGroup("000000", "1").getData();
						String deptIds = "";
						if (gDeptids.size() > 0) {
							for (int k = 0; k < gDeptids.size(); k++) {
								deptIds += gDeptids.get(k).getId() + ",";
							}
						}
						String deptIdsss = deptId;
						//如果当前单位的id属于县区，那就查找子部门的分数
						String deptId1 = deptIdsss;
						if (deptIds.contains(deptIdsss)) {
							R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptIdsss));
							List<Dept> deptChildRData = deptChildR.getData();
							if (deptChildRData.size() > 0) {
								for (int jj = 0; jj < deptChildRData.size(); jj++) {
									deptId1 += "," + deptChildRData.get(jj).getId();
								}
							}
						}
						String[] deptIdss = deptId1.split(",");
						QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
						dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
						//dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
						dcdbqueryWrapper.in("dept_id", deptIdss);
						dcdbqueryWrapper.likeRight("create_time", annualSumScore.getAnnualYear());
						dcdbqueryWrapper.groupBy("dept_id,serv_code");

						List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
						int size = 0;
						if (supervisionScores.size() > 0) {
							for (int jj = 0; jj < supervisionScores.size(); jj++) {
								LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
									.select(SupervisionInfo::getFlowStatus)
									.eq(SupervisionInfo::getServCode, supervisionScores.get(jj).getServCode());
								SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
								if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
									dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(jj).getScore()));
									size++;
								}
							}
						}
						//根据supervisionScores长度计算dcdbScore的平均分
						if (supervisionScores.size() > 0) {
							if (size > 0) {
								dcdbScore = dcdbScore / size;
							}
						}
						//将督查督办得分的10%算到季度评价中
						dcdbScore = dcdbScore * 0.1;
						dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
						if (dcdbScore < 10.0) {
							if (dcdbScore < 0) {
								dcdbScore = 0.0;
							}
						} else {
							dcdbScore = 10.0;
						}*/

						//TODO 计算总分 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
						double sumScore =  jdleaderScore + jdaddScore - jdminusScore;// + dcdbScore
						sumScore = Double.parseDouble(String.format("%.2f", sumScore));
						quarterlySumScore = sumScore;
					}
					//计算出今年季度的分数
					quarterlySumScore = quarterlySumScore * 0.1;
					quarterlySumScore = Double.parseDouble(String.format("%.2f", quarterlySumScore));
					//判断年度得分是否发布，未发布的话就不计算总分
					if (isShowQuarterScore == 0) {
						quarterlySumScore = 0.0;
					}
					//TODO 计算总分 = 季度得分 + 评价总分 + 领导评价得分 + 加分项 -减分项
					double sumScore = quarterlySumScore + leaderScore + addScore - minusScore;
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					AnnualSumScore annualSumScore1 = new AnnualSumScore();
					annualSumScore1.setResponsibleUnitId(deptId);
					annualSumScore1.setResponsibleUnitName(deptName);
					annualSumScore1.setZzsxjsScore(0.0);
					annualSumScore1.setLdnlScore(0.0);
					annualSumScore1.setDdjsScore(0.0);
					annualSumScore1.setXqgzlfzScore(0.0);
					annualSumScore1.setSzgzlfzScore(0.0);
					annualSumScore1.setAddScore(addScore);
					annualSumScore1.setMinusScore(minusScore);
					annualSumScore1.setLeaderScore(leaderScore);
					annualSumScore1.setJdpjScore(quarterlySumScore);
					annualSumScore1.setAnnualSumScore(sumScore);
					annualSumScore1.setAnnualType(0);
					detail.add(annualSumScore1);
				}
			}
		/*}*/


		//给总得分排名次
		double[] annualscore = new double[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			annualscore[i] = detail.get(i).getAnnualSumScore();
		}
		//去重
		List<Double> list = new ArrayList<Double>();
		for (double v : annualscore) {
			if (!list.contains(v)) {
				list.add(v);
			}
		}
		//先升序排列
		Collections.sort(list);
		//然后降序排列，得到list[0]最大
		Collections.reverse(list);
		//依次设置排名
		for (int i = 0; i < list.size(); i++) {
			for (AnnualSumScore sumScore : detail) {
				if (sumScore.getAnnualSumScore().equals(list.get(i))) {
					//排序
					sumScore.setAnnualType(i + 1);
				}
			}
		}
		//对【排名进行】进行升序排列
		detail.sort(new Comparator<AnnualSumScore>() {
			@Override
			public int compare(AnnualSumScore o1, AnnualSumScore o2) {
				Integer i1 = o1.getAnnualType();
				Integer i2 = o2.getAnnualType();
				return i1.compareTo(i2);
			}
		});

		return R.data(detail);
	}

	/**
	 * 年度指标完成情况详细信息
	 */
	@GetMapping("/SumDetail")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-首页年度指标完成情况百分比信息", notes = "传入 quarterlySumScore")
	public R<List<Map<String, Object>>> detail(AnnualEvaluation annualEvaluation) {
		//sql查询条件
		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<AnnualEvaluation>();
		queryWrapper.select("target_status,count(target_status) target_status_num ");
		queryWrapper.eq(annualEvaluation.getAppraiseClassify()!=null && !Objects.equals(annualEvaluation.getAppraiseClassify(), ""),"appraise_classify",annualEvaluation.getAppraiseClassify());
		queryWrapper.like("create_time",annualEvaluation.getQuarterlyYear());
		if (StringUtils.isNotNull(annualEvaluation.getTargetStatus())) {
			queryWrapper.in("target_status",annualEvaluation.getTargetStatus().split(","));
		}
		queryWrapper.groupBy("target_status");
		List<Map<String, Object>> detail = iAnnualEvaluationService.listMaps(queryWrapper);
		if (detail.size()==0) {
			Map<String, Object> map = new HashMap<>();
			map.put("target_status",0);
			map.put("target_status_num",0);
			detail.add(map);
		}
		return R.data(detail);
	}


	/**
	 * 年度指标基本信息中的指标得分表
	 */
	@GetMapping("/baseInfo")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-年度指标基本信息中的指标得分表", notes = "传入 annualEvaluation")
	public R<Map<String,Map<String, Object>>> baseInfo(AnnualEvaluation annualEvaluation) {
		AnnualEvaluation annualEvaluation1 = iAnnualEvaluationService.getById(annualEvaluation.getId());
		//获取评价部门ID
		String appraiseDeptId = annualEvaluation1.getAppraiseDeptid();
		String[] appraiseDeptIds = appraiseDeptId.split(",");
		//获取被考核部门ID
		String responsibleUnitId = annualEvaluation1.getAppraiseObjectId();
		String[] responsibleUnitIds = responsibleUnitId.split(",");
		//获取被考核部门名称
		String responsibleUnitName = annualEvaluation1.getAppraiseObject();
		String[] responsibleUnitNames = responsibleUnitName.split(",");

		List<List<AnnualSumScore>> listArrayList = new ArrayList<>();

		//循环查询每个评价部门的各项总分
		for (int i = 0; i < appraiseDeptIds.length; i++) {
			//sql查询条件
			QueryWrapper<AnnualSumScore> queryWrapper = new QueryWrapper<AnnualSumScore>();
			queryWrapper.select("responsible_unit_id,responsible_unit_name,appraise_deptid,appraise_deptname," +
				"sum(annual_sum_score) annual_sum_score ");
			queryWrapper.eq("annual_evaluation_id",annualEvaluation.getId().toString());
			queryWrapper.eq("appraise_deptid",appraiseDeptIds[i]);
			queryWrapper.eq("annual_year",String.valueOf(DateUtil.year(new Date())));
			queryWrapper.in("responsible_unit_id",responsibleUnitIds);
			queryWrapper.eq(StringUtils.isNotBlank(annualEvaluation.getStageId()),"stage_id",annualEvaluation.getStageId());
			queryWrapper.groupBy("responsible_unit_id");
			queryWrapper.orderByAsc("responsible_unit_id");
			List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService.list(queryWrapper);
			if (annualSumScoreList != null & annualSumScoreList.size() > 0) {
				List<AppriseDept> appriseDeptList = iAppriseDeptService.list(
					Wrappers.<AppriseDept>query().lambda()
						.eq(AppriseDept::getEvaluationId,annualEvaluation.getId())
						.eq(AppriseDept::getType,"1")
						.eq(AppriseDept::getCreateDept,appraiseDeptIds[i])
				);
				if (appriseDeptList.size() > 0 & appriseDeptList.size() == annualSumScoreList.size()) {
					for (int j = 0; j < annualSumScoreList.size(); j++) {
						for (int k = 0; k < appriseDeptList.size(); k++) {
							//设置分数
							if (annualSumScoreList.get(j).getResponsibleUnitName().equals(appriseDeptList.get(k).getResponsibleUnitName())) {
								annualSumScoreList.get(j).setAnnualSumScore(Double.valueOf(appriseDeptList.get(k).getScore()));
							}
						}
					}
				} else {
					throw new RuntimeException("评价得分纪录与指标得分纪录不一致！");
				}
				listArrayList.add(annualSumScoreList);
			}
		}
		//对【列表中查询的AnnualSumScore数量】进行升序排列
		listArrayList.sort(new Comparator<List<AnnualSumScore>>() {
			@Override
			public int compare(List<AnnualSumScore> o1, List<AnnualSumScore> o2) {
				Integer i1 = o1.size();
				Integer i2 = o2.size();
				return i1.compareTo(i2);
			}
		});

		/*try {
			//计算平均分
			for (int j = 0; j < responsibleUnitIds.length; j++) {
				double avgSumScore = 0.0;
				//遍历各部门
				for (int i = 0; i < listArrayList.size(); i++) {
					double AnnualSumScore = 0.0;
					try {
						AnnualSumScore = listArrayList.get(i).get(j).getAnnualSumScore();
						avgSumScore += AnnualSumScore;
						if (i == listArrayList.size()-1) {
							avgSumScore = Double.parseDouble(String.format("%.2f", avgSumScore / listArrayList.size()));
							listArrayList.get(listArrayList.size()-1).get(j).setAvgAnnualSumScore(avgSumScore);
						}
					}catch (Exception exception){
						System.out.println("获取年度总分时出错！可能是对象数量不匹配！");
					}
				}
			}
		}
		catch (Exception exception) {
			return R.fail("操作失败："+exception.toString());
		}*/
		//考核单位
		// --评价单位1:"sumScore":"50",
		// --评价单位2:"sumScore":"30",
		// --平均分:"avgScore":"40",
		Map<String,Map<String, Object>> mapMap = new HashMap<>();
		if (responsibleUnitNames.length > 0) {
			for (int i = 0; i < responsibleUnitNames.length; i++) {//考核单位
				Map<String, Object> map = new HashMap<>();
				for (int j = 0; j < listArrayList.size(); j++) {//评价单位
					List<AnnualSumScore> annualSumScoreList = listArrayList.get(j);
					for (int k = 0; k < annualSumScoreList.size(); k++) {
						String appraiseDept = annualSumScoreList.get(k).getAppraiseDeptname();
						/*//评价单位数量
						double zhanbi = 0.0;
						if ("1".equals(annualEvaluation1.getType())) {//政治思想建设 占15%
							zhanbi = 0.15;
						}else if ("2".equals(annualEvaluation1.getType())) {//领导能力 占10%
							zhanbi = 0.10;
						}else if ("3".equals(annualEvaluation1.getType())) {//党的建设 占15%
							zhanbi = 0.15;
						}else if ("4".equals(annualEvaluation1.getType())) {//市直高质量发展 占60%
							zhanbi = 0.60;
						}else if ("5".equals(annualEvaluation1.getType())) {//县区高质量发展 占60%
							zhanbi = 0.60;
						}
						//权重
						double weight = 0.0;
						if ("5".equals(annualEvaluation.getType())) {//县区高质量发展
							if ("甘州区".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getGanzhouqu())) {
									annualEvaluation1.setGanzhouqu("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getGanzhouqu()) / 100;
							} else if ("临泽县".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getLinzexian())) {
									annualEvaluation1.setLinzexian("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getLinzexian()) / 100;
							} else if ("高台县".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getGaotaixian())) {
									annualEvaluation1.setGaotaixian("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getGaotaixian()) / 100;
							} else if ("山丹县".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getShandanxian())) {
									annualEvaluation1.setShandanxian("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getShandanxian()) / 100;
							} else if ("民乐县".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getMinlexian())) {
									annualEvaluation1.setMinlexian("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getMinlexian()) / 100;
							} else if ("肃南县".equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
								if (StringUtils.isEmpty(annualEvaluation1.getSunanxian())) {
									annualEvaluation1.setSunanxian("0.0");
								}
								weight = Double.parseDouble(annualEvaluation.getSunanxian()) / 100;
							}
						} else {
							if (StringUtils.isEmpty(annualEvaluation1.getWeight())) {
								annualEvaluation1.setWeight("0.0");
							}
							weight = Double.parseDouble(annualEvaluation1.getWeight()) / 100;
						}
						weight = Double.parseDouble(String.format("%.2f", weight));
						//拿到分数
						double score = Double.parseDouble(String.format("%.3f", annualSumScoreList.get(k).getAnnualSumScore()));;
						//反向推出百分制得分
						score = (score / weight / zhanbi) * appraiseDeptIds.length;
						double sumScore = Double.parseDouble(String.valueOf(String.format("%.2f", score)));*/
						double sumScore = annualSumScoreList.get(k).getAnnualSumScore();
						if (responsibleUnitNames[i].equals(annualSumScoreList.get(k).getResponsibleUnitName())) {
							map.put(appraiseDept,sumScore);
							break;
						}
					}
				}
				if (map.size() > 0) {
					Set<String> set = map.keySet();
					Iterator<String> iterator = set.iterator();
					double avgScore = 0.0;
					while (iterator.hasNext()) {
						String key = iterator.next();
						double score = Double.parseDouble(String.valueOf(map.get(key)));
						avgScore+= Double.parseDouble(String.format("%.2f", score));
					}
					avgScore = Double.parseDouble(String.format("%.2f", avgScore / set.size()));
					map.put("avgScore",avgScore);
				}
				mapMap.put(responsibleUnitNames[i],map);
			}
		}

		return R.data(mapMap);
	}


	/**
	 * 修改考核评价-年度指标指标为发布状态
	 */
	@GetMapping("/send")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "修改考核评价-年度指标指标为发布状态", notes = "传入 ids")
	public R send(@RequestParam("ids") String ids) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String[] id = ids.split(",");
		//sql查询条件
		List<AnnualEvaluation> annualEvaluations = null;
		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<>();
		if (id.length > 0) {
			queryWrapper.in("id",id);
			annualEvaluations = iAnnualEvaluationService.list(queryWrapper);
		}
		String msg = "暂无发布内容！";
		if (annualEvaluations != null && annualEvaluations.size()>0) {
			for (int i = 0; i < annualEvaluations.size(); i++) {
				UpdateWrapper<AnnualSumScore> annualSumScoreUpdateWrapper =new UpdateWrapper<>();
				annualSumScoreUpdateWrapper.set("is_send",1);
				annualSumScoreUpdateWrapper.eq("annual_evaluation_id",annualEvaluations.get(i).getId());
				annualSumScoreUpdateWrapper.in("responsible_unit_id",annualEvaluations.get(i).getAppraiseObjectId().split(","));
				//annualSumScoreUpdateWrapper.eq("appraise_deptid",user.getDeptId());
				boolean isok = iAnnualSumScoreService.update(annualSumScoreUpdateWrapper);
				if (isok) {
					msg = "发布成功！";
				}
			}
			String title1 = "修改考核评价-年度指标评价为发布状态";
			String businessId = String.valueOf(ids);
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessId, businessTable, businessType);

			return R.success(msg);


		}
		return R.fail("发布失败！");
	}




}
