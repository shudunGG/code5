package org.springblade.integrated.platform.controller;

import cn.hutool.core.date.DateTime;
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
import org.redisson.api.RList;
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
import org.springblade.integrated.platform.excel.SyQuarterExcel;
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
 * 考核评价首页-季度 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/19 22:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("QuarterlySumScore")
@Api(value = "考核评价-季度首页", tags = "考核评价-季度首页控制层代码")
public class QuarterlySumScoreController extends BladeController {

	@Resource
	private IQuarterlySumScoreService iQuarterlySumScoreService;

	@Resource
	private IQuarterlyEvaluationService iQuarterlyEvaluationService;
	//加分项服务类
	private final IScoreAddService iScoreAddService;
	//减分项服务类
	private final IScoreMinusService iScoreMinusService;
	private final ILeaderAppriseService iLeaderAppriseService;
	private final IAppriseDeptService iAppriseDeptService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserClient userClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	@GetMapping("/detailApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-App季度首页详细信息", notes = "传入 quarterlySumScore")
	public R<Map<String, Map<String, Object>>> detailApp(QuarterlySumScore quarterlySumScore) {
		//这里需要传年份 quarterlySumScore.setStageYear("2024");
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(currentUser.getRoleId()).toString();

		String quarter = "第一季度";
		String quarterNum = "1";
		int ii = DateTime.now().month();
		//如果是2024年之前
		if (quarterlySumScore != null && quarterlySumScore.getStageYear() != null) {
			if (Integer.parseInt(quarterlySumScore.getStageYear()) < 2024) {
				if (ii==1 || ii==2 || ii==3) {//第一季度
					quarter = "第一季度";
					quarterNum = "1";
				}else if (ii==4 || ii==5 || ii==6) {//第二季度
					quarter = "第二季度";
					quarterNum = "2";
				}else if (ii==7 || ii==8 || ii==9) {//第三季度
					quarter = "第三季度";
					quarterNum = "3";
				}else if (ii==10 || ii==11 || ii==12) {//第四季度
					quarter = "第四季度";
					quarterNum = "4";
				}
			} else if (Integer.parseInt(quarterlySumScore.getStageYear()) == 2024) {
				if (ii==1 || ii==2 || ii==3) {//第一季度
					quarter = "第一季度";
					quarterNum = "1";
				}else if (ii==4 || ii==5 || ii==6) {//第二季度
					quarter = "第二季度";
					quarterNum = "2";
				}else {//下半年
					quarter = "下半年";
					quarterNum = "6";
				}
			} else if (Integer.parseInt(quarterlySumScore.getStageYear()) > 2024) {
				if (ii <= 6) {//上半年
					quarter = "上半年";
					quarterNum = "5";
				} else {//下半年
					quarter = "下半年";
					quarterNum = "6";
				}
			}
		}
		//sql查询条件
		QueryWrapper<QuarterlySumScore> queryWrapper = new QueryWrapper<QuarterlySumScore>();
		queryWrapper.select(" check_classify,check_classify_name,\n" +
			"       responsible_unit_id,responsible_unit_name,\n" +
			"        serv_name,stage,stage_year,\n" +
			"       sum(djgz_score) as djgz_score,\n" +
			"       sum(gzsj_score) as gzsj_score,\n" +
			"       sum(dflz_score) as dflz_score,\n" +
			"       sum(s3z3c_score) as s3z3c_score,\n" +
			"       sum(add_score) as add_score,\n" +
			"       sum(minus_score) as minus_score,\n" +
			"       sum(leader_score) as leader_score,\n" +
			"       sum(quarterly_sum_score) as quarterly_sum_score ");
		queryWrapper.eq(quarterlySumScore.getStageYear()!=null && !Objects.equals(quarterlySumScore.getStageYear(), ""),"stage_year",quarterlySumScore.getStageYear());
		queryWrapper.eq(quarterlySumScore.getCheckClassify()!=null && !Objects.equals(quarterlySumScore.getCheckClassify(), ""),"check_classify",quarterlySumScore.getCheckClassify());
		queryWrapper.eq(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId()),"responsible_unit_id",quarterlySumScore.getResponsibleUnitId());
		queryWrapper.eq(StringUtils.isNotBlank(quarter),"stage",quarter);
		//没有打分详情角色的账号要发布过后才能查看分数
		if (!roleNames.contains(isLookRole)) {
			queryWrapper.apply(" is_send = 1");
		}
		queryWrapper.groupBy("responsible_unit_name");//,"quarterly_sum_score"
		queryWrapper.orderByDesc("quarterly_sum_score");
		List<QuarterlySumScore> detail = iQuarterlySumScoreService.list(queryWrapper);

		for (int i = 0; i < detail.size(); i++) {
			//TODO 领导评价得分 总分 × 20%
			double leaderScore = 0.0;
			QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper =new QueryWrapper<>();
			leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore ");
			leaderAppriseQueryWrapper.eq(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			leaderAppriseQueryWrapper.eq("evaluation_type","2");
			leaderAppriseQueryWrapper.like("apprise_year",quarterlySumScore.getStageYear());
			leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
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
			queryWrapperAdd.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
			queryWrapperAdd.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperAdd.eq("check_way","1");
			queryWrapperAdd.apply("isok=1");
			List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);

			if (scoreAddList.size() > 0) {
				for (int j = 0; j < scoreAddList.size(); j++) {
					addScore+=Double.parseDouble(scoreAddList.get(j).getScore());
				}
			}
			//addScore = addScore * 0.1;
			addScore = Double.parseDouble(String.format("%.2f", addScore));
			detail.get(i).setAddScore(addScore);

			//TODO 减分项
			double minusScore = 0.0;
			QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
			queryWrapperMinus.select(" * ");
			queryWrapperMinus.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
			queryWrapperMinus.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperMinus.eq("check_way","1");
			List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
			if (scoreMinusList.size() > 0) {
				for (int j = 0; j < scoreMinusList.size(); j++) {
					minusScore+=Double.parseDouble(scoreMinusList.get(j).getScore());
				}
			}
			//minusScore = minusScore * 0.1;
			minusScore = Double.parseDouble(String.format("%.2f", minusScore));
			detail.get(i).setMinusScore(minusScore);

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
			String deptId = detail.get(i).getResponsibleUnitId();
			//如果当前单位的id属于县区，那就查找子部门的分数
			String deptId1 = deptId;
			if (deptIds.contains(deptId)) {
				R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
				List<Dept> deptChildRData = deptChildR.getData();
				if (deptChildRData.size() > 0) {
					for (int j = 0; j < deptChildRData.size(); j++) {
						deptId1+= "," +deptChildRData.get(j).getId();
					}
				}
			}
			String[] deptIdss = deptId1.split(",");
			QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
			dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
			dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
			dcdbqueryWrapper.in("dept_id",deptIdss);
			dcdbqueryWrapper.likeRight("create_time",quarterlySumScore.getStageYear());
			dcdbqueryWrapper.groupBy("dept_id,serv_code");

			List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
			int size = 0;
			if (supervisionScores.size() > 0) {
				for (int j = 0; j < supervisionScores.size(); j++) {
					LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
						.select(SupervisionInfo::getFlowStatus)
						.eq(SupervisionInfo::getServCode, supervisionScores.get(j).getServCode());
					SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
					if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
						dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(j).getScore()));
						size++;
					}
				}
			}
			//根据supervisionScores长度计算dcdbScore的平均分
			if (supervisionScores.size() > 0) {
				dcdbScore = dcdbScore / size;
			}
			//将督查督办得分的10%算到季度评价中
			dcdbScore = dcdbScore * 0.1;
			dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
			if (dcdbScore < 0.0) {
				detail.get(i).setDcdbScore(0.0);
			}
			if(dcdbScore < 10.0) {
				detail.get(i).setDcdbScore(dcdbScore);
			}else {
				detail.get(i).setDcdbScore(10.0);
			}*/


			//TODO 保留两位小数
			detail.get(i).setDjgzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDjgzScore())));
			detail.get(i).setGzsjScore(Double.parseDouble(String.format("%.2f", detail.get(i).getGzsjScore())));
			detail.get(i).setDflzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDflzScore())));
			detail.get(i).setS3z3cScore(Double.parseDouble(String.format("%.2f", detail.get(i).getS3z3cScore())));

			//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
			double sumScore = detail.get(i).getQuarterlySumScore() + leaderScore + addScore - minusScore;// + detail.get(i).getDcdbScore()
			sumScore = Double.parseDouble(String.format("%.2f", sumScore));
			detail.get(i).setQuarterlySumScore(sumScore);
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
		/*int year = DateTime.now().year();
		int year1 = Integer.parseInt(quarterlySumScore.getStageYear());
		//判断查询的年份是否小于当前年份
		if (year1 >= year) {*/
			//获取分组后的单位id
			R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000",quarterlySumScore.getCheckClassify(), quarterlySumScore.getStageYear());
			List<Dept> depts = Rdeptids.getData();
			if (depts.size() > 0 && StringUtils.isEmpty(quarterlySumScore.getResponsibleUnitId())) {
				for (int i = 0; i < depts.size(); i++) {
					String deptId = depts.get(i).getId().toString();
					String deptName = depts.get(i).getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "2");

					leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
					leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
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
					queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
					queryWrapperAdd.eq("check_way", "1");
					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (int j = 0; j < scoreAddList.size(); j++) {
							addScore += Double.parseDouble(scoreAddList.get(j).getScore());
						}
					}
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
					queryWrapperMinus.eq("check_way", "1");
					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (int j = 0; j < scoreMinusList.size(); j++) {
							minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

					//TODO 督察督办分数
					/*double dcdbScore = 0.0;
					//把所有县区的单位查出来
					List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1").getData();
					String deptIds = "";
					if (gDeptids.size() > 0) {
						for (int k = 0; k < gDeptids.size(); k++) {
							deptIds+=gDeptids.get(k).getId()+",";
						}
					}

					//如果当前单位的id属于县区，那就查找子部门的分数
					String deptId1 = deptId;
					if (deptIds.contains(deptId)) {
						R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
						List<Dept> deptChildRData = deptChildR.getData();
						if (deptChildRData.size() > 0) {
							for (int j = 0; j < deptChildRData.size(); j++) {
								deptId1+= "," +deptChildRData.get(j).getId();
							}
						}
					}
					String[] deptIdss = deptId1.split(",");
					QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
					dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) as score,create_user,create_dept");
					dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
					dcdbqueryWrapper.in("dept_id",deptIdss);
					dcdbqueryWrapper.likeRight("create_time",quarterlySumScore.getStageYear());
					dcdbqueryWrapper.groupBy("dept_id,serv_code");
					List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
					int size = 0;
					if (supervisionScores.size() > 0) {
						for (int j = 0; j < supervisionScores.size(); j++) {
							LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
								.select(SupervisionInfo::getFlowStatus)
								.eq(SupervisionInfo::getServCode, supervisionScores.get(j).getServCode());
							SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
							if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
								System.out.println("===================="+String.valueOf(supervisionScores.get(j).getScore()));
								dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(j).getScore()));
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
					if(dcdbScore < 0) {
						dcdbScore = 0.0;
					}
					if(dcdbScore > 10.0) {
						dcdbScore = 10.0;
					}*/
					//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
					double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					if (list1.size() > 0) {
						if (!list1.toString().contains(deptName)) {
							QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
							quarterlySumScore1.setResponsibleUnitId(deptId);
							quarterlySumScore1.setResponsibleUnitName(deptName);
							quarterlySumScore1.setDflzScore(0.0);
							quarterlySumScore1.setGzsjScore(0.0);
							quarterlySumScore1.setDjgzScore(0.0);
							quarterlySumScore1.setS3z3cScore(0.0);
							quarterlySumScore1.setAddScore(addScore);
							quarterlySumScore1.setMinusScore(minusScore);
							quarterlySumScore1.setLeaderScore(leaderScore);
							quarterlySumScore1.setQuarterlySumScore(sumScore);
							quarterlySumScore1.setQuarterlyType(0);
							detail.add(quarterlySumScore1);
						}
					} else if (list1.size() == 0) {
						QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
						quarterlySumScore1.setResponsibleUnitId(deptId);
						quarterlySumScore1.setResponsibleUnitName(deptName);
						quarterlySumScore1.setDflzScore(0.0);
						quarterlySumScore1.setGzsjScore(0.0);
						quarterlySumScore1.setDjgzScore(0.0);
						quarterlySumScore1.setS3z3cScore(0.0);
						quarterlySumScore1.setAddScore(addScore);
						quarterlySumScore1.setMinusScore(minusScore);
						quarterlySumScore1.setLeaderScore(leaderScore);
						quarterlySumScore1.setQuarterlySumScore(sumScore);
						quarterlySumScore1.setQuarterlyType(0);
						detail.add(quarterlySumScore1);
					}
				}
			}
			else if(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId())){
				Dept dept = SysCache.getDept(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
				if (dept != null) {
					String deptId = dept.getId().toString();
					String deptName = dept.getDeptName();
					//TODO 领导评价得分 总分 × 20%
					double leaderScore = 0.0;
					QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
					leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
					leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					leaderAppriseQueryWrapper.eq("evaluation_type", "2");
					leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
					leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
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
					queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
					queryWrapperAdd.eq("check_way", "1");
					queryWrapperAdd.apply("isok=1");
					List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
					if (scoreAddList.size() > 0) {
						for (int j = 0; j < scoreAddList.size(); j++) {
							addScore += Double.parseDouble(scoreAddList.get(j).getScore());
						}
					}
					//addScore = addScore * 0.1;
					addScore = Double.parseDouble(String.format("%.2f", addScore));

					//TODO 减分项
					double minusScore = 0.0;
					QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
					queryWrapperMinus.select(" * ");
					queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
					queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
					queryWrapperMinus.eq("check_way", "1");
					List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
					if (scoreMinusList.size() > 0) {
						for (int j = 0; j < scoreMinusList.size(); j++) {
							minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
						}
					}
					//minusScore = minusScore * 0.1;
					minusScore = Double.parseDouble(String.format("%.2f", minusScore));

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
					//如果当前单位的id属于县区，那就查找子部门的分数
					String deptId1 = deptId;
					if (deptIds.contains(deptId)) {
						R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
						List<Dept> deptChildRData = deptChildR.getData();
						if (deptChildRData.size() > 0) {
							for (int j = 0; j < deptChildRData.size(); j++) {
								deptId1+= "," +deptChildRData.get(j).getId();
							}
						}
					}
					String[] deptIdss = deptId1.split(",");
					QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
					dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
					dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
					dcdbqueryWrapper.in("dept_id",deptIdss);
					dcdbqueryWrapper.likeRight("create_time",quarterlySumScore.getStageYear());
					dcdbqueryWrapper.groupBy("dept_id,serv_code");
					List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
					int size = 0;
					if (supervisionScores.size() > 0) {
						for (int j = 0; j < supervisionScores.size(); j++) {
							LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
								.select(SupervisionInfo::getFlowStatus)
								.eq(SupervisionInfo::getServCode, supervisionScores.get(j).getServCode());
							SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
							if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
								dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(j).getScore()));
								size++;
							}
						}
					}
					//根据supervisionScores长度计算dcdbScore的平均分
					if (supervisionScores.size() > 0) {
						dcdbScore = dcdbScore / size;
					}
					//将督查督办得分的10%算到季度评价中,最多10分
					dcdbScore = dcdbScore * 0.1;
					dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
					if(dcdbScore < 0){
						dcdbScore = 0.0;
					}
					if(dcdbScore > 10.0) {
						dcdbScore = 10.0;
					}*/
					//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
					double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
					sumScore = Double.parseDouble(String.format("%.2f", sumScore));

					QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
					quarterlySumScore1.setResponsibleUnitId(deptId);
					quarterlySumScore1.setResponsibleUnitName(deptName);
					quarterlySumScore1.setDflzScore(0.0);
					quarterlySumScore1.setGzsjScore(0.0);
					quarterlySumScore1.setDjgzScore(0.0);
					quarterlySumScore1.setS3z3cScore(0.0);
					quarterlySumScore1.setAddScore(addScore);
					quarterlySumScore1.setMinusScore(minusScore);
					quarterlySumScore1.setLeaderScore(leaderScore);
					quarterlySumScore1.setQuarterlySumScore(sumScore);
					quarterlySumScore1.setQuarterlyType(0);
					detail.add(quarterlySumScore1);
				}
			}
	//	}

		//给总得分排名次
		double[] annualscore = new double[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			annualscore[i] = detail.get(i).getQuarterlySumScore();
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
			for (QuarterlySumScore sumScore : detail) {
				Map<String, Object> map1 = new HashMap();
				if (sumScore.getQuarterlySumScore().equals(list.get(i))) {
					sumScore.setQuarterlyType(i + 1);
					//总分
					map1.put("sumScore",sumScore.getQuarterlySumScore());
					//排名
					map1.put("ranking",sumScore.getQuarterlyType());
					map.put(sumScore.getResponsibleUnitId().toString(),map1);
				}
			}
		}
		//筛选出当前责任单位的排名和分数
		Map<String, Map<String, Object>> map2 = null;
		if (quarterlySumScore.getResponsibleUnitId()!=null) {
			String rid = quarterlySumScore.getResponsibleUnitId().toString();
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
	@ApiOperation(value = "考核评价-季度首页详细信息", notes = "传入 quarterlySumScore")
	public R<List<QuarterlySumScore>> detail(QuarterlySumScore quarterlySumScore){
		R<List<QuarterlySumScore>> rList = null;
		if (quarterlySumScore != null && quarterlySumScore.getStage() != null) {
			if (quarterlySumScore.getStage().contains("季度")) {
				rList = SyDetail(quarterlySumScore);
			} else {
				rList = SyBnDetail(quarterlySumScore);
			}
		} else {
			rList = R.data(new ArrayList<>());
		}
		return rList;
	}

	/**
	 * 详细信息-app
	 */
	@PostMapping("/detailApplication")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价-季度首页详细信息-app", notes = "传入 quarterlySumScore")
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

			QuarterlySumScore quarterlySumScore = objectMapper.convertValue(jsonParams, QuarterlySumScore.class);
			// List<QuarterlySumScore> rList = SyDetail(quarterlySumScore).getData();
			R<List<QuarterlySumScore>> rList = null;
			if (quarterlySumScore != null && quarterlySumScore.getStage() != null) {
				if (quarterlySumScore.getStage().contains("季度")) {
					rList = SyDetail(quarterlySumScore);
				} else {
					rList = SyBnDetail(quarterlySumScore);
				}
			} else {
				rList = R.data(new ArrayList<>());
			}
			JSONArray jsonArray = objectMapper.convertValue(rList.getData(), JSONArray.class);
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
	@ApiOperation(value = "考核评价-导出季度首页详细信息", notes = "传入 quarterlySumScore")
	public void exportDetail(QuarterlySumScore quarterlySumScore,HttpServletResponse response){

//		R<List<QuarterlySumScore>> rList = SyDetail(quarterlySumScore);
		R<List<QuarterlySumScore>> rList = null;
		if (quarterlySumScore != null && quarterlySumScore.getStage() != null) {
			if (quarterlySumScore.getStage().contains("季度")) {
				rList = SyDetail(quarterlySumScore);
			} else {
				rList = SyBnDetail(quarterlySumScore);
			}
		} else {
			rList = R.data(new ArrayList<>());
		}
		List<QuarterlySumScore> list = rList.getData();
		List<SyQuarterExcel> syQuarterExcels = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SyQuarterExcel syQuarterExcel = new SyQuarterExcel();
			syQuarterExcel.setZrdw(list.get(i).getResponsibleUnitName());
			syQuarterExcel.setDjgz(String.valueOf(list.get(i).getDjgzScore()));
			syQuarterExcel.setGzsj(String.valueOf(list.get(i).getGzsjScore()));
			syQuarterExcel.setDflz(String.valueOf(list.get(i).getDflzScore()));
			syQuarterExcel.setJiaFen(String.valueOf(list.get(i).getAddScore()));
			syQuarterExcel.setJianFen(String.valueOf(list.get(i).getMinusScore()));
			syQuarterExcel.setLdpj(String.valueOf(list.get(i).getLeaderScore()));
			syQuarterExcel.setZf(String.valueOf(list.get(i).getQuarterlySumScore()));
			syQuarterExcel.setPm(String.valueOf(list.get(i).getQuarterlyType()));
			syQuarterExcels.add(syQuarterExcel);
		}
		String fileName = quarterlySumScore.getCheckClassifyName() + "-" + quarterlySumScore.getStageYear() + "-" + quarterlySumScore.getStage() + "-" + "得分详情";
		ExcelUtil.export(response, fileName , "季度评价得分详情", syQuarterExcels, SyQuarterExcel.class);
	}

	/**如果是季度走这个方法*/
	public R<List<QuarterlySumScore>> SyDetail(QuarterlySumScore quarterlySumScore) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(currentUser.getRoleId()).toString();

		//当前角色是否包含 打分详情 或者 考核绩效发布
		boolean flag = roleNames.contains(isLookRole) || roleNames.contains("考核绩效发布");

		String quarterNum = "1";
		if (quarterlySumScore.getStage().equals("第一季度")) {
			quarterNum = "1";
		}else if (quarterlySumScore.getStage().equals("第二季度")) {
			quarterNum = "2";
		}else if (quarterlySumScore.getStage().equals("第三季度")) {
			quarterNum = "3";
		}else if (quarterlySumScore.getStage().equals("第四季度")) {
			quarterNum = "4";
		}

		//sql查询条件
		QueryWrapper<QuarterlySumScore> queryWrapper = new QueryWrapper<QuarterlySumScore>();
		queryWrapper.select(" check_classify,check_classify_name,\n" +
			"       responsible_unit_id,responsible_unit_name,\n" +
			"       is_send,\n" +
			"        serv_name,stage,stage_year,\n" +
			"       sum(djgz_score) as djgz_score,\n" +
			"       sum(gzsj_score) as gzsj_score,\n" +
			"       sum(dflz_score) as dflz_score,\n" +
			"       sum(s3z3c_score) as s3z3c_score,\n" +
			"       sum(add_score) as add_score,\n" +
			"       sum(minus_score) as minus_score,\n" +
			"       sum(leader_score) as leader_score,\n" +
			"       sum(quarterly_sum_score) as quarterly_sum_score ");
		//queryWrapper.eq(quarterlySumScore.getStage()!=null && !Objects.equals(quarterlySumScore.getStage(), ""),"stage",quarterlySumScore.getStage());
		queryWrapper.eq(quarterlySumScore.getStageYear()!=null && !Objects.equals(quarterlySumScore.getStageYear(), ""),"stage_year",quarterlySumScore.getStageYear());
		queryWrapper.eq(quarterlySumScore.getCheckClassify()!=null && !Objects.equals(quarterlySumScore.getCheckClassify(), ""),"check_classify",quarterlySumScore.getCheckClassify());
		queryWrapper.eq(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId()),"responsible_unit_id",quarterlySumScore.getResponsibleUnitId());
		queryWrapper.eq(StringUtils.isNotBlank(quarterlySumScore.getStage()),"stage",quarterlySumScore.getStage());


		//没有打分详情角色的账号  要发布过后才能查看分数
//		if (!roleNames.contains(isLookRole)) {
//			queryWrapper.apply(" is_send = 1");
//		}
		//2023年4月12日 与现场沟通确认 去掉打分详情角色过滤 统一查发布后的数据
//		queryWrapper.apply(" is_send = 1");

		//2023年4月19日 与现场沟通确认 发布角色在未发布之前就要能看到，不然不知道能不能发布
		if (!roleNames.contains("考核绩效发布")) {
			queryWrapper.apply(" is_send = 1");
		}

		queryWrapper.groupBy("responsible_unit_name");
		queryWrapper.orderByDesc("quarterly_sum_score");
		List<QuarterlySumScore> detail = iQuarterlySumScoreService.list(queryWrapper);
		System.out.println("detail = " + detail);
		for (int i = 0; i < detail.size(); i++) {
			//TODO 领导评价得分 总分 × 20%
			double leaderScore = 0.0;
			QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper =new QueryWrapper<>();
			leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
			leaderAppriseQueryWrapper.eq(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			leaderAppriseQueryWrapper.eq("evaluation_type","2");
			//没有打分详情角色的账号  要发布过后才能查看分数
			if (!roleNames.contains(isLookRole)) {
				leaderAppriseQueryWrapper.eq("is_send","1");
			}
			leaderAppriseQueryWrapper.like("apprise_year",quarterlySumScore.getStageYear());
			leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
			leaderAppriseQueryWrapper.groupBy("apprise_rolename");
			List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
			try {
				if (leaderApprise.size() > 0) {
					for (LeaderApprise leaderApprise1 : leaderApprise) {
						leaderScore += beginQuarter(leaderApprise1) ?
							Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
//						leaderScore += Double.parseDouble(leaderApprise1.getScore());
//						leaderScore += Double.parseDouble(leaderApprise1.getAvgScore());
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
			queryWrapperAdd.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
//			queryWrapperAdd.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperAdd.eq("year(create_time)", quarterlySumScore.getStageYear());
			queryWrapperAdd.eq(" quarter(create_time)",quarterNum);
			queryWrapperAdd.eq("check_way","1");

			//没有打分详情 或者考核绩效发布的 的账号需要发布后才能看到加分项
			if (!flag) {
				queryWrapperAdd.eq("is_send","1");
			}

			queryWrapperAdd.apply("isok=1");

			List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);

			if (scoreAddList.size() > 0) {
				for (int j = 0; j < scoreAddList.size(); j++) {
					addScore+=Double.parseDouble(scoreAddList.get(j).getScore());
				}
			}
			//addScore = addScore * 0.1;
			addScore = Double.parseDouble(String.format("%.2f", addScore));
			detail.get(i).setAddScore(addScore);

			//TODO 减分项
			double minusScore = 0.0;
			QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
			queryWrapperMinus.select(" * ");
			queryWrapperMinus.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
//			queryWrapperMinus.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperMinus.eq("year(create_time)", quarterlySumScore.getStageYear());
			queryWrapperMinus.eq(" quarter(create_time)",quarterNum);
			queryWrapperMinus.eq("check_way","1");

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {queryWrapperMinus.eq("is_send","1");}

			List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
			if (scoreMinusList.size() > 0) {
				for (int j = 0; j < scoreMinusList.size(); j++) {
					minusScore+=Double.parseDouble(scoreMinusList.get(j).getScore());
				}
			}
			//minusScore = minusScore * 0.1;
			minusScore = Double.parseDouble(String.format("%.2f", minusScore));
			detail.get(i).setMinusScore(minusScore);

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
			String deptId = detail.get(i).getResponsibleUnitId();
			//如果当前单位的id属于县区，那就查找子部门的分数
			String deptId1 = deptId;
			if (deptIds.contains(deptId)) {
				R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
				List<Dept> deptChildRData = deptChildR.getData();
				if (deptChildRData.size() > 0) {
					for (int j = 0; j < deptChildRData.size(); j++) {
						deptId1+= "," +deptChildRData.get(j).getId();
					}
				}
			}
			String[] deptIdss = deptId1.split(",");
			QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
			dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
			dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
			dcdbqueryWrapper.in("dept_id",deptIdss);
			dcdbqueryWrapper.likeRight("create_time",quarterlySumScore.getStageYear());
			dcdbqueryWrapper.groupBy("dept_id,serv_code");

			List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
			int size = 0;
			if (supervisionScores.size() > 0) {
				for (int j = 0; j < supervisionScores.size(); j++) {
					LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
						.select(SupervisionInfo::getFlowStatus)
						.eq(SupervisionInfo::getServCode, supervisionScores.get(j).getServCode());
					SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
					if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
						dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(j).getScore()));
						size++;
					}
				}
			}
			//根据supervisionScores长度计算dcdbScore的平均分
			if (supervisionScores.size() > 0) {
				dcdbScore = dcdbScore / size;
			}
			//将督查督办得分的10%算到季度评价中
			dcdbScore = dcdbScore * 0.1;
			dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
			if (dcdbScore < 0.0) {
				detail.get(i).setDcdbScore(0.0);
			}
			if(dcdbScore < 10.0) {
				detail.get(i).setDcdbScore(dcdbScore);
			}else {
				detail.get(i).setDcdbScore(10.0);
			}*/


			//TODO 保留两位小数
			//20230406 单独处理工作实绩分数，添加缺项情况处理
			/*Double gzsjScore = detail.get(i).getGzsjScore();
			String responsibleUnitId = detail.get(i).getResponsibleUnitId(); //责任单位id
			String stageYear = detail.get(i).getStageYear();  //年份
			String stage = detail.get(i).getStage();  //季度
			Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage);
			Double trueFullScore = Double.valueOf(map.get("totalWeight").toString());*/
			Double newGzsjScore =detail.get(i).getGzsjScore();
			/*
			工作实绩 添加考评总分列数据 包含：
			考评总分=XXX
			工作实绩指标总分值（A1）=100
			扣除缺项后的指标分值(A2)=XXX
			承担工作的实际得分(A3)=XXX
			效率系数(α)=XXX
			考评总分=A3+α×（A1-A2)。
			*/
			//1、工作实绩指标总分值（A1）=100 固定值
			Double A1 = Double.valueOf(100);
			//2、扣除缺项后的指标分值(A2)
			String responsibleUnitId = detail.get(i).getResponsibleUnitId();
			String stageYear = detail.get(i).getStageYear();
			String stage = detail.get(i).getStage();
			Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage, "2");
			Double A2 = Double.valueOf(map.get("totalWeight").toString());
			newGzsjScore = A2 == 0 ? 0 : newGzsjScore / A2 * 100 * 0.9;

			detail.get(i).setDjgzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDjgzScore())));
			//列表展示 工作实绩占 90%
			detail.get(i).setGzsjScore(Double.parseDouble(String.format("%.2f", newGzsjScore)));
			detail.get(i).setDflzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDflzScore())));
			detail.get(i).setS3z3cScore(Double.parseDouble(String.format("%.2f", detail.get(i).getS3z3cScore())));

			//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
//			double sumScore = detail.get(i).getQuarterlySumScore() + leaderScore + addScore - minusScore;// + detail.get(i).getDcdbScore()
//			double sumScore = detail.get(i).getQuarterlySumScore() + leaderScore + addScore - minusScore;// + detail.get(i).getDcdbScore()
			//评价总分： = 党建工作得分 + 工作实绩得分 + 党风廉政得分 + 三抓三促得分 + 加分 - 减分 + 领导评价得分 + 督查督办得分
			double sumScore = detail.get(i).getDjgzScore() + detail.get(i).getGzsjScore() + detail.get(i).getDflzScore() + detail.get(i).getS3z3cScore()
				+ addScore - minusScore + leaderScore;
			sumScore = Double.parseDouble(String.format("%.2f", sumScore));
			detail.get(i).setQuarterlySumScore(sumScore);
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

		//获取分组后的单位id
		R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000",quarterlySumScore.getCheckClassify(), quarterlySumScore.getStageYear());
		System.out.println("//Rdeptids======= " + Rdeptids);
		List<Dept> depts = Rdeptids.getData();
		if (depts.size() > 0 && StringUtils.isEmpty(quarterlySumScore.getResponsibleUnitId())) {
			for (int i = 0; i < depts.size(); i++) {
				String deptId = depts.get(i).getId().toString();
				String deptName = depts.get(i).getDeptName();
				//TODO 领导评价得分 总分 × 20%
				double leaderScore = 0.0;
				QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
				leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
				leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				leaderAppriseQueryWrapper.eq("evaluation_type", "2");

				//没有打分详情角色的账号  要发布过后才能查看分数
				if (!roleNames.contains(isLookRole)) {
					leaderAppriseQueryWrapper.eq("is_send", "1");
				}

				leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
//				leaderAppriseQueryWrapper.eq("year(create_time)", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.groupBy("apprise_rolename");
				List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
				try {
					if (leaderApprise.size() > 0) {
						for (LeaderApprise leaderApprise1 : leaderApprise) {
							leaderScore += beginQuarter(leaderApprise1) ?
								Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getAvgScore());
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
//					queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperAdd.eq("year(create_time)", quarterlySumScore.getStageYear());
				//2023年7月11日 改为季度查询
				queryWrapperAdd.eq(" quarter(create_time)",quarterNum);
				queryWrapperAdd.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperAdd.eq("is_send","1");}

				queryWrapperAdd.apply("isok=1");
				List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
				if (scoreAddList.size() > 0) {
					for (int j = 0; j < scoreAddList.size(); j++) {
						addScore += Double.parseDouble(scoreAddList.get(j).getScore());
					}
				}
				//addScore = addScore * 0.1;
				addScore = Double.parseDouble(String.format("%.2f", addScore));

				//TODO 减分项
				double minusScore = 0.0;
				QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
				queryWrapperMinus.select(" * ");
				queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
//					queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperMinus.eq(" quarter(create_time)",quarterNum);
				queryWrapperMinus.eq("check_way", "1");
				queryWrapperMinus.eq("year(create_time)", quarterlySumScore.getStageYear());

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperMinus.eq("is_send","1");}

				List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
				if (scoreMinusList.size() > 0) {
					for (int j = 0; j < scoreMinusList.size(); j++) {
						minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
					}
				}
				//minusScore = minusScore * 0.1;
				minusScore = Double.parseDouble(String.format("%.2f", minusScore));

				//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
				double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));

				if (list1.size() > 0) {
					if (!list1.toString().contains(deptName)) {
						QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
						quarterlySumScore1.setResponsibleUnitId(deptId);
						quarterlySumScore1.setResponsibleUnitName(deptName);
						quarterlySumScore1.setDflzScore(0.0);
						quarterlySumScore1.setGzsjScore(0.0);
						quarterlySumScore1.setDjgzScore(0.0);
						quarterlySumScore1.setS3z3cScore(0.0);
						quarterlySumScore1.setAddScore(addScore);
						quarterlySumScore1.setMinusScore(minusScore);
						quarterlySumScore1.setLeaderScore(leaderScore);
						quarterlySumScore1.setQuarterlySumScore(sumScore);
						quarterlySumScore1.setQuarterlyType(0);
						detail.add(quarterlySumScore1);
					}
				} else if (list1.size() == 0) {
					QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
					quarterlySumScore1.setResponsibleUnitId(deptId);
					quarterlySumScore1.setResponsibleUnitName(deptName);
					quarterlySumScore1.setDflzScore(0.0);
					quarterlySumScore1.setGzsjScore(0.0);
					quarterlySumScore1.setDjgzScore(0.0);
					quarterlySumScore1.setS3z3cScore(0.0);
					quarterlySumScore1.setAddScore(addScore);
					quarterlySumScore1.setMinusScore(minusScore);
					quarterlySumScore1.setLeaderScore(leaderScore);
					quarterlySumScore1.setQuarterlySumScore(sumScore);
					quarterlySumScore1.setQuarterlyType(0);
					detail.add(quarterlySumScore1);
				}
			}
		}
		else if(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId())){
			Dept dept = SysCache.getDept(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
			if (dept != null) {
				String deptId = dept.getId().toString();
				String deptName = dept.getDeptName();
				//TODO 领导评价得分 总分 × 20%
				double leaderScore = 0.0;
				QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
				leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
				leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				leaderAppriseQueryWrapper.eq("evaluation_type", "2");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {leaderAppriseQueryWrapper.eq("is_send","1");}

				leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);

				leaderAppriseQueryWrapper.groupBy("apprise_rolename");
				List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
				try {
					if (leaderApprise.size() > 0) {
						for (LeaderApprise leaderApprise1 : leaderApprise) {
							leaderScore += beginQuarter(leaderApprise1) ?
								Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getAvgScore());
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
				queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperAdd.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperAdd.eq("is_send","1");}

				queryWrapperAdd.apply("isok=1");
				List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
				if (scoreAddList.size() > 0) {
					for (int j = 0; j < scoreAddList.size(); j++) {
						addScore += Double.parseDouble(scoreAddList.get(j).getScore());
					}
				}
				//addScore = addScore * 0.1;
				addScore = Double.parseDouble(String.format("%.2f", addScore));

				//TODO 减分项
				double minusScore = 0.0;
				QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
				queryWrapperMinus.select(" * ");
				queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperMinus.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperMinus.eq("is_send","1");}

				List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
				if (scoreMinusList.size() > 0) {
					for (int j = 0; j < scoreMinusList.size(); j++) {
						minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
					}
				}
				//minusScore = minusScore * 0.1;
				minusScore = Double.parseDouble(String.format("%.2f", minusScore));

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
				//如果当前单位的id属于县区，那就查找子部门的分数
				String deptId1 = deptId;
				if (deptIds.contains(deptId)) {
					R<List<Dept>> deptChildR = sysClient.getDeptChild(Long.valueOf(deptId));
					List<Dept> deptChildRData = deptChildR.getData();
					if (deptChildRData.size() > 0) {
						for (int j = 0; j < deptChildRData.size(); j++) {
							deptId1+= "," +deptChildRData.get(j).getId();
						}
					}
				}
				String[] deptIdss = deptId1.split(",");
				QueryWrapper<SupervisionScore> dcdbqueryWrapper = new QueryWrapper<>();
				dcdbqueryWrapper.select("serv_code,dept_id,score_type,sum(score) score,create_user,create_dept");
				dcdbqueryWrapper.apply("quarter(create_time)="+quarterNum);
				dcdbqueryWrapper.in("dept_id",deptIdss);
				dcdbqueryWrapper.likeRight("create_time",quarterlySumScore.getStageYear());
				dcdbqueryWrapper.groupBy("dept_id,serv_code");
				List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(dcdbqueryWrapper);
				int size = 0;
				if (supervisionScores.size() > 0) {
					for (int j = 0; j < supervisionScores.size(); j++) {
						LambdaQueryWrapper<SupervisionInfo> lambdaQueryWrapper = Wrappers.<SupervisionInfo>query().lambda()
							.select(SupervisionInfo::getFlowStatus)
							.eq(SupervisionInfo::getServCode, supervisionScores.get(j).getServCode());
						SupervisionInfo supervisionInfo = iSupervisionInfoService.getOne(lambdaQueryWrapper);
						if (supervisionInfo.getFlowStatus().trim().equals("4")) {//4表示事项已办结
							dcdbScore+=Double.parseDouble(String.valueOf(supervisionScores.get(j).getScore()));
							size++;
						}
					}
				}
				//根据supervisionScores长度计算dcdbScore的平均分
				if (supervisionScores.size() > 0) {
					dcdbScore = dcdbScore / size;
				}
				//将督查督办得分的10%算到季度评价中,最多10分
				dcdbScore = dcdbScore * 0.1;
				dcdbScore = Double.parseDouble(String.format("%.2f", dcdbScore));
				if(dcdbScore < 0){
					dcdbScore = 0.0;
				}
				if(dcdbScore > 10.0) {
					dcdbScore = 10.0;
				}*/
				//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
				double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));

				QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
				quarterlySumScore1.setResponsibleUnitId(deptId);
				quarterlySumScore1.setResponsibleUnitName(deptName);
				quarterlySumScore1.setDflzScore(0.0);
				quarterlySumScore1.setGzsjScore(0.0);
				quarterlySumScore1.setDjgzScore(0.0);
				quarterlySumScore1.setS3z3cScore(0.0);
				quarterlySumScore1.setAddScore(addScore);
				quarterlySumScore1.setMinusScore(minusScore);
				quarterlySumScore1.setLeaderScore(leaderScore);
				quarterlySumScore1.setQuarterlySumScore(sumScore);
				quarterlySumScore1.setQuarterlyType(0);
				detail.add(quarterlySumScore1);
			}
		}

		//给总得分排名次
		double[] annualscore = new double[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			annualscore[i] = detail.get(i).getQuarterlySumScore();
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
			for (QuarterlySumScore sumScore : detail) {
				if (sumScore.getQuarterlySumScore().equals(list.get(i))) {
					sumScore.setQuarterlyType(i + 1);
				}
			}
		}
		//对【排名进行】进行升序排列
		detail.sort(new Comparator<QuarterlySumScore>() {
			@Override
			public int compare(QuarterlySumScore o1, QuarterlySumScore o2) {
				Integer i1 = o1.getQuarterlyType();
				Integer i2 = o2.getQuarterlyType();
				return i1.compareTo(i2);
			}
		});
		return R.data(detail);
	}

	/**如果是上半年或者下半年走这个方法*/
	public R<List<QuarterlySumScore>> SyBnDetail(QuarterlySumScore quarterlySumScore) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(currentUser.getRoleId()).toString();

		//当前角色是否包含 打分详情 或者 考核绩效发布
		boolean flag = roleNames.contains(isLookRole) || roleNames.contains("考核绩效发布");

		String quarterNum = quarterlySumScore.getStage().equals("上半年") ? "5" : "6";

		//sql查询条件
		QueryWrapper<QuarterlySumScore> queryWrapper = new QueryWrapper<QuarterlySumScore>();
		queryWrapper.select(" check_classify,check_classify_name,\n" +
				"       responsible_unit_id,responsible_unit_name,\n" +
				"       is_send,\n" +
				"        serv_name,stage,stage_year,\n" +
				"       sum(djgz_score) as djgz_score,\n" +
				"       sum(gzsj_score) as gzsj_score,\n" +
				"       sum(dflz_score) as dflz_score,\n" +
				"       sum(s3z3c_score) as s3z3c_score,\n" +
				"       sum(add_score) as add_score,\n" +
				"       sum(minus_score) as minus_score,\n" +
				"       sum(leader_score) as leader_score,\n" +
				"       sum(quarterly_sum_score) as quarterly_sum_score ");
		//queryWrapper.eq(quarterlySumScore.getStage()!=null && !Objects.equals(quarterlySumScore.getStage(), ""),"stage",quarterlySumScore.getStage());
		queryWrapper.eq(quarterlySumScore.getStageYear()!=null && !Objects.equals(quarterlySumScore.getStageYear(), ""),"stage_year",quarterlySumScore.getStageYear());
		queryWrapper.eq(quarterlySumScore.getCheckClassify()!=null && !Objects.equals(quarterlySumScore.getCheckClassify(), ""),"check_classify",quarterlySumScore.getCheckClassify());
		queryWrapper.eq(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId()),"responsible_unit_id",quarterlySumScore.getResponsibleUnitId());
		queryWrapper.eq(StringUtils.isNotBlank(quarterlySumScore.getStage()),"stage",quarterlySumScore.getStage());


		//没有打分详情角色的账号  要发布过后才能查看分数
		if (!roleNames.contains(isLookRole)) {
			queryWrapper.apply(" is_send = 1");
		}
		//2023年4月12日 与现场沟通确认 去掉打分详情角色过滤 统一查发布后的数据
//		queryWrapper.apply(" is_send = 1");

		//2023年4月19日 与现场沟通确认 发布角色在未发布之前就要能看到，不然不知道能不能发布
		if (!roleNames.contains("考核绩效发布")) {
			queryWrapper.apply(" is_send = 1");
		}

		queryWrapper.groupBy("responsible_unit_name");
		queryWrapper.orderByDesc("quarterly_sum_score");
		List<QuarterlySumScore> detail = iQuarterlySumScoreService.list(queryWrapper);
		System.out.println("detail = " + detail);
		for (int i = 0; i < detail.size(); i++) {
			//TODO 领导评价得分 10分 总分 × 10%
			double leaderScore = 0.0;
			QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper =new QueryWrapper<>();
			leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
			leaderAppriseQueryWrapper.eq(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId().toString());
			leaderAppriseQueryWrapper.eq("evaluation_type","2");
			//没有打分详情角色的账号  要发布过后才能查看分数
			if (!roleNames.contains(isLookRole)) {
				leaderAppriseQueryWrapper.eq("is_send","1");
			}
			leaderAppriseQueryWrapper.like("apprise_year",quarterlySumScore.getStageYear());
			leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
			leaderAppriseQueryWrapper.groupBy("apprise_rolename");
			List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
			try {
				if (leaderApprise.size() > 0) {
					for (LeaderApprise leaderApprise1 : leaderApprise) {
						leaderScore += beginQuarter(leaderApprise1) ?
								Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
//						leaderScore += Double.parseDouble(leaderApprise1.getScore());
//						leaderScore += Double.parseDouble(leaderApprise1.getAvgScore());
					}
				}
				leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
				leaderScore = leaderScore * 0.1;
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
			queryWrapperAdd.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
//			queryWrapperAdd.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperAdd.eq("year(create_time)", quarterlySumScore.getStageYear());
			if (Integer.parseInt(quarterNum) == 5) {
				//如果查上半年 则月份小于等于6
				queryWrapperAdd.le(" month(create_time)",6);
			} else {
				//否则就是查下半年，月份大于6
				queryWrapperAdd.gt(" month(create_time)",6);
			}
			// queryWrapperAdd.eq(" quarter(create_time)",quarterNum);
			queryWrapperAdd.eq("check_way","1");

			//没有打分详情 或者考核绩效发布的 的账号需要发布后才能看到加分项
			if (!flag) {
				queryWrapperAdd.eq("is_send","1");
			}

			queryWrapperAdd.apply("isok=1");

			List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);

			if (scoreAddList.size() > 0) {
				for (int j = 0; j < scoreAddList.size(); j++) {
					addScore+=Double.parseDouble(scoreAddList.get(j).getScore());
				}
			}
			//addScore = addScore * 0.1;
			addScore = Double.parseDouble(String.format("%.2f", addScore));
			detail.get(i).setAddScore(addScore);

			//TODO 减分项
			double minusScore = 0.0;
			QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
			queryWrapperMinus.select(" * ");
			queryWrapperMinus.like(detail.get(i).getResponsibleUnitId()!=null,"dept_id",detail.get(i).getResponsibleUnitId());
//			queryWrapperMinus.like("create_time",quarterlySumScore.getStageYear());
			queryWrapperMinus.eq("year(create_time)", quarterlySumScore.getStageYear());
			//queryWrapperMinus.eq(" quarter(create_time)",quarterNum);
			if (Integer.parseInt(quarterNum) == 5) {
				//如果查上半年 则月份小于等于6
				queryWrapperMinus.le(" month(create_time)",6);
			} else {
				//否则就是查下半年，月份大于6
				queryWrapperMinus.gt(" month(create_time)",6);
			}
			queryWrapperMinus.eq("check_way","1");

			//判断是否包含打分详情或者考核绩效发布
			if(!flag) {queryWrapperMinus.eq("is_send","1");}

			List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
			if (scoreMinusList.size() > 0) {
				for (int j = 0; j < scoreMinusList.size(); j++) {
					minusScore+=Double.parseDouble(scoreMinusList.get(j).getScore());
				}
			}
			//minusScore = minusScore * 0.1;
			minusScore = Double.parseDouble(String.format("%.2f", minusScore));
			detail.get(i).setMinusScore(minusScore);

			//TODO 工作实绩分数 65分
			//TODO 保留两位小数
			Double newGzsjScore =detail.get(i).getGzsjScore();
			/*
			工作实绩 添加考评总分列数据 包含：
			考评总分=XXX
			工作实绩指标总分值（A1）=100
			扣除缺项后的指标分值(A2)=XXX
			承担工作的实际得分(A3)=XXX
			效率系数(α)=XXX
			考评总分=A3+α×（A1-A2)。
			*/
			//1、工作实绩指标总分值（A1）=100 固定值
			Double A1 = Double.valueOf(100);
			//2、扣除缺项后的指标分值(A2)
			String responsibleUnitId = detail.get(i).getResponsibleUnitId();
			String stageYear = detail.get(i).getStageYear();
			String stage = detail.get(i).getStage();
			Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage, "2");
			Double A2 = Double.valueOf(map.get("totalWeight").toString());
			newGzsjScore = A2 == 0 ? 0 : newGzsjScore / A2 * 100 * 0.65;

			detail.get(i).setDjgzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDjgzScore())));
			//列表展示 工作实绩占 90%
			detail.get(i).setGzsjScore(Double.parseDouble(String.format("%.2f", newGzsjScore)));
			detail.get(i).setDflzScore(Double.parseDouble(String.format("%.2f", detail.get(i).getDflzScore())));
			detail.get(i).setS3z3cScore(Double.parseDouble(String.format("%.2f", detail.get(i).getS3z3cScore())));

			//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
			//评价总分： = 党建工作得分 + 工作实绩得分 + 党风廉政得分 + 三抓三促得分 + 加分 - 减分 + 领导评价得分 + 督查督办得分
			double sumScore = detail.get(i).getDjgzScore() + detail.get(i).getGzsjScore() + detail.get(i).getDflzScore() + detail.get(i).getS3z3cScore()
					+ addScore - minusScore + leaderScore;
			sumScore = Double.parseDouble(String.format("%.2f", sumScore));
			detail.get(i).setQuarterlySumScore(sumScore);
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

		//获取分组后的单位id
		R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000",quarterlySumScore.getCheckClassify(), quarterlySumScore.getStageYear());
		System.out.println("//Rdeptids======= " + Rdeptids);
		List<Dept> depts = Rdeptids.getData();
		if (depts.size() > 0 && StringUtils.isEmpty(quarterlySumScore.getResponsibleUnitId())) {
			for (int i = 0; i < depts.size(); i++) {
				String deptId = depts.get(i).getId().toString();
				String deptName = depts.get(i).getDeptName();
				//TODO 领导评价得分10 总分 × 10%
				double leaderScore = 0.0;
				QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
				leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
				leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				leaderAppriseQueryWrapper.eq("evaluation_type", "2");

				//没有打分详情角色的账号  要发布过后才能查看分数
				if (!roleNames.contains(isLookRole)) {
					leaderAppriseQueryWrapper.eq("is_send", "1");
				}

				leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);
				// leaderAppriseQueryWrapper.eq("year(create_time)", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.groupBy("apprise_rolename");
				List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
				try {
					if (leaderApprise.size() > 0) {
						for (LeaderApprise leaderApprise1 : leaderApprise) {
							leaderScore += beginQuarter(leaderApprise1) ?
									Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getScore());
//							leaderScore += Double.parseDouble(leaderApprise1.getAvgScore());
						}
					}
					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
					leaderScore = leaderScore * 0.1;
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
//					queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperAdd.eq("year(create_time)", quarterlySumScore.getStageYear());
				//2023年7月11日 改为季度查询
				//queryWrapperAdd.eq(" quarter(create_time)",quarterNum);
				if (Integer.parseInt(quarterNum) == 5) {
					//如果查上半年 则月份小于等于6
					queryWrapperAdd.le(" month(create_time)",6);
				} else {
					//否则就是查下半年，月份大于6
					queryWrapperAdd.gt(" month(create_time)",6);
				}
				queryWrapperAdd.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperAdd.eq("is_send","1");}

				queryWrapperAdd.apply("isok=1");
				List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
				if (scoreAddList.size() > 0) {
					for (int j = 0; j < scoreAddList.size(); j++) {
						addScore += Double.parseDouble(scoreAddList.get(j).getScore());
					}
				}
				//addScore = addScore * 0.1;
				addScore = Double.parseDouble(String.format("%.2f", addScore));

				//TODO 减分项
				double minusScore = 0.0;
				QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
				queryWrapperMinus.select(" * ");
				queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
//					queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
				// queryWrapperMinus.eq(" quarter(create_time)",quarterNum);
				if (Integer.parseInt(quarterNum) == 5) {
					//如果查上半年 则月份小于等于6
					queryWrapperMinus.le(" month(create_time)",6);
				} else {
					//否则就是查下半年，月份大于6
					queryWrapperMinus.gt(" month(create_time)",6);
				}
				queryWrapperMinus.eq("check_way", "1");
				queryWrapperMinus.eq("year(create_time)", quarterlySumScore.getStageYear());

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperMinus.eq("is_send","1");}

				List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
				if (scoreMinusList.size() > 0) {
					for (int j = 0; j < scoreMinusList.size(); j++) {
						minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
					}
				}
				//minusScore = minusScore * 0.1;
				minusScore = Double.parseDouble(String.format("%.2f", minusScore));

				//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
				double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));

				if (list1.size() > 0) {
					if (!list1.toString().contains(deptName)) {
						QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
						quarterlySumScore1.setResponsibleUnitId(deptId);
						quarterlySumScore1.setResponsibleUnitName(deptName);
						quarterlySumScore1.setDflzScore(0.0);
						quarterlySumScore1.setGzsjScore(0.0);
						quarterlySumScore1.setDjgzScore(0.0);
						quarterlySumScore1.setS3z3cScore(0.0);
						quarterlySumScore1.setAddScore(addScore);
						quarterlySumScore1.setMinusScore(minusScore);
						quarterlySumScore1.setLeaderScore(leaderScore);
						quarterlySumScore1.setQuarterlySumScore(sumScore);
						quarterlySumScore1.setQuarterlyType(0);
						detail.add(quarterlySumScore1);
					}
				} else if (list1.size() == 0) {
					QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
					quarterlySumScore1.setResponsibleUnitId(deptId);
					quarterlySumScore1.setResponsibleUnitName(deptName);
					quarterlySumScore1.setDflzScore(0.0);
					quarterlySumScore1.setGzsjScore(0.0);
					quarterlySumScore1.setDjgzScore(0.0);
					quarterlySumScore1.setS3z3cScore(0.0);
					quarterlySumScore1.setAddScore(addScore);
					quarterlySumScore1.setMinusScore(minusScore);
					quarterlySumScore1.setLeaderScore(leaderScore);
					quarterlySumScore1.setQuarterlySumScore(sumScore);
					quarterlySumScore1.setQuarterlyType(0);
					detail.add(quarterlySumScore1);
				}
			}
		} else if(StringUtils.isNotEmpty(quarterlySumScore.getResponsibleUnitId())){
			Dept dept = SysCache.getDept(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
			if (dept != null) {
				String deptId = dept.getId().toString();
				String deptName = dept.getDeptName();
				//TODO 领导评价得分10 总分 × 10%
				double leaderScore = 0.0;
				QueryWrapper<LeaderApprise> leaderAppriseQueryWrapper = new QueryWrapper<>();
				leaderAppriseQueryWrapper.select(" *,ROUND(AVG(score),2) as avgScore  ");
				leaderAppriseQueryWrapper.eq(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				leaderAppriseQueryWrapper.eq("evaluation_type", "2");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {leaderAppriseQueryWrapper.eq("is_send","1");}

				leaderAppriseQueryWrapper.like("apprise_year", quarterlySumScore.getStageYear());
				leaderAppriseQueryWrapper.apply("apprise_quarter=" + quarterNum);

				leaderAppriseQueryWrapper.groupBy("apprise_rolename");
				List<LeaderApprise> leaderApprise = iLeaderAppriseService.list(leaderAppriseQueryWrapper);
				try {
					if (leaderApprise.size() > 0) {
						for (LeaderApprise leaderApprise1 : leaderApprise) {
							leaderScore += beginQuarter(leaderApprise1) ?
									Double.parseDouble(leaderApprise1.getAvgScore()) : Double.parseDouble(leaderApprise1.getScore());
						}
					}
					leaderScore = Double.parseDouble(String.format("%.2f", leaderScore));
					leaderScore = leaderScore * 0.1;
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
				queryWrapperAdd.like("create_time", quarterlySumScore.getStageYear());
				queryWrapperAdd.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperAdd.eq("is_send","1");}

				queryWrapperAdd.apply("isok=1");
				List<ScoreAdd> scoreAddList = iScoreAddService.list(queryWrapperAdd);
				if (scoreAddList.size() > 0) {
					for (int j = 0; j < scoreAddList.size(); j++) {
						addScore += Double.parseDouble(scoreAddList.get(j).getScore());
					}
				}
				//addScore = addScore * 0.1;
				addScore = Double.parseDouble(String.format("%.2f", addScore));

				//TODO 减分项
				double minusScore = 0.0;
				QueryWrapper<ScoreMinus> queryWrapperMinus = new QueryWrapper<>();
				queryWrapperMinus.select(" * ");
				queryWrapperMinus.like(StringUtils.isNotBlank(deptId), "dept_id", deptId);
				queryWrapperMinus.like("create_time", quarterlySumScore.getStageYear());
				if (Integer.parseInt(quarterNum) == 5) {
					//如果查上半年 则月份小于等于6
					queryWrapperMinus.le(" month(create_time)",6);
				} else {
					//否则就是查下半年，月份大于6
					queryWrapperMinus.gt(" month(create_time)",6);
				}
				queryWrapperMinus.eq("check_way", "1");

				//判断是否包含打分详情或者考核绩效发布
				if(!flag) {queryWrapperMinus.eq("is_send","1");}

				List<ScoreMinus> scoreMinusList = iScoreMinusService.list(queryWrapperMinus);
				if (scoreMinusList.size() > 0) {
					for (int j = 0; j < scoreMinusList.size(); j++) {
						minusScore += Double.parseDouble(scoreMinusList.get(j).getScore());
					}
				}
				//minusScore = minusScore * 0.1;
				minusScore = Double.parseDouble(String.format("%.2f", minusScore));

				//TODO 计算总分 评价总分 + 领导评价得分 + 加分项 - 减分项 + 督查督办得分（已注释，不参与总分计算）
				double sumScore = leaderScore + addScore - minusScore;// + dcdbScore
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));

				QuarterlySumScore quarterlySumScore1 = new QuarterlySumScore();
				quarterlySumScore1.setResponsibleUnitId(deptId);
				quarterlySumScore1.setResponsibleUnitName(deptName);
				quarterlySumScore1.setDflzScore(0.0);
				quarterlySumScore1.setGzsjScore(0.0);
				quarterlySumScore1.setDjgzScore(0.0);
				quarterlySumScore1.setS3z3cScore(0.0);
				quarterlySumScore1.setAddScore(addScore);
				quarterlySumScore1.setMinusScore(minusScore);
				quarterlySumScore1.setLeaderScore(leaderScore);
				quarterlySumScore1.setQuarterlySumScore(sumScore);
				quarterlySumScore1.setQuarterlyType(0);
				detail.add(quarterlySumScore1);
			}
		}

		//给总得分排名次
		double[] annualscore = new double[detail.size()];
		for (int i = 0; i < detail.size(); i++) {
			annualscore[i] = detail.get(i).getQuarterlySumScore();
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
			for (QuarterlySumScore sumScore : detail) {
				if (sumScore.getQuarterlySumScore().equals(list.get(i))) {
					sumScore.setQuarterlyType(i + 1);
				}
			}
		}
		//对【排名进行】进行升序排列
		detail.sort(new Comparator<QuarterlySumScore>() {
			@Override
			public int compare(QuarterlySumScore o1, QuarterlySumScore o2) {
				Integer i1 = o1.getQuarterlyType();
				Integer i2 = o2.getQuarterlyType();
				return i1.compareTo(i2);
			}
		});
		return R.data(detail);
	}
	//判断是否是从2023年 第二季度开始
	public boolean beginQuarter(LeaderApprise leaderApprise){
		boolean result = false;
		int year = Integer.parseInt(leaderApprise.getAppriseYear());
		int quarter = Integer.parseInt(leaderApprise.getAppriseQuarter());
		if (year == 2023) result = quarter > 1 ? true : false;
		if (year > 2023 || result) result = true;
		return result;
	}

	/**
	 * 季度指标完成情况百分比信息
	 */
	@GetMapping("/SumDetail")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-首页季度指标完成情况百分比信息", notes = "传入 quarterlySumScore")
	public R<List<Map<String, Object>>> detail(QuarterlyEvaluation quarterlyEvaluation) {
		String quarter = quarterlyEvaluation.getQuarterStr();
		if ("1".equals(quarter)) {//第一季度
			quarter = "第一季度";
		}else if ("2".equals(quarter)) {//第二季度
			quarter = "第二季度";
		}else if ("3".equals(quarter)) {//第三季度
			quarter = "第三季度";
		}else if ("4".equals(quarter)) {//第四季度
			quarter = "第四季度";
		}else if ("5".equals(quarter)) {//上半年
			quarter = "上半年";
		}else if ("6".equals(quarter)) {//下半年
			quarter = "下半年";
		}
		//sql查询条件
		QueryWrapper<QuarterlyEvaluation> queryWrapper = new QueryWrapper<QuarterlyEvaluation>();
		queryWrapper.select("target_status,count(target_status) target_status_num ");
		queryWrapper.in(quarterlyEvaluation.getCheckClassify()!=null && !Objects.equals(quarterlyEvaluation.getCheckClassify(), ""),"check_classify",quarterlyEvaluation.getCheckClassify());
		queryWrapper.like("create_time",quarterlyEvaluation.getQuarterlyYear());
		if (StringUtils.isNotNull(quarterlyEvaluation.getTargetStatus())) {
			queryWrapper.in("target_status",quarterlyEvaluation.getTargetStatus().split(","));
		}
		queryWrapper.eq("to_quarter",quarter);
		queryWrapper.groupBy("target_status");
		List<Map<String, Object>> detail = iQuarterlyEvaluationService.listMaps(queryWrapper);
		if (detail.size()==0) {
			Map<String, Object> map = new HashMap<>();
			map.put("target_status",0);
			map.put("target_status_num",0);
			detail.add(map);
		}
		return R.data(detail);
	}

	/**
	 * 修改考核评价-季度指标指标为发布状态
	 */
	@GetMapping("/send")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "修改考核评价-季度指标指标为发布状态", notes = "传入 ids")
	public R send(@RequestParam("ids") String ids) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String[] id = ids.split(",");
		//sql查询条件
		List<QuarterlyEvaluation> quarterlyEvaluations = null;
		QueryWrapper<QuarterlyEvaluation> queryWrapper = new QueryWrapper<>();
		if (id.length > 0) {
			queryWrapper.in("id",id);
			quarterlyEvaluations = iQuarterlyEvaluationService.list(queryWrapper);
		}
		if (quarterlyEvaluations != null && quarterlyEvaluations.size()>0) {
			for (int i = 0; i < quarterlyEvaluations.size(); i++) {
				UpdateWrapper<QuarterlySumScore> quarterlySumScoreUpdateWrapper =new UpdateWrapper<>();
				quarterlySumScoreUpdateWrapper.set("is_send",1);
				quarterlySumScoreUpdateWrapper.eq("quarterly_evaluation_id",quarterlyEvaluations.get(i).getId());
				quarterlySumScoreUpdateWrapper.in("responsible_unit_id",quarterlyEvaluations.get(i).getCheckObjectId().split(","));
				//quarterlySumScoreUpdateWrapper.eq("appraise_deptid",user.getDeptId());
				iQuarterlySumScoreService.update(quarterlySumScoreUpdateWrapper);
			}


			String title1 = "修改考核评价-季度指标指标评价为发布状态";
			String businessId = String.valueOf(ids);
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.success("发布成功！");
		}
		return R.fail("发布失败！");
	}




	/**
	 * 季度指标基本信息中的指标得分表
	 */
	@GetMapping("/baseInfo")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-季度指标基本信息中的指标得分表", notes = "传入 quarterlySumScore")
	public R<Map<String,Map<String, Object>>> baseInfo(QuarterlyEvaluation quarterlyEvaluation) {
		QuarterlyEvaluation quarterlyEvaluation1 = iQuarterlyEvaluationService.getById(quarterlyEvaluation.getId());

		//获取评价部门ID
		String appraiseDeptId = quarterlyEvaluation1.getAppraiseDeptid();
		String[] appraiseDeptIds = appraiseDeptId.split(",");
		//获取被考核部门ID
		String responsibleUnitId = quarterlyEvaluation1.getCheckObjectId();
		String[] responsibleUnitIds = responsibleUnitId.split(",");
		//获取被考核部门名称
		String responsibleUnitName = quarterlyEvaluation1.getCheckObject();
		String[] responsibleUnitNames = responsibleUnitName.split(",");

		List<List<QuarterlySumScore>> listArrayList = new ArrayList<>();

		//循环查询每个评价部门的各项总分
		for (int i = 0; i < appraiseDeptIds.length; i++) {
			//sql查询条件
			QueryWrapper<QuarterlySumScore> queryWrapper = new QueryWrapper<QuarterlySumScore>();
			queryWrapper.select("responsible_unit_id,responsible_unit_name,appraise_deptid,appraise_deptname," +
				"sum(quarterly_sum_score) quarterly_sum_score ");
			queryWrapper.eq("quarterly_evaluation_id",quarterlyEvaluation.getId().toString());
			queryWrapper.eq("appraise_deptid",appraiseDeptIds[i]);
			queryWrapper.in("responsible_unit_id",responsibleUnitIds);
			queryWrapper.eq(StringUtils.isNotBlank(quarterlyEvaluation.getStageId()),"stage_id",quarterlyEvaluation.getStageId());
			queryWrapper.groupBy("responsible_unit_id");
			queryWrapper.orderByAsc("responsible_unit_id");
			List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper);
			if (quarterlySumScoreList != null & quarterlySumScoreList.size()>0) {
				List<AppriseDept> appriseDeptList = iAppriseDeptService.list(
					Wrappers.<AppriseDept>query().lambda()
						.eq(AppriseDept::getEvaluationId,quarterlyEvaluation.getId())
						.eq(AppriseDept::getType,"1")
						.eq(AppriseDept::getCreateDept,appraiseDeptIds[i])
				);
				if (appriseDeptList.size() > 0 & appriseDeptList.size() == quarterlySumScoreList.size()) {
					for (int j = 0; j < quarterlySumScoreList.size(); j++) {
						for (int k = 0; k < appriseDeptList.size(); k++) {
							//设置分数
							if (quarterlySumScoreList.get(j).getResponsibleUnitName().equals(appriseDeptList.get(k).getResponsibleUnitName())) {
								quarterlySumScoreList.get(j).setQuarterlySumScore(Double.valueOf(appriseDeptList.get(k).getScore()));
							}
						}
					}
				} else {
					throw new RuntimeException("评价得分纪录与指标得分纪录不一致！");
				}
				listArrayList.add(quarterlySumScoreList);
			}
		}
		//对【列表中查询的 QuarterlySumScore 数量】进行升序排列
		listArrayList.sort(new Comparator<List<QuarterlySumScore>>() {
			@Override
			public int compare(List<QuarterlySumScore> o1, List<QuarterlySumScore> o2) {
				Integer i1 = o1.size();
				Integer i2 = o2.size();
				return i1.compareTo(i2);
			}
		});

		/*//计算平均分
		for (int j = 0; j < responsibleUnitIds.length; j++) {
			double avgSumScore = 0.0;
			//遍历2各部门
			for (int i = 0; i < listArrayList.size(); i++) {
				double quarterlySumScore = 0.0;
				try {
					quarterlySumScore = listArrayList.get(i).get(j).getQuarterlySumScore();
					avgSumScore += quarterlySumScore;

					if (i == listArrayList.size()-1) {
						avgSumScore = Double.parseDouble(String.format("%.2f", avgSumScore / listArrayList.size()));
						listArrayList.get(listArrayList.size()-1).get(j).setAvgQuarterlySumScore(avgSumScore);
					}
				}catch (Exception exception){
					System.out.println("获取季度度总分时出错！可能是对象数量不匹配！"+exception);
				}
			}
		}*/


		Map<String,Map<String, Object>> mapMap = new HashMap<>();
		if (responsibleUnitNames.length > 0) {
			for (int i = 0; i < responsibleUnitNames.length; i++) {//考核单位
				Map<String, Object> map = new HashMap<>();
				for (int j = 0; j < listArrayList.size(); j++) {//评价单位
					List<QuarterlySumScore> quarterlySumScoreList = listArrayList.get(j);
					for (int k = 0; k < quarterlySumScoreList.size(); k++) {
						String appraiseDept = quarterlySumScoreList.get(k).getAppraiseDeptname();
						/*//评价单位数量
						double zhanbi = 0.0;
						if ("1".equals(quarterlyEvaluation1.getJdzbType())) {//党建工作 占30%
							zhanbi = 0.30;
						}else if ("2".equals(quarterlyEvaluation1.getJdzbType())) {//工作实绩 占60%
							zhanbi = 0.60;
						}else if ("3".equals(quarterlyEvaluation1.getJdzbType())) {//党风廉政 占10%
							zhanbi = 0.10;
						}
						//权重
						double weight = Double.parseDouble(quarterlyEvaluation1.getWeight()) / 100;
						weight = Double.parseDouble(String.format("%.2f", weight));
						//乘上权重占比后的得分
						double score = Double.parseDouble(String.format("%.2f", quarterlySumScoreList.get(k).getQuarterlySumScore()));
						//反向推出百分制得分，后面需求改了，先不计算党建工作和党风廉政的得分占比
						score = score / weight;// / zhanbi
						//只有工作实绩的要算得分占比
						if ("2".equals(quarterlyEvaluation1.getJdzbType())) {
							score = score / zhanbi;
						}
						double sumScore = Double.parseDouble(String.valueOf(String.format("%.2f", score)));*/
						double sumScore = quarterlySumScoreList.get(k).getQuarterlySumScore();
						if (responsibleUnitNames[i].equals(quarterlySumScoreList.get(k).getResponsibleUnitName())) {
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




}
