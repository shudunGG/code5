package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 项目管理调度
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectPhaseReport")
@Api(value = "项目管理调度", tags = "项目管理调度")
public class ProjectPhaseReportController extends BladeController {

	private final IProjectPhaseReportService projectPhaseReportService;
	private final IProjectPhasePlanService projectPhasePlanService;
	private final IProjectFilesService projectFilesService;
	private final IProjectSummaryService projectSummaryService;
	private final IProjectLogService projectLogService;
	private final IUnifyMessageService messageService;
	private final IProjectPhaseReportSwitchService projectPhaseReportSwitchService;
	private final IProjectSummaryAppraiseService projectSummaryAppraiseService;
	private final IUnifyMessageService unifyMessageService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final ISysClient sysClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	@Resource
	private IUserSearchClient iUserSearchClient;
	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目阶段调度", notes = "传入projectPhaseReport")
	public R<List<ProjectPhaseReport>> detail(ProjectPhaseReport projectPhaseReport) {
		//sql查询条件
		QueryWrapper<ProjectPhaseReport> queryWrapper = new QueryWrapper<ProjectPhaseReport>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhaseReport.getId()!=null,"id",projectPhaseReport.getId());
		List<ProjectPhaseReport> detail = projectPhaseReportService.list(queryWrapper);
		return R.data(detail);
	}

	/**
	 * 详细信息
	 */
	@GetMapping("/getDetail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目阶段调度", notes = "传入id")
	public R<ProjectPhaseReport> getDetail(String id) {
		ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getById(id);
		List<ProjectFiles> files = projectFilesService.list(Wrappers.<ProjectFiles>query().lambda().eq(ProjectFiles::getProjId, projectPhaseReport.getProjId()).eq(ProjectFiles::getPhaseId,projectPhaseReport.getJhjdId()));
		projectPhaseReport.setFiles(files);
		return R.data(projectPhaseReport);
	}

	/**
	 * 审核
	 */
	@GetMapping("/examine")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目阶段调度审核", notes = "传入id")
	public R<ProjectPhaseReport> examine(String id,String shyj,String shjg) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getById(id);
		ProjectSummary projectSummary = projectSummaryService.getById(projectPhaseReport.getProjId());
		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(projectPhaseReport.getProjId());
		projectLog.setHandleType("项目审核");

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(projectSummary.getId());
		message.setMsgTitle("项目管理-项目调度审核通过");
		message.setMsgType("37");
		message.setMsgStatus(0);
		message.setTwoLevelType("45");
		message.setMsgPlatform("web");
		message.setCreateTime(new Date());
		//项目调度通知申请人
		String userids = projectSummaryService.getUserIdListByDeptId(projectSummary.getDwmc(),user.getId()+"");
		message.setReceiveUser(userids+projectSummary.getCreateUser());

		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
		}


		boolean isSheng = String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId("6207"));//是否是市级审核
		if(isSheng){
			projectPhaseReport.setSjshyj(shyj);
		}else{
			projectPhaseReport.setShyj(shyj);
		}
		if("1".equals(shjg)){//审核通过
			if(isSheng){
				ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getById(projectPhaseReport.getJhjdId());
				int reportMonth=projectPhasePlan.getPlanMonth();
				if(reportMonth>projectPhasePlan.getPlanMonth()){
					projectSummary.setReportStatus("8");//超期已调度
				}else{
					projectSummary.setReportStatus("3");//已调度
				}
				projectSummary.setTotalEconomic(projectPhaseReport.getLjddtz());
				projectSummary.setSfkfg(projectPhaseReport.getSfkfg());
				projectSummary.setSfnrtj(projectPhaseReport.getSfnrtjk());
				projectSummary.setWnrtjkyy(projectPhaseReport.getWnrtjkyy());
				projectSummary.setWkfgyy(projectPhaseReport.getWkfgyy());
				projectSummary.setKfgsj(projectPhaseReport.getKfgsj());
				projectSummary.setSnrtjksj(projectPhaseReport.getNrtjksj());
				projectSummary.setWcqk(projectPhaseReport.getJzzt());
				projectSummary.setStartDatePlan(projectPhaseReport.getStartDatePlan());
				//全面完成
//				if("1".equals(projectPhaseReport.getJzzt())){
//					projectSummary.setPorjStatus("7");
//					projectSummary.setReportStatus("13");
//				}
				switch (reportMonth){
					case 1:
						projectSummary.setWctz01(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 2:
						projectSummary.setWctz02(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 3:
						projectSummary.setWctz03(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 4:
						projectSummary.setWctz04(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 5:
						projectSummary.setWctz05(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 6:
						projectSummary.setWctz06(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 7:
						projectSummary.setWctz07(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 8:
						projectSummary.setWctz08(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 9:
						projectSummary.setWctz09(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 10:
						projectSummary.setWctz10(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 11:
						projectSummary.setWctz11(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
					case 12:
						projectSummary.setWctz12(String.valueOf(projectPhaseReport.getDyddtz()));
						break;
				}
			}else{
				projectSummary.setReportStatus("9");//市级待审核
				//上报更改审核人
				projectPhaseReport.setShrId(Long.parseLong(Objects.requireNonNull(PropConstant.getProjectShzhId("6207"))));
			}
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】审核通过【"+projectSummary.getTitle()+"】调度信息");
			message.setMsgIntro(projectLog.getHandleContent());
		//审核退回
		}else{
			if(isSheng){
				projectSummary.setReportStatus("10");//调度市级退回
			}else{
				projectSummary.setReportStatus("6");
			}

//			projectLog.setHandleContent(projectPhaseReport.getShyj());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】退回【"+projectSummary.getTitle()+"】调度信息");
			message.setMsgIntro(projectLog.getHandleContent());
			message.setTwoLevelType("38");
			message.setMsgTitle("项目管理-项目调度审核退回");

		}
		projectPhaseReport.setShzt(projectSummary.getReportStatus());
		projectPhaseReportService.updateById(projectPhaseReport);
		projectSummaryService.updateById(projectSummary);
		projectLogService.save(projectLog);
		messageService.sendMessageInfo(message);
		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgSubitem("项目调度审核");
		message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
		messageService.sendMessageInfo(message);
		return R.data(projectPhaseReport);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理调度", notes = "")
	public R<IPage<ProjectPhaseReport>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<ProjectPhaseReport> pages = projectPhaseReportService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectPhaseReport.class));
		return R.data(pages);
	}


	/**
	 * 新增
	 * @param projectPhaseReport
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectPhaseReport projectPhaseReport) {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <25 && day >20){
			return R.fail("当月25日后至次月20日前才是调度时间");
		}
		List<ProjectFiles> files  = projectPhaseReport.getFiles();
		for (int i = 0; i < files.size(); i++) {
			ProjectFiles file = files.get(i);
			String[] fileNameSplit = file.getFileName().split("\\.");
			file.setProjId(projectPhaseReport.getProjId());
			file.setPhaseId(projectPhaseReport.getJhjdId());
			file.setFileType("."+fileNameSplit[fileNameSplit.length-1]);
		}
		projectFilesService.saveBatch(files);
		ProjectSummary projectSummary = projectSummaryService.getById(projectPhaseReport.getProjId());
		if(projectSummary!=null){
			if(String.valueOf(projectPhaseReport.getShrId()).equals(PropConstant.getProjectShzhId("6207"))){//是否是市级审核
				projectSummary.setReportStatus("9");
			}else{
				projectSummary.setReportStatus("5");
			}
		}
		projectSummaryService.updateById(projectSummary);

		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(projectPhaseReport.getProjId());
		projectLog.setHandleType("项目调度");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
		}
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		projectLog.setHandleContent("【"+userNameDecrypt+"】对【"+projectSummary.getTitle()+"】进行了调度");
		projectLogService.save(projectLog);

		boolean result = projectPhaseReportService.save(projectPhaseReport);

		if(result){
//			List<FollowInformation>
			UnifyMessage message = new UnifyMessage();
			message.setMsgId(projectPhaseReport.getProjId());
			message.setMsgTitle("项目管理-项目调度");
			message.setMsgType("36");
			message.setMsgStatus(0);
			message.setTwoLevelType("36");
			message.setMsgPlatform("web");
			message.setMsgIntro(projectLog.getHandleContent()+"，请及时审核");
			message.setCreateTime(new Date());
			//项目调度通知审核人
			message.setReceiveUser(projectPhaseReport.getShrId()+"");
			messageService.sendMessageInfo(message);

			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgSubitem("项目调度");
			message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
			message.setTwoLevelType("36");
			messageService.sendMessageInfo(message);
		}
		return R.status(result);
	}

	/**
	 * 修改
	 * @param projectPhaseReport
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectPhaseReport projectPhaseReport) {
		if(projectPhaseReport.getId()==null){
			return  R.fail("id 不能为空");
		}
		//需先删除相关附件
		List<ProjectFiles> oldFiles = projectFilesService.list(Wrappers.<ProjectFiles>query().lambda().eq(ProjectFiles::getProjId, projectPhaseReport.getProjId()).eq(ProjectFiles::getPhaseId,projectPhaseReport.getJhjdId()));
		List<Long> ids = new ArrayList<>();
		for (ProjectFiles file:oldFiles) {
			ids.add(file.getId());
		}
		projectFilesService.removeByIds(ids);

		List<ProjectFiles> files  = projectPhaseReport.getFiles();
		for (int i = 0; i < files.size(); i++) {
			ProjectFiles file = files.get(i);
			file.setId(null);
			file.setProjId(projectPhaseReport.getProjId());
			file.setPhaseId(projectPhaseReport.getJhjdId());
		}
		projectFilesService.saveBatch(files);
		ProjectSummary projectSummary = projectSummaryService.getById(projectPhaseReport.getProjId());
		String areacode = sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getAreaCode();
		projectPhaseReport.setShrId(Long.parseLong(Objects.requireNonNull(PropConstant.getProjectShzhId(areacode))));
		//是否是投资科账号
		boolean isTZK = String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId("6207"));

		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(projectPhaseReport.getProjId());
		projectLog.setHandleType("项目调度修改");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
		}
		String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
		projectLog.setHandleContent("【"+handleUserDecrypt+"】修改项目【"+projectSummary.getTitle()+"】阶段调度操作");
		if(isTZK){
			ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getById(projectPhaseReport.getJhjdId());
			int reportMonth=projectPhasePlan.getPlanMonth();
			projectSummary.setReportStatus("3");//已调度
			projectSummary.setTotalEconomic(projectPhaseReport.getLjddtz());
			projectSummary.setSfkfg(projectPhaseReport.getSfkfg());
			projectSummary.setSfnrtj(projectPhaseReport.getSfnrtjk());
			projectSummary.setWnrtjkyy(projectPhaseReport.getWnrtjkyy());
			projectSummary.setWkfgyy(projectPhaseReport.getWkfgyy());
			projectSummary.setKfgsj(projectPhaseReport.getKfgsj());
			projectSummary.setSnrtjksj(projectPhaseReport.getNrtjksj());
			projectSummary.setWcqk(projectPhaseReport.getJzzt());
			projectSummary.setStartDatePlan(projectPhaseReport.getStartDatePlan());
			switch (reportMonth){
				case 1:
					projectSummary.setWctz01(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 2:
					projectSummary.setWctz02(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 3:
					projectSummary.setWctz03(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 4:
					projectSummary.setWctz04(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 5:
					projectSummary.setWctz05(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 6:
					projectSummary.setWctz06(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 7:
					projectSummary.setWctz07(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 8:
					projectSummary.setWctz08(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 9:
					projectSummary.setWctz09(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 10:
					projectSummary.setWctz10(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 11:
					projectSummary.setWctz11(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
				case 12:
					projectSummary.setWctz12(String.valueOf(projectPhaseReport.getDyddtz()));
					break;
			}
			projectPhaseReport.setShzt(projectSummary.getReportStatus());
			projectSummaryService.updateById(projectSummary);
		}else{
			if(StringUtil.isNotBlank(areacode)){
				if("6207".equals(areacode)){//省级
					projectSummary.setReportStatus("9");
				}else{
					projectSummary.setReportStatus("5");
				}
			}
			projectSummaryService.updateById(projectSummary);


			UnifyMessage message = new UnifyMessage();
			message.setMsgId(projectPhaseReport.getProjId());
			message.setMsgTitle("项目管理-修改项目调度");
			message.setMsgType("36");
			message.setMsgStatus(0);
			message.setTwoLevelType("36");
			message.setMsgPlatform("web");
			message.setMsgIntro(projectLog.getHandleContent());
			message.setCreateTime(new Date());
			//项目调度通知审核人
			message.setReceiveUser(projectPhaseReport.getShrId()+"");
			messageService.sendMessageInfo(message);

			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgSubitem("项目调度审核");
			message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
			messageService.sendMessageInfo(message);
		}

		projectLogService.save(projectLog);
		return R.status(projectPhaseReportService.updateById(projectPhaseReport));
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String id) {
		return R.status(projectPhaseReportService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return`
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(projectPhaseReportService.removeByIds(Arrays.asList(id)));
	}

	/**
	 * 项目调度列表
	 * @param id
	 * @return
	 */
	@GetMapping("/batchReportList")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目调度列表", notes = "")
	public R batchReportScreen(@ApiIgnore @RequestParam String id){


		return R.data(projectPhaseReportService.getProjectPhaseReportByHbjdId(Long.valueOf(id)));
	}

	/**
	 * 项目调度列表
	 * @param id
	 * @return
	 */
	@GetMapping("/getReportByProjId")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目调度列表", notes = "")
	public R getReportByProjId(@ApiIgnore @RequestParam String id){
		ProjectSummary projectSummary = projectSummaryService.getById(id);
		List<ProjectPhasePlan> projectPhasePlans = projectPhasePlanService.getProjectPhasePlanListByProjId(id);
		for (ProjectPhasePlan projectPlan:projectPhasePlans) {
			ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getProjectPhaseReportByPlanId(projectPlan.getId());
			if(projectPhaseReport!=null){
				List<ProjectFiles> files = projectFilesService.list(Wrappers.<ProjectFiles>query().lambda().eq(ProjectFiles::getProjId, projectPhaseReport.getProjId()).eq(ProjectFiles::getPhaseId,projectPhaseReport.getJhjdId()));
				projectPhaseReport.setFiles(files);
				projectPlan.setProjectPhaseReport(projectPhaseReport);
			}
			//评价状态
			List<ProjectSummaryAppraise> appraiseList = projectSummaryAppraiseService.list(Wrappers.<ProjectSummaryAppraise>query().lambda()
				.eq(ProjectSummaryAppraise::getProjId, projectPlan.getProjId()).eq(ProjectSummaryAppraise::getJhtzyf, projectPlan.getPhaseName()));
			if(appraiseList != null && appraiseList.size() > 0){
				projectPlan.setAppraiseStatus("Y");
				projectPlan.setAppraiseId(appraiseList.get(0).getId());
			}else{
				projectPlan.setAppraiseStatus("N");
			}
		}
		JSONObject obj = new JSONObject();
		obj.put("xmdl",projectSummary.getXmdl());
		obj.put("title",projectSummary.getTitle());
		obj.put("plans",projectPhasePlans);
		return R.data(obj);
	}

	/**
	 * 项目调度列表-app
	 * @return
	 */
	@PostMapping("/getReportByProjIdApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目调度列表-app", notes = "")
	public R getReportByProjIdApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("项目调度列表-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String id = jsonParams.getString("id");
			ProjectSummary projectSummary = projectSummaryService.getById(id);
			List<ProjectPhasePlan> projectPhasePlans = projectPhasePlanService.getProjectPhasePlanListByProjId(id);
			for (ProjectPhasePlan projectPlan:projectPhasePlans) {
				ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getProjectPhaseReportByPlanId(projectPlan.getId());
				if(projectPhaseReport!=null){
					List<ProjectFiles> files = projectFilesService.list(Wrappers.<ProjectFiles>query().lambda().eq(ProjectFiles::getProjId, projectPhaseReport.getProjId()).eq(ProjectFiles::getPhaseId,projectPhaseReport.getJhjdId()));
					projectPhaseReport.setFiles(files);
					projectPlan.setProjectPhaseReport(projectPhaseReport);
				}
				//评价状态
				List<ProjectSummaryAppraise> appraiseList = projectSummaryAppraiseService.list(Wrappers.<ProjectSummaryAppraise>query().lambda()
					.eq(ProjectSummaryAppraise::getProjId, projectPlan.getProjId()).eq(ProjectSummaryAppraise::getJhtzyf, projectPlan.getPhaseName()));
				if(appraiseList != null && appraiseList.size() > 0){
					projectPlan.setAppraiseStatus("Y");
					projectPlan.setAppraiseId(appraiseList.get(0).getId());
				}else{
					projectPlan.setAppraiseStatus("N");
				}
			}
			JSONObject obj = new JSONObject();
			obj.put("xmdl",projectSummary.getXmdl());
			obj.put("title",projectSummary.getTitle());
			obj.put("plans",projectPhasePlans);
			return R.data(VSTool.encrypt(encryptSign, obj.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}

	}

	/**
	 * 项目调度开关状态
	 * @return
	 */
	@GetMapping("/getReportSwitch")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取项目调度开关", notes = "")
	public R getReportSwitch(){
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <25 && day >20){
			return R.data("0");
		}
		if(day >= 25){
			if(month==12){
				year+=1;
				month=1;
			}else{
				month+=1;
			}
		}
		String time = year+"-"+month;
		List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
		if(switchList!=null && !switchList.isEmpty()){
			return R.data(switchList.get(0).getStatus());
		}else{
			ProjectPhaseReportSwitch swi = new ProjectPhaseReportSwitch();
			swi.setStatus(0);
			swi.setTime(time);
			projectPhaseReportSwitchService.save(swi);
		}
		return R.data("0");
	}

	/**
	 * 项目调度开关状态
	 * @return
	 */
	@GetMapping("/updateReportSwitch")
	@ApiOperation(value = "设置项目调度开关", notes = "")
	public R updateReportSwitch(@ApiIgnore @RequestParam String kg, String searchYear){
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <25 && day >20){
			return R.data(false,"当月25日后至次月20日前才能设置");
		}
		if(day >= 25){
			if(month==12){
				year+=1;
				month=1;
			}else{
				month+=1;
			}
		}
		String time = year+"-"+month;
		List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
		if(switchList!=null && !switchList.isEmpty()){
			ProjectPhaseReportSwitch swi = switchList.get(0);
			swi.setStatus(Integer.parseInt(kg));
			boolean result = projectPhaseReportSwitchService.updateById(swi);
			if(result){
				if(StringUtils.isNotBlank(kg) && kg.equals("1")){  //打开
					//给调度单位发消息
					List<ProjectSummary> projectSummaryList = projectSummaryService.selectListByYear(searchYear);
					if(projectSummaryList != null && projectSummaryList.size() > 0){
						for(ProjectSummary projectSummary : projectSummaryList){
							UnifyMessage message = new UnifyMessage();
							message.setMsgId(projectSummary.getId());
							message.setMsgTitle("项目调度");
							message.setMsgType("77");  //项目调度消息
							message.setMsgStatus(0);
							message.setMsgPlatform("web");
							message.setMsgIntro((month-1)+"月份"+projectSummary.getTitle()+"项目调度已开启，调度期限为2个工作日，请抓紧按时开展项目调度工作。");
							message.setCreateTime(new Date());
							//根据调度单位获取单位下面所有管理员用户
							String receiver="";
							R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
							String glyId = rgly.getData();
							R<List<User>> ruser = iUserSearchClient.listByPostAndDept(glyId,projectSummary.getDwmc());//获取单位下面所有管理员用户
							if(ruser != null){
								List<User> userList = ruser.getData();
								for(User user : userList){
									receiver += user.getId()+",";
								}
							}
							message.setReceiveUser(receiver);
							unifyMessageService.sendMessageInfo(message);

							message.setId(null);
							message.setMsgPlatform("app");
							message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
							message.setTwoLevelType("77");//项目调度消息
							unifyMessageService.sendMessageInfo(message);
						}
					}
				}
				return R.data(true,"设置成功");
			}else{
				return R.data(false,"设置失败");
			}
		}else{
			ProjectPhaseReportSwitch swi = new ProjectPhaseReportSwitch();
			swi.setStatus(0);
			swi.setTime(time);
			projectPhaseReportSwitchService.save(swi);
			if(StringUtils.isNotBlank(kg) && kg.equals("1")){  //打开
				//给调度单位发消息
				List<ProjectSummary> projectSummaryList = projectSummaryService.selectListByYear(searchYear);
				if(projectSummaryList != null && projectSummaryList.size() > 0){
					for(ProjectSummary projectSummary : projectSummaryList){
						UnifyMessage message = new UnifyMessage();
						message.setMsgId(projectSummary.getId());
						message.setMsgTitle("项目调度");
						message.setMsgType("77");  //项目调度消息
						message.setMsgStatus(0);
						message.setMsgPlatform("web");
						message.setMsgIntro((month-1)+"月份"+projectSummary.getTitle()+"项目调度已开启，调度期限为2个工作日，请抓紧按时开展项目调度工作。");
						message.setCreateTime(new Date());
						//根据调度单位获取单位下面所有管理员用户
						String receiver="";
						R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
						String glyId = rgly.getData();
						R<List<User>> ruser = iUserSearchClient.listByPostAndDept(glyId,projectSummary.getDwmc());//获取单位下面所有管理员用户
						if(ruser != null){
							List<User> userList = ruser.getData();
							for(User user : userList){
								receiver += user.getId()+",";
							}
						}
						message.setReceiveUser(receiver);
						unifyMessageService.sendMessageInfo(message);

						message.setId(null);
						message.setMsgPlatform("app");
						message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						message.setTwoLevelType("77");//项目调度消息
						unifyMessageService.sendMessageInfo(message);
					}
				}
			}
			return R.data(true,"设置成功");
		}
	}
}
