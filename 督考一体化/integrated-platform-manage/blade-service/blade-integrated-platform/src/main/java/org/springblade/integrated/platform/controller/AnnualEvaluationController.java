package org.springblade.integrated.platform.controller;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.injector.methods.DeleteById;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.*;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.*;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-09 12:04
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/annualEvaluation")
@Api(value = "年度考评", tags = "年度考评控制层代码")
public class AnnualEvaluationController extends BladeController {

	private final IAnnualEvaluationService annualEvaluationService;

	private final ReportsBaseinfoService reportsBaseinfoService;

	private final IFollowInformationService followInformationService;
	@Resource
	private final IUserClient userClient;

	private final IReportsService iReportsService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;

	private final ISupervisionPhaseReportService supervisionPhaseReportService;

	@Resource
	private IUnifyMessageService unifyMessageService;

	@Resource
	private ISysClient sysClient;

	@Resource
	private IUserSearchClient iUserSearchClient;

	private final IAnnualSumScoreService iAnnualSumScoreService;

	@Resource
	private IDictBizClient dictBizClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 *
	 * @param ae
	 * @return
	 * @throws ParseException
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@RequestBody AnnualEvaluation ae) throws ParseException {
		boolean isok = annualEvaluationService.saveAnnualEvaluation(ae);
		String title1 = "新增年度指标数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 修改
	 * @param ae
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody AnnualEvaluation ae) {
		boolean isok = annualEvaluationService.uptAnnualEvaluation(ae);
		String title1 = "修改年度指标数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam("ids") String ids) {
		boolean isok = annualEvaluationService.removeByIds(Func.toLongList(ids));
		String title1 = "逻辑删除年度指标数据";
		String businessId = String.valueOf(ids);
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.DELETE.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessId, businessTable, businessType);
		}

		//删除关联的所有信息
		List<Long> idss = Func.toLongList(ids);
		if (idss.size() > 0) {
				//删除打分表
				LambdaQueryWrapper<AnnualSumScore> annualSumScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
				annualSumScoreLambdaQueryWrapper.in(AnnualSumScore::getAnnualEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IAnnualSumScoreService.class).remove(annualSumScoreLambdaQueryWrapper);
				//删除指标申请表
				LambdaQueryWrapper<ApplyInformation> informationLambdaQueryWrapper = new LambdaQueryWrapper<>();
				informationLambdaQueryWrapper.in(ApplyInformation::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IApplyInformationService.class).remove(informationLambdaQueryWrapper);
				//删除评价基本信息
				LambdaQueryWrapper<AppriseBaseinfo> baseinfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
				baseinfoLambdaQueryWrapper.in(AppriseBaseinfo::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IAppriseBaseinfoService.class).remove(baseinfoLambdaQueryWrapper);
				//删除单位评价记录
				LambdaQueryWrapper<AppriseDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
				deptLambdaQueryWrapper.in(AppriseDept::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IAppriseDeptService.class).remove(deptLambdaQueryWrapper);
				//删除附件信息
				LambdaQueryWrapper<AppriseFiles> filesLambdaQueryWrapper = new LambdaQueryWrapper<>();
				filesLambdaQueryWrapper.in(AppriseFiles::getBusinessId,Func.toLongList(ids));
				SpringUtil.getBean(IAppriseFilesService.class).remove(filesLambdaQueryWrapper);
				//删除关注信息
				LambdaQueryWrapper<FollowInformation> followInformationLambdaQueryWrapper = new LambdaQueryWrapper<>();
				followInformationLambdaQueryWrapper.in(FollowInformation::getBusinessId,Func.toLongList(ids));
				SpringUtil.getBean(IFollowInformationService.class).remove(followInformationLambdaQueryWrapper);
				//删除批示留言信息
				LambdaQueryWrapper<MessageInformation> messageInformationLambdaQueryWrapper = new LambdaQueryWrapper<>();
				messageInformationLambdaQueryWrapper.in(MessageInformation::getBusinessId,Func.toLongList(ids));
				SpringUtil.getBean(IMessageInformationService.class).remove(messageInformationLambdaQueryWrapper);
				//删除催办信息
				LambdaQueryWrapper<ReminderRecord> reminderRecordLambdaQueryWrapper = new LambdaQueryWrapper<>();
				reminderRecordLambdaQueryWrapper.in(ReminderRecord::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IReminderRecordService.class).remove(reminderRecordLambdaQueryWrapper);
				//删除汇报基本信息
				LambdaQueryWrapper<ReportsBaseinfo> reportsBaseinfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
				reportsBaseinfoLambdaQueryWrapper.in(ReportsBaseinfo::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(ReportsBaseinfoService.class).remove(reportsBaseinfoLambdaQueryWrapper);
				//删除汇报信息
				LambdaQueryWrapper<Reports> reportsLambdaQueryWrapper = new LambdaQueryWrapper<>();
				reportsLambdaQueryWrapper.in(Reports::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IReportsService.class).remove(reportsLambdaQueryWrapper);
				//删除送审表信息
				LambdaQueryWrapper<SupervisionSubmitAudit> supervisionSubmitAuditLambdaQueryWrapper = new LambdaQueryWrapper<>();
				supervisionSubmitAuditLambdaQueryWrapper.in(SupervisionSubmitAudit::getReportId,Func.toLongList(ids));
				SpringUtil.getBean(ISupervisionSubmitAuditService.class).remove(supervisionSubmitAuditLambdaQueryWrapper);
				//删除任务文件表信息
				LambdaQueryWrapper<TaskFiles> taskFilesLambdaQueryWrapper = new LambdaQueryWrapper<>();
				taskFilesLambdaQueryWrapper.in(TaskFiles::getEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(ITaskFilesService.class).remove(taskFilesLambdaQueryWrapper);
				//删除消息表信息
				LambdaQueryWrapper<UnifyMessage> unifyMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
				unifyMessageLambdaQueryWrapper.in(UnifyMessage::getMsgId,Func.toLongList(ids));
				SpringUtil.getBean(IUnifyMessageService.class).remove(unifyMessageLambdaQueryWrapper);
				//删除日志表信息
				LambdaQueryWrapper<OperLog> operLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
				operLogLambdaQueryWrapper.in(OperLog::getBusinessId,Func.toStrList(ids));
				SpringUtil.getBean(IOperLogService.class).remove(operLogLambdaQueryWrapper);
		}




		return R.status(isok);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "年度评价列表查询", notes = "")
	public R<IPage<AnnualEvaluation>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//sql查询条件
		Object type = entity.get("type");//指标分类字典值(字典编码：ndkp-type）
		Object appraiseClassify = entity.get("appraiseClassify");//考核分类
		Object majorTarget = entity.get("majorTarget");//主要指标及评价要点
		Object appraiseObjectId =  entity.get("appraiseObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object targetStatus =  entity.get("targetStatus");//指标状态：0暂存 1推进中 2已完成 3申请办结 4申请中止 5已中止（字典编码zb_status）
		Object annualUserType = entity.get("annualUserType");
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) type),"type",type);
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseClassify),"appraise_classify",appraiseClassify);
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) majorTarget),"major_target",majorTarget);
		//queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseObjectId),"appraise_object_id",appraiseObjectId);
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) targetStatus),"target_status",targetStatus);
		queryWrapper.like(!StringUtil.isBlank((CharSequence) quarterlyYear),"create_time",quarterlyYear);
		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = currentUser.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		//如果用户不是四大班子领导，那就查看自己部门相关的信息
		if (isok) {
			queryWrapper.and(Wrappers -> Wrappers
				.eq(StringUtils.isNotNull(currentUser.getId()),"create_user",currentUser.getId().toString())
				.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),"create_dept",currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),"appraise_object_id",currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),"appraise_deptid",currentUser.getDeptId())
			);
		}


		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (annualUserType!=null && annualUserType.equals("1")) {//如果是领导关注

			if (currentUser!=null) {// && !"1123598817738675202".equals(currentUser.getPostId())
				if (StringUtil.isBlank(currentUser.getDeptId())) {
					return R.fail("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
				List<String> leaderIds1 = new ArrayList<>();
				for (User leader : leaders) {
					leaderIds1.add(leader.getId().toString());
				}
				String[] leaderIds = leaderIds1.toString()
					.replace("[","")
					.replace("]","")
					.replace(" ","")
					.split(",");
				followInformationQueryWrapper.in("follow_user_id", leaderIds);
			}
		}else if(annualUserType!=null && annualUserType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","2");//2是年度指标，4是季度指标

		String [] tabId = null;
		if (annualUserType!=null) {
			if ("1".equals(annualUserType) || "2".equals(annualUserType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() >0 ) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in("id", tabId);
				} else {
					queryWrapper.in("id", "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) appraiseObjectId)){
			queryWrapper.apply("FIND_IN_SET('"+appraiseObjectId+"',appraise_object_id)");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("FIND_IN_SET('"+appraiseDeptid+"',appraise_deptid)");
		}
		queryWrapper.orderByDesc("create_Time");
		IPage<AnnualEvaluation> pages = annualEvaluationService.page(Condition.getPage(query), queryWrapper);
		//根据未评价人员NotAppriseUser判断是否评价
		List<AnnualEvaluation> list = pages.getRecords();
		String deptName = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		for (int i = 0; i < list.size(); i++) {
			//TODO 查出当前指标的单位是否汇报，用于控制列表的【汇报】按钮显示
			LambdaQueryWrapper<Reports> lambdaQueryWrapper = Wrappers.<Reports>query().lambda()
				.eq(Reports::getEvaluationId,list.get(i).getId())
				.eq(Reports::getDeptId,currentUser.getDeptId());
			Reports reports = iReportsService.getOne(lambdaQueryWrapper);
			if (reports != null) {
				if (StringUtils.isNotNull(reports.getReportStatus())&& "1,2,3".contains(reports.getReportStatus())) {
					list.get(i).setIsHb("1");
				} else {
					list.get(i).setIsHb("0");
				}
			} else {
				list.get(i).setIsHb("0");
			}
			//TODO 控制列表的【评价】按钮是否显示
			if (list.get(i).getAppraiseDeptname().contains(deptName)) {
				if (StringUtils.isNotBlank(list.get(i).getNotAppriseUser())) {
					if (list.get(i).getNotAppriseUser().contains(deptName)) {
						list.get(i).setIsAppraise(0);
					} else {
						list.get(i).setIsAppraise(1);
					}
				}
			}
			//TODO 控制列表的【修改评价】按钮是否显示
			List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService
				.list(
					Wrappers.<AnnualSumScore>query().lambda()
					.eq(AnnualSumScore::getAnnualEvaluationId,list.get(i).getId())
					.eq(AnnualSumScore::getIsSend,"1")
				);
			if (annualSumScoreList.size() > 0) {
				list.get(i).setIsSend("1");
			} else {
				list.get(i).setIsSend("0");
			}
		}
		return R.data(pages);
	}

	/**
	 * 导出
	 * @param entity
	 * @param response
	 */
	@GetMapping("export")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "年度评价导出", notes = "传入annualEvaluation")
	public void export(@ApiIgnore @RequestParam Map<String, Object> entity, HttpServletResponse response) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		//sql查询条件
		Object type = entity.get("type");//指标分类字典值(字典编码：ndkp-type）
		Object appraiseClassify = entity.get("appraiseClassify");//考核分类
		Object majorTarget = entity.get("majorTarget");//主要指标及评价要点
		Object appraiseObjectId =  entity.get("appraiseObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object targetStatus =  entity.get("targetStatus");//指标状态：0暂存 1推进中 2已完成 3申请办结 4申请中止 5已中止（字典编码zb_status）
		Object annualUserType = entity.get("annualUserType");
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(!StringUtil.isEmpty(type),"type",type);
		queryWrapper.eq(!StringUtil.isEmpty(appraiseClassify),"appraise_classify",appraiseClassify);
		queryWrapper.eq(!StringUtil.isEmpty(majorTarget),"major_target",majorTarget);
		//queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseObjectId),"appraise_object_id",appraiseObjectId);
		queryWrapper.eq(!StringUtil.isEmpty(targetStatus),"target_status",targetStatus);
		queryWrapper.like(!StringUtil.isEmpty(quarterlyYear),"create_time",quarterlyYear);
		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = currentUser.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		//如果用户不是四大班子领导，那就查看自己部门相关的信息
		if (isok) {
			queryWrapper.and(Wrappers -> Wrappers
				.eq(StringUtils.isNotNull(currentUser.getId()),"create_user",currentUser.getId().toString())
				.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),"create_dept",currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),"appraise_object_id",currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),"appraise_deptid",currentUser.getDeptId())
			);
		}


		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (annualUserType!=null && annualUserType.equals("1")) {//如果是领导关注

			if (currentUser!=null) {// && !"1123598817738675202".equals(currentUser.getPostId())
				if (StringUtil.isBlank(currentUser.getDeptId())) {
					throw new RuntimeException("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
				List<String> leaderIds1 = new ArrayList<>();
				for (User leader : leaders) {
					leaderIds1.add(leader.getId().toString());
				}
				String[] leaderIds = leaderIds1.toString()
					.replace("[","")
					.replace("]","")
					.replace(" ","")
					.split(",");
				followInformationQueryWrapper.in("follow_user_id", leaderIds);
			}
		}else if(annualUserType!=null && annualUserType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","2");

		String [] tabId = null;
		if (annualUserType!=null) {
			if ("1".equals(annualUserType) || "2".equals(annualUserType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() >0 ) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in("id", tabId);
				} else {
					queryWrapper.in("id", "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) appraiseObjectId)){
			queryWrapper.apply("FIND_IN_SET('"+appraiseObjectId+"',appraise_object_id)");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("FIND_IN_SET('"+appraiseDeptid+"',appraise_deptid)");
		}
		queryWrapper.orderByDesc("create_Time");
		List<AnnualEvaluation> annualEvaluations = annualEvaluationService.list(queryWrapper);

		if(type.equals("1")){
			List<ZzsxjsExcel> list = new ArrayList<>();
			if (annualEvaluations.size() > 0) {
				for (int i = 0; i < annualEvaluations.size(); i++) {
					ZzsxjsExcel zzsxjsExcel = new ZzsxjsExcel();
					zzsxjsExcel.setAppraiseClassifyName(annualEvaluations.get(i).getAppraiseClassifyName());
					zzsxjsExcel.setMajorTarget(annualEvaluations.get(i).getMajorTarget());
					zzsxjsExcel.setAppraiseObject(annualEvaluations.get(i).getAppraiseObject());
					zzsxjsExcel.setAppraiseDeptname(annualEvaluations.get(i).getAppraiseDeptname());
					zzsxjsExcel.setWeight(annualEvaluations.get(i).getWeight());
					zzsxjsExcel.setFinishDate(annualEvaluations.get(i).getFinishDate());
					if (annualEvaluations.get(i).getTargetStatus().equals("0")) {
						zzsxjsExcel.setTargetStatus("暂存");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("1")) {
						zzsxjsExcel.setTargetStatus("推进中");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("2")) {
						zzsxjsExcel.setTargetStatus("已完成");
					}
					list.add(zzsxjsExcel);
				}
			}
			ExcelUtil.export(response, "政治思想建设-" + DateUtil.time(), "政治思想建设年度考评", list, ZzsxjsExcel.class);
		}else if (type.equals("2")) {
			List<LdnlExcel> list = new ArrayList<>();
			if (annualEvaluations.size() > 0) {
				for (int i = 0; i < annualEvaluations.size(); i++) {
					LdnlExcel ldnlExcel = new LdnlExcel();
					ldnlExcel.setAppraiseClassifyName(annualEvaluations.get(i).getAppraiseClassifyName());
					ldnlExcel.setMajorTarget(annualEvaluations.get(i).getMajorTarget());
					ldnlExcel.setAppraiseObject(annualEvaluations.get(i).getAppraiseObject());
					ldnlExcel.setAppraiseDeptname(annualEvaluations.get(i).getAppraiseDeptname());
					ldnlExcel.setWeight(annualEvaluations.get(i).getWeight());
					ldnlExcel.setFinishDate(annualEvaluations.get(i).getFinishDate());
					if (annualEvaluations.get(i).getTargetStatus().equals("0")) {
						ldnlExcel.setTargetStatus("暂存");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("1")) {
						ldnlExcel.setTargetStatus("推进中");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("2")) {
						ldnlExcel.setTargetStatus("已完成");
					}
					list.add(ldnlExcel);
				}
			}
			ExcelUtil.export(response, "领导能力-" + DateUtil.time(), "领导能力年度考评", list, LdnlExcel.class);
		}else if (type.equals("3")) {
			List<DdjsExcel> list = new ArrayList<>();
			if (annualEvaluations.size() > 0) {
				for (int i = 0; i < annualEvaluations.size(); i++) {
					DdjsExcel ddjsExcel = new DdjsExcel();
					ddjsExcel.setAppraiseClassifyName(annualEvaluations.get(i).getAppraiseClassifyName());
					ddjsExcel.setMajorTarget(annualEvaluations.get(i).getMajorTarget());
					ddjsExcel.setAppraiseObject(annualEvaluations.get(i).getAppraiseObject());
					ddjsExcel.setAppraiseDeptname(annualEvaluations.get(i).getAppraiseDeptname());
					ddjsExcel.setWeight(annualEvaluations.get(i).getWeight());
					ddjsExcel.setFinishDate(annualEvaluations.get(i).getFinishDate());
					if (annualEvaluations.get(i).getTargetStatus().equals("0")) {
						ddjsExcel.setTargetStatus("暂存");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("1")) {
						ddjsExcel.setTargetStatus("推进中");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("2")) {
						ddjsExcel.setTargetStatus("已完成");
					}
					list.add(ddjsExcel);
				}
			}
			ExcelUtil.export(response, "党的建设-" + DateUtil.time(), "党的建设年度考评", list, DdjsExcel.class);
		}else if (type.equals("4")) {
			List<SgzlfzExcel> list = new ArrayList<>();
			if (annualEvaluations.size() > 0) {
				for (int i = 0; i < annualEvaluations.size(); i++) {
					SgzlfzExcel sgzlfzExcel = new SgzlfzExcel();
					sgzlfzExcel.setProjectName(annualEvaluations.get(i).getProjectName());
					sgzlfzExcel.setAppraiseClassifyName(annualEvaluations.get(i).getAppraiseClassifyName());
					sgzlfzExcel.setMajorTarget(annualEvaluations.get(i).getMajorTarget());
					sgzlfzExcel.setAppraiseObject(annualEvaluations.get(i).getAppraiseObject());
					sgzlfzExcel.setAppraiseDeptname(annualEvaluations.get(i).getAppraiseDeptname());
					sgzlfzExcel.setWeight(annualEvaluations.get(i).getWeight());
					sgzlfzExcel.setFinishDate(annualEvaluations.get(i).getFinishDate());
					if (annualEvaluations.get(i).getTargetStatus().equals("0")) {
						sgzlfzExcel.setTargetStatus("暂存");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("1")) {
						sgzlfzExcel.setTargetStatus("推进中");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("2")) {
						sgzlfzExcel.setTargetStatus("已完成");
					}
					list.add(sgzlfzExcel);
				}
			}
			ExcelUtil.export(response, "市直高质量发展-" + DateUtil.time(), "市直高质量发展年度考评", list, SgzlfzExcel.class);
		}else if (type.equals("5")) {
			List<QxgzlfzExcel> list = new ArrayList<>();
			if (annualEvaluations.size() > 0) {
				for (int i = 0; i < annualEvaluations.size(); i++) {
					QxgzlfzExcel qxgzlfzExcel = new QxgzlfzExcel();
					qxgzlfzExcel.setAppraiseClassifyName(annualEvaluations.get(i).getAppraiseClassifyName());
					qxgzlfzExcel.setProjectName(annualEvaluations.get(i).getProjectName());
					qxgzlfzExcel.setMajorTarget(annualEvaluations.get(i).getMajorTarget());
					qxgzlfzExcel.setAppraiseObject(annualEvaluations.get(i).getAppraiseObject());
					qxgzlfzExcel.setAppraiseDeptname(annualEvaluations.get(i).getAppraiseDeptname());
					qxgzlfzExcel.setGanzhouqu(annualEvaluations.get(i).getGanzhouqu());
					qxgzlfzExcel.setLinzexian(annualEvaluations.get(i).getLinzexian());
					qxgzlfzExcel.setGaotaixian(annualEvaluations.get(i).getGaotaixian());
					qxgzlfzExcel.setShandanxian(annualEvaluations.get(i).getShandanxian());
					qxgzlfzExcel.setMinlexian(annualEvaluations.get(i).getMinlexian());
					qxgzlfzExcel.setSunanxian(annualEvaluations.get(i).getSunanxian());
					qxgzlfzExcel.setFinishDate(annualEvaluations.get(i).getFinishDate());
					if (annualEvaluations.get(i).getTargetStatus().equals("0")) {
						qxgzlfzExcel.setTargetStatus("暂存");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("1")) {
						qxgzlfzExcel.setTargetStatus("推进中");
					} else if (annualEvaluations.get(i).getTargetStatus().equals("2")) {
						qxgzlfzExcel.setTargetStatus("已完成");
					}
					list.add(qxgzlfzExcel);
				}
			}

			ExcelUtil.export(response, "区县高质量发展-" + DateUtil.time(), "区县高质量发展年度考评", list, QxgzlfzExcel.class);
		}

	}


	/**
	 * 导入excel
	 */
	@PostMapping("importExcel")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "导入年度评价", notes = "传入excel")
	public R importExcel(MultipartFile file,String type) throws IOException {
		try {
			AnnualEvaluationImporter aeImporter = new AnnualEvaluationImporter(annualEvaluationService,type);
			ExcelUtil.save(file, aeImporter, AnnualEvaluationExcel.class);

		} catch (Exception ex) {
			throw ex;
		}
		return R.success("操作成功");
	}

	/**
	 * 下发
	 * @param ids
	 * @return
	 */
	@GetMapping("/issue")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "列表下发", notes = "传入id")
	public R issue(@ApiParam(value = "主键", required = true) @RequestParam("id") String ids) {
		List<AnnualEvaluation> ae = annualEvaluationService.listByIds(Func.toLongList(ids));
		if(ae.size() > 0) {
			for (int j = 0; j < ae.size(); j++) {
				//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
				reportsBaseinfoService.saveForAnnual(ae.get(j));

				//发送消息
				String receiver="";
				String appraiseObjectIds= ae.get(j).getAppraiseObjectId();//评价对象ids
				R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
				String glyId=rgly.getData();
				if(StringUtils.isNotBlank(appraiseObjectIds)){
					String[] idss = appraiseObjectIds.split(",");
					for(int i=0;i<idss.length;i++){
						R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,idss[i]);//获取单位下面所有管理员用户
						if(ruser!=null){
							List<User> userList = ruser.getData();
							for(User user : userList){
								receiver+=user.getId()+",";
							}
						}
					}
					//发送消息
					String msgSubmit = dictBizClient.getValue("ndkp-type",ae.get(j).getType()).getData();
					BladeUser user = AuthUtil.getUser();
					String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
					String msgIntro = "";
					if (ae.get(j).getMajorTarget() != null && ae.get(j).getMajorTarget()!="") {
						msgIntro = "【"+deptName+"】下发了年度评价指标："+ae.get(j).getMajorTarget();
					}else{
						msgIntro = "【"+deptName+"】下发了年度评价指标。";
					}
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(ae.get(j).getId());//消息主键（业务主键）
					unifyMessage.setMsgTitle("年度评价下发");//消息标题
					unifyMessage.setMsgType("8");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(receiver);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem(msgSubmit);//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);

					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
					unifyMessage.setTwoLevelType("19");//年度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
				LambdaUpdateWrapper<AnnualEvaluation> lambdaUpdateWrapper = Wrappers.<AnnualEvaluation>update().lambda()
					.set(AnnualEvaluation::getTargetStatus,"1")
					.eq(AnnualEvaluation::getId,ae.get(j).getId().toString());
				boolean isok =annualEvaluationService.update(lambdaUpdateWrapper);
				String title1 = "下发考核评价-年度指标信息";
				String businessId = String.valueOf(ae.get(j).getId());
				String businessTable = "AnnualEvaluation";
				int businessType = BusinessType.UPDATE.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);
			}
			return R.status(true);
		}else{
			return R.status(false);
		}
	}

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<AnnualEvaluation> details(@Valid @RequestParam Long id) {

		String title1 = "查看了考核评价-年度指标详情";
		String businessId = String.valueOf(id);
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.data(annualEvaluationService.details(id));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/listForApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询", notes = "")
	public R<IPage<AnnualEvaluation>> listForApp(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String appraiseObjectId = (String) entity.get("appraiseObjectId");
		String targetStatus= (String) entity.get("targetStatus");
		String createTime =(String) entity.get("createTime");
		String majorTarget =(String) entity.get("majorTarget");
		//sql查询条件
		QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<AnnualEvaluation>();
		queryWrapper.apply("FIND_IN_SET('"+appraiseObjectId+"',appraise_object_id)");
		if(targetStatus.equals("1")){
			queryWrapper.apply("target_Status in ('1','3','4')");
		}else if(targetStatus.equals("2")){
			queryWrapper.apply("target_Status in ('2','5')");
		}else if(targetStatus.equals("0")){
			queryWrapper.eq("target_Status",targetStatus);
		}
		queryWrapper.eq("type",entity.get("type"));
		if(StringUtils.isNotBlank(createTime)){
			queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
		}
		if(StringUtils.isNotBlank(majorTarget)){
			queryWrapper.like("major_target",majorTarget);
		}
		IPage<AnnualEvaluation> pages = annualEvaluationService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}

	/**
	 * 分页查询
	 */
	@GetMapping("/listForApplication")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询-app", notes = "")
	public R listForApplication(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("APP年度评价列表查询-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			Map<String, Object> entity = new HashMap<>(jsonParams);
			String appraiseObjectId = (String) entity.get("appraiseObjectId");
			String targetStatus= (String) entity.get("targetStatus");
			String createTime =(String) entity.get("createTime");
			String majorTarget =(String) entity.get("majorTarget");
			//sql查询条件
			QueryWrapper<AnnualEvaluation> queryWrapper = new QueryWrapper<AnnualEvaluation>();
			queryWrapper.apply("FIND_IN_SET('"+appraiseObjectId+"',appraise_object_id)");
			if(targetStatus.equals("1")){
				queryWrapper.apply("target_Status in ('1','3','4')");
			}else if(targetStatus.equals("2")){
				queryWrapper.apply("target_Status in ('2','5')");
			}else if(targetStatus.equals("0")){
				queryWrapper.eq("target_Status",targetStatus);
			}
			queryWrapper.eq("type",entity.get("type"));
			if(StringUtils.isNotBlank(createTime)){
				queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
			}
			if(StringUtils.isNotBlank(majorTarget)){
				queryWrapper.like("major_target",majorTarget);
			}
			IPage<AnnualEvaluation> pages = annualEvaluationService.page(Condition.getPage(query), queryWrapper);
			JSONObject pageJson = objectMapper.convertValue(pages, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 事项办结
	 * @param ids
	 * @return
	 */
	@PostMapping("/servComplete")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "事项办结", notes = "传入id")
	public R servComplete(@ApiParam(value = "主键", required = true) @RequestParam("ids") String ids,@RequestParam("type") String type) {
		String[] idstr = ids.split(",");
		boolean flag =false;
		for(String id : idstr){
			AnnualEvaluation ae = annualEvaluationService.getById(id);
			ae.setTargetStatus(type);//指标状态：0暂存 1推进中 2已完成 3申请办结 4申请中止 5 已中止（字典编码zb_status）
			flag=annualEvaluationService.updateById(ae);
		}
		String title1 = "办结了考核评价-年度指标";
		String businessId = String.valueOf(ids);
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(flag);
	}

	/**
	 * 修改阶段
	 */
	@PostMapping("/updateStage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateStage(@Valid @RequestBody AnnualEvaluation ae) throws ParseException {
		boolean isok = annualEvaluationService.uptStage(ae);
		String title1 = "修改年度评价阶段数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualEvaluation";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}


	/**
	 * 汇报-送审
	 * @return
	 */
	@PostMapping("/submitAudit")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "汇报-送审", notes = "汇报-送审")
	@Transactional
	public R submitAudit(@RequestBody AnnualEvaluation annualEvaluation){

		try {
			if (StringUtils.isNull(annualEvaluation.getReportId())) {
				return R.fail("年度汇报送审-汇报id为空");
			}
			if (StringUtils.isNull(annualEvaluation.getTitle())) {
				return R.fail("年度汇报送审-送审标题为空");
			}
			if (StringUtils.isNull(annualEvaluation.getUserIds())) {
				return R.fail("年度汇报送审-接收人id为空");
			}
			if (StringUtils.isNull(annualEvaluation.getSync())) {
				return R.fail("年度汇报送审-是否异步为空");
			}
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(annualEvaluation.getReportId()),annualEvaluation.getTitle(),annualEvaluation.getUserIds(),annualEvaluation.getSync(), StatusConstant.OPERATION_TYPE_ANNUALAPPRISEHB);

			//发送消息
			AnnualEvaluation ae = annualEvaluationService.getById(annualEvaluation.getId());
			ae.setTargetStatus("7");//汇报送审
			annualEvaluationService.updateById(ae);

			String msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】送审了年度评价指标："+ae.getMajorTarget();

			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(annualEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("年度汇报送审");//消息标题
			unifyMessage.setMsgType("10");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(annualEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("21");//年度汇报送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title1 = "送审年度评价阶段数据-汇报";
			String businessId = String.valueOf(ae.getId());
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}


	/**
	 * 改分-送审
	 * @return
	 */
	@PostMapping("/submitAuditScore")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "改分-送审", notes = "改分-送审")
	@Transactional
	public R submitAuditScore(@RequestBody AnnualEvaluation annualEvaluation){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(annualEvaluation.getId()),annualEvaluation.getTitle(),annualEvaluation.getUserIds(),annualEvaluation.getSync(), StatusConstant.OPERATION_TYPE_ANNUALAPPRISESCORE);

			//发送消息
			AnnualEvaluation ae = annualEvaluationService.getById(annualEvaluation.getId());
			String msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】送审了年度评价改分申请："+ae.getMajorTarget();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(annualEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("年度评价改分申请");//消息标题
			unifyMessage.setMsgType("27");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(annualEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("26");//年度改分送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title1 = "送审年度评价阶段数据-改分";
			String businessId = String.valueOf(ae.getId());
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}
	/**
	 * 下发-送审
	 * @param annualEvaluation		指标id主键,送审标题,用户主键多个逗号隔开,同步还是异步 1同步；0异步
	 * @return
	 */
	@PostMapping("/submitAuditXf")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "下发-送审", notes = "下发-送审")
	public R submitAuditXf(@RequestBody AnnualEvaluation annualEvaluation){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(annualEvaluation.getId()),annualEvaluation.getTitle(),annualEvaluation.getUserIds(),annualEvaluation.getSync(), StatusConstant.OPERATION_TYPE_ANNUALAPPRISEXF);

			//发送消息
			AnnualEvaluation ae = annualEvaluationService.getById(annualEvaluation.getId());
			ae.setTargetStatus("6");//下发送审
			annualEvaluationService.updateById(ae);

			String msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】提交了年度评价下发送审："+ae.getMajorTarget();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(annualEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("年度评价下发送审");//消息标题
			unifyMessage.setMsgType("28");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(annualEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("27");//年度下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title1 = "送审年度评价阶段数据-下发";
			String businessId = String.valueOf(ae.getId());
			String businessTable = "AnnualEvaluation";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}




}
