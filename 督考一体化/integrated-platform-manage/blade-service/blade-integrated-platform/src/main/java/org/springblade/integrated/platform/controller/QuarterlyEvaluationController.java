package org.springblade.integrated.platform.controller;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vingsoft.entity.*;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.common.utils.file.FileUploadUtils;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Watchable;
import java.util.*;
import java.text.ParseException;


/**
 * 考核评价-季度评价 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/9 17:50
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/QuarterlyEvaluation")
//@Api(value = "季度评价", tags = "季度评价控制层代码")
@Api(value = "综合评价", tags = "综合评价控制层代码")
public class QuarterlyEvaluationController extends BladeController {

	@Resource
	private IQuarterlyEvaluationService iQuarterlyEvaluationService;
	@Resource
	private ReportsBaseinfoService reportsBaseinfoService;
	private final IFollowInformationService followInformationService;
	@Resource
	private final IUserClient userClient;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	private final IReportsService iReportsService;
	@Resource
	private IUnifyMessageService unifyMessageService;
	private IQuarterlySumScoreService iQuarterlySumScoreService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserSearchClient iUserSearchClient;
	@Resource
	private IDictBizClient dictBizClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 公共方法--文件下载（传入文件名，例如：党风廉政季度评价.xls）
	 */
	@GetMapping("downFiles")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "公共方法--文件下载", notes = "公共方法--文件下载")
	public void downRegion(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String fileName = request.getParameter("fileName").toString();
		//文件父路径 /khpjExcel/annualAPPrise
		String filePath = request.getParameter("filePath").toString();
		if (fileName.length() == 0) {
			System.out.println("文件名称不能为空！！");
		}

		String parentPathName = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
		System.out.println("-------=========-------========"+parentPathName);
		try {
			FileUploadUtils.downPrintFile(parentPathName + filePath,fileName,request,response);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		//return R.success("操作成功");
	}


	/**
	 * 导入-季度评价数据
	 */
	@PostMapping("import-QuarterlyEvaluation")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "导入综合评价", notes = "传入excel")
//	@ApiOperation(value = "导入季度评价", notes = "传入excel")
	public R importRegion(MultipartFile file) throws IOException {
		String type = "";
		String name = "";
		Long businessId=0L;
		String businessTable="";
		if (Objects.requireNonNull(file.getOriginalFilename()).contains("党建工作")) {
			type = "1";
			name = "党建工作";
		} else if (file.getOriginalFilename().contains("工作实绩")) {
			type = "2";
			name = "工作实绩";
		} else if (file.getOriginalFilename().contains("党风廉政")) {
			type = "3";
			name = "党风廉政";
		}else if (file.getOriginalFilename().contains("三抓三促")) {
			type = "4";
			name = "三抓三促";
		}
		try {
			QuarterlyEvaluationImporter regionImporter = new QuarterlyEvaluationImporter(iQuarterlyEvaluationService,type,name);
			ExcelUtil.save(file, regionImporter, QuarterlyEvaluationExcel.class);
		} catch (Exception ex) {
			throw ex;
		}

		return R.success("操作成功！");
	}



	/**
     * 导出
	 * @param entity
     * @param response
	 *
	 */
	@GetMapping("export-quarterlyEvaluation")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "综合评价-导出", notes = "传入quarterlyEvaluation")
//	@ApiOperation(value = "季度评价-导出", notes = "传入quarterlyEvaluation")
	public void exportUser(@ApiIgnore @RequestParam Map<String, Object> entity, HttpServletResponse response) {
		Object quarterlyType = entity.get("quarterlyType");
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//sql查询条件
		Object jdzbType = entity.get("jdzbType");//季度指标类型
		Object checkClassify = entity.get("checkClassify");//考核分类
		Object checkObjectId = entity.get("checkObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object targetStatus = entity.get("targetStatus");//指标状态：0暂存 1推进中 2已完成
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		LambdaQueryWrapper<QuarterlyEvaluation> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper
			.eq(!StringUtil.isEmpty(checkClassify),QuarterlyEvaluation::getCheckClassify,checkClassify)
			.eq(!StringUtil.isEmpty(checkObjectId),QuarterlyEvaluation::getCheckObjectId,checkObjectId)
			.eq(!StringUtil.isEmpty(appraiseDeptid),QuarterlyEvaluation::getAppraiseDeptid,appraiseDeptid)
			.eq(!StringUtil.isEmpty(targetStatus),QuarterlyEvaluation::getTargetStatus,targetStatus)
			.like(!StringUtil.isEmpty(quarterlyYear.toString()),QuarterlyEvaluation::getCreateTime,quarterlyYear.toString());
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
				.eq(StringUtils.isNotNull(currentUser.getId()),QuarterlyEvaluation::getCreateUser,currentUser.getId().toString())
				.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getCreateDept,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getCheckObjectId,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getAppraiseDeptid,currentUser.getDeptId())
			);
		}
		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (quarterlyType!=null && quarterlyType.equals("1")) {//如果是领导关注

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
		}else if(quarterlyType!=null && quarterlyType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","4");

		String [] tabId = null;
		if (quarterlyType!=null) {
			if ("1".equals(quarterlyType) || "2".equals(quarterlyType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() > 0) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in(QuarterlyEvaluation::getId, tabId);
				} else {
					queryWrapper.in(QuarterlyEvaluation::getId, "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) checkObjectId)){
			queryWrapper.apply("locate('"+checkObjectId+",',concat(check_object_id,',')) >0");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("locate('"+appraiseDeptid+",',concat(appraise_deptid,',')) >0");
		}

		queryWrapper.eq(!StringUtil.isBlank((CharSequence) jdzbType),QuarterlyEvaluation::getJdzbType,jdzbType);
		queryWrapper.orderByDesc(QuarterlyEvaluation::getCreateTime);
		List<QuarterlyEvaluation> quarterlyEvaluationList = iQuarterlyEvaluationService.list(queryWrapper);

		if ("1".equals(jdzbType)) {
			List<QuarterlyEvaluationExcel1> list = new ArrayList<>();
			if (quarterlyEvaluationList.size() > 0) {
				for (int i = 0; i < quarterlyEvaluationList.size(); i++) {
					QuarterlyEvaluationExcel1 qqq = new QuarterlyEvaluationExcel1();
					qqq.setCheckClassifyName(quarterlyEvaluationList.get(i).getCheckClassifyName());
					qqq.setMajorTarget(quarterlyEvaluationList.get(i).getMajorTarget());
					qqq.setCheckObject(quarterlyEvaluationList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyEvaluationList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyEvaluationList.get(i).getWeight());
					qqq.setFinishDate(quarterlyEvaluationList.get(i).getFinishDate());
					if (quarterlyEvaluationList.get(i).getTargetStatus().equals("0")) {
						qqq.setTargetStatus("暂存");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("1")) {
						qqq.setTargetStatus("推进中");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("2")) {
						qqq.setTargetStatus("已完成");
					}
					list.add(qqq);
				}
			}

			ExcelUtil.export(response, "党建工作-" + DateUtil.time(), "党建工作", list, QuarterlyEvaluationExcel1.class);
		}else if ("2".equals(jdzbType)) {
			List<QuarterlyEvaluationExcel2> list = new ArrayList<>();
			if (quarterlyEvaluationList.size() > 0) {
				for (int i = 0; i < quarterlyEvaluationList.size(); i++) {
					QuarterlyEvaluationExcel2 qqq = new QuarterlyEvaluationExcel2();
					qqq.setCheckClassifyName(quarterlyEvaluationList.get(i).getCheckClassifyName());
					qqq.setFirstTarget(quarterlyEvaluationList.get(i).getFirstTarget());
					qqq.setTwoTarget(quarterlyEvaluationList.get(i).getTwoTarget());
					qqq.setCheckObject(quarterlyEvaluationList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyEvaluationList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyEvaluationList.get(i).getWeight());
					qqq.setFinishDate(quarterlyEvaluationList.get(i).getFinishDate());
					if (quarterlyEvaluationList.get(i).getTargetStatus().equals("0")) {
						qqq.setTargetStatus("暂存");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("1")) {
						qqq.setTargetStatus("推进中");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("2")) {
						qqq.setTargetStatus("已完成");
					}
					list.add(qqq);
				}
			}
			ExcelUtil.export(response, "工作实绩-" + DateUtil.time(), "工作实绩", list, QuarterlyEvaluationExcel2.class);
		}else if ("3".equals(jdzbType)) {
			List<QuarterlyEvaluationExcel3> list = new ArrayList<>();

			if (quarterlyEvaluationList.size() > 0) {
				for (int i = 0; i < quarterlyEvaluationList.size(); i++) {
					QuarterlyEvaluationExcel3 qqq = new QuarterlyEvaluationExcel3();
					qqq.setCheckClassifyName(quarterlyEvaluationList.get(i).getCheckClassifyName());
					qqq.setMajorTarget(quarterlyEvaluationList.get(i).getMajorTarget());
					qqq.setScoringRubric(quarterlyEvaluationList.get(i).getScoringRubric());
					qqq.setCheckObject(quarterlyEvaluationList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyEvaluationList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyEvaluationList.get(i).getWeight());
					qqq.setFinishDate(quarterlyEvaluationList.get(i).getFinishDate());
					if (quarterlyEvaluationList.get(i).getTargetStatus().equals("0")) {
						qqq.setTargetStatus("暂存");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("1")) {
						qqq.setTargetStatus("推进中");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("2")) {
						qqq.setTargetStatus("已完成");
					}
					list.add(qqq);
				}
			}
			ExcelUtil.export(response, "党建工作-" + DateUtil.time(), "党建工作", list, QuarterlyEvaluationExcel3.class);
		}else if ("4".equals(jdzbType)) {
			List<QuarterlyEvaluationExcel3> list = new ArrayList<>();

			if (quarterlyEvaluationList.size() > 0) {
				for (int i = 0; i < quarterlyEvaluationList.size(); i++) {
					QuarterlyEvaluationExcel3 qqq = new QuarterlyEvaluationExcel3();
					qqq.setImportWork(quarterlyEvaluationList.get(i).getJdzbName());
					qqq.setCheckClassifyName(quarterlyEvaluationList.get(i).getCheckClassifyName());
					qqq.setMajorTarget(quarterlyEvaluationList.get(i).getMajorTarget());
					qqq.setScoringRubric(quarterlyEvaluationList.get(i).getScoringRubric());
					qqq.setCheckObject(quarterlyEvaluationList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyEvaluationList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyEvaluationList.get(i).getWeight());
					qqq.setFinishDate(quarterlyEvaluationList.get(i).getFinishDate());
					if (quarterlyEvaluationList.get(i).getTargetStatus().equals("0")) {
						qqq.setTargetStatus("暂存");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("1")) {
						qqq.setTargetStatus("推进中");
					} else if (quarterlyEvaluationList.get(i).getTargetStatus().equals("2")) {
						qqq.setTargetStatus("已完成");
					}
					list.add(qqq);
				}
			}
			ExcelUtil.export(response, "三抓三促-" + DateUtil.time(), "三抓三促", list, QuarterlyEvaluationExcel3.class);
		}





	}

	/**
	 * 季度评价新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@RequestBody QuarterlyEvaluation qe) throws ParseException {
	    boolean isok =	iQuarterlyEvaluationService.saveEvaluation(qe);

//		String title1 = "新增季度评价数据";
		String title1 = "新增综合评价数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyEvaluation";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 季度评价修改接口
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody QuarterlyEvaluation qe) throws ParseException {
		boolean isok =iQuarterlyEvaluationService.uptEvaluation(qe);
		String title1 = "修改综合评价数据";
//		String title1 = "修改季度评价数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyEvaluation";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 删除
	 * @param ids
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam("ids") String ids) {
		boolean isok =iQuarterlyEvaluationService.removeByIds(Func.toLongList(ids));
		String title1 = "逻辑删除综合评价数据";
//		String title1 = "逻辑删除季度评价数据";
		String businessId = String.valueOf(ids);
		String businessTable = "QuarterlyEvaluation";
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
				LambdaQueryWrapper<QuarterlySumScore> quarterlySumScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
				quarterlySumScoreLambdaQueryWrapper.in(QuarterlySumScore::getQuarterlyEvaluationId,Func.toLongList(ids));
				SpringUtil.getBean(IQuarterlySumScoreService.class).remove(quarterlySumScoreLambdaQueryWrapper);
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
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "综合评价列表查询", notes = "")
	// @ApiOperation(value = "季度评价列表查询", notes = "")
	public R<IPage<QuarterlyEvaluation>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Object quarterlyType = entity.get("quarterlyType");

		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//sql查询条件
		Object jdzbType = entity.get("jdzbType");//季度指标类型
		Object checkClassify = entity.get("checkClassify");//考核分类
		Object checkObjectId = entity.get("checkObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object targetStatus = entity.get("targetStatus");//指标状态：0暂存 1推进中 2已完成
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份
		Object quarter = entity.get("quarter");//季度 1，2，3，4   ,5,6
		if ("1".equals(quarter)) {//第一季度
			quarter = "第一季度";
		}else if ("2".equals(quarter)) {//第二季度
			quarter = "第二季度";
		}else if ("3".equals(quarter)) {//第三季度
			quarter = "第三季度";
		}else if ("4".equals(quarter)) {//第四季度
			quarter = "第四季度";
		}else if ("5".equals(quarter)) { //上半年
			quarter = "上半年";
		}else if ("6".equals(quarter)) { // 下半年
			quarter = "下半年";
		}
		LambdaQueryWrapper<QuarterlyEvaluation> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper
		.eq(StringUtils.isNotBlank((CharSequence) checkClassify),QuarterlyEvaluation::getCheckClassify,checkClassify)
		.like(StringUtils.isNotBlank((CharSequence) checkObjectId),QuarterlyEvaluation::getCheckObjectId,checkObjectId)
		.like(StringUtils.isNotBlank((CharSequence) appraiseDeptid),QuarterlyEvaluation::getAppraiseDeptid,appraiseDeptid)
		.eq(StringUtils.isNotBlank((CharSequence) targetStatus),QuarterlyEvaluation::getTargetStatus,targetStatus)
		.eq(StringUtils.isNotBlank((CharSequence) quarter),QuarterlyEvaluation::getToQuarter,quarter)
		.like(!StringUtil.isBlank(quarterlyYear.toString()),QuarterlyEvaluation::getCreateTime,quarterlyYear.toString());

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
			.eq(StringUtils.isNotNull(currentUser.getId()),QuarterlyEvaluation::getCreateUser,currentUser.getId().toString())
			.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getCreateDept,currentUser.getDeptId())
			.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getCheckObjectId,currentUser.getDeptId())
			.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyEvaluation::getAppraiseDeptid,currentUser.getDeptId())
		);
		}


		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (quarterlyType!=null && quarterlyType.equals("1")) {//如果是领导关注

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
		}
		else if(quarterlyType!=null && quarterlyType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","4");//2是年度指标，4是季度指标

		String [] tabId = null;
		if (quarterlyType!=null) {
			if ("1".equals(quarterlyType) || "2".equals(quarterlyType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() > 0) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in(QuarterlyEvaluation::getId, tabId);
				} else {
					queryWrapper.in(QuarterlyEvaluation::getId, "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) checkObjectId)){
			queryWrapper.apply("locate('"+checkObjectId+",',concat(check_object_id,',')) >0");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("locate('"+appraiseDeptid+",',concat(appraise_deptid,',')) >0");
		}
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) jdzbType),QuarterlyEvaluation::getJdzbType,jdzbType);
		queryWrapper.orderByDesc(QuarterlyEvaluation::getCreateTime);
		IPage<QuarterlyEvaluation> pages = iQuarterlyEvaluationService.page(Condition.getPage(query), queryWrapper);
		//根据未评价人员NotAppriseUser判断是否评价
		List<QuarterlyEvaluation> list = pages.getRecords();
		String deptName = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		for (int i = 0; i < list.size(); i++) {
			//查出当前指标的单位是否汇报，用于控制列表的汇报按钮显示
			LambdaQueryWrapper<Reports> lambdaQueryWrapper = Wrappers.<Reports>query().lambda()
				.eq(Reports::getEvaluationId,list.get(i).getId())
				.eq(Reports::getDeptId,currentUser.getDeptId());
			Reports reports = iReportsService.getOne(lambdaQueryWrapper);
			if (reports != null) {
				if (StringUtils.isNotNull(reports.getReportStatus()) && "1,2,3".contains(reports.getReportStatus())) {
					list.get(i).setIsHb("1");
				} else {
					list.get(i).setIsHb("0");
				}
			} else {
				list.get(i).setIsHb("0");
			}
			//控制列表的评价按钮是否显示
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
			List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService
				.list(
					Wrappers.<QuarterlySumScore>query().lambda()
						.eq(QuarterlySumScore::getQuarterlyEvaluationId,list.get(i).getId())
						.eq(QuarterlySumScore::getIsSend,"1")
				);
			if (quarterlySumScoreList.size() > 0) {
				list.get(i).setIsSend("1");
			} else {
				list.get(i).setIsSend("0");
			}
		}
		return R.data(pages);
	}

	/**
	 * 下发
	 * @param idss
	 */
	@GetMapping("/issue")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "列表下发", notes = "传入id")
	public R issue(@ApiParam(value = "主键", required = true) @RequestParam("id") String idss) {
		List<QuarterlyEvaluation> qe = iQuarterlyEvaluationService.listByIds(Func.toLongList(idss));
		if(qe.size() > 0){
			for (int j = 0; j < qe.size(); j++) {
				//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
				reportsBaseinfoService.saveForQuarter(qe.get(j));

				//发送消息
				String msgSubmit = dictBizClient.getValue("jdpj-type",qe.get(j).getJdzbType()).getData();
				String receiver="";
				String appraiseObjectIds= qe.get(j).getCheckObjectId();//评价对象ids
				R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
				String glyId=rgly.getData();
				if(StringUtils.isNotBlank(appraiseObjectIds)){
					String[] ids = appraiseObjectIds.split(",");
					for(int i=0;i<ids.length;i++){
						R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,ids[i]);//获取单位下面所有管理员用户
						if(ruser!=null){
							List<User> userList = ruser.getData();
							for(User user : userList){
								receiver+=user.getId()+",";
							}
						}
					}
					//发送消息
					BladeUser user = AuthUtil.getUser();
					String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
					String msgIntro = "";
					if (qe.get(j).getMajorTarget() != null && qe.get(j).getMajorTarget()!="") {
						msgIntro = "【"+deptName+"】下发了综合评价指标："+qe.get(j).getMajorTarget();
//						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.get(j).getMajorTarget();
					}else if (qe.get(j).getFirstTarget() != null && qe.get(j).getFirstTarget()!="") {
						msgIntro = "【"+deptName+"】下发了综合评价指标："+qe.get(j).getFirstTarget();
//						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.get(j).getFirstTarget();
					} else if (qe.get(j).getTwoTarget() != null && qe.get(j).getTwoTarget()!="") {
						msgIntro = "【"+deptName+"】下发了综合评价指标："+qe.get(j).getTwoTarget();
//						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.get(j).getTwoTarget();
					} else {
						msgIntro = "【" + deptName + "】下发了综合评价指标。";
//						msgIntro = "【" + deptName + "】下发了季度评价指标。";
					}
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(qe.get(j).getId()));//消息主键（业务主键）
					unifyMessage.setMsgTitle("综合评价下发");//消息标题
//					unifyMessage.setMsgTitle("季度评价下发");//消息标题
					unifyMessage.setMsgType("1");//消息类型，字典编码：web_message_type
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
					unifyMessage.setTwoLevelType("12");//季度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
				LambdaUpdateWrapper<QuarterlyEvaluation> lambdaUpdateWrapper = Wrappers.<QuarterlyEvaluation>update().lambda()
					.set(QuarterlyEvaluation::getTargetStatus,"1")
					.eq(QuarterlyEvaluation::getId,qe.get(j).getId().toString());
				boolean isok =iQuarterlyEvaluationService.update(lambdaUpdateWrapper);
				String title1 = "下发了综合评价列表指标";
//				String title1 = "下发了季度评价列表指标";
				String businessId = String.valueOf(qe.get(j).getId());
				String businessTable = "QuarterlyEvaluation";
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
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<QuarterlyEvaluation> details(@Valid @RequestParam Long id) {

		String title1 = "查看了考核评价-综合指标详情";
//		String title1 = "查看了考核评价-季度指标详情";
		String businessId = String.valueOf(id);
		String businessTable = "QuarterlyEvaluation";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.data(iQuarterlyEvaluationService.details(id));
	}

	/**
	 * 分页查询
	 */
	@GetMapping("/listForApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询", notes = "")
	public R<IPage<QuarterlyEvaluation>> listForApp(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String checkObjectId = (String) entity.get("checkObjectId");
		String targetStatus= (String) entity.get("targetStatus");
		String createTime =(String) entity.get("createTime");
		String majorTarget =(String) entity.get("majorTarget");
		//sql查询条件
		QueryWrapper<QuarterlyEvaluation> queryWrapper = new QueryWrapper<QuarterlyEvaluation>();
		queryWrapper.apply("FIND_IN_SET('"+checkObjectId+"',check_object_id)"  );
		if(targetStatus.equals("1")){
			queryWrapper.apply("target_Status in ('1','3','4')");
		}else if(targetStatus.equals("2")){
			queryWrapper.apply("target_Status in ('2','5')");
		}else if(targetStatus.equals("0")){
			queryWrapper.eq("target_Status",targetStatus);
		}
		queryWrapper.eq("jdzb_type",entity.get("jdzbType"));
		if(StringUtils.isNotBlank(createTime)){
			queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
		}
		if(StringUtils.isNotBlank(majorTarget)){
			queryWrapper.like("major_target",majorTarget);
		}
		String quarter = "第一季度";
		int ii = DateTime.now().month();
		int year = DateTime.now().year();
		/*if (ii==1 || ii==2 || ii==3) {//第一季度
			quarter = "第一季度";
		}else if (ii==4 || ii==5 || ii==6) {//第二季度
			quarter = "第二季度";
		}else if (ii==7 || ii==8 || ii==9) {//第三季度
			quarter = "第三季度";
		}else if (ii==10 || ii==11 || ii==12) {//第四季度
			quarter = "第四季度";
		}*/
		if (year < 2024) {
			if (ii==1 || ii==2 || ii==3) {//第一季度
				quarter = "第一季度";
			}else if (ii==4 || ii==5 || ii==6) {//第二季度
				quarter = "第二季度";
			}else if (ii==7 || ii==8 || ii==9) {//第三季度
				quarter = "第三季度";
			}else if (ii==10 || ii==11 || ii==12) {//第四季度
				quarter = "第四季度";
			}
		} else if (year == 2024) {
			if (ii==1 || ii==2 || ii==3) {//第一季度
				quarter = "第一季度";
			}else if (ii==4 || ii==5 || ii==6) {//第二季度
				quarter = "第二季度";
			}else {//下半年
				quarter = "下半年";
			}
		} else {
			if (ii <= 6) {//上半年
				quarter = "上半年";
			} else {//下半年
				quarter = "下半年";
			}
		}
		queryWrapper.eq("to_quarter",quarter);
		IPage<QuarterlyEvaluation> pages = iQuarterlyEvaluationService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}

	/**
	 * 分页查询
	 */
	@PostMapping("/listForApplication")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询", notes = "")
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
			String checkObjectId = (String) entity.get("checkObjectId");
			String targetStatus= (String) entity.get("targetStatus");
			String createTime =(String) entity.get("createTime");
			String majorTarget =(String) entity.get("majorTarget");
			//sql查询条件
			QueryWrapper<QuarterlyEvaluation> queryWrapper = new QueryWrapper<QuarterlyEvaluation>();
			queryWrapper.apply("FIND_IN_SET('"+checkObjectId+"',check_object_id)"  );
			if(targetStatus.equals("1")){
				queryWrapper.apply("target_Status in ('1','3','4')");
			}else if(targetStatus.equals("2")){
				queryWrapper.apply("target_Status in ('2','5')");
			}else if(targetStatus.equals("0")){
				queryWrapper.eq("target_Status",targetStatus);
			}
			queryWrapper.eq("jdzb_type",entity.get("jdzbType"));
			if(StringUtils.isNotBlank(createTime)){
				queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
			}
			if(StringUtils.isNotBlank(majorTarget)){
				queryWrapper.like("major_target",majorTarget);
			}
			String quarter = "第一季度";
			int ii = DateTime.now().month();
			int year = DateTime.now().year();
			/*if (ii==1 || ii==2 || ii==3) {//第一季度
				quarter = "第一季度";
			}else if (ii==4 || ii==5 || ii==6) {//第二季度
				quarter = "第二季度";
			}else if (ii==7 || ii==8 || ii==9) {//第三季度
				quarter = "第三季度";
			}else if (ii==10 || ii==11 || ii==12) {//第四季度
				quarter = "第四季度";
			}*/
			if (year < 2024) {
				if (ii==1 || ii==2 || ii==3) {//第一季度
					quarter = "第一季度";
				}else if (ii==4 || ii==5 || ii==6) {//第二季度
					quarter = "第二季度";
				}else if (ii==7 || ii==8 || ii==9) {//第三季度
					quarter = "第三季度";
				}else if (ii==10 || ii==11 || ii==12) {//第四季度
					quarter = "第四季度";
				}
			} else if (year == 2024) {
				if (ii==1 || ii==2 || ii==3) {//第一季度
					quarter = "第一季度";
				}else if (ii==4 || ii==5 || ii==6) {//第二季度
					quarter = "第二季度";
				}else {//下半年
					quarter = "下半年";
				}
			} else {
				if (ii <= 6) {//上半年
					quarter = "上半年";
				} else {//下半年
					quarter = "下半年";
				}
			}
			queryWrapper.eq("to_quarter",quarter);
			IPage<QuarterlyEvaluation> pages = iQuarterlyEvaluationService.page(Condition.getPage(query), queryWrapper);
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
			QuarterlyEvaluation qe = iQuarterlyEvaluationService.getById(id);
			qe.setTargetStatus(type);//指标状态：0暂存 1推进中 2已完成 3申请办结 4申请中止 5 已中止（字典编码zb_status）
			flag=iQuarterlyEvaluationService.updateById(qe);
		}
		String title1 = "办结了综合评价事项";
//		String title1 = "办结了季度评价事项";
		String businessId = String.valueOf(ids);
		String businessTable = "QuarterlyEvaluation";
		int businessType = BusinessType.OTHER.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(flag);
	}

	/**
	 * 修改阶段
	 */
	@PostMapping("/updateStage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateStage(@Valid @RequestBody QuarterlyEvaluation qe) throws ParseException {
		boolean isok = iQuarterlyEvaluationService.uptStage(qe);
		String title1 = "修改综合评价阶段数据";
//		String title1 = "修改季度评价阶段数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyEvaluation";
		int businessType = BusinessType.OTHER.ordinal();
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
	public R submitAudit(@RequestBody QuarterlyEvaluation quarterlyEvaluation){

		try {
			if (StringUtils.isNull(quarterlyEvaluation.getReportId())) {
				return R.fail("季度汇报送审-汇报id为空");
			}
			if (StringUtils.isNull(quarterlyEvaluation.getTitle())) {
				return R.fail("季度汇报送审-送审标题为空");
			}
			if (StringUtils.isNull(quarterlyEvaluation.getUserIds())) {
				return R.fail("季度汇报送审-接收人id为空");
			}
			if (StringUtils.isNull(quarterlyEvaluation.getSync())) {
				return R.fail("季度汇报送审-是否异步为空");
			}
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(quarterlyEvaluation.getReportId()),
				quarterlyEvaluation.getTitle(),quarterlyEvaluation.getUserIds(),
				quarterlyEvaluation.getSync(), StatusConstant.OPERATION_TYPE_QUARTERAPPRISEHB);

			String title1 = "送审综合评价-汇报";
//			String title1 = "送审季度评价-汇报";
			String businessId = String.valueOf(quarterlyEvaluation.getId());
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.OTHER.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			//发送消息
			QuarterlyEvaluation qe = iQuarterlyEvaluationService.getById(quarterlyEvaluation.getId());
			qe.setTargetStatus("7");//汇报送审
			iQuarterlyEvaluationService.updateById(qe);



			String msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro="";
			if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
//				msgIntro = "【"+deptName+"】送审了季度评价指标：【"+qe.getMajorTarget()+"】";
				msgIntro = "【"+deptName+"】送审了综合评价指标：【"+qe.getMajorTarget()+"】";
			}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
//				msgIntro = "【"+deptName+"】送审了季度评价指标：【"+qe.getFirstTarget()+"】";
				msgIntro = "【"+deptName+"】送审了综合评价指标：【"+qe.getFirstTarget()+"】";
			} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价指标：【"+qe.getTwoTarget()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价指标：【"+qe.getTwoTarget()+"】";
			}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价指标：【"+qe.getImportWork()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价指标：【"+qe.getImportWork()+"】";
			} else {
				msgIntro = "【"+deptName+"】送审了综合评价指标";
//				msgIntro = "【"+deptName+"】送审了季度评价指标";
			}
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(quarterlyEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("综合汇报送审");//消息标题
//			unifyMessage.setMsgTitle("季度汇报送审");//消息标题
			unifyMessage.setMsgType("3");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(quarterlyEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("14");//季度汇报送审
			unifyMessageService.sendMessageInfo(unifyMessage);

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
	public R submitAuditScore(@RequestBody QuarterlyEvaluation quarterlyEvaluation){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(quarterlyEvaluation.getId()),quarterlyEvaluation.getTitle(),quarterlyEvaluation.getUserIds(),quarterlyEvaluation.getSync(), StatusConstant.OPERATION_TYPE_QUARTERAPPRISESCORE);

			String title1 = "送审综合评价-改分";
//			String title1 = "送审季度评价-改分";
			String businessId = String.valueOf(quarterlyEvaluation.getId());
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.OTHER.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			//发送消息
			QuarterlyEvaluation ae = iQuarterlyEvaluationService.getById(quarterlyEvaluation.getId());
			String msgSubmit=dictBizClient.getValue("jdpj-type",ae.getJdzbType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro="";
			if (ae.getMajorTarget() != null && ae.getMajorTarget()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价改分申请：【"+ae.getMajorTarget()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价改分申请：【"+ae.getMajorTarget()+"】";
			}else if (ae.getFirstTarget() != null && ae.getFirstTarget()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价改分申请：【"+ae.getFirstTarget()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价改分申请：【"+ae.getFirstTarget()+"】";
			} else if (ae.getTwoTarget() != null && ae.getTwoTarget()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价改分申请：【"+ae.getTwoTarget()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价改分申请：【"+ae.getTwoTarget()+"】";
			}else if (ae.getImportWork()!= null && ae.getImportWork()!="") {
				msgIntro = "【"+deptName+"】送审了综合评价改分申请：【"+ae.getImportWork()+"】";
//				msgIntro = "【"+deptName+"】送审了季度评价改分申请：【"+ae.getImportWork()+"】";
			} else {
				msgIntro = "【"+deptName+"】送审了综合评价改分申请";
//				msgIntro = "【"+deptName+"】送审了季度评价改分申请";
			}
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(quarterlyEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("综合评价改分申请");//消息标题
//			unifyMessage.setMsgTitle("季度评价改分申请");//消息标题
			unifyMessage.setMsgType("29");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(quarterlyEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("28");//季度下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}

	/**
	 * 下发-送审
	 * @param quarterlyEvaluation		指标id主键
	 * @param quarterlyEvaluation		送审标题
	 * @param quarterlyEvaluation		用户主键，多个逗号隔开
	 * @param quarterlyEvaluation		同步还是异步 1同步；0异步
	 * @return
	 */
	@PostMapping("/submitAuditXf")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "下发-送审", notes = "下发-送审")
	@Transactional
	public R submitAuditXf(@RequestBody QuarterlyEvaluation quarterlyEvaluation){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(quarterlyEvaluation.getId()),quarterlyEvaluation.getTitle(),quarterlyEvaluation.getUserIds(),quarterlyEvaluation.getSync(), StatusConstant.OPERATION_TYPE_QUARTERAPPRISEXF);

			String title1 = "送审综合评价-下发";
//			String title1 = "送审季度评价-下发";
			String businessId = String.valueOf(quarterlyEvaluation.getId());
			String businessTable = "QuarterlyEvaluation";
			int businessType = BusinessType.OTHER.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			//发送消息
			QuarterlyEvaluation ae = iQuarterlyEvaluationService.getById(quarterlyEvaluation.getId());
			//更新指标状态为下发送审
			ae.setTargetStatus("6");//下发送审
			iQuarterlyEvaluationService.updateById(ae);
			String msgSubmit=dictBizClient.getValue("jdpj-type",ae.getJdzbType()).getData();
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro="";
			if (ae.getMajorTarget() != null && ae.getMajorTarget()!="") {
				msgIntro = "【"+deptName+"】提交了综合评价下发申请：【"+ae.getMajorTarget()+"】";
//				msgIntro = "【"+deptName+"】提交了季度评价下发申请：【"+ae.getMajorTarget()+"】";
			}else if (ae.getFirstTarget() != null && ae.getFirstTarget()!="") {
				msgIntro = "【"+deptName+"】提交了综合评价下发申请：【"+ae.getFirstTarget()+"】";
//				msgIntro = "【"+deptName+"】提交了季度评价下发申请：【"+ae.getFirstTarget()+"】";
			} else if (ae.getTwoTarget() != null && ae.getTwoTarget()!="") {
				msgIntro = "【"+deptName+"】提交了综合评价下发申请：【"+ae.getTwoTarget()+"】";
//				msgIntro = "【"+deptName+"】提交了季度评价下发申请：【"+ae.getTwoTarget()+"】";
			}else if (ae.getImportWork()!= null && ae.getImportWork()!="") {
				msgIntro = "【"+deptName+"】提交了综合评价下发申请：【"+ae.getImportWork()+"】";
//				msgIntro = "【"+deptName+"】提交了季度评价下发申请：【"+ae.getImportWork()+"】";
			} else {
				msgIntro = "【"+deptName+"】提交了综合评价下发申请";
//				msgIntro = "【"+deptName+"】提交了季度评价下发申请";
			}
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(quarterlyEvaluation.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("综合评价下发申请");//消息标题
//			unifyMessage.setMsgTitle("季度评价下发申请");//消息标题
			unifyMessage.setMsgType("30");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(quarterlyEvaluation.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("29");//季度下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);
			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}



}
