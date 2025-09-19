package org.springblade.integrated.platform.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.vo.LeaderAndMyFocusVO;
import com.vingsoft.vo.QuarterBaseInfoVO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Query;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import com.vingsoft.vo.AnnualBaseInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:评价基本信息控制层
 * @date 2022-04-18 10:07
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/apprise")
@Api(value = "评价基本信息", tags = "评价基本信息控制层代码")
public class AppriseBaseinfoController extends BladeController {
	//评分基本信息服务类
	private final IAppriseBaseinfoService appriseBaseinfoService;
	//季度评价服务类
	private final IQuarterlyEvaluationService iQuarterlyEvaluationService;
	//年度评价服务类
	private final IAnnualEvaluationService iAnnualEvaluationService;
	//季度评价得分服务类
	private final IQuarterlySumScoreService iQuarterlySumScoreService;
	//年度评价得分服务类
	private final IAnnualSumScoreService iAnnualSumScoreService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	private IUnifyMessageService unifyMessageService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private ISysClient sysClient;
	private final IAppriseDeptService iAppriseDeptService;
	@Resource
	private IUserSearchClient iUserSearchClient;
	private final IFollowInformationService followInformationService;
	private final IScoreAddService iScoreAddService;
	private final IScoreMinusService iScoreMinusService;
	private final ILeaderAppriseService iLeaderAppriseService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;



	/**
	 * 考核评价首页年度和季度分数发布接口
	 */
	@GetMapping("/fabu")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价首页年度和季度分数发布", notes = "传入year和quarter")
	public R fabu(@RequestParam("year") String year,@RequestParam("quarter") String quarter)
	{
		try {
			//改变季度得分是否发布状态
			boolean quarterRes = iQuarterlySumScoreService.update(
				Wrappers.<QuarterlySumScore>update().lambda()
					.set(QuarterlySumScore::getIsSend, "1")
					.eq(QuarterlySumScore::getStageYear, year)
					.eq(StrUtil.isNotBlank(quarter),QuarterlySumScore::getStage, quarter)
			);
			//改变年度得分是否发布状态
			boolean yearRes = iAnnualSumScoreService.update(
				Wrappers.<AnnualSumScore>update().lambda()
					.set(AnnualSumScore::getIsSend, "1")
					.eq(AnnualSumScore::getAnnualYear, year)
			);

			if ("第一季度".equals(quarter)) {//第一季度
				quarter = "1";
			}else if ("第二季度".equals(quarter)) {//第二季度
				quarter = "2";
			}else if ("第三季度".equals(quarter)) {//第三季度
				quarter = "3";
			}else if ("第四季度".equals(quarter)) {//第四季度
				quarter = "4";
			} else if ("上半年".equals(quarter)) {//上半年
				quarter = "5";
			}else if ("下半年".equals(quarter)) {//下半年
				quarter = "6";
			}
			//改变领导评价是否发布状态
			boolean leaderRes = iLeaderAppriseService.update(
				Wrappers.<LeaderApprise>update().lambda()
					.set(LeaderApprise::getIsSend, "1")
					.eq(LeaderApprise::getAppriseYear, year)
					.eq(LeaderApprise::getAppriseQuarter, quarter)
			);

			//改变加分项发布状态
			boolean scoreAddFlag = iScoreAddService.updateScoreAddIsSend(year);
//			//改变减分项发布状态
			int scoreMinusFlag = iScoreMinusService.updateScoreMinusIsSend(year);
			return R.success("发布成功！");
		} catch (Exception e) {
			return R.fail("绩效考核发布分数时出错！");
		}
	}

	/**
	 * 季度评价首页详细信息分页
	 */
	@GetMapping("/quarterlyList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核分析-首页季度评价详细信息列表", notes = "传入 appriseBaseInfoVO")
	public R<PageInfo> quarterlyList(QuarterBaseInfoVO quarterBaseInfoVO, Query query) {
		PageHelper.startPage(query.getCurrent(),query.getSize());

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(user.getRoleId()).toString();

		//appriseLeader
		boolean isok = true;//true 不是 false 是四大班子
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		String quarter = quarterBaseInfoVO.getStage();
		//1、工作实绩指标总分值（A1）=100 固定值
		Double A1 = Double.valueOf(100);
		if ("1".equals(quarter)) {//第一季度
			quarter = "第一季度";
		}else if ("2".equals(quarter)) {//第二季度
			quarter = "第二季度";
		}else if ("3".equals(quarter)) {//第三季度
			quarter = "第三季度";
		}else if ("4".equals(quarter)) {//第四季度
			quarter = "第四季度";
		} else if ("5".equals(quarter)) {// 上半年
			quarter = "上半年";
			A1 = Double.valueOf(65);
		} else if ("6".equals(quarter)) {//下半年
			quarter = "下半年";
			A1 = Double.valueOf(65);
		}
		quarterBaseInfoVO.setQuarter(quarter);
		List<QuarterBaseInfoVO> records = appriseBaseinfoService.QuarterBaseInfoList(quarterBaseInfoVO);

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
		// Double A1 = Double.valueOf(100);
		//2、扣除缺项后的指标分值(A2)
		String responsibleUnitId = quarterBaseInfoVO.getResponsibleUnitId();
		String stageYear = quarterBaseInfoVO.getStageYear();
		String stage = quarter;
		Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage, "2");
		Double A2 = Double.valueOf(map.get("totalWeight").toString());
		//3、承担工作的实际得分(A3)
		Double A3 = 0.0;

		if (records.size() == 0) {
			QueryWrapper<QuarterlyEvaluation> queryWrapper =new QueryWrapper<>();
			queryWrapper.select("jdzb_type,jdzb_name,first_target,two_target,major_target,scoring_rubric,id,check_classify,\n" +
				"               check_classify_name,weight,appraise_deptid,appraise_deptname,check_object_id,\n" +
				"               check_object,create_user,\n" +
				"               create_dept,create_time");
			queryWrapper.like(StringUtils.isNotBlank(quarterBaseInfoVO.getStageYear()),"create_time",quarterBaseInfoVO.getStageYear());
			queryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getJdzbType()),"jdzb_type",quarterBaseInfoVO.getJdzbType());
			queryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getMajorTarget()),"major_target",quarterBaseInfoVO.getMajorTarget());

			if(isok){//非四大班子的单位只查询本单位的数据
				queryWrapper.like("check_object_id",user.getDeptId());
			}else{
				queryWrapper.like(quarterBaseInfoVO.getResponsibleUnitId()!=null,"check_object_id",quarterBaseInfoVO.getResponsibleUnitId());
			}
			queryWrapper.like(quarterBaseInfoVO.getAppraiseDeptid()!=null,"appraise_deptid",quarterBaseInfoVO.getAppraiseDeptid());
			queryWrapper.eq("to_quarter",quarter);
			List<QuarterlyEvaluation> qList = iQuarterlyEvaluationService.list(queryWrapper);

			for (QuarterlyEvaluation qe : qList) {
				QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<>();
				queryWrapper1.select("quarterly_sum_score");
				queryWrapper1.eq("quarterly_evaluation_id",qe.getId().toString());
				queryWrapper1.eq("responsible_unit_id",quarterBaseInfoVO.getResponsibleUnitId());
				queryWrapper1.eq("stage",quarter);
				List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
				double sumScore = 0.0;
				double gzsjScore= 0.0;
				//是否发布
				String isSend = "0"; //默认未发布
				if (quarterlySumScoreList.size() > 0) {
					for (int i = 0; i < quarterlySumScoreList.size(); i++) {
						if(ObjectUtils.isNotEmpty(quarterlySumScoreList.get(i).getIsSend()) && quarterlySumScoreList.get(i).getIsSend().toString().equals("1")){
							isSend = "1";
						}
						QuarterlySumScore qss = quarterlySumScoreList.get(i);
						if(isok){//非四大班子
							if(quarterBaseInfoVO.getResponsibleUnitId().equals(user.getDeptId())){
								sumScore+=quarterlySumScoreList.get(i).getQuarterlySumScore();
								gzsjScore+=quarterlySumScoreList.get(i).getGzsjScore();
							}
						}else{
							sumScore+=quarterlySumScoreList.get(i).getQuarterlySumScore();
							gzsjScore+=quarterlySumScoreList.get(i).getGzsjScore();
						}
					}
				}
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));
				gzsjScore = Double.parseDouble(String.format("%.2f", gzsjScore));

				QuarterBaseInfoVO quarterBaseInfoVO1 =new QuarterBaseInfoVO();
				quarterBaseInfoVO1.setJdzbType(qe.getJdzbType());
				quarterBaseInfoVO1.setJdzbName(qe.getJdzbName());
				quarterBaseInfoVO1.setFirstTarget(qe.getFirstTarget());
				quarterBaseInfoVO1.setMajorTarget(qe.getMajorTarget());
				quarterBaseInfoVO1.setScoringRubric(qe.getScoringRubric());
				quarterBaseInfoVO1.setQuarterlyEvaluationId(qe.getId());
				quarterBaseInfoVO1.setCheckClassify(qe.getCheckClassify());
				quarterBaseInfoVO1.setCheckClassifyName(qe.getCheckClassifyName());
				quarterBaseInfoVO1.setAppraiseDeptid(qe.getAppraiseDeptid());
				quarterBaseInfoVO1.setAppraiseDeptname(qe.getAppraiseDeptname());
				quarterBaseInfoVO1.setResponsibleUnitId(qe.getCheckObjectId());
				quarterBaseInfoVO1.setResponsibleUnitName(qe.getCheckObject());
				quarterBaseInfoVO1.setCreateUser(qe.getCreateUser());
				quarterBaseInfoVO1.setCreateDept(qe.getCreateDept());
				quarterBaseInfoVO1.setCreateTime(qe.getCreateTime());
				quarterBaseInfoVO1.setTwoTarget(qe.getTwoTarget());
				quarterBaseInfoVO1.setWeight(qe.getWeight());

				quarterBaseInfoVO1.setIsSend(isSend);
				if(StringUtils.isNotBlank(qe.getJdzbType()) && qe.getJdzbType().equals("2")){
					A3 += gzsjScore;
					quarterBaseInfoVO1.setA3(A3);
					quarterBaseInfoVO1.setQuarterlySumScore(gzsjScore);
				}else{
					quarterBaseInfoVO1.setQuarterlySumScore(sumScore);
				}

				if (isok) {
					//如果当前登录账号作为有查看评分的权限，则可以查看评分
					if (roleNames.contains(isLookRole)) {
						quarterBaseInfoVO1.setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(qe.getJdzbType()) && qe.getJdzbType().equals("2")){
							quarterBaseInfoVO1.setQuarterlySumScore(gzsjScore);
						}else{
							quarterBaseInfoVO1.setQuarterlySumScore(sumScore);
						}

						/*if (!qe.getAppraiseDeptname().contains(deptName) & !qe.getCheckObject().contains(deptName)) {
							quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getResponsibleUnitId()),"responsible_unit_id",qe.getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getAppraiseDeptid()),"create_dept",qe.getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getId().toString()),"evaluation_id",qe.getId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							quarterBaseInfoVO1.setScoringDescription(appriseDepts.get(0).getScoringDescription());
							quarterBaseInfoVO1.setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							quarterBaseInfoVO1.setScoringDescription("-");
							quarterBaseInfoVO1.setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(qe.getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							quarterBaseInfoVO1.setScoringDescription("-");
							quarterBaseInfoVO1.setScore(0.0);
						}
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						/*if (!qe.getAppraiseDeptname().contains(deptName) || qe.getCheckObject().contains(deptName)) {
							quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
						quarterBaseInfoVO1.setIsSend("0"); //发布了才能看到考评总分
					}
				}else{
					//查评分说明 并赋值
					QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getResponsibleUnitId()),"responsible_unit_id",qe.getResponsibleUnitId());
					//通过对应的责任单位id查询
					appQueryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getResponsibleUnitId()),"responsible_unit_id", quarterBaseInfoVO.getResponsibleUnitId());
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getAppraiseDeptid()),"create_dept",qe.getAppraiseDeptid());
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getId().toString()),"evaluation_id",qe.getId());
					appQueryWrapper.eq("type","1");
					List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
					if(appriseDepts != null && appriseDepts.size() > 0){
						quarterBaseInfoVO1.setScoringDescription(appriseDepts.get(0).getScoringDescription());
						quarterBaseInfoVO1.setScore(Double.valueOf(appriseDepts.get(0).getScore()));
					}else{
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
					}
					//不是当前区划的账号，不能看得分
					if(!user.getDeptId().equals(qe.getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
					}
				}
				records.add(quarterBaseInfoVO1);
			}

		} else {
			for (int i = 0; i < records.size(); i++) {
				if (isok) { //非四大班子
					double sumScore1=0.0;
					double gzsjScore1=0.0;
					if(quarterBaseInfoVO.getResponsibleUnitId().equals(user.getDeptId())){
						sumScore1= records.get(i).getQuarterlySumScore();
						gzsjScore1=records.get(i).getGzsjScore();
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", gzsjScore1)));
						}else{
							records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", sumScore1)));
						}
					}
					if (roleNames.contains(isLookRole)) {
						records.get(i).setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(gzsjScore1);
						}else{
							records.get(i).setQuarterlySumScore(sumScore1);
						}
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) & !records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getResponsibleUnitId()),"responsible_unit_id",records.get(i).getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getAppraiseDeptid()),"create_dept",records.get(i).getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getQuarterlyEvaluationId().toString()),"evaluation_id",records.get(i).getQuarterlyEvaluationId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							records.get(i).setScoringDescription(appriseDepts.get(0).getScoringDescription());
							records.get(i).setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(records.get(i).getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}

					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						records.get(i).setQuarterlySumScore(0.0);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) || records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						records.get(i).setScoringDescription("-");
						records.get(i).setScore(0.0);
						records.get(i).setIsSend("0"); //发布了才能看到考评总分
					}
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")) {
						A3 += gzsjScore1;
						records.get(i).setA3(A3);
					}
				}else{ //四大班子
					double sumScore1 = records.get(i).getQuarterlySumScore();
					double gzsjScore1 = records.get(i).getGzsjScore();
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
						records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", gzsjScore1)));
					}else{
						records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", sumScore1)));
					}
					if (roleNames.contains(isLookRole)) {
						records.get(i).setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(gzsjScore1);
						}else{
							records.get(i).setQuarterlySumScore(sumScore1);
						}
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) & !records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getResponsibleUnitId()),"responsible_unit_id",records.get(i).getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getAppraiseDeptid()),"create_dept",records.get(i).getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getQuarterlyEvaluationId().toString()),"evaluation_id",records.get(i).getQuarterlyEvaluationId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							records.get(i).setScoringDescription(appriseDepts.get(0).getScoringDescription());
							records.get(i).setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(records.get(i).getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						records.get(i).setQuarterlySumScore(0.0);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) || records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						records.get(i).setScoringDescription("-");
						records.get(i).setScore(0.0);
						records.get(i).setIsSend("0"); //发布了才能看到考评总分
					}
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")) {
						A3 += gzsjScore1;
						records.get(i).setA3(A3);
					}
				}
			}
		}

		//统一处理考评总分
		for(int m = 0; m < records.size(); m++){
			if(StringUtils.isNotBlank(records.get(m).getJdzbType()) && records.get(m).getJdzbType().equals("2")) {
				Map<String,Object> scoreMap = new HashMap<>();
				scoreMap.put("A1",Double.parseDouble(String.format("%.2f", A1)));
				scoreMap.put("A2",Double.parseDouble(String.format("%.2f", A2)));
				scoreMap.put("A3",Double.parseDouble(String.format("%.2f", A3)));
				//效率系数(α) A3/A2
				Double α = A2 == 0 ? 0 : A3 / A2;
				scoreMap.put("alpha",Double.parseDouble(String.format("%.4f", α)));
				//考评总分=A3+α×（A1-A2)。
				Double totalScore = A3 + α * (A1 - A2);
				if(StringUtils.isNotBlank(records.get(m).getIsSend()) && records.get(m).getIsSend().equals("1")) {
					scoreMap.put("total", Double.parseDouble(String.format("%.2f", totalScore)));
				}
				records.get(m).setTotalScore(scoreMap);
			}
		}

		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}


	/**
	 * 季度评价首页详细信息分页-app
	 */
	@PostMapping("/quarterlyListApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核分析-首页季度评价详细信息列表-app", notes = "传入 appriseBaseInfoVO")
	public R quarterlyListApp(@RequestBody Map<String, Object> map2) {
		//参数解密
		String params = map2.get("params").toString();
		//1、日志记录
		bladeLogger.info("季度评价首页详细信息分页-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		String encryptSign;
		JSONObject jsonParams;
		if (dataMap.get("extra") != null) {
			encryptSign = dataMap.get("sign").toString();
			jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
		}else{
			return R.fail("加密解析错误");
		}

		QuarterBaseInfoVO quarterBaseInfoVO = objectMapper.convertValue(jsonParams, QuarterBaseInfoVO.class);
		Query query = new Query();
		query.setSize(jsonParams.getInteger("size"));
		query.setCurrent(jsonParams.getInteger("current"));

		PageHelper.startPage(query.getCurrent(),query.getSize());
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(user.getRoleId()).toString();

		//appriseLeader
		boolean isok = true;//true 不是 false 是四大班子
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		String quarter = quarterBaseInfoVO.getStage();
		//1、工作实绩指标总分值（A1）=100 固定值
		Double A1 = Double.valueOf(100);
		if ("1".equals(quarter)) {//第一季度
			quarter = "第一季度";
		}else if ("2".equals(quarter)) {//第二季度
			quarter = "第二季度";
		}else if ("3".equals(quarter)) {//第三季度
			quarter = "第三季度";
		}else if ("4".equals(quarter)) {//第四季度
			quarter = "第四季度";
		} else if ("5".equals(quarter)) {// 上半年
			quarter = "上半年";
			A1 = Double.valueOf(65);
		} else if ("6".equals(quarter)) {//下半年
			quarter = "下半年";
			A1 = Double.valueOf(65);
		}
		quarterBaseInfoVO.setQuarter(quarter);
		List<QuarterBaseInfoVO> records = appriseBaseinfoService.QuarterBaseInfoList(quarterBaseInfoVO);

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
		// Double A1 = Double.valueOf(100);
		//2、扣除缺项后的指标分值(A2)
		String responsibleUnitId = quarterBaseInfoVO.getResponsibleUnitId();
		String stageYear = quarterBaseInfoVO.getStageYear();
		String stage = quarter;
		Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage, "2");
		Double A2 = Double.valueOf(map.get("totalWeight").toString());
		//3、承担工作的实际得分(A3)
		Double A3 = 0.0;

		if (records.size() == 0) {
			QueryWrapper<QuarterlyEvaluation> queryWrapper =new QueryWrapper<>();
			queryWrapper.select("jdzb_type,jdzb_name,first_target,two_target,major_target,scoring_rubric,id,check_classify,\n" +
				"               check_classify_name,weight,appraise_deptid,appraise_deptname,check_object_id,\n" +
				"               check_object,create_user,\n" +
				"               create_dept,create_time");
			queryWrapper.like(StringUtils.isNotBlank(quarterBaseInfoVO.getStageYear()),"create_time",quarterBaseInfoVO.getStageYear());
			queryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getJdzbType()),"jdzb_type",quarterBaseInfoVO.getJdzbType());
			queryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getMajorTarget()),"major_target",quarterBaseInfoVO.getMajorTarget());

			if(isok){//非四大班子的单位只查询本单位的数据
				queryWrapper.like("check_object_id",user.getDeptId());
			}else{
				queryWrapper.like(quarterBaseInfoVO.getResponsibleUnitId()!=null,"check_object_id",quarterBaseInfoVO.getResponsibleUnitId());
			}
			queryWrapper.like(quarterBaseInfoVO.getAppraiseDeptid()!=null,"appraise_deptid",quarterBaseInfoVO.getAppraiseDeptid());
			queryWrapper.eq("to_quarter",quarter);
			List<QuarterlyEvaluation> qList = iQuarterlyEvaluationService.list(queryWrapper);

			for (QuarterlyEvaluation qe : qList) {
				QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<>();
				queryWrapper1.select("quarterly_sum_score");
				queryWrapper1.eq("quarterly_evaluation_id",qe.getId().toString());
				queryWrapper1.eq("responsible_unit_id",quarterBaseInfoVO.getResponsibleUnitId());
				queryWrapper1.eq("stage",quarter);
				List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(queryWrapper1);
				double sumScore = 0.0;
				double gzsjScore= 0.0;
				//是否发布
				String isSend = "0"; //默认未发布
				if (quarterlySumScoreList.size() > 0) {
					for (int i = 0; i < quarterlySumScoreList.size(); i++) {
						if(ObjectUtils.isNotEmpty(quarterlySumScoreList.get(i).getIsSend()) && quarterlySumScoreList.get(i).getIsSend().toString().equals("1")){
							isSend = "1";
						}
						QuarterlySumScore qss = quarterlySumScoreList.get(i);
						if(isok){//非四大班子
							if(quarterBaseInfoVO.getResponsibleUnitId().equals(user.getDeptId())){
								sumScore+=quarterlySumScoreList.get(i).getQuarterlySumScore();
								gzsjScore+=quarterlySumScoreList.get(i).getGzsjScore();
							}
						}else{
							sumScore+=quarterlySumScoreList.get(i).getQuarterlySumScore();
							gzsjScore+=quarterlySumScoreList.get(i).getGzsjScore();
						}
					}
				}
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));
				gzsjScore = Double.parseDouble(String.format("%.2f", gzsjScore));

				QuarterBaseInfoVO quarterBaseInfoVO1 =new QuarterBaseInfoVO();
				quarterBaseInfoVO1.setJdzbType(qe.getJdzbType());
				quarterBaseInfoVO1.setJdzbName(qe.getJdzbName());
				quarterBaseInfoVO1.setFirstTarget(qe.getFirstTarget());
				quarterBaseInfoVO1.setMajorTarget(qe.getMajorTarget());
				quarterBaseInfoVO1.setScoringRubric(qe.getScoringRubric());
				quarterBaseInfoVO1.setQuarterlyEvaluationId(qe.getId());
				quarterBaseInfoVO1.setCheckClassify(qe.getCheckClassify());
				quarterBaseInfoVO1.setCheckClassifyName(qe.getCheckClassifyName());
				quarterBaseInfoVO1.setAppraiseDeptid(qe.getAppraiseDeptid());
				quarterBaseInfoVO1.setAppraiseDeptname(qe.getAppraiseDeptname());
				quarterBaseInfoVO1.setResponsibleUnitId(qe.getCheckObjectId());
				quarterBaseInfoVO1.setResponsibleUnitName(qe.getCheckObject());
				quarterBaseInfoVO1.setCreateUser(qe.getCreateUser());
				quarterBaseInfoVO1.setCreateDept(qe.getCreateDept());
				quarterBaseInfoVO1.setCreateTime(qe.getCreateTime());
				quarterBaseInfoVO1.setTwoTarget(qe.getTwoTarget());
				quarterBaseInfoVO1.setWeight(qe.getWeight());

				quarterBaseInfoVO1.setIsSend(isSend);
				if(StringUtils.isNotBlank(qe.getJdzbType()) && qe.getJdzbType().equals("2")){
					A3 += gzsjScore;
					quarterBaseInfoVO1.setA3(A3);
					quarterBaseInfoVO1.setQuarterlySumScore(gzsjScore);
				}else{
					quarterBaseInfoVO1.setQuarterlySumScore(sumScore);
				}

				if (isok) {
					//如果当前登录账号作为有查看评分的权限，则可以查看评分
					if (roleNames.contains(isLookRole)) {
						quarterBaseInfoVO1.setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(qe.getJdzbType()) && qe.getJdzbType().equals("2")){
							quarterBaseInfoVO1.setQuarterlySumScore(gzsjScore);
						}else{
							quarterBaseInfoVO1.setQuarterlySumScore(sumScore);
						}

						/*if (!qe.getAppraiseDeptname().contains(deptName) & !qe.getCheckObject().contains(deptName)) {
							quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getResponsibleUnitId()),"responsible_unit_id",qe.getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getAppraiseDeptid()),"create_dept",qe.getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(qe.getId().toString()),"evaluation_id",qe.getId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							quarterBaseInfoVO1.setScoringDescription(appriseDepts.get(0).getScoringDescription());
							quarterBaseInfoVO1.setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							quarterBaseInfoVO1.setScoringDescription("-");
							quarterBaseInfoVO1.setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(qe.getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							quarterBaseInfoVO1.setScoringDescription("-");
							quarterBaseInfoVO1.setScore(0.0);
						}
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						/*if (!qe.getAppraiseDeptname().contains(deptName) || qe.getCheckObject().contains(deptName)) {
							quarterBaseInfoVO1.setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
						quarterBaseInfoVO1.setIsSend("0"); //发布了才能看到考评总分
					}
				}else{
					//查评分说明 并赋值
					QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getResponsibleUnitId()),"responsible_unit_id",qe.getResponsibleUnitId());
					//通过对应的责任单位id查询
					appQueryWrapper.eq(StringUtils.isNotBlank(quarterBaseInfoVO.getResponsibleUnitId()),"responsible_unit_id", quarterBaseInfoVO.getResponsibleUnitId());
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getAppraiseDeptid()),"create_dept",qe.getAppraiseDeptid());
					appQueryWrapper.eq(StringUtils.isNotBlank(qe.getId().toString()),"evaluation_id",qe.getId());
					appQueryWrapper.eq("type","1");
					List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
					if(appriseDepts != null && appriseDepts.size() > 0){
						quarterBaseInfoVO1.setScoringDescription(appriseDepts.get(0).getScoringDescription());
						quarterBaseInfoVO1.setScore(Double.valueOf(appriseDepts.get(0).getScore()));
					}else{
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
					}
					//不是当前区划的账号，不能看得分
					if(!user.getDeptId().equals(qe.getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
						quarterBaseInfoVO1.setScoringDescription("-");
						quarterBaseInfoVO1.setScore(0.0);
					}
				}
				records.add(quarterBaseInfoVO1);
			}

		} else {
			for (int i = 0; i < records.size(); i++) {
				if (isok) { //非四大班子
					double sumScore1=0.0;
					double gzsjScore1=0.0;
					if(quarterBaseInfoVO.getResponsibleUnitId().equals(user.getDeptId())){
						sumScore1= records.get(i).getQuarterlySumScore();
						gzsjScore1=records.get(i).getGzsjScore();
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", gzsjScore1)));
						}else{
							records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", sumScore1)));
						}
					}
					if (roleNames.contains(isLookRole)) {
						records.get(i).setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(gzsjScore1);
						}else{
							records.get(i).setQuarterlySumScore(sumScore1);
						}
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) & !records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getResponsibleUnitId()),"responsible_unit_id",records.get(i).getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getAppraiseDeptid()),"create_dept",records.get(i).getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getQuarterlyEvaluationId().toString()),"evaluation_id",records.get(i).getQuarterlyEvaluationId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							records.get(i).setScoringDescription(appriseDepts.get(0).getScoringDescription());
							records.get(i).setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(records.get(i).getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}

					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						records.get(i).setQuarterlySumScore(0.0);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) || records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						records.get(i).setScoringDescription("-");
						records.get(i).setScore(0.0);
						records.get(i).setIsSend("0"); //发布了才能看到考评总分
					}
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")) {
						A3 += gzsjScore1;
						records.get(i).setA3(A3);
					}
				}else{ //四大班子
					double sumScore1 = records.get(i).getQuarterlySumScore();
					double gzsjScore1 = records.get(i).getGzsjScore();
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
						records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", gzsjScore1)));
					}else{
						records.get(i).setQuarterlySumScore(Double.parseDouble(String.format("%.2f", sumScore1)));
					}
					if (roleNames.contains(isLookRole)) {
						records.get(i).setIsSend("1"); //发布了才能看到考评总分
						if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")){
							records.get(i).setQuarterlySumScore(gzsjScore1);
						}else{
							records.get(i).setQuarterlySumScore(sumScore1);
						}
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) & !records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//查评分说明 并赋值
						QueryWrapper<AppriseDept> appQueryWrapper =new QueryWrapper<>();
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getResponsibleUnitId()),"responsible_unit_id",records.get(i).getResponsibleUnitId());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getAppraiseDeptid()),"create_dept",records.get(i).getAppraiseDeptid());
						appQueryWrapper.eq(StringUtils.isNotBlank(records.get(i).getQuarterlyEvaluationId().toString()),"evaluation_id",records.get(i).getQuarterlyEvaluationId());
						appQueryWrapper.eq("type","1");
						List<AppriseDept> appriseDepts = iAppriseDeptService.list(appQueryWrapper);
						if(appriseDepts != null && appriseDepts.size() > 0){
							records.get(i).setScoringDescription(appriseDepts.get(0).getScoringDescription());
							records.get(i).setScore(Double.valueOf(appriseDepts.get(0).getScore()));
						}else{
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
						//不是当前区划的账号，不能看得分
						if(!user.getDeptId().equals(records.get(i).getResponsibleUnitId()) && !user.getAccount().equals("swbgs")){
							records.get(i).setScoringDescription("-");
							records.get(i).setScore(0.0);
						}
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						records.get(i).setQuarterlySumScore(0.0);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) || records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setQuarterlySumScore(0.0);
						}*/
						//如果当前登陆账号不是评价部门，评分说明不显示
						records.get(i).setScoringDescription("-");
						records.get(i).setScore(0.0);
						records.get(i).setIsSend("0"); //发布了才能看到考评总分
					}
					if(StringUtils.isNotBlank(records.get(i).getJdzbType()) && records.get(i).getJdzbType().equals("2")) {
						A3 += gzsjScore1;
						records.get(i).setA3(A3);
					}
				}
			}
		}

		//统一处理考评总分
		for(int m = 0; m < records.size(); m++){
			if(StringUtils.isNotBlank(records.get(m).getJdzbType()) && records.get(m).getJdzbType().equals("2")) {
				Map<String,Object> scoreMap = new HashMap<>();
				scoreMap.put("A1",Double.parseDouble(String.format("%.2f", A1)));
				scoreMap.put("A2",Double.parseDouble(String.format("%.2f", A2)));
				scoreMap.put("A3",Double.parseDouble(String.format("%.2f", A3)));
				//效率系数(α) A3/A2
				Double α = A2 == 0 ? 0 : A3 / A2;
				scoreMap.put("alpha",Double.parseDouble(String.format("%.4f", α)));
				//考评总分=A3+α×（A1-A2)。
				Double totalScore = A3 + α * (A1 - A2);
				if(StringUtils.isNotBlank(records.get(m).getIsSend()) && records.get(m).getIsSend().equals("1")) {
					scoreMap.put("total", Double.parseDouble(String.format("%.2f", totalScore)));
				}
				records.get(m).setTotalScore(scoreMap);
			}
		}

		PageInfo pageInfo = new PageInfo(records);
		JSONObject userJson = objectMapper.convertValue(pageInfo, JSONObject.class);
		return R.data(VSTool.encrypt(encryptSign, userJson.toJSONString(), VSTool.CHN));
	}


	/**
	 * 年度评价首页详细信息分页
	 */
	@GetMapping("/annualList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核分析-首页年度评价详细信息列表", notes = "传入 appriseBaseInfoVO")
	public R<PageInfo> annualList(AnnualBaseInfoVO annualBaseInfoVO, Query query) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<AnnualBaseInfoVO> records = appriseBaseinfoService.AnnualBaseInfoList(annualBaseInfoVO);
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String roleNames = SysCache.getRoleNames(user.getRoleId()).toString();
		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",", "");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));

		if (records.size() == 0) {
			QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<>();
			queryWrapper.select("type,weight,ganzhouqu,linzexian,gaotaixian,shandanxian,minlexian,sunanxian," +
				"project_id, project_name, major_target,id,appraise_classify,\n" +
				"         appraise_classify_name, appraise_deptid, appraise_deptname,appraise_object_id,\n" +
				"         appraise_object, create_user,\n" +
				"         create_dept, create_time");
			queryWrapper.like(StringUtils.isNotBlank(annualBaseInfoVO.getAnnualYear()), "create_time", annualBaseInfoVO.getAnnualYear());
			queryWrapper.eq(StringUtils.isNotBlank(annualBaseInfoVO.getProjectId()), "project_id", annualBaseInfoVO.getProjectId());
			queryWrapper.eq(StringUtils.isNotBlank(annualBaseInfoVO.getType()), "type", annualBaseInfoVO.getType());
			queryWrapper.like(StringUtils.isNotBlank(annualBaseInfoVO.getMajorTarget()), "major_target", annualBaseInfoVO.getMajorTarget());
			queryWrapper.like(StringUtils.isNotBlank(annualBaseInfoVO.getAppraiseDeptid()), "appraise_deptid", annualBaseInfoVO.getAppraiseDeptid());
			queryWrapper.like(StringUtils.isNotBlank(annualBaseInfoVO.getResponsibleUnitId()), "appraise_object_id", annualBaseInfoVO.getResponsibleUnitId());
			List<AnnualEvaluation> aList = iAnnualEvaluationService.list(queryWrapper);

			for (AnnualEvaluation ae : aList) {
				QueryWrapper<AnnualSumScore> queryWrapper1 = new QueryWrapper<>();
				queryWrapper1.select("annual_sum_score");
				queryWrapper1.eq("annual_evaluation_id",ae.getId().toString());
				queryWrapper1.eq("responsible_unit_id",annualBaseInfoVO.getResponsibleUnitId());
				queryWrapper1.like(StringUtils.isNotBlank(annualBaseInfoVO.getAnnualYear()), "create_time", annualBaseInfoVO.getAnnualYear());

				List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService.list(queryWrapper1);
				double sumScore = 0.0;
				if (annualSumScoreList.size() > 0) {
					for (int i = 0; i < annualSumScoreList.size(); i++) {
						sumScore+=annualSumScoreList.get(i).getAnnualSumScore();
					}
				}
				sumScore = Double.parseDouble(String.format("%.2f", sumScore));
				AnnualBaseInfoVO annualBaseInfoVO1 = new AnnualBaseInfoVO();
				annualBaseInfoVO1.setProjectId(ae.getProjectId());
				annualBaseInfoVO1.setType(ae.getType());
				annualBaseInfoVO1.setProjectName(ae.getProjectName());
				annualBaseInfoVO1.setMajorTarget(ae.getMajorTarget());
				annualBaseInfoVO1.setAnnualEvaluationId(ae.getId());
				annualBaseInfoVO1.setCheckClassify(ae.getAppraiseClassify());
				annualBaseInfoVO1.setCheckClassifyName(ae.getAppraiseClassifyName());
				annualBaseInfoVO1.setAppraiseDeptid(ae.getAppraiseDeptid());
				annualBaseInfoVO1.setAppraiseDeptname(ae.getAppraiseDeptname());
				annualBaseInfoVO1.setResponsibleUnitId(ae.getAppraiseObjectId());
				annualBaseInfoVO1.setResponsibleUnitName(ae.getAppraiseObject());
				//获取部门名称
				String dName = SysCache.getDeptName(Long.valueOf(annualBaseInfoVO.getResponsibleUnitId()));

				if ("甘州区".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getGanzhouqu());
				}else if ("临泽县".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getLinzexian());
				}else if ("高台县".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getGaotaixian());
				}else if ("山丹县".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getShandanxian());
				}else if ("民乐县".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getMinlexian());
				} else if ("肃南县".equals(dName)) {
					annualBaseInfoVO1.setWeight(ae.getSunanxian());
				}
				if (!"5".equals(ae.getType())) {
					annualBaseInfoVO1.setWeight(ae.getWeight());
				}
				annualBaseInfoVO1.setCreateUser(ae.getCreateUser());
				annualBaseInfoVO1.setCreateDept(ae.getCreateDept());
				annualBaseInfoVO1.setCreateTime(ae.getCreateTime());
				annualBaseInfoVO1.setAnnualSumScore(sumScore);

				if (isok) {
					//如果当前登录账号作为有查看评分的权限，则可以查看评分
					if (roleNames.contains(isLookRole)) {
						annualBaseInfoVO1.setAnnualSumScore(sumScore);
						/*if (!ae.getAppraiseDeptname().contains(deptName) & !ae.getAppraiseObject().contains(deptName)) {
							annualBaseInfoVO1.setAnnualSumScore(0.0);
						}*/
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						annualBaseInfoVO1.setAnnualSumScore(0.0);
						/*if (!ae.getAppraiseDeptname().contains(deptName) || ae.getAppraiseObject().contains(deptName)) {
							annualBaseInfoVO1.setAnnualSumScore(0.0);
						}*/
					}
				}
				records.add(annualBaseInfoVO1);
			}
		} else {
			for (int i = 0; i < records.size(); i++) {
				records.get(i).setAnnualSumScore(Double.parseDouble(String.format("%.2f", records.get(i).getAnnualSumScore())));
				double sumScore1 = records.get(i).getAnnualSumScore();
				if (isok) {
					//如果当前登录账号作为有查看评分的权限，则可以查看评分
					if (roleNames.contains(isLookRole)) {
						records.get(i).setAnnualSumScore(sumScore1);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) & !records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setAnnualSumScore(0.0);
						}*/
					} else {
						//如果当前登陆账号不是评价部门，则该条数据的评价分数为0
						records.get(i).setAnnualSumScore(0.0);
						/*if (!records.get(i).getAppraiseDeptname().contains(deptName) || records.get(i).getResponsibleUnitName().contains(deptName)) {
							records.get(i).setAnnualSumScore(0.0);
						}*/

					}
				}
			}
		}
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 考核评价详细信息项目成员接口
	 */
	@GetMapping("/deptUserInfo")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价详细信息项目成员", notes = "传入 appriseBaseInfoVO")
	public R deptUserInfo(@RequestParam("appriseId") String appriseId,@RequestParam("appraiseObjectId") String appraiseObjectId,@RequestParam("createDept") String createDept) {
		//评价单位和评价对象ids
		String appraiseObjectIds = appriseId + "," + appraiseObjectId;
		if (!appraiseObjectIds.contains(createDept)) {
			appraiseObjectIds = appraiseObjectIds + "," + createDept;
		}
		R<String> rgly = sysClient.getPostIdsByFuzzy("000000", "管理员");//获取管理员岗位id
		String glyId = rgly.getData();
		List<User> userList = new ArrayList<>();
		if (StringUtils.isNotBlank(appraiseObjectIds)) {
			String[] ids = appraiseObjectIds.split(",");
			for (int i = 0; i < ids.length; i++) {
				R<List<User>> ruser = iUserSearchClient.listByPostAndDept(glyId, ids[i]);//获取单位下面所有管理员用户
				if (ruser.getData().size() > 0) {
					for (int j = 0; j < ruser.getData().size(); j++) {
						userList.add(ruser.getData().get(j));
					}
				}
			}
		}
		//去重
		List<User> list1 = new ArrayList<User>();
		for (User v : userList) {
			if (!list1.contains(v)) {
				list1.add(v);
			}
		}

		return R.data(list1);
	}


	/**
	 * 评价新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "保存", notes = "vo")
	@Transactional(rollbackFor = Exception.class)
	public R pjSave(@Valid @RequestBody AppriseBaseinfo ab) throws ParseException {
		//保存评价基本信息表
		appriseBaseinfoService.save(ab);

		Long createuser = ab.getCreateUser();
		User user = UserCache.getUser(createuser);
		Long createDept = ab.getCreateDept();//评价部门id
		long appriseBaseinfoId = ab.getEvaluationId();

		//获取部门名称
		String deptName = SysCache.getDeptName(createDept);
		ab.setDeptName(deptName);
		if (Objects.equals(ab.getEvaluationType(), "2")) {//如果是【季度评价】
			Date shijian = iQuarterlyEvaluationService.getById(appriseBaseinfoId).getCreateTime();
			String year= new SimpleDateFormat("yyyy").format(shijian);
			/*
			* 季度评价按【党建工作】占30%、【工作实绩】占90%、【廉政建设】占10%、【三抓三促】占10%的比例进行计算；
			* 每季度评价结果按10%计入年度考评结果。（这里应该指总分）
			* */
			QuarterlyEvaluation quarterlyEvaluation = iQuarterlyEvaluationService.getById(ab.getEvaluationId());
			//获取当前评价部门的个数，用于计算各部门的平均分
			String[] avgStr = quarterlyEvaluation.getAppraiseDeptid().split(",");
			int avgCount = avgStr.length;

			//把当前部门从未评价部门中排除
			String[] notAppriseUserStr = null;
			if (StringUtils.isNotBlank(quarterlyEvaluation.getNotAppriseUser())) {
				notAppriseUserStr = quarterlyEvaluation.getNotAppriseUser().split(",");
			} else{
				notAppriseUserStr = quarterlyEvaluation.getAppraiseDeptname().split(",");
			}
			List<String> stringList = new ArrayList<>();
			for (int i = 0; i < notAppriseUserStr.length; i++) {
				if (!notAppriseUserStr[i].contains(ab.getDeptName())) {
					stringList.add(notAppriseUserStr[i]);
				}
			}
			String[] notAppriseUserStr1 = new String[stringList.size()];
			for (int i = 0; i < stringList.size(); i++) {
				notAppriseUserStr1[i] = stringList.get(i);
			}
			if (notAppriseUserStr1.length >= 0) {
				quarterlyEvaluation.setNotAppriseUser(Arrays.toString(notAppriseUserStr1).replace("[", "").replace("]", ""));
			}

			List<AppriseDept> appriseDeptList1 = ab.getAppriseDeptList();
			List<AppriseDept> appriseDeptList = new ArrayList<>();
			//获取原有的考核对象集合
			List<AppriseDept> appriseDepts = iAppriseDeptService.list(Wrappers.<AppriseDept>query().lambda()
					.eq(AppriseDept::getIsDeleted, 0)
					.eq(AppriseDept::getEvaluationId, ab.getEvaluationId())
					.eq(AppriseDept::getCreateUserName, user.getName())
				.eq(AppriseDept::getType, "1")
			);
			//对比传参进来的的考核对象集合
			if (appriseDepts.size() != ab.getAppriseDeptList().size()) {
				//使用流的方式将删除的AppriseDept找出来
				List<AppriseDept> removeAppriseDepts = appriseDepts.stream().filter(appriseDept -> !ab.getAppriseDeptList().stream().map(a ->
						a.getResponsibleUnitName()).collect(Collectors.joining()).contains(appriseDept.getResponsibleUnitName()))
					.collect(Collectors.toList());
				if (removeAppriseDepts != null && removeAppriseDepts.size() > 0) {
					Set<Long> ids = new HashSet<>();
					Set<String> deptIds = new HashSet<>();
					//将删除的部门从考核对象中删除
					String[] checkObjectIds = quarterlyEvaluation.getCheckObjectId().split(",");
					String[] objects = quarterlyEvaluation.getCheckObject().split(",");
					List<String> checkObjectIdList = new ArrayList<>(checkObjectIds.length);
					List<String> objectList = new ArrayList<>(objects.length);
					Collections.addAll(checkObjectIdList, checkObjectIds);
					Collections.addAll(objectList, objects);
					removeAppriseDepts.forEach(a -> {
						ids.add(a.getId());
						deptIds.add(a.getResponsibleUnitId().toString());
						//将考核对象的id和名称删除
						checkObjectIdList.remove(a.getResponsibleUnitId().toString());
						objectList.remove(a.getResponsibleUnitName());
					});
					if (checkObjectIdList.size() > 0) {
						String checkObjectIdStr = Joiner.on(",").join(checkObjectIdList);
						String objectStr = Joiner.on(",").join(objectList);
						quarterlyEvaluation.setCheckObjectId(checkObjectIdStr);
						quarterlyEvaluation.setCheckObject(objectStr);
					}else {
						quarterlyEvaluation.setCheckObjectId(null);
						quarterlyEvaluation.setCheckObject(null);
					}
					//将百分制 打分标记删除
					iAppriseDeptService.update(Wrappers.<AppriseDept>update().lambda().set(AppriseDept::getIsDeleted, 1)
						.eq(AppriseDept::getIsDeleted, 0).in(AppriseDept::getId, ids));
					//将对应的打分表中的打分数据标记为已删除
					iQuarterlySumScoreService.update(Wrappers.<QuarterlySumScore>update().lambda()
						.set(QuarterlySumScore::getIsDeleted, 1)
						.eq(QuarterlySumScore::getIsDeleted, 0)
						.eq(QuarterlySumScore::getStage, ab.getStage())
						.eq(QuarterlySumScore::getStageYear, year)
						.in(QuarterlySumScore::getResponsibleUnitId, deptIds)
						.eq(QuarterlySumScore::getServName, ab.getServName())
					);
				}
			}
			if (appriseDeptList1.size() > 0) {
				for (AppriseDept appriseDept : appriseDeptList1) {
					if (!StringUtils.isBlank(appriseDept.getScore())) {
						appriseDeptList.add(appriseDept);
					}
				}
			}
			for (int i = 0; i < appriseDeptList.size(); i++) {
				QuarterlySumScore quarterlySumScore = new QuarterlySumScore();
				//获取【考核分类的id】
				quarterlySumScore.setCheckClassify(quarterlyEvaluation.getCheckClassify());
				//获取【考核分类的名称】
				quarterlySumScore.setCheckClassifyName(quarterlyEvaluation.getCheckClassifyName());
				//获取【评价部门id】
				quarterlySumScore.setResponsibleUnitId(String.valueOf(appriseDeptList.get(i).getResponsibleUnitId()));
				//获取【评价部门名称】
				quarterlySumScore.setResponsibleUnitName(appriseDeptList.get(i).getResponsibleUnitName());
				//获取【当前事项名称】
				quarterlySumScore.setServName(ab.getServName());
				//获取【当前阶段】
				quarterlySumScore.setStage(ab.getStage());
				//获取【阶段id】
				quarterlySumScore.setStageId(ab.getStageId());
				//获取【年份】
				quarterlySumScore.setStageYear(year);
				//获取【季度指标表id】
				quarterlySumScore.setQuarterlyEvaluationId(ab.getEvaluationId());
				//获取【季考核部门id】
				quarterlySumScore.setAppraiseDeptid(String.valueOf(ab.getCreateDept()));
				//获取【考核部门名称】
				quarterlySumScore.setAppraiseDeptname(ab.getDeptName());

				QueryWrapper<QuarterlySumScore> quarterlySumScoreQueryWrapper = new QueryWrapper<>();
				quarterlySumScoreQueryWrapper.eq("check_classify",quarterlySumScore.getCheckClassify());
				quarterlySumScoreQueryWrapper.eq("check_classify_name",quarterlySumScore.getCheckClassifyName());
				quarterlySumScoreQueryWrapper.eq("responsible_unit_id",quarterlySumScore.getResponsibleUnitId());
				quarterlySumScoreQueryWrapper.eq("stage_id",quarterlySumScore.getStageId());
				quarterlySumScoreQueryWrapper.eq("serv_name",quarterlySumScore.getServName());
				quarterlySumScoreQueryWrapper.eq("stage_year",quarterlySumScore.getStageYear());
				quarterlySumScoreQueryWrapper.eq("appraise_deptid",quarterlySumScore.getAppraiseDeptid());
				//查询数据库中是否有这条数据，有就修改，没有就保存
				List<QuarterlySumScore> annualSumScoreList = iQuarterlySumScoreService.list(quarterlySumScoreQueryWrapper);

				//根据当前季度指标类型分别保存数据
				if (quarterlyEvaluation.getJdzbName().contains("党建工作")) {
					/*
					 *  计算分数（百分制得分 × 权重）
					 * （一）党建工作考评办法。对照县区与市直部门单位党建工作的具体要求，
					 * 	   分类进行季度考评，设置相应党建工作季度考评重点指标，安排相关评分单位出台考评细则，
					 *      按照百分比进行量化考评打分，得分以分配权重进行汇总，最终按30%计入班子季度考评最终得分。
					 *     （评价完了之后各项指标得分加起来×30%）
					 * */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					if (StringUtils.isEmpty(ab.getWeight())) {
						ab.setWeight("0.0");
					}
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,季度评价按【党建工作】占 30%，后面又修改了需求，先把0.3注释掉
					double getScore = (weight * score);// * 0.3
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setDjgzScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					getScore = Double.parseDouble(String.format("%.3f", getScore));
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("党建工作得分 × 权重 × 党建工作得分占比："+ score +"×"+ weight + "×" + 0.3 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					appriseDept.setCreateUserName(userNameDecrypt);
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				} else if (quarterlyEvaluation.getJdzbName().contains("工作实绩")) {
					/*
					*	第一个指标打80分，权重15%，第二个指标打60分，权重10%
					*	这两个指标的综合得分就是（（80×15%）+（60×10%）） / 2
					*  （二）工作实绩考评办法。采用线性加权法合成综合评价指数，
					*  即：将单项评价指标的百分制得分乘以相应权重，然后加总，
					*  得出各县区和市直部门单位每季度工作实绩评价的综合指数。（求平均数）
					*  计算公式为：D=（ΣWi*Di）/i
					*  其中Dｉ为第ｉ个指标的得分，Wｉ为第ｉ个指标的权数。
					*/
					//获取当前指标权重，权重为百分比，所以这里要除以100
					if (StringUtils.isEmpty(ab.getWeight())) {
						ab.setWeight("0.0");
					}
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【工作实绩】占 90%
					double getScore = (weight * score);
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setGzsjScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("工作实绩得分 × 权重 × 工作实绩得分占比："+ score +"×"+ weight + "×" + 0.9 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					appriseDept.setCreateUserName(userNameDecrypt);
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				} else if (quarterlyEvaluation.getJdzbName().contains("党风廉政")) {
					/*
					*（三）廉政建设考评办法。主要围绕《党风廉政建设责任制考核县区（市直部门单位）重点任务评价表》中
					* “责任落实、监督情况、惩治腐败、作风建设、制度落实、问题整改”6项重点任务，以及细化的评分标准，
					* 进行量化打分，计算得出各县区和市直部门单位每季度党风廉政建设考评得分。（直接对得分进行计算）
					* */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					if (StringUtils.isEmpty(ab.getWeight())) {
						ab.setWeight("0.0");
					}
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【廉政建设】占 10%，后面又修改了需求，先把 0.1注释掉
					double getScore = (weight * score); //* 0.1
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setDflzScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("党风廉政得分 × 权重 × 党风廉政得分占比："+ score +"×"+ weight + "×" + 0.1 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					appriseDept.setCreateUserName(userNameDecrypt);
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				}else if (quarterlyEvaluation.getJdzbName().contains("三抓三促")) {
					/*
					 * 三抓三促 完全参照党风廉政建设
					 *（三）廉政建设考评办法。主要围绕《党风廉政建设责任制考核县区（市直部门单位）重点任务评价表》中
					 * “责任落实、监督情况、惩治腐败、作风建设、制度落实、问题整改”6项重点任务，以及细化的评分标准，
					 * 进行量化打分，计算得出各县区和市直部门单位每季度党风廉政建设考评得分。（直接对得分进行计算）
					 * */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					if (StringUtils.isEmpty(ab.getWeight())) {
						ab.setWeight("0.0");
					}
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【三抓三促】占 10%，后面又修改了需求，先把 0.1注释掉
					double getScore = (weight * score); //* 0.1
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setS3z3cScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("三抓三促得分 × 权重 × 三抓三促得分占比："+ score +"×"+ weight + "×" + 0.1 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					appriseDept.setCreateUserName(userNameDecrypt);
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				}


				//TODO 打完分之后计算总分（党建工作 + 工作实绩 + 党风廉政 = 总分）
				//+ 加分项 - 减分项 + 领导评价
				//党建工作
				double djgzSocre = quarterlySumScore.getDjgzScore() == null ? 0.0 : quarterlySumScore.getDjgzScore();
				//工作实绩 A3/A2*100*0.9
				double gzsjSocre = quarterlySumScore.getGzsjScore() == null ? 0.0 : quarterlySumScore.getGzsjScore();
				//1、工作实绩指标总分值（A1）=100 固定值
				Double A1 = Double.valueOf(100);
				//2、扣除缺项后的指标分值(A2)
				String responsibleUnitId = quarterlySumScore.getResponsibleUnitId();
				String stageYear = quarterlySumScore.getStageYear();
				String stage = quarterlySumScore.getStage();
				Map<String,Object> map = iQuarterlySumScoreService.getTotalWeight(responsibleUnitId,stageYear,stage, quarterlyEvaluation.getJdzbType());
				Double A2 = Double.valueOf(map.get("totalWeight").toString());
				gzsjSocre = A2 == 0 ? 0 : gzsjSocre / A2 * 100 * 0.9;

				//党风廉政
				double dflzSocre = quarterlySumScore.getDflzScore() == null ? 0.0 : quarterlySumScore.getDflzScore();
				//三抓三促
				double s3z3cSocre = quarterlySumScore.getS3z3cScore() == null ? 0.0 : quarterlySumScore.getS3z3cScore();
				//加分项
				//double addSocre = quarterlySumScore.getAddScore() == null ? 0.0 : quarterlySumScore.getAddScore();
				//减分项
				//double minusSocre = quarterlySumScore.getMinusScore() == null ? 0.0 : quarterlySumScore.getMinusScore();
				//领导评价
				//double leaderSocre = quarterlySumScore.getLeaderScore() == null ? 0.0 : quarterlySumScore.getLeaderScore();

				//TODO 计算总分
				double sumSocre = djgzSocre + gzsjSocre + dflzSocre + s3z3cSocre;// + addSocre - minusSocre + leaderSocre
				quarterlySumScore.setQuarterlySumScore(Double.parseDouble(String.format("%.3f", sumSocre)));

				if (annualSumScoreList.size() > 0) {
					//如果当前记录存在，就修改
					iQuarterlySumScoreService.update(quarterlySumScore,quarterlySumScoreQueryWrapper);
					System.out.println("季度评价数据已存在，执行修改！");
					//保存当前评价记录
					AppriseDept appriseDept3 =new AppriseDept();
					appriseDept3.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept3.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept3.setScore(String.valueOf(quarterlySumScore.getQuarterlySumScore()));
					appriseDept3.setScoringDescription("修改总分：=" + quarterlySumScore.getQuarterlySumScore());
					appriseDept3.setEvaluationType("2");
					appriseDept3.setEvaluationId(ab.getEvaluationId());
					appriseDept3.setType("2");//计算后得分
					appriseDept3.setAppriseBaseinfoId(ab.getId());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					appriseDept3.setCreateUserName(userNameDecrypt);
					appriseDept3.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept3);
				} else {
					//保存到季度评分表中，最后计算【党建工作】等单个总分的时候只需要按条件查询出所有的【党建工作】总分，然后加在一起就行
					iQuarterlySumScoreService.save(quarterlySumScore);
					//保存当前评价记录
					AppriseDept appriseDept3 =new AppriseDept();
					appriseDept3.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept3.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept3.setScore(String.valueOf(quarterlySumScore.getQuarterlySumScore()));
					appriseDept3.setScoringDescription("保存总分：=" + quarterlySumScore.getQuarterlySumScore());
					appriseDept3.setEvaluationType("2");
					appriseDept3.setEvaluationId(ab.getEvaluationId());
					appriseDept3.setType("2");//计算后得分
					appriseDept3.setAppriseBaseinfoId(ab.getId());
					appriseDept3.setCreateUserName(user.getRealName());
					appriseDept3.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept3);
				}
			}

			//TODO 如果未评价单位个数为0，就将是否评价的状态改成1
			if (notAppriseUserStr1.length == 0) {
				quarterlyEvaluation.setIsAppraise(1);
			}
			iQuarterlyEvaluationService.updateById(quarterlyEvaluation);

			String title = "新增季度评价指标评价";
			String businessId = String.valueOf(ab.getEvaluationId());
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		}
		else if (Objects.equals(ab.getEvaluationType(), "1")) {//如果是【年度评价】
			/*
			 * 每季度评价结果按10%计入年度考评结果。（这里应该指总分）
			 *
			 * */
			AnnualEvaluation annualEvaluation = iAnnualEvaluationService.getById(ab.getEvaluationId());
			String[] avgStr = annualEvaluation.getAppraiseDeptid().split(",");
			//获取当前评价部门的个数，用于计算各部门的平均分
			int avgCount = avgStr.length;

			//把当前部门从未评价部门中排除
			String[] notAppriseUserStr = null;
			if (StringUtils.isNotBlank(annualEvaluation.getNotAppriseUser())) {
				notAppriseUserStr = annualEvaluation.getNotAppriseUser().split(",");
			} else{
				notAppriseUserStr = annualEvaluation.getAppraiseDeptname().split(",");
			}
			List<String> stringList = new ArrayList<>();
			for (int i = 0; i < notAppriseUserStr.length; i++) {
				if (!notAppriseUserStr[i].contains(ab.getDeptName())) {
					stringList.add(notAppriseUserStr[i]);
				}
			}
			String[] notAppriseUserStr1 = new String[stringList.size()];
			for (int i = 0; i < stringList.size(); i++) {
				notAppriseUserStr1[i] = stringList.get(i);
			}
			if (notAppriseUserStr1.length >= 0) {
				annualEvaluation.setNotAppriseUser(Arrays.toString(notAppriseUserStr1).replace("[","").replace("]",""));
			}

			List<AppriseDept> appriseDeptList1 = ab.getAppriseDeptList();
			List<AppriseDept> appriseDeptList = new ArrayList<>();
			if (appriseDeptList1.size() > 0) {
				for (AppriseDept appriseDept : appriseDeptList1) {
					if (!StringUtils.isBlank(appriseDept.getScore())) {
						appriseDeptList.add(appriseDept);
					}
				}
			}

			for (int i = 0; i < appriseDeptList.size(); i++) {
				AnnualSumScore annualSumScore = new AnnualSumScore();
				//获取【考核分类的id】
				annualSumScore.setCheckClassify(annualEvaluation.getAppraiseClassify());
				//获取【考核分类的名称】
				annualSumScore.setCheckClassifyName(annualEvaluation.getAppraiseClassifyName());
				//获取【评价部门id】
				annualSumScore.setResponsibleUnitId(String.valueOf(appriseDeptList.get(i).getResponsibleUnitId()));
				//获取【评价部门名称】
				annualSumScore.setResponsibleUnitName(appriseDeptList.get(i).getResponsibleUnitName());
				//获取【当前事项名称】，【二级指标】，【评价要点】，【主要指标及评价要点】（有很多种叫法）
				annualSumScore.setServName(ab.getServName());
				//获取【当前年份】
				annualSumScore.setAnnualYear(String.valueOf(DateUtil.year(new Date())));
				//获取【年度指标表id】
				annualSumScore.setAnnualEvaluationId(ab.getEvaluationId());
				//获取【年考核部门id】
				annualSumScore.setAppraiseDeptid(String.valueOf(ab.getCreateDept()));
				//获取【考核部门名称】
				annualSumScore.setAppraiseDeptname(ab.getDeptName());
				//获取【阶段】
				annualSumScore.setStage(ab.getStage());
				//获取【阶段id】
				annualSumScore.setStageId(ab.getStageId());

				//查询数据库中是否有这条数据，有就修改，没有就保存
				QueryWrapper<AnnualSumScore> annualSumScoreQueryWrapper = new QueryWrapper<>();
				annualSumScoreQueryWrapper.eq("check_classify",annualSumScore.getCheckClassify());
				annualSumScoreQueryWrapper.eq("check_classify_name",annualSumScore.getCheckClassifyName());
				annualSumScoreQueryWrapper.eq("responsible_unit_id",annualSumScore.getResponsibleUnitId());
				annualSumScoreQueryWrapper.eq("stage_id",annualSumScore.getStageId());
				annualSumScoreQueryWrapper.eq("serv_name",annualSumScore.getServName());
				annualSumScoreQueryWrapper.eq("annual_year",annualSumScore.getAnnualYear());
				annualSumScoreQueryWrapper.eq("appraise_deptid",annualSumScore.getAppraiseDeptid());

				List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService.list(annualSumScoreQueryWrapper);

				//获取当前指标权重是普通权重还是区县权重，权重为百分比，所以这里要除以100
				double weight = 0.0;
				if ("5".equals(annualEvaluation.getType())) {//县区高质量发展
					if ("甘州区".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getGanzhouqu())) {
							annualEvaluation.setGanzhouqu("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getGanzhouqu()) / 100;
					} else if ("临泽县".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getLinzexian())) {
							annualEvaluation.setLinzexian("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getLinzexian()) / 100;
					} else if ("高台县".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getGaotaixian())) {
							annualEvaluation.setGaotaixian("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getGaotaixian()) / 100;
					} else if ("山丹县".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getShandanxian())) {
							annualEvaluation.setShandanxian("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getShandanxian()) / 100;
					} else if ("民乐县".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getMinlexian())) {
							annualEvaluation.setMinlexian("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getMinlexian()) / 100;
					} else if ("肃南县".equals(appriseDeptList.get(i).getResponsibleUnitName())) {
						if (StringUtils.isEmpty(annualEvaluation.getSunanxian())) {
							annualEvaluation.setSunanxian("0.0");
						}
						weight = Double.parseDouble(annualEvaluation.getSunanxian()) / 100;
					}
				} else {
					if (StringUtils.isEmpty(ab.getWeight())) {
						ab.setWeight("0.0");
					}
					weight = Double.parseDouble(ab.getWeight()) / 100;
				}

				//获取当前指标的评价得分
				double score = Double.parseDouble(appriseDeptList.get(i).getScore());
				//计算得分
				double getScore = (weight * score);
				//如果评价部门不止一个，就取各部门的平均值
				if (avgCount > 1) {
					getScore = getScore / avgCount;
				}

				//TODO 根据当前年度指标项目类型分别保存数据
				if (annualEvaluation.getType().equals("1")) {//政治思想建设 占15%，需求变动先注释掉
					getScore = Double.parseDouble(String.format("%.3f", getScore));// * 0.15
					annualSumScore.setZzsxjsScore(getScore);
				} else if (annualEvaluation.getType().equals("2")) {//领导能力 占10%，需求变动先注释掉
					getScore = Double.parseDouble(String.format("%.3f", getScore));// * 0.10
					annualSumScore.setLdnlScore(getScore);
				} else if (annualEvaluation.getType().equals("3")) {//党的建设 占15%，需求变动先注释掉
					getScore = Double.parseDouble(String.format("%.3f", getScore));// * 0.15
					annualSumScore.setDdjsScore(getScore);
				} else if (annualEvaluation.getType().equals("4")) {//市直高质量发展 占60%
					getScore = Double.parseDouble(String.format("%.3f", getScore * 0.6));
					annualSumScore.setSzgzlfzScore(getScore);
				} else if (annualEvaluation.getType().equals("5")) {//县区高质量发展 占60%
					getScore = Double.parseDouble(String.format("%.3f", getScore * 0.6));
					annualSumScore.setXqgzlfzScore(getScore);
				}


				//TODO （政治思想建设 + 领导能力 + 党的建设 + 市直高质量发展 + 县区高质量发展 = 总分）
				// + 季度分数 + 加分项 - 减分项 + 领导评价得分
				//政治思想建设
				double zzsxjsSocre = annualSumScore.getZzsxjsScore() == null ? 0.0 : annualSumScore.getZzsxjsScore();
				//领导能力
				double ldnlSocre = annualSumScore.getLdnlScore() == null ? 0.0 : annualSumScore.getLdnlScore();
				//党的建设
				double ddjsSocre = annualSumScore.getDdjsScore() == null ? 0.0 : annualSumScore.getDdjsScore();
				//市直高质量发展
				double szgzlfzSocre = annualSumScore.getSzgzlfzScore() == null ? 0.0 : annualSumScore.getSzgzlfzScore();
				//县区高质量发展
				double xqgzlfzSocre = annualSumScore.getXqgzlfzScore() == null ? 0.0 : annualSumScore.getXqgzlfzScore();
				//季度分数
				//double jspjScore = annualSumScore.getJdpjScore() == null ? 0.0 : annualSumScore.getJdpjScore();
				//加分项
				//double addSocre = annualSumScore.getAddScore() == null ? 0.0 : annualSumScore.getAddScore();
				//减分项
				//double minusSocre = annualSumScore.getMinusScore() == null ? 0.0 : annualSumScore.getMinusScore();
				//领导评价得分
				//double leaderSocre = annualSumScore.getLeaderScore() == null ? 0.0 : annualSumScore.getLeaderScore();

				//计算总分
				double sumSocre = zzsxjsSocre + ldnlSocre + ddjsSocre + szgzlfzSocre + xqgzlfzSocre;// + jspjScore + addSocre + minusSocre + leaderSocre
				annualSumScore.setAnnualSumScore(Double.parseDouble(String.format("%.3f", sumSocre)));
				if (annualSumScoreList.size() > 0) {
					//如果当前记录存在就执行修改
					iAnnualSumScoreService.update(annualSumScore,annualSumScoreQueryWrapper);
					System.out.println("年度评价数据已存在，执行修改！");
					//保存当前评价记录
					AppriseDept appriseDept4 =new AppriseDept();
					appriseDept4.setResponsibleUnitId(Long.valueOf(annualSumScore.getResponsibleUnitId()));
					appriseDept4.setResponsibleUnitName(annualSumScore.getResponsibleUnitName());
					appriseDept4.setScore(String.valueOf(annualSumScore.getAnnualSumScore()));
					appriseDept4.setScoringDescription("修改总分：=" + annualSumScore.getAnnualSumScore());
					appriseDept4.setEvaluationType("1");
					appriseDept4.setEvaluationId(ab.getEvaluationId());
					appriseDept4.setType("2");//计算后得分
					appriseDept4.setAppriseBaseinfoId(ab.getId());
					appriseDept4.setCreateUserName(user.getRealName());
					appriseDept4.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept4);
				} else {
					//保存到年度评分表中
					iAnnualSumScoreService.save(annualSumScore);
					//保存当前评价记录
					AppriseDept appriseDept4 =new AppriseDept();
					appriseDept4.setResponsibleUnitId(Long.valueOf(annualSumScore.getResponsibleUnitId()));
					appriseDept4.setResponsibleUnitName(annualSumScore.getResponsibleUnitName());
					appriseDept4.setScore(String.valueOf(annualSumScore.getAnnualSumScore()));
					appriseDept4.setScoringDescription("保存总分：=" + annualSumScore.getAnnualSumScore());
					appriseDept4.setEvaluationType("1");
					appriseDept4.setEvaluationId(ab.getEvaluationId());
					appriseDept4.setType("2");//计算后得分
					appriseDept4.setAppriseBaseinfoId(ab.getId());
					appriseDept4.setCreateUserName(user.getRealName());
					appriseDept4.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept4);
				}

			}

			//TODO 如果未评价单位为0，就将是否评价的状态改成1
			if (notAppriseUserStr1.length == 0) {
				annualEvaluation.setIsAppraise(1);
			}
			iAnnualEvaluationService.updateById(annualEvaluation);

			String title = "新增年度评价指标评价";
			String businessId = String.valueOf(ab.getEvaluationId());
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		}

		return R.status(appriseBaseinfoService.saveApprise(ab));
	}

	/**
	 * PC端领导关注和我的关注
	 */
	@GetMapping("/LeaderAndMy")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "领导门户-领导关注和我的关注信息", notes = "传入 appriseBaseInfoVO")
	public R<PageInfo> LeaderAndMy(@RequestParam("userFocusType") String userFocusType,
						 @RequestParam("targetStatus") String targetStatus,
						 @RequestParam("appraiseClassify") String appraiseClassify,
						 @RequestParam("createTime") String createTime,
						 @RequestParam("proType") String proType,Query query) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		BladeUser currentUser = AuthUtil.getUser();
		List<LeaderAndMyFocusVO> leaderAndMyFocusVOS = new ArrayList();
		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[", "").replace("]", "");
		QueryWrapper<FollowInformation> followInformationQueryWrapper = new QueryWrapper<>();

		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(!StringUtil.isBlank(targetStatus), "target_status", targetStatus);
		queryWrapper.eq(!StringUtil.isBlank(appraiseClassify), "appraise_classify", appraiseClassify);
		queryWrapper.eq(!StringUtil.isBlank(proType), "project_name", proType);
		queryWrapper.like(!StringUtil.isBlank(createTime), "create_time", createTime);

		QueryWrapper<QuarterlyEvaluation> queryWrapper1 = new QueryWrapper<>();
		queryWrapper1.eq(!StringUtil.isBlank(targetStatus), "target_status", targetStatus);
		queryWrapper1.eq(!StringUtil.isBlank(appraiseClassify), "appraise_classify", appraiseClassify);
		queryWrapper1.eq(!StringUtil.isBlank(proType), "jdzb_name", proType);
		queryWrapper1.like(!StringUtil.isBlank(createTime), "create_time", createTime);

		if (userFocusType != null && userFocusType.equals("1")) {//如果是领导关注

			if (currentUser != null) {// && !"1123598817738675202".equals(currentUser.getPostId())
				if (StringUtil.isBlank(currentUser.getDeptId())) {
					return R.fail("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
				List<String> leaderIds1 = new ArrayList<>();
				for (User leader : leaders) {
					leaderIds1.add(leader.getId().toString());
				}
				String leaderIds = leaderIds1.toString().replace("[", "").replace("]", "");
				followInformationQueryWrapper.in("follow_user_id", leaderIds);
			}
		} else if (userFocusType != null && userFocusType.equals("2")) {//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id", currentUser.getUserId());
		}
		followInformationQueryWrapper.eq("business_type", "2");

		String[] tabId = null;
		if (userFocusType != null) {
			if ("1".equals(userFocusType) || "2".equals(userFocusType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				if (followInformations.size() > 0) {
					tabId = new String[followInformations.size()];
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in("id", tabId);
					queryWrapper1.in("id", tabId);
				} else {
					queryWrapper.in("id", "xxxxxxxx");
					queryWrapper1.in("id", "xxxxxxxx");
				}
			}
			List<QuarterlyEvaluation> quarterlyEvaluations = iQuarterlyEvaluationService.list(queryWrapper1);
			List<AnnualEvaluation> annualEvaluations = iAnnualEvaluationService.list(queryWrapper);
			if (quarterlyEvaluations.size() > 0) {
				for (QuarterlyEvaluation quarterlyEvaluation : quarterlyEvaluations) {
					LeaderAndMyFocusVO leaderAndMyFocusVO = new LeaderAndMyFocusVO();
					leaderAndMyFocusVO.setProjectName(quarterlyEvaluation.getJdzbName());
					leaderAndMyFocusVO.setAppraiseClassify(quarterlyEvaluation.getCheckClassify());
					leaderAndMyFocusVO.setAppraiseClassifyName(quarterlyEvaluation.getCheckClassifyName());
					leaderAndMyFocusVO.setAppraiseObject(quarterlyEvaluation.getCheckObject());
					leaderAndMyFocusVO.setAppraiseObjectId(quarterlyEvaluation.getCheckObjectId());
					leaderAndMyFocusVO.setMajorTarget(quarterlyEvaluation.getMajorTarget());
					leaderAndMyFocusVO.setFinishDate(quarterlyEvaluation.getFinishDate());
					leaderAndMyFocusVO.setAppraiseDeptid(quarterlyEvaluation.getAppraiseDeptid());
					leaderAndMyFocusVO.setAppraiseDeptname(quarterlyEvaluation.getAppraiseDeptname());
					leaderAndMyFocusVO.setTargetStatus(quarterlyEvaluation.getTargetStatus());
					leaderAndMyFocusVOS.add(leaderAndMyFocusVO);
				}
			}
			if (annualEvaluations.size() > 0) {
				for (AnnualEvaluation annualEvaluation : annualEvaluations) {
					LeaderAndMyFocusVO leaderAndMyFocusVO = new LeaderAndMyFocusVO();
					leaderAndMyFocusVO.setProjectName(annualEvaluation.getProjectName());
					leaderAndMyFocusVO.setAppraiseClassify(annualEvaluation.getAppraiseClassify());
					leaderAndMyFocusVO.setAppraiseClassifyName(annualEvaluation.getAppraiseClassifyName());
					leaderAndMyFocusVO.setAppraiseObject(annualEvaluation.getAppraiseObject());
					leaderAndMyFocusVO.setAppraiseObjectId(annualEvaluation.getAppraiseObjectId());
					leaderAndMyFocusVO.setMajorTarget(annualEvaluation.getMajorTarget());
					leaderAndMyFocusVO.setFinishDate(annualEvaluation.getFinishDate());
					leaderAndMyFocusVO.setAppraiseDeptid(annualEvaluation.getAppraiseDeptid());
					leaderAndMyFocusVO.setAppraiseDeptname(annualEvaluation.getAppraiseDeptname());
					leaderAndMyFocusVO.setTargetStatus(annualEvaluation.getTargetStatus());
					leaderAndMyFocusVOS.add(leaderAndMyFocusVO);
				}
			}
		}
		PageInfo pageInfo = new PageInfo(leaderAndMyFocusVOS);
		return R.data(pageInfo);
	}

	/**
	 * 阶段评价详情
	 * @param
	 * @return
	 */
	@GetMapping("/stage-details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "阶段评价信息详情", notes = "")
	@Log(title = "查看阶段评价详情信息",businessType = BusinessType.LOOK)
	public R<AppriseBaseinfo> details(@ApiIgnore @RequestParam("stageId") String stageId,@RequestParam("evaluationId") String evaluationId,
									  @RequestParam("evaluationType") String evaluationType) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userName = user.getName();
		userName = CryptoFactory.createCrypto(CryptoType.SM4).checkDataIsEncrypt(userName) ? CryptoFactory.createCrypto(CryptoType.SM4).decrypt(userName) : userName;
		//基本信息
		QueryWrapper<AppriseBaseinfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.select(" * ");
		queryWrapper.eq(StrUtil.isNotBlank(stageId),"stage_id",stageId);
		queryWrapper.eq(StrUtil.isNotBlank(evaluationId),"evaluation_id",evaluationId);
		queryWrapper.eq(StrUtil.isNotBlank(evaluationType),"evaluation_type",evaluationType);
		AppriseBaseinfo ab = appriseBaseinfoService.getOne(queryWrapper.last("LIMIT 1"));
		if(ab!=null){
			//单位评价信息
			QueryWrapper<AppriseDept> queryWrapperInfo = new QueryWrapper<>();
			queryWrapperInfo.select("*");
			queryWrapperInfo.eq("evaluation_id",evaluationId);
			queryWrapperInfo.eq("create_user_name",userName);

			//查询百分制得分
			queryWrapperInfo.eq("type","1");
			List<AppriseDept> appriseDeptList =iAppriseDeptService.list(queryWrapperInfo);
			ab.setAppriseDeptList(appriseDeptList);
		}
		return R.data(ab);
	}


	/**
	 * 评价修改接口
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	@Log(title = "修改评价打分",businessType = BusinessType.UPDATE)
	@Transactional
	public R update(@Valid @RequestBody AppriseBaseinfo ab) throws ParseException {
		Long createuser = ab.getCreateUser();
		User user = UserCache.getUser(createuser);
		Long createDept = ab.getCreateDept();//评价部门id
		//获取部门名称
		String deptName = SysCache.getDeptName(createDept);
		ab.setDeptName(deptName);
		if (Objects.equals(ab.getEvaluationType(), "2")) {//如果是【季度评价】
			/*
			 * 季度评价按【党建工作】占30%、【工作实绩】占90%、【廉政建设】占10%的比例进行计算；
			 * 每季度评价结果按10%计入年度考评结果。（这里应该指总分）
			 * */
			QuarterlyEvaluation quarterlyEvaluation = iQuarterlyEvaluationService.getById(ab.getEvaluationId());
			//获取当前评价部门的个数，用于计算各部门的平均分
			String[] avgStr = quarterlyEvaluation.getAppraiseDeptid().split(",");
			int avgCount = avgStr.length;

			//把当前部门从未评价部门中排除
			String[] notAppriseUserStr = quarterlyEvaluation.getNotAppriseUser().split(",");
			List<String> stringList = new ArrayList<>();
			for (int i = 0; i < notAppriseUserStr.length; i++) {
				if (!notAppriseUserStr[i].contains(ab.getDeptName())) {
					stringList.add(notAppriseUserStr[i]);
				}
			}
			String[] notAppriseUserStr1 = new String[stringList.size()];
			for (int i = 0; i < stringList.size(); i++) {
				notAppriseUserStr1[i] = stringList.get(i);
			}
			if (notAppriseUserStr1.length >= 0) {
				quarterlyEvaluation.setNotAppriseUser(Arrays.toString(notAppriseUserStr1).replace("[", "").replace("]", ""));
			}

			List<AppriseDept> appriseDeptList = ab.getAppriseDeptList();
			for (int i = 0; i < appriseDeptList.size(); i++) {
				QuarterlySumScore quarterlySumScore = new QuarterlySumScore();
				//获取【考核分类的id】
				quarterlySumScore.setCheckClassify(quarterlyEvaluation.getCheckClassify());
				//获取【考核分类的名称】
				quarterlySumScore.setCheckClassifyName(quarterlyEvaluation.getCheckClassifyName());
				//获取【评价部门id】
				quarterlySumScore.setResponsibleUnitId(String.valueOf(appriseDeptList.get(i).getResponsibleUnitId()));
				//获取【评价部门名称】
				quarterlySumScore.setResponsibleUnitName(appriseDeptList.get(i).getResponsibleUnitName());
				//获取【当前事项名称】
				quarterlySumScore.setServName(ab.getServName());
				//获取【当前阶段】
				quarterlySumScore.setStage(ab.getStage());
				//获取【当前年份】
				quarterlySumScore.setStageYear(String.valueOf(DateUtil.year(new Date())));
				//获取【季度指标表id】
				quarterlySumScore.setQuarterlyEvaluationId(ab.getEvaluationId());
				//获取【季考核部门id】
				quarterlySumScore.setAppraiseDeptid(String.valueOf(ab.getCreateDept()));
				//获取【考核部门名称】
				quarterlySumScore.setAppraiseDeptname(ab.getDeptName());

				QueryWrapper<QuarterlySumScore> quarterlySumScoreQueryWrapper = new QueryWrapper<>();
				quarterlySumScoreQueryWrapper.eq("check_classify",quarterlySumScore.getCheckClassify());
				quarterlySumScoreQueryWrapper.eq("check_classify_name",quarterlySumScore.getCheckClassifyName());
				quarterlySumScoreQueryWrapper.eq("responsible_unit_id",quarterlySumScore.getResponsibleUnitId());
				quarterlySumScoreQueryWrapper.eq("responsible_unit_name",quarterlySumScore.getResponsibleUnitName());
				quarterlySumScoreQueryWrapper.eq("serv_name",quarterlySumScore.getServName());
				quarterlySumScoreQueryWrapper.eq("stage_year",quarterlySumScore.getStageYear());
				quarterlySumScoreQueryWrapper.eq("appraise_deptid",quarterlySumScore.getAppraiseDeptid());
				//查询数据库中是否有这条数据，有就修改，没有就保存
				List<QuarterlySumScore> annualSumScoreList = iQuarterlySumScoreService.list(quarterlySumScoreQueryWrapper);

				//根据当前季度指标类型分别保存数据
				if (quarterlyEvaluation.getJdzbName().contains("党建工作")) {
					/*
					 *  计算分数（百分制得分 × 权重）
					 * （一）党建工作考评办法。对照县区与市直部门单位党建工作的具体要求，
					 * 	   分类进行季度考评，设置相应党建工作季度考评重点指标，安排相关评分单位出台考评细则，
					 *      按照百分比进行量化考评打分，得分以分配权重进行汇总，最终按30%计入班子季度考评最终得分。
					 *     （评价完了之后各项指标得分加起来×30%）
					 * */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,季度评价按【党建工作】占 30%
					double getScore = (weight * score) * 0.3;
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setDjgzScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("党建工作得分 × 权重 × 党建工作得分占比："+ score +"×"+ weight + "×" + 0.3 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setEvaluationId(ab.getEvaluationId());
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					appriseDept.setCreateUserName(user.getRealName());
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				} else if (quarterlyEvaluation.getJdzbName().contains("工作实绩")) {
					/*
					 *	第一个指标打80分，权重15%，第二个指标打60分，权重10%
					 *	这两个指标的综合得分就是（（80×15%）+（60×10%）） / 2
					 *  （二）工作实绩考评办法。采用线性加权法合成综合评价指数，
					 *  即：将单项评价指标的百分制得分乘以相应权重，然后加总，
					 *  得出各县区和市直部门单位每季度工作实绩评价的综合指数。（求平均数）
					 *  计算公式为：D=（ΣWi*Di）/i
					 *  其中Dｉ为第ｉ个指标的得分，Wｉ为第ｉ个指标的权数。
					 */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【工作实绩】占 90%；
					double getScore = (weight * score) * 0.9;
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setGzsjScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("工作实绩得分 × 权重 × 工作实绩得分占比："+ score +"×"+ weight + "×" + 0.9 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setEvaluationId(ab.getEvaluationId());
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					appriseDept.setCreateUserName(user.getRealName());
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				} else if (quarterlyEvaluation.getJdzbName().contains("党风廉政")) {
					/*
					 *（三）廉政建设考评办法。主要围绕《党风廉政建设责任制考核县区（市直部门单位）重点任务评价表》中
					 * “责任落实、监督情况、惩治腐败、作风建设、制度落实、问题整改”6项重点任务，以及细化的评分标准，
					 * 进行量化打分，计算得出各县区和市直部门单位每季度党风廉政建设考评得分。（直接对得分进行计算）
					 * */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【廉政建设】占 10%
					double getScore = (weight * score) * 0.1;
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setDflzScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("党风廉政得分 × 权重 × 党风廉政得分占比："+ score +"×"+ weight + "×" + 0.1 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setEvaluationId(ab.getEvaluationId());
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					appriseDept.setCreateUserName(user.getRealName());
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				}else if (quarterlyEvaluation.getJdzbName().contains("三抓三促")) {
					/*
					 * 三抓三促规范 按照党风廉政假设一样的来
					 *（三）廉政建设考评办法。主要围绕《党风廉政建设责任制考核县区（市直部门单位）重点任务评价表》中
					 * “责任落实、监督情况、惩治腐败、作风建设、制度落实、问题整改”6项重点任务，以及细化的评分标准，
					 * 进行量化打分，计算得出各县区和市直部门单位每季度党风廉政建设考评得分。（直接对得分进行计算）
					 * */
					//获取当前指标权重，权重为百分比，所以这里要除以100
					double weight = Double.parseDouble(ab.getWeight()) / 100;
					//获取当前指标的评价得分
					double score = Double.parseDouble(appriseDeptList.get(i).getScore());
					//计算得分,【三抓三促】占 10%
					double getScore = (weight * score) * 0.1;
					//如果评价部门不止一个，就取各部门的平均值
					if (avgCount > 1) {
						getScore = getScore / avgCount;
					}
					quarterlySumScore.setS3z3cScore(Double.parseDouble(String.format("%.3f", getScore)));

					//保存当前评价记录
					AppriseDept appriseDept =new AppriseDept();
					appriseDept.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept.setScore(String.valueOf(getScore));
					appriseDept.setScoringDescription("三抓三促得分 × 权重 × 三抓三促得分占比："+ score +"×"+ weight + "×" + 0.1 + "=" + getScore);
					appriseDept.setEvaluationType("2");
					appriseDept.setEvaluationId(ab.getEvaluationId());
					appriseDept.setType("2");//计算后得分
					appriseDept.setAppriseBaseinfoId(ab.getId());
					appriseDept.setCreateUserName(user.getRealName());
					appriseDept.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept);
				}


				//TODO 打完分之后计算总分（党建工作 + 工作实绩 + 党风廉政 = 总分）
				//+ 加分项 - 减分项 + 领导评价
				//党建工作
				double djgzSocre = quarterlySumScore.getDjgzScore() == null ? 0.0 : quarterlySumScore.getDjgzScore();
				//工作实绩
				double gzsjSocre = quarterlySumScore.getGzsjScore() == null ? 0.0 : quarterlySumScore.getGzsjScore();
				//党风廉政
				double dflzSocre = quarterlySumScore.getDflzScore() == null ? 0.0 : quarterlySumScore.getDflzScore();
				//加分项
				//double addSocre = quarterlySumScore.getAddScore() == null ? 0.0 : quarterlySumScore.getAddScore();
				//减分项
				//double minusSocre = quarterlySumScore.getMinusScore() == null ? 0.0 : quarterlySumScore.getMinusScore();
				//领导评价
				//double leaderSocre = quarterlySumScore.getLeaderScore() == null ? 0.0 : quarterlySumScore.getLeaderScore();

				//TODO 计算总分
				double sumSocre = djgzSocre + gzsjSocre + dflzSocre;// + addSocre - minusSocre + leaderSocre
				quarterlySumScore.setQuarterlySumScore(Double.parseDouble(String.format("%.3f", sumSocre)));

				if (annualSumScoreList.size() > 0) {
					//如果当前记录存在，就修改
					iQuarterlySumScoreService.update(quarterlySumScore,quarterlySumScoreQueryWrapper);
					System.out.println("季度评价数据已存在，执行修改！");
					//保存当前评价记录
					AppriseDept appriseDept3 =new AppriseDept();
					appriseDept3.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept3.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept3.setScore(String.valueOf(quarterlySumScore.getQuarterlySumScore()));
					appriseDept3.setScoringDescription("修改总分：=" + quarterlySumScore.getQuarterlySumScore());
					appriseDept3.setEvaluationType("2");
					appriseDept3.setEvaluationId(ab.getEvaluationId());
					appriseDept3.setType("2");//计算后得分
					appriseDept3.setAppriseBaseinfoId(ab.getId());
					appriseDept3.setCreateUserName(user.getRealName());
					appriseDept3.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept3);
				} else {
					//保存到季度评分表中，最后计算【党建工作】等单个总分的时候只需要按条件查询出所有的【党建工作】总分，然后加在一起就行
					iQuarterlySumScoreService.save(quarterlySumScore);
					//保存当前评价记录
					AppriseDept appriseDept3 =new AppriseDept();
					appriseDept3.setResponsibleUnitId(Long.valueOf(quarterlySumScore.getResponsibleUnitId()));
					appriseDept3.setResponsibleUnitName(quarterlySumScore.getResponsibleUnitName());
					appriseDept3.setScore(String.valueOf(quarterlySumScore.getQuarterlySumScore()));
					appriseDept3.setScoringDescription("保存总分：=" + quarterlySumScore.getQuarterlySumScore());
					appriseDept3.setEvaluationType("2");
					appriseDept3.setEvaluationId(ab.getEvaluationId());
					appriseDept3.setType("2");//计算后得分
					appriseDept3.setAppriseBaseinfoId(ab.getId());
					appriseDept3.setCreateUserName(user.getRealName());
					appriseDept3.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept3);
				}
			}

			//TODO 如果未评价单位个数为0，就将是否评价的状态改成1
			if (notAppriseUserStr1.length == 0) {
				quarterlyEvaluation.setIsAppraise(1);
			}
			iQuarterlyEvaluationService.updateById(quarterlyEvaluation);

			String title = "新增季度评价指标评价";
			String businessId = String.valueOf(ab.getEvaluationId());
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		}
		else if (Objects.equals(ab.getEvaluationType(), "1")) {//如果是【年度评价】
			/*
			 * 每季度评价结果按10%计入年度考评结果。（这里应该指总分）
			 *
			 * */
			AnnualEvaluation annualEvaluation = iAnnualEvaluationService.getById(ab.getEvaluationId());
			String[] avgStr = annualEvaluation.getAppraiseDeptid().split(",");
			//获取当前评价部门的个数，用于计算各部门的平均分
			int avgCount = avgStr.length;

			//把当前部门从未评价部门中排除
			String[] notAppriseUserStr = annualEvaluation.getNotAppriseUser().split(",");
			List<String> stringList = new ArrayList<>();
			for (int i = 0; i < notAppriseUserStr.length; i++) {
				if (!notAppriseUserStr[i].contains(ab.getDeptName())) {
					stringList.add(notAppriseUserStr[i]);
				}
			}
			String[] notAppriseUserStr1 = new String[stringList.size()];
			for (int i = 0; i < stringList.size(); i++) {
				notAppriseUserStr1[i] = stringList.get(i);
			}
			if (notAppriseUserStr1.length >= 0) {
				annualEvaluation.setNotAppriseUser(Arrays.toString(notAppriseUserStr1).replace("[","").replace("]",""));
			}

			List<AppriseDept> appriseDeptList = ab.getAppriseDeptList();
			for (int i = 0; i < appriseDeptList.size(); i++) {
				AnnualSumScore annualSumScore = new AnnualSumScore();
				//获取【考核分类的id】
				annualSumScore.setCheckClassify(annualEvaluation.getAppraiseClassify());
				//获取【考核分类的名称】
				annualSumScore.setCheckClassifyName(annualEvaluation.getAppraiseClassifyName());
				//获取【评价部门id】
				annualSumScore.setResponsibleUnitId(String.valueOf(appriseDeptList.get(i).getResponsibleUnitId()));
				//获取【评价部门名称】
				annualSumScore.setResponsibleUnitName(appriseDeptList.get(i).getResponsibleUnitName());
				//获取【当前事项名称】，【二级指标】，【评价要点】，【主要指标及评价要点】（有很多种叫法）
				annualSumScore.setServName(ab.getServName());
				//获取【当前年份】
				annualSumScore.setAnnualYear(String.valueOf(DateUtil.year(new Date())));
				//获取【年度指标表id】
				annualSumScore.setAnnualEvaluationId(ab.getEvaluationId());
				//获取【年考核部门id】
				annualSumScore.setAppraiseDeptid(String.valueOf(ab.getCreateDept()));
				//获取【考核部门名称】
				annualSumScore.setAppraiseDeptname(ab.getDeptName());

				//查询数据库中是否有这条数据，有就修改，没有就保存
				QueryWrapper<AnnualSumScore> annualSumScoreQueryWrapper = new QueryWrapper<>();
				annualSumScoreQueryWrapper.eq("check_classify",annualSumScore.getCheckClassify());
				annualSumScoreQueryWrapper.eq("check_classify_name",annualSumScore.getCheckClassifyName());
				annualSumScoreQueryWrapper.eq("responsible_unit_id",annualSumScore.getResponsibleUnitId());
				annualSumScoreQueryWrapper.eq("responsible_unit_name",annualSumScore.getResponsibleUnitName());
				annualSumScoreQueryWrapper.eq("serv_name",annualSumScore.getServName());
				annualSumScoreQueryWrapper.eq("annual_year",annualSumScore.getAnnualYear());
				annualSumScoreQueryWrapper.eq("appraise_deptid",annualSumScore.getAppraiseDeptid());

				List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService.list(annualSumScoreQueryWrapper);


				//获取当前指标权重，权重为百分比，所以这里要除以100
				double weight = Double.parseDouble(ab.getWeight()) / 100;
				//获取当前指标的评价得分
				double score = Double.parseDouble(appriseDeptList.get(i).getScore());
				//计算得分
				double getScore = (weight * score);
				//如果评价部门不止一个，就取各部门的平均值
				if (avgCount > 1) {
					getScore = getScore / avgCount;
				}
				getScore = Double.parseDouble(String.format("%.3f", getScore));
				//TODO 根据当前年度指标项目类型分别保存数据
				if (annualEvaluation.getType().equals("1")) {//政治思想建设
					annualSumScore.setZzsxjsScore(getScore);
				} else if (annualEvaluation.getType().equals("2")) {//领导能力
					annualSumScore.setLdnlScore(getScore);
				} else if (annualEvaluation.getType().equals("3")) {//党的建设
					annualSumScore.setDdjsScore(getScore);
				} else if (annualEvaluation.getType().equals("4")) {//市直高质量发展
					annualSumScore.setSzgzlfzScore(getScore);
				} else if (annualEvaluation.getType().equals("5")) {//县区高质量发展
					annualSumScore.setXqgzlfzScore(getScore);
				}


				//TODO （政治思想建设 + 领导能力 + 党的建设 + 市直高质量发展 + 县区高质量发展 = 总分）
				// + 季度分数 + 加分项 - 减分项 + 领导评价得分
				//政治思想建设
				double zzsxjsSocre = annualSumScore.getZzsxjsScore() == null ? 0.0 : annualSumScore.getZzsxjsScore();
				//领导能力
				double ldnlSocre = annualSumScore.getLdnlScore() == null ? 0.0 : annualSumScore.getLdnlScore();
				//党的建设
				double ddjsSocre = annualSumScore.getDdjsScore() == null ? 0.0 : annualSumScore.getDdjsScore();
				//市直高质量发展
				double szgzlfzSocre = annualSumScore.getSzgzlfzScore() == null ? 0.0 : annualSumScore.getSzgzlfzScore();
				//县区高质量发展
				double xqgzlfzSocre = annualSumScore.getXqgzlfzScore() == null ? 0.0 : annualSumScore.getXqgzlfzScore();
				//季度分数
				//double jspjScore = annualSumScore.getJdpjScore() == null ? 0.0 : annualSumScore.getJdpjScore();
				//加分项
				//double addSocre = annualSumScore.getAddScore() == null ? 0.0 : annualSumScore.getAddScore();
				//减分项
				//double minusSocre = annualSumScore.getMinusScore() == null ? 0.0 : annualSumScore.getMinusScore();
				//领导评价得分
				//double leaderSocre = annualSumScore.getLeaderScore() == null ? 0.0 : annualSumScore.getLeaderScore();

				//计算总分
				double sumSocre = zzsxjsSocre + ldnlSocre + ddjsSocre + szgzlfzSocre + xqgzlfzSocre;// + jspjScore + addSocre + minusSocre + leaderSocre
				annualSumScore.setAnnualSumScore(Double.parseDouble(String.format("%.2f", sumSocre)));
				if (annualSumScoreList.size() > 0) {
					//如果当前记录存在就执行修改
					iAnnualSumScoreService.update(annualSumScore,annualSumScoreQueryWrapper);
					System.out.println("年度评价数据已存在，执行修改！");
					//保存当前评价记录
					AppriseDept appriseDept4 =new AppriseDept();
					appriseDept4.setResponsibleUnitId(Long.valueOf(annualSumScore.getResponsibleUnitId()));
					appriseDept4.setResponsibleUnitName(annualSumScore.getResponsibleUnitName());
					appriseDept4.setScore(String.valueOf(annualSumScore.getAnnualSumScore()));
					appriseDept4.setScoringDescription("修改总分：=" + annualSumScore.getAnnualSumScore());
					appriseDept4.setEvaluationType("1");
					appriseDept4.setEvaluationId(ab.getEvaluationId());
					appriseDept4.setType("2");//计算后得分
					appriseDept4.setAppriseBaseinfoId(ab.getId());
					appriseDept4.setCreateUserName(user.getRealName());
					appriseDept4.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept4);
				} else {
					//保存到年度评分表中
					iAnnualSumScoreService.save(annualSumScore);
					//保存当前评价记录
					AppriseDept appriseDept4 =new AppriseDept();
					appriseDept4.setResponsibleUnitId(Long.valueOf(annualSumScore.getResponsibleUnitId()));
					appriseDept4.setResponsibleUnitName(annualSumScore.getResponsibleUnitName());
					appriseDept4.setScore(String.valueOf(annualSumScore.getAnnualSumScore()));
					appriseDept4.setScoringDescription("保存总分：=" + annualSumScore.getAnnualSumScore());
					appriseDept4.setEvaluationType("1");
					appriseDept4.setEvaluationId(ab.getEvaluationId());
					appriseDept4.setType("2");//计算后得分
					appriseDept4.setAppriseBaseinfoId(ab.getId());
					appriseDept4.setCreateUserName(user.getRealName());
					appriseDept4.setCreateDeptName(deptName);
					iAppriseDeptService.save(appriseDept4);
				}

			}

			//TODO 如果未评价单位为0，就将是否评价的状态改成1
			if (notAppriseUserStr1.length == 0) {
				annualEvaluation.setIsAppraise(1);
			}
			iAnnualEvaluationService.updateById(annualEvaluation);

			String title = "新增年度评价指标评价";
			String businessId = String.valueOf(ab.getEvaluationId());
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		}
		return R.status(appriseBaseinfoService.updateApprise(ab));
	}

}
