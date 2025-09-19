package org.springblade.integrated.platform.controller;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
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
 * @author zrj
 * @version 1.0
 * @description:
 * @date 2022-04-09 12:04
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/annualAssessment")
@Api(value = "年度考评", tags = "年度考评控制层代码")
public class AnnualAssessmentController extends BladeController {

	private final IAnnualAssessmentService annualAssessmentService;

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
	public R save(@RequestBody AnnualAssessment ae) throws ParseException {
		boolean isok = annualAssessmentService.saveAnnualAssessment(ae);
		String title1 = "新增年度指标数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualAssessment";
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
	public R update(@Valid @RequestBody AnnualAssessment ae) {
		boolean isok = annualAssessmentService.uptAnnualAssessment(ae);
		String title1 = "修改年度指标数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualAssessment";
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
		boolean isok = annualAssessmentService.removeByIds(Func.toLongList(ids));
		String title1 = "逻辑删除年度指标数据";
		String businessId = String.valueOf(ids);
		String businessTable = "AnnualAssessment";
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
		/*if (idss.size() > 0) {
				//删除打分表
				LambdaQueryWrapper<AnnualSumScore> annualSumScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
				annualSumScoreLambdaQueryWrapper.in(AnnualSumScore::getAnnualAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(IAnnualSumScoreService.class).remove(annualSumScoreLambdaQueryWrapper);
				//删除指标申请表
				LambdaQueryWrapper<ApplyInformation> informationLambdaQueryWrapper = new LambdaQueryWrapper<>();
				informationLambdaQueryWrapper.in(ApplyInformation::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(IApplyInformationService.class).remove(informationLambdaQueryWrapper);
				//删除评价基本信息
				LambdaQueryWrapper<AppriseBaseinfo> baseinfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
				baseinfoLambdaQueryWrapper.in(AppriseBaseinfo::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(IAppriseBaseinfoService.class).remove(baseinfoLambdaQueryWrapper);
				//删除单位评价记录
				LambdaQueryWrapper<AppriseDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
				deptLambdaQueryWrapper.in(AppriseDept::getAssessmentId,Func.toLongList(ids));
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
				reminderRecordLambdaQueryWrapper.in(ReminderRecord::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(IReminderRecordService.class).remove(reminderRecordLambdaQueryWrapper);
				//删除汇报基本信息
				LambdaQueryWrapper<ReportsBaseinfo> reportsBaseinfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
				reportsBaseinfoLambdaQueryWrapper.in(ReportsBaseinfo::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(ReportsBaseinfoService.class).remove(reportsBaseinfoLambdaQueryWrapper);
				//删除汇报信息
				LambdaQueryWrapper<Reports> reportsLambdaQueryWrapper = new LambdaQueryWrapper<>();
				reportsLambdaQueryWrapper.in(Reports::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(IReportsService.class).remove(reportsLambdaQueryWrapper);
				//删除送审表信息
				LambdaQueryWrapper<SupervisionSubmitAudit> supervisionSubmitAuditLambdaQueryWrapper = new LambdaQueryWrapper<>();
				supervisionSubmitAuditLambdaQueryWrapper.in(SupervisionSubmitAudit::getReportId,Func.toLongList(ids));
				SpringUtil.getBean(ISupervisionSubmitAuditService.class).remove(supervisionSubmitAuditLambdaQueryWrapper);
				//删除任务文件表信息
				LambdaQueryWrapper<TaskFiles> taskFilesLambdaQueryWrapper = new LambdaQueryWrapper<>();
				taskFilesLambdaQueryWrapper.in(TaskFiles::getAssessmentId,Func.toLongList(ids));
				SpringUtil.getBean(ITaskFilesService.class).remove(taskFilesLambdaQueryWrapper);
				//删除消息表信息
				LambdaQueryWrapper<UnifyMessage> unifyMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
				unifyMessageLambdaQueryWrapper.in(UnifyMessage::getMsgId,Func.toLongList(ids));
				SpringUtil.getBean(IUnifyMessageService.class).remove(unifyMessageLambdaQueryWrapper);
				//删除日志表信息
				LambdaQueryWrapper<OperLog> operLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
				operLogLambdaQueryWrapper.in(OperLog::getBusinessId,Func.toStrList(ids));
				SpringUtil.getBean(IOperLogService.class).remove(operLogLambdaQueryWrapper);
		}*/




		return R.status(isok);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "年度评价列表查询", notes = "")
	public R<IPage<AnnualAssessment>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//sql查询条件
		Object type = entity.get("type");//指标分类字典值(字典编码：ndkp-type）
		Object appraiseClassify = entity.get("appraiseClassify");//考核分类
		Object majorTarget = entity.get("majorTarget");//主要指标及评价要点
		Object appraiseObjectId =  entity.get("appraiseObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object annualUserType = entity.get("annualUserType");
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		QueryWrapper<AnnualAssessment> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) type),"type",type);
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseClassify),"appraise_classify",appraiseClassify);
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) majorTarget),"major_target",majorTarget);
		//queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseObjectId),"appraise_object_id",appraiseObjectId);
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
		IPage<AnnualAssessment> pages = annualAssessmentService.page(Condition.getPage(query), queryWrapper);

		return R.data(pages);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "年度评价列表查询-app", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("年度评价列表查询-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			Map<String, Object> entity = new HashMap<>(jsonParams);
			User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

			//sql查询条件
			Object type = entity.get("type");//指标分类字典值(字典编码：ndkp-type）
			Object appraiseClassify = entity.get("appraiseClassify");//考核分类
			Object majorTarget = entity.get("majorTarget");//主要指标及评价要点
			Object appraiseObjectId =  entity.get("appraiseObjectId");//评价对象id
			Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
			Object annualUserType = entity.get("annualUserType");
			Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

			QueryWrapper<AnnualAssessment> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq(!StringUtil.isBlank((CharSequence) type),"type",type);
			queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseClassify),"appraise_classify",appraiseClassify);
			queryWrapper.eq(!StringUtil.isBlank((CharSequence) majorTarget),"major_target",majorTarget);
			//queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseObjectId),"appraise_object_id",appraiseObjectId);
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
			IPage<AnnualAssessment> pages = annualAssessmentService.page(Condition.getPage(query), queryWrapper);

			JSONObject pageJson = objectMapper.convertValue(pages, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 导出
	 * @param entity
	 * @param response
	 */
	@GetMapping("export")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "年度评价导出", notes = "传入annualAssessment")
	public void export(@ApiIgnore @RequestParam Map<String, Object> entity, HttpServletResponse response) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		//sql查询条件
		Object type = entity.get("type");//指标分类字典值(字典编码：ndkp-type）
		Object appraiseClassify = entity.get("appraiseClassify");//考核分类
		Object majorTarget = entity.get("majorTarget");//主要指标及评价要点
		Object appraiseObjectId =  entity.get("appraiseObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object annualUserType = entity.get("annualUserType");
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		QueryWrapper<AnnualAssessment> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(!StringUtil.isEmpty(type),"type",type);
		queryWrapper.eq(!StringUtil.isEmpty(appraiseClassify),"appraise_classify",appraiseClassify);
		queryWrapper.eq(!StringUtil.isEmpty(majorTarget),"major_target",majorTarget);
		//queryWrapper.eq(!StringUtil.isBlank((CharSequence) appraiseObjectId),"appraise_object_id",appraiseObjectId);

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
		List<AnnualAssessment> annualAssessments = annualAssessmentService.list(queryWrapper);

		if(type.equals("1")){
			List<ZzsxjsAssExcel> list = new ArrayList<>();
			if (annualAssessments.size() > 0) {
				for (int i = 0; i < annualAssessments.size(); i++) {
					ZzsxjsAssExcel zzsxjsAssExcel = new ZzsxjsAssExcel();
					zzsxjsAssExcel.setAppraiseClassifyName(annualAssessments.get(i).getAppraiseClassifyName());
					zzsxjsAssExcel.setMajorTarget(annualAssessments.get(i).getMajorTarget());
					zzsxjsAssExcel.setAppraiseObject(annualAssessments.get(i).getAppraiseObject());
					zzsxjsAssExcel.setAppraiseDeptname(annualAssessments.get(i).getAppraiseDeptname());
					zzsxjsAssExcel.setWeight(annualAssessments.get(i).getWeight());

					list.add(zzsxjsAssExcel);
				}
			}
			ExcelUtil.export(response, "政治思想建设-" + DateUtil.time(), "政治思想建设年度考评", list, ZzsxjsAssExcel.class);
		}else if (type.equals("2")) {
			List<LdnlAssExcel> list = new ArrayList<>();
			if (annualAssessments.size() > 0) {
				for (int i = 0; i < annualAssessments.size(); i++) {
					LdnlAssExcel ldnlAssExcel = new LdnlAssExcel();
					ldnlAssExcel.setAppraiseClassifyName(annualAssessments.get(i).getAppraiseClassifyName());
					ldnlAssExcel.setMajorTarget(annualAssessments.get(i).getMajorTarget());
					ldnlAssExcel.setAppraiseObject(annualAssessments.get(i).getAppraiseObject());
					ldnlAssExcel.setAppraiseDeptname(annualAssessments.get(i).getAppraiseDeptname());
					ldnlAssExcel.setWeight(annualAssessments.get(i).getWeight());

					list.add(ldnlAssExcel);
				}
			}
			ExcelUtil.export(response, "领导能力-" + DateUtil.time(), "领导能力年度考评", list, LdnlAssExcel.class);
		}else if (type.equals("3")) {
			List<DdjsAssExcel> list = new ArrayList<>();
			if (annualAssessments.size() > 0) {
				for (int i = 0; i < annualAssessments.size(); i++) {
					DdjsAssExcel ddjsAssExcel = new DdjsAssExcel();
					ddjsAssExcel.setAppraiseClassifyName(annualAssessments.get(i).getAppraiseClassifyName());
					ddjsAssExcel.setMajorTarget(annualAssessments.get(i).getMajorTarget());
					ddjsAssExcel.setAppraiseObject(annualAssessments.get(i).getAppraiseObject());
					ddjsAssExcel.setAppraiseDeptname(annualAssessments.get(i).getAppraiseDeptname());
					ddjsAssExcel.setWeight(annualAssessments.get(i).getWeight());


					list.add(ddjsAssExcel);
				}
			}
			ExcelUtil.export(response, "党的建设-" + DateUtil.time(), "党的建设年度考评", list, DdjsAssExcel.class);
		}else if (type.equals("4")) {
			List<SgzlfzAssExcel> list = new ArrayList<>();
			if (annualAssessments.size() > 0) {
				for (int i = 0; i < annualAssessments.size(); i++) {
					SgzlfzAssExcel sgzlfzAssExcel = new SgzlfzAssExcel();
					sgzlfzAssExcel.setProjectName(annualAssessments.get(i).getProjectName());
					sgzlfzAssExcel.setAppraiseClassifyName(annualAssessments.get(i).getAppraiseClassifyName());
					sgzlfzAssExcel.setMajorTarget(annualAssessments.get(i).getMajorTarget());
					sgzlfzAssExcel.setAppraiseObject(annualAssessments.get(i).getAppraiseObject());
					sgzlfzAssExcel.setAppraiseDeptname(annualAssessments.get(i).getAppraiseDeptname());
					sgzlfzAssExcel.setWeight(annualAssessments.get(i).getWeight());


					list.add(sgzlfzAssExcel);
				}
			}
			ExcelUtil.export(response, "市直高质量发展-" + DateUtil.time(), "市直高质量发展年度考评", list, SgzlfzAssExcel.class);
		}else if (type.equals("5")) {
			List<QxgzlfzAssExcel> list = new ArrayList<>();
			if (annualAssessments.size() > 0) {
				for (int i = 0; i < annualAssessments.size(); i++) {
					QxgzlfzAssExcel qxgzlfzAssExcel = new QxgzlfzAssExcel();
					qxgzlfzAssExcel.setAppraiseClassifyName(annualAssessments.get(i).getAppraiseClassifyName());
					qxgzlfzAssExcel.setProjectName(annualAssessments.get(i).getProjectName());
					qxgzlfzAssExcel.setMajorTarget(annualAssessments.get(i).getMajorTarget());
					qxgzlfzAssExcel.setAppraiseObject(annualAssessments.get(i).getAppraiseObject());
					qxgzlfzAssExcel.setAppraiseDeptname(annualAssessments.get(i).getAppraiseDeptname());
					qxgzlfzAssExcel.setGanzhouqu(annualAssessments.get(i).getGanzhouqu());
					qxgzlfzAssExcel.setLinzexian(annualAssessments.get(i).getLinzexian());
					qxgzlfzAssExcel.setGaotaixian(annualAssessments.get(i).getGaotaixian());
					qxgzlfzAssExcel.setShandanxian(annualAssessments.get(i).getShandanxian());
					qxgzlfzAssExcel.setMinlexian(annualAssessments.get(i).getMinlexian());
					qxgzlfzAssExcel.setSunanxian(annualAssessments.get(i).getSunanxian());


					list.add(qxgzlfzAssExcel);
				}
			}

			ExcelUtil.export(response, "区县高质量发展-" + DateUtil.time(), "区县高质量发展年度考评", list, QxgzlfzAssExcel.class);
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
			AnnualAssessmentImporter aeImporter = new AnnualAssessmentImporter(annualAssessmentService,type);
			ExcelUtil.save(file, aeImporter, AnnualAssessmentExcel.class);

		} catch (Exception ex) {
			throw ex;
		}
		return R.success("操作成功");
	}



	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<AnnualAssessment> details(@Valid @RequestParam Long id) {

		String title1 = "查看了考核评价-年度指标详情";
		String businessId = String.valueOf(id);
		String businessTable = "AnnualAssessment";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.data(annualAssessmentService.details(id));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/listForApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询", notes = "")
	public R<IPage<AnnualAssessment>> listForApp(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String appraiseObjectId = (String) entity.get("appraiseObjectId");
		String createTime =(String) entity.get("createTime");
		String majorTarget =(String) entity.get("majorTarget");
		//sql查询条件
		QueryWrapper<AnnualAssessment> queryWrapper = new QueryWrapper<AnnualAssessment>();
		queryWrapper.apply("FIND_IN_SET('"+appraiseObjectId+"',appraise_object_id)");

		queryWrapper.eq("type",entity.get("type"));
		if(StringUtils.isNotBlank(createTime)){
			queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
		}
		if(StringUtils.isNotBlank(majorTarget)){
			queryWrapper.like("major_target",majorTarget);
		}
		IPage<AnnualAssessment> pages = annualAssessmentService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}



	/**
	 * 修改阶段
	 */
	@PostMapping("/updateStage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateStage(@Valid @RequestBody AnnualAssessment ae) throws ParseException {
		boolean isok = annualAssessmentService.uptStage(ae);
		String title1 = "修改年度评价阶段数据";
		String businessId = String.valueOf(ae.getId());
		String businessTable = "AnnualAssessment";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}









}
