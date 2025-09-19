package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.*;
import com.vingsoft.vo.MapPorjectVO;
import com.vingsoft.vo.TzProjectSummaryVO;
import com.vingsoft.vo.ZfzqProjectSummaryVO;
import com.vingsoft.vo.ZysjProjectSummaryVO;
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
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.*;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 项目管理基础信息
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectSummary")
@Api(value = "项目管理基础信息", tags = "项目管理基础信息")
public class ProjectSummaryController extends BladeController {

	private final IProjectSummaryService projectSummaryService;
	private final IProjectPhasePlanService projectPhasePlanService;
	private final IProjectFilesService projectFilesService;
	private final IProjectPhaseRemindService projectPhaseRemindService;
	private final IProjectUrgeService projectUrgeService;
	private final IProjectPhaseReportService projectPhaseReportService;
	private final IMessageInformationService messageInformationService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	private final IProjectLogService projectLogService;
	private final IProjectSpecialistService specialistService;
	@Resource
	private final IUserClient userClient;
	private final IProjectPhaseReportSwitchService projectPhaseReportSwitchService;
	@Resource
	private final ISysClient sysClient;
	private final IMessageInformationService iMessageInformationService;
	private final IAppriseFilesService iAppriseFilesService;
	public final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	@Resource
	private IUnifyMessageService unifyMessageService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理基础信息", notes = "")
	public R<IPage<ProjectSummary>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<ProjectSummary> pages = projectSummaryService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectSummary.class));
		return R.data(pages);
	}

	/**
	 * 新增
	 * @param projectSummary
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectSummary projectSummary) {
		if(StringUtils.isBlank(projectSummary.getXmnf())) {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			projectSummary.setXmnf(String.valueOf(year));
		}
		projectSummary.setReportStatus("1");
		projectSummary.setPorjStatus("1");
		boolean save = projectSummaryService.save(projectSummary);
		//为避免删除不掉，先全部删除项目专员列表，再添加
		//项目专员列表
		if (save) {
			ProjectSummary one = projectSummaryService.getOne(Wrappers.<ProjectSummary>query().lambda()
				.eq(ProjectSummary::getTitle, projectSummary.getTitle())
				.eq(ProjectSummary::getXmType, projectSummary.getXmType())
				.orderByDesc(ProjectSummary::getCreateTime).last("limit 1"));
			List<ProjectSpecialist> list = projectSummary.getProjectSpecialistList();
			list.stream().forEach(p -> p.setProjectId(one.getId().toString()));
			specialistService.listBatchByProjectId(list, one.getId().toString());
		}
		return R.status(save);
	}

	/**
	 * 修改
	 * @param projectSummary
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectSummary projectSummary) {
		boolean update = projectSummaryService.updateById(projectSummary);
		if (update) {
			//为避免删除不掉，先全部删除项目专员列表，再添加
			//项目专员列表
			specialistService.listBatchByProjectId(projectSummary.getProjectSpecialistList(), projectSummary.getId().toString());
		}
		return R.status(update);
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
		boolean remove = projectSummaryService.removeById(id);
		if (remove) {
			//删除对应的项目专员
			specialistService.remove(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, id));
		}
		return R.status(remove);
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
		List<String> idList = Arrays.asList(id);
		boolean remove = projectSummaryService.removeByIds(idList);
		if (remove) {
			if (remove) {
				//删除对应的项目专员
				specialistService.remove(Wrappers.<ProjectSpecialist>query().lambda().in(ProjectSpecialist::getProjectId, idList));
			}
		}
		return R.status(remove);
	}


	/**
	 * 统计项目数
	 * @return`
	 */
	@GetMapping("/statisticsCount")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目数量统计", notes = "")
	public R statisticsCount(@ApiIgnore @RequestParam String year) {
		JSONObject object = new JSONObject();
		object.put("total",projectSummaryService.getTotalCount(year));
		object.put("started",projectSummaryService.getStartedCount(year));
		if(object.getFloatValue("total")>0) {
			object.put("bl",String.format("%.2f", object.getFloatValue("started")/object.getFloatValue("total")*100)+"%");
		} else {
			object.put("bl","0%");
		}
		return R.data(object);
	}

	/**
	 * 统计项目数-app
	 */
	@PostMapping("/statisticsCountApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目数量统计-app", notes = "")
	public R statisticsCountApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("项目数量统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String year = jsonParams.getString("year");

			JSONObject object = new JSONObject();
			object.put("total",projectSummaryService.getTotalCount(year));
			object.put("started",projectSummaryService.getStartedCount(year));
			if(object.getFloatValue("total")>0)
				object.put("bl",String.format("%.2f", object.getFloatValue("started")/object.getFloatValue("total")*100)+"%");
			else
				object.put("bl","0%");
			return R.data(VSTool.encrypt(encryptSign, object.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 统计项目投资情况数
	 * @return`
	 */
	@GetMapping("/statisticsInvestment")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目投资统计", notes = "")
	public R statisticsInvestment(@ApiIgnore @RequestParam String year) {
		JSONObject object = new JSONObject();
		object.put("total",String.format("%.2f",projectSummaryService.getTotalInvestment(year)/10000f));
		object.put("year",String.format("%.2f",projectSummaryService.getYearInvestment(year)/10000f));
		object.put("done",String.format("%.2f",projectSummaryService.getDoneInvestment(year)/10000f));
		if(object.getFloatValue("total")>0) {
			object.put("bl", String.format("%.2f", object.getFloatValue("done") / object.getFloatValue("year") * 100) + "%");
		}
		else
			object.put("bl","0%");
		return R.data(object);
	}

	/**
	 * 统计项目投资情况数-app
	 */
	@PostMapping("/statisticsInvestmentApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "统计项目投资情况数-app", notes = "")
	public R statisticsInvestmentApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("统计项目投资情况数-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String year = jsonParams.getString("year");

			JSONObject object = new JSONObject();
			object.put("total",String.format("%.2f",projectSummaryService.getTotalInvestment(year)/10000f));
			object.put("year",String.format("%.2f",projectSummaryService.getYearInvestment(year)/10000f));
			object.put("done",String.format("%.2f",projectSummaryService.getDoneInvestment(year)/10000f));
			if(object.getFloatValue("total")>0) {
				object.put("bl", String.format("%.2f", object.getFloatValue("done") / object.getFloatValue("year") * 100) + "%");
			}
			else
				object.put("bl","0%");
			return R.data(VSTool.encrypt(encryptSign, object.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 统计项目数
	 */
	@GetMapping("/statisticsTzCount")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目数量统计", notes = "")
	public R statisticsTzCount(@ApiIgnore @RequestParam String projLabel,String year) {
		JSONObject object = new JSONObject();
		object.put("total",projectSummaryService.getTZTotalCount(projLabel,year));
		object.put("started",projectSummaryService.getTZStartedCount(projLabel,year));
		if(object.getFloatValue("total")>0) {
			object.put("bl", String.format("%.2f", object.getFloatValue("started") / object.getFloatValue("total") * 100) + "%");
		}else {
			object.put("bl", "0%");
		}
		return R.data(object);
	}

	/**
	 * 投资项目数量统计
	 */
	@PostMapping("/statisticsTzCountApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "投资项目数量统计", notes = "")
	public R statisticsTzCountApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("投资项目数量统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String projLabel = jsonParams.getString("projLabel");
			String year = jsonParams.getString("year");

			JSONObject object = new JSONObject();
			object.put("total",projectSummaryService.getTZTotalCount(projLabel,year));
			object.put("started",projectSummaryService.getTZStartedCount(projLabel,year));
			if(object.getFloatValue("total")>0) {
				object.put("bl", String.format("%.2f", object.getFloatValue("started") / object.getFloatValue("total") * 100) + "%");
			}else {
				object.put("bl", "0%");
			}
			return R.data(VSTool.encrypt(encryptSign, object.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 统计项目投资情况数
	 * @return`
	 */
	@GetMapping("/statisticsTzInvestment")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目投资统计", notes = "")
	public R statisticsTZInvestment(@ApiIgnore @RequestParam String projLabel,String year) {
		JSONObject object = new JSONObject();
		object.put("total",String.format("%.2f",projectSummaryService.getTZTotalInvestment(projLabel,year)/10000f));
		object.put("year",String.format("%.2f",projectSummaryService.getTZYearInvestment(projLabel,year)/10000f));
		object.put("done",String.format("%.2f",projectSummaryService.getTZDoneInvestment(projLabel,year)/10000f));
		if(object.getFloatValue("total")>0) {
			object.put("bl", String.format("%.2f", object.getFloatValue("done") / object.getFloatValue("year") * 100) + "%");
		}
		else
			object.put("bl","0%");
		return R.data(object);
	}

	/**
	 * 统计项目投资情况数
	 */
	@PostMapping("/statisticsTzInvestmentApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目投资统计-app", notes = "")
	public R statisticsTzInvestmentApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("统计项目投资情况数-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String projLabel = jsonParams.getString("projLabel");
			String year = jsonParams.getString("year");

			JSONObject object = new JSONObject();
			object.put("total",String.format("%.2f",projectSummaryService.getTZTotalInvestment(projLabel,year)/10000f));
			object.put("year",String.format("%.2f",projectSummaryService.getTZYearInvestment(projLabel,year)/10000f));
			object.put("done",String.format("%.2f",projectSummaryService.getTZDoneInvestment(projLabel,year)/10000f));
			if(object.getFloatValue("total")>0) {
				object.put("bl", String.format("%.2f", object.getFloatValue("done") / object.getFloatValue("year") * 100) + "%");
			}
			else
				object.put("bl","0%");
			return R.data(VSTool.encrypt(encryptSign, object.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理详情", notes = "projectSummary")
	public R<ProjectSummary> detail(@ApiIgnore @RequestParam String id){
		QueryWrapper<ProjectSummary> queryWrapper = new QueryWrapper<ProjectSummary>();
		queryWrapper.select(" * ");
		queryWrapper.eq(id!="0","id",id);
		ProjectSummary detail = projectSummaryService.getOne(queryWrapper);
		List<ProjectSpecialist> specialistList = specialistService.list(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, detail.getId()));
		detail.setProjectSpecialistList(specialistList);
		//2024年4月10日21点47分-敏感字段处理开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(detail.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(detail.getSzhyzgbmZrrName())){
			detail.setSzhyzgbmZrrName(sm4.decrypt(detail.getSzhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(detail.getXqhyzgbmZrrName())){
			detail.setXqhyzgbmZrrName(sm4.decrypt(detail.getXqhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getDhhm()) && sm4.checkDataIsEncrypt(detail.getDhhm())){
			detail.setDhhm(sm4.decrypt(detail.getDhhm()));
		}
		if(StringUtils.isNotEmpty(detail.getGddh()) && sm4.checkDataIsEncrypt(detail.getGddh())){
			detail.setGddh(sm4.decrypt(detail.getGddh()));
		}
		if(StringUtils.isNotEmpty(detail.getManagerContact()) && sm4.checkDataIsEncrypt(detail.getManagerContact())){
			detail.setManagerContact(sm4.decrypt(detail.getManagerContact()));
		}
		if(StringUtils.isNotEmpty(detail.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(detail.getBzzgdwZrrName())){
			detail.setBzzgdwZrrName(sm4.decrypt(detail.getBzzgdwZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(detail.getBzzrdwZrrName())){
			detail.setBzzrdwZrrName(sm4.decrypt(detail.getBzzrdwZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getSgfzrName()) && sm4.checkDataIsEncrypt(detail.getSgfzrName())){
			detail.setSgfzrName(sm4.decrypt(detail.getSgfzrName()));
		}
		if(StringUtils.isNotEmpty(detail.getZrrName()) && sm4.checkDataIsEncrypt(detail.getZrrName())){
			detail.setZrrName(sm4.decrypt(detail.getZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getSjbzldName()) && sm4.checkDataIsEncrypt(detail.getSjbzldName())){
			detail.setSjbzldName(sm4.decrypt(detail.getSjbzldName()));
		}
		if(StringUtils.isNotEmpty(detail.getXjbzldName()) && sm4.checkDataIsEncrypt(detail.getXjbzldName())){
			detail.setXjbzldName(sm4.decrypt(detail.getXjbzldName()));
		}
		//2024年4月10日21点47分-敏感字段处理结束
		return R.data(detail);
	}

	/**
	 * 投资项目申报
	 * @param projectSummary
	 * @return
	 */
	@PostMapping("/batchSave")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "申报", notes = "传入json")
	public R batchSave(@Valid @RequestBody ProjectSummary projectSummary){
		if(StringUtils.isBlank(projectSummary.getXmnf())) {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			projectSummary.setXmnf(String.valueOf(year));
		}
		projectSummary.setReportStatus("1");
		projectSummary.setPorjStatus("1");
		boolean save = projectSummaryService.saveProjectSummary(projectSummary);
		//为避免删除不掉，先全部删除项目专员列表，再添加
		//项目专员列表
		if (save) {
			ProjectSummary one = projectSummaryService.getOne(Wrappers.<ProjectSummary>query().lambda()
				.eq(ProjectSummary::getTitle, projectSummary.getTitle())
				.eq(ProjectSummary::getXmType, projectSummary.getXmType())
				.orderByDesc(ProjectSummary::getCreateTime).last("limit 1"));
			List<ProjectSpecialist> list = projectSummary.getProjectSpecialistList();
			list.stream().forEach(p -> p.setProjectId(one.getId().toString()));
			specialistService.listBatchByProjectId(list, one.getId().toString());
		}
		return R.data(save);
	}

	/**
	 * 投资项目修改
	 * @param projectSummary
	 * @return
	 */
	@PostMapping("/batchUpdate")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "修改", notes = "传入json")
	@Transactional(rollbackFor = Exception.class)
	public R batchUpdate(@Valid @RequestBody ProjectSummary projectSummary){
		boolean update = projectSummaryService.updateProjectSummary(projectSummary);
		//为避免删除不掉，先全部删除项目专员列表，再添加
		//项目专员列表
		if (update) {
			/*ProjectSummary one = projectSummaryService.getOne(Wrappers.<ProjectSummary>query().lambda()
				.eq(ProjectSummary::getTitle, projectSummary.getTitle())
				.eq(ProjectSummary::getXmType, projectSummary.getXmType())
				.orderByDesc(ProjectSummary::getCreateTime).last("limit 1"));*/
			List<ProjectSpecialist> list = projectSummary.getProjectSpecialistList();
			list.stream().forEach(p -> p.setProjectId(projectSummary.getId().toString()));
			specialistService.listBatchByProjectId(list, projectSummary.getId().toString());
		}
		return R.data(update);
	}

	/**
	 * 项目入库
	 * @param id
	 * @return
	 */
	@PostMapping("/batchRk")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "县级送审", notes = "传入id")
	public R batchRk(@RequestParam String id,@RequestParam(value = "title", required = false) String title,@RequestParam String userIds,@RequestParam String sync) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		Long userId = user.getId();
		String areaCode = sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getAreaCode();
		String glyId = PropConstant.getProjectShzhId(areaCode);
		String[] idStr = id.split(",");
		String deptName = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		boolean result = false;
		if(idStr.length>0){
			for(int i=0;i<idStr.length;i++){
				if(areaCode.length() > 4){
					if(glyId.equals(userId.toString())){
						result = supervisionSubmitAuditService.saveSubmitAudit(idStr[i],title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
						String xmdl = projectSummaryService.getById(idStr[i]).getXmdl();
						String xmdlName = "";
						if (xmdl.equals("1")){
							xmdlName = "投资项目清单";
						}else if (xmdl.equals("2")){
							xmdlName = "新增投资项目清单";
						}else if (xmdl.equals("3")){
							xmdlName = "前期项目清单";
						}else if (xmdl.equals("4")){
							xmdlName = "新增前期项目清单";
						}else if (xmdl.equals("5")){
							xmdlName = "中央和省级预算内项目";
						}else if (xmdl.equals("6")){
							xmdlName = "政府专项债券项目";
						}
						ProjectLog projectLog = new ProjectLog();
						projectLog.setProjId(Long.parseLong(idStr[i]));
						String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
						projectLog.setHandleUser(userNameDecrypt);
						projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
						projectLog.setHandleType("项目送审");
						String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
						projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName);
						projectLogService.save(projectLog);
						result = projectSummaryService.updateProjStatus(idStr[i],"5");

						//发送消息
						String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(idStr[i]));//消息主键（业务主键）
						unifyMessage.setMsgTitle("项目纳入送审");//消息标题
						unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
						unifyMessage.setMsgPlatform("web");//平台：web或app
						unifyMessage.setReceiveUser(userIds);
						unifyMessage.setMsgIntro(msgIntro);//消息简介
						unifyMessage.setMsgSubitem("");//消息分项
						unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
						unifyMessage.setCreateTime(new Date());
						unifyMessageService.sendMessageInfo(unifyMessage);
						String xmbq = projectSummaryService.getById(idStr[i]).getProjLabel();
						if (xmdl.equals("1")||xmdl.equals("2")){
							if (xmbq.contains("1")){
								unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
							}else if(xmbq.contains("2")){
								unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
							}else {
								unifyMessage.setMsgSubitem("投资项目清单");//消息分项
							}
						}else if (xmdl.equals("3")||xmdl.equals("4")){
							unifyMessage.setMsgSubitem("前期项目清单");//消息分项
						}else if (xmdl.equals("5")){
							unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
						}else if (xmdl.equals("6")) {
							unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
						}
						unifyMessage.setId(null);
						unifyMessage.setMsgPlatform("app");
						unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						unifyMessage.setTwoLevelType("32");//项目纳入
						unifyMessageService.sendMessageInfo(unifyMessage);



					}else{
						result = supervisionSubmitAuditService.saveSubmitAudit(idStr[i],title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
						String xmdl = projectSummaryService.getById(idStr[i]).getXmdl();
						String xmdlName = "";
						if (xmdl.equals("1")){
							xmdlName = "投资项目清单";
						}else if (xmdl.equals("2")){
							xmdlName = "新增投资项目清单";
						}else if (xmdl.equals("3")){
							xmdlName = "前期项目清单";
						}else if (xmdl.equals("4")){
							xmdlName = "新增前期项目清单";
						}else if (xmdl.equals("5")){
							xmdlName = "中央和省级预算内项目";
						}else if (xmdl.equals("6")){
							xmdlName = "政府专项债券项目";
						}
						ProjectLog projectLog = new ProjectLog();
						projectLog.setProjId(Long.parseLong(idStr[i]));
						String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
						projectLog.setHandleUser(userNameDecrypt);
						projectLog.setHandleType("项目送审");
						projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
						String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
						projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName);
						projectLogService.save(projectLog);
						result = projectSummaryService.updateProjStatus(idStr[i],"3");

						String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(idStr[i]));//消息主键（业务主键）
						unifyMessage.setMsgTitle("项目纳入送审");//消息标题
						unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
						unifyMessage.setMsgPlatform("web");//平台：web或app
						unifyMessage.setReceiveUser(userIds);
						unifyMessage.setMsgIntro(msgIntro);//消息简介
						unifyMessage.setMsgSubitem("");//消息分项
						unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
						unifyMessage.setCreateTime(new Date());
						unifyMessageService.sendMessageInfo(unifyMessage);
						String xmbq = projectSummaryService.getById(idStr[i]).getProjLabel();
						if (xmdl.equals("1")||xmdl.equals("2")){
							if (xmbq.contains("1")){
								unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
							}else if(xmbq.contains("2")){
								unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
							}else {
								unifyMessage.setMsgSubitem("投资项目清单");//消息分项
							}
						}else if (xmdl.equals("3")||xmdl.equals("4")){
							unifyMessage.setMsgSubitem("前期项目清单");//消息分项
						}else if (xmdl.equals("5")){
							unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
						}else if (xmdl.equals("6")) {
							unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
						}
						unifyMessage.setId(null);
						unifyMessage.setMsgPlatform("app");
						unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						unifyMessage.setTwoLevelType("32");//项目纳入
						unifyMessageService.sendMessageInfo(unifyMessage);
					}
				}else{
					if(glyId.equals(userId.toString())){
						result = projectSummaryService.projectSummaryRk(idStr[i],"1");
						ProjectLog projectLog1 = new ProjectLog();
						projectLog1.setProjId(Long.parseLong(idStr[i]));
						String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
						projectLog1.setHandleUser(userNameDecrypt);
						projectLog1.setHandleType("项目审核");
						projectLog1.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
						projectLog1.setHandleContent("【"+projectLog1.getHandleUser()+"】审核通过【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入申请");
						projectLogService.save(projectLog1);
						//发送消息
						String msgIntro = "【"+deptName+"】审核通过【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入申请";
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(idStr[i]));//消息主键（业务主键）
						unifyMessage.setMsgTitle("项目纳入");//消息标题
						unifyMessage.setMsgType("31");//消息类型，字典编码：web_message_type
						unifyMessage.setMsgPlatform("web");//平台：web或app
						unifyMessage.setReceiveUser(projectSummaryService.getById(idStr[i]).getCreateUser().toString());
						unifyMessage.setMsgIntro(msgIntro);//消息简介
						unifyMessage.setMsgSubitem("");//消息分项
						unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
						unifyMessage.setCreateTime(new Date());
						unifyMessageService.sendMessageInfo(unifyMessage);
						String xmdl = projectSummaryService.getById(idStr[i]).getXmdl();
						String xmbq = projectSummaryService.getById(idStr[i]).getProjLabel();
						if (xmdl.equals("1")||xmdl.equals("2")){
							if (xmbq.contains("1")){
								unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
							}else if(xmbq.contains("2")){
								unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
							}else {
								unifyMessage.setMsgSubitem("投资项目清单");//消息分项
							}
						}else if (xmdl.equals("3")||xmdl.equals("4")){
							unifyMessage.setMsgSubitem("前期项目清单");//消息分项
						}else if (xmdl.equals("5")){
							unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
						}else if (xmdl.equals("6")) {
							unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
						}
						unifyMessage.setId(null);
						unifyMessage.setMsgPlatform("app");
						unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						unifyMessage.setTwoLevelType("31");//项目纳入
						unifyMessageService.sendMessageInfo(unifyMessage);
					}else{
						result = supervisionSubmitAuditService.saveSubmitAudit(idStr[i],title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
						String xmdl = projectSummaryService.getById(idStr[i]).getXmdl();
						String xmdlName = "";
						if (xmdl.equals("1")){
							xmdlName = "投资项目清单";
						}else if (xmdl.equals("2")){
							xmdlName = "新增投资项目清单";
						}else if (xmdl.equals("3")){
							xmdlName = "前期项目清单";
						}else if (xmdl.equals("4")){
							xmdlName = "新增前期项目清单";
						}else if (xmdl.equals("5")){
							xmdlName = "中央和省级预算内项目";
						}else if (xmdl.equals("6")){
							xmdlName = "政府专项债券项目";
						}
						ProjectLog projectLog = new ProjectLog();
						projectLog.setProjId(Long.parseLong(idStr[i]));
						String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
						projectLog.setHandleUser(userNameDecrypt);
						projectLog.setHandleType("项目送审");
						projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
						String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
						projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName);
						projectLogService.save(projectLog);
						result = projectSummaryService.updateProjStatus(idStr[i],"5");

						//发送消息
						String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(idStr[i]).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(idStr[i]));//消息主键（业务主键）
						unifyMessage.setMsgTitle("项目纳入送审");//消息标题
						unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
						unifyMessage.setMsgPlatform("web");//平台：web或app
						unifyMessage.setReceiveUser(userIds);
						unifyMessage.setMsgIntro(msgIntro);//消息简介
						unifyMessage.setMsgSubitem("");//消息分项
						unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
						unifyMessage.setCreateTime(new Date());
						unifyMessageService.sendMessageInfo(unifyMessage);
						String xmbq = projectSummaryService.getById(idStr[i]).getProjLabel();
						if (xmdl.equals("1")||xmdl.equals("2")){
							if (xmbq.contains("1")){
								unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
							}else if(xmbq.contains("2")){
								unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
							}else {
								unifyMessage.setMsgSubitem("投资项目清单");//消息分项
							}
						}else if (xmdl.equals("3")||xmdl.equals("4")){
							unifyMessage.setMsgSubitem("前期项目清单");//消息分项
						}else if (xmdl.equals("5")){
							unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
						}else if (xmdl.equals("6")) {
							unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
						}
						unifyMessage.setId(null);
						unifyMessage.setMsgPlatform("app");
						unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						unifyMessage.setTwoLevelType("32");//项目纳入
						unifyMessageService.sendMessageInfo(unifyMessage);

					}
				}
			}
		}else{
			if(areaCode.length() > 4){
				if(glyId.equals(userId.toString())){
					result = supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
					String xmdl = projectSummaryService.getById(id).getXmdl();
					String xmdlName = "";
					if (xmdl.equals("1")){
						xmdlName = "投资项目清单";
					}else if (xmdl.equals("2")){
						xmdlName = "新增投资项目清单";
					}else if (xmdl.equals("3")){
						xmdlName = "前期项目清单";
					}else if (xmdl.equals("4")){
						xmdlName = "新增前期项目清单";
					}else if (xmdl.equals("5")){
						xmdlName = "中央和省级预算内项目";
					}else if (xmdl.equals("6")){
						xmdlName = "政府专项债券项目";
					}
					ProjectLog projectLog = new ProjectLog();
					projectLog.setProjId(Long.parseLong(id));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog.setHandleUser(userNameDecrypt);
					projectLog.setHandleType("项目送审");
					projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
					projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName);
					projectLogService.save(projectLog);
					result = projectSummaryService.updateProjStatus(id,"5");

					//发送消息
					String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目纳入送审");//消息标题
					unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(userIds);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmbq = projectSummaryService.getById(id).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("32");//项目纳入
					unifyMessageService.sendMessageInfo(unifyMessage);

				}else{
					result = supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
					String xmdl = projectSummaryService.getById(id).getXmdl();
					String xmdlName = "";
					if (xmdl.equals("1")){
						xmdlName = "投资项目清单";
					}else if (xmdl.equals("2")){
						xmdlName = "新增投资项目清单";
					}else if (xmdl.equals("3")){
						xmdlName = "前期项目清单";
					}else if (xmdl.equals("4")){
						xmdlName = "新增前期项目清单";
					}else if (xmdl.equals("5")){
						xmdlName = "中央和省级预算内项目";
					}else if (xmdl.equals("6")){
						xmdlName = "政府专项债券项目";
					}
					ProjectLog projectLog = new ProjectLog();
					projectLog.setProjId(Long.parseLong(id));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog.setHandleUser(userNameDecrypt);
					projectLog.setHandleType("项目送审");
					projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
					projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName);
					projectLogService.save(projectLog);
					result = projectSummaryService.updateProjStatus(id,"3");

					//发送消息
					String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目纳入送审");//消息标题
					unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(userIds);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmbq = projectSummaryService.getById(id).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("32");//项目纳入
					unifyMessageService.sendMessageInfo(unifyMessage);

				}
			}else{
				if(glyId.equals(userId.toString())){
					result = projectSummaryService.projectSummaryRk(id,"1");
					ProjectLog projectLog1 = new ProjectLog();
					projectLog1.setProjId(Long.parseLong(id));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog1.setHandleUser(userNameDecrypt);
					projectLog1.setHandleType("项目审核");
					projectLog1.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					projectLog1.setHandleContent("【"+projectLog1.getHandleUser()+"】审核通过【"+projectSummaryService.getById(id).getTitle()+"】纳入申请");
					projectLogService.save(projectLog1);

					//发送消息
					String msgIntro = "【"+deptName+"】审核通过【"+projectSummaryService.getById(id).getTitle()+"】纳入申请";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目纳入");//消息标题
					unifyMessage.setMsgType("31");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(projectSummaryService.getById(id).getCreateUser().toString());
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmdl = projectSummaryService.getById(id).getXmdl();
					String xmbq = projectSummaryService.getById(id).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("31");//项目纳入
					unifyMessageService.sendMessageInfo(unifyMessage);
				}else{
					result = supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
					String xmdl = projectSummaryService.getById(id).getXmdl();
					String xmdlName = "";
					if (xmdl.equals("1")){
						xmdlName = "投资项目清单";
					}else if (xmdl.equals("2")){
						xmdlName = "新增投资项目清单";
					}else if (xmdl.equals("3")){
						xmdlName = "前期项目清单";
					}else if (xmdl.equals("4")){
						xmdlName = "新增前期项目清单";
					}else if (xmdl.equals("5")){
						xmdlName = "中央和省级预算内项目";
					}else if (xmdl.equals("6")){
						xmdlName = "政府专项债券项目";
					}
					ProjectLog projectLog = new ProjectLog();
					projectLog.setProjId(Long.parseLong(id));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog.setHandleUser(userNameDecrypt);
					projectLog.setHandleType("项目送审");
					projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
					projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName);
					projectLogService.save(projectLog);
					result = projectSummaryService.updateProjStatus(id,"5");

					//发送消息
					String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目纳入送审");//消息标题
					unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(userIds);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmbq = projectSummaryService.getById(id).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("32");//项目纳入
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
			}
		}
		return R.status(result);
	}

	/**
	 * 市级送审
	 * @param id
	 * @param title
	 * @param userIds
	 * @param sync
	 * @return
	 */
	@PostMapping("/batchSs")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "市级送审", notes = "传入id")
	public R batchSs(@RequestParam String id,@RequestParam String title,@RequestParam String userIds,@RequestParam String sync) {
		boolean result = false;
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		result = supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync, StatusConstant.OPERATION_TYPE_WARE);
		String xmdl = projectSummaryService.getById(id).getXmdl();
		String xmdlName = "";
		if (xmdl.equals("1")){
			xmdlName = "投资项目清单";
		}else if (xmdl.equals("2")){
			xmdlName = "新增投资项目清单";
		}else if (xmdl.equals("3")){
			xmdlName = "前期项目清单";
		}else if (xmdl.equals("4")){
			xmdlName = "新增前期项目清单";
		}else if (xmdl.equals("5")){
			xmdlName = "中央和省级预算内项目";
		}else if (xmdl.equals("6")){
			xmdlName = "政府专项债券项目";
		}
		ProjectLog projectLog = new ProjectLog();
		projectLog.setProjId(Long.parseLong(id));
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		projectLog.setHandleUser(userNameDecrypt);
		projectLog.setHandleType("项目送审");
		projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
		String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
		projectLog.setHandleContent("【"+handleUserDecrypt+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName);
		projectLogService.save(projectLog);
		//发送消息
		String deptName = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		String msgIntro = "【"+deptName+"】申请【"+projectSummaryService.getById(id).getTitle()+"】纳入"+xmdlName+"，请及时审核。";
		UnifyMessage unifyMessage = new UnifyMessage();
		unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
		unifyMessage.setMsgTitle("项目纳入送审");//消息标题
		unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
		unifyMessage.setMsgPlatform("web");//平台：web或app
		unifyMessage.setReceiveUser(userIds);
		unifyMessage.setMsgIntro(msgIntro);//消息简介
		unifyMessage.setMsgSubitem("");//消息分项
		unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
		unifyMessage.setCreateTime(new Date());
		unifyMessageService.sendMessageInfo(unifyMessage);
		String xmbq = projectSummaryService.getById(id).getProjLabel();
		if (xmdl.equals("1")||xmdl.equals("2")){
			if (xmbq.contains("1")){
				unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
			}else if(xmbq.contains("2")){
				unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
			}else {
				unifyMessage.setMsgSubitem("投资项目清单");//消息分项
			}
		}else if (xmdl.equals("3")||xmdl.equals("4")){
			unifyMessage.setMsgSubitem("前期项目清单");//消息分项
		}else if (xmdl.equals("5")){
			unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
		}else if (xmdl.equals("6")) {
			unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
		}
		unifyMessage.setId(null);
		unifyMessage.setMsgPlatform("app");
		unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
		unifyMessage.setTwoLevelType("32");//项目送审
		unifyMessageService.sendMessageInfo(unifyMessage);

		if(result){
			result = projectSummaryService.updateProjStatus(id,"5");
		}
		return R.status(result);
	}

	/**
	 * 项目退回
	 * @param param
	 * @return
	 */
	@PostMapping("/batchRebake")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "退回", notes = "传入id")
	public R batchRebake(@Valid @RequestBody String param) {
		boolean result = false;
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String areaCode = sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getAreaCode();
		JSONObject paramObj =  JSONObject.parseObject(param);
		String ids = paramObj.getString("ids");
		String[] id =ids.split(",");
		if(id.length>0){
			for(int i=0;i<id.length;i++){
				if(areaCode.length()>4){
					result = projectSummaryService.projectSummaryRebake(id[i],"2","4");
					ProjectLog projectLog = new ProjectLog();
					projectLog.setProjId(Long.parseLong(id[i]));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog.setHandleUser(userNameDecrypt);
					projectLog.setHandleType("项目退回");
					projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
					projectLog.setHandleContent("【"+handleUserDecrypt+"】退回【"+projectSummaryService.getById(id[i]).getTitle()+"】纳入申请");
					projectLogService.save(projectLog);

					String msgIntro = "【"+userNameDecrypt+"】退回【"+projectSummaryService.getById(id[i]).getTitle()+"】纳入申请";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id[i]));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目退回");//消息标题
					unifyMessage.setMsgType("33");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(projectSummaryService.getById(id[i]).getCreateUser().toString());
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmdl = projectSummaryService.getById(id[i]).getXmdl();
					String xmbq = projectSummaryService.getById(id[i]).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("33");//项目退回
					unifyMessageService.sendMessageInfo(unifyMessage);
				}else{
					result = projectSummaryService.projectSummaryRebake(id[i],"12","6");
					ProjectLog projectLog = new ProjectLog();
					projectLog.setProjId(Long.parseLong(id[i]));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					projectLog.setHandleUser(userNameDecrypt);
					projectLog.setHandleType("项目退回");
					projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
					String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
					projectLog.setHandleContent("【"+handleUserDecrypt+"】退回【"+projectSummaryService.getById(id[i]).getTitle()+"】纳入申请");
					projectLogService.save(projectLog);
					String msgIntro = "【"+userNameDecrypt+"】退回【"+projectSummaryService.getById(id[i]).getTitle()+"】纳入申请";
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(id[i]));//消息主键（业务主键）
					unifyMessage.setMsgTitle("项目退回");//消息标题
					unifyMessage.setMsgType("33");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(projectSummaryService.getById(id[i]).getCreateUser().toString());
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem("");//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					String xmdl = projectSummaryService.getById(id[i]).getXmdl();
					String xmbq = projectSummaryService.getById(id[i]).getProjLabel();
					if (xmdl.equals("1")||xmdl.equals("2")){
						if (xmbq.contains("1")){
							unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
						}else if(xmbq.contains("2")){
							unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
						}else {
							unifyMessage.setMsgSubitem("投资项目清单");//消息分项
						}
					}else if (xmdl.equals("3")||xmdl.equals("4")){
						unifyMessage.setMsgSubitem("前期项目清单");//消息分项
					}else if (xmdl.equals("5")){
						unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
					}else if (xmdl.equals("6")) {
						unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
					}
					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
					unifyMessage.setTwoLevelType("33");//项目退回
					unifyMessageService.sendMessageInfo(unifyMessage);
				}

			}
		}else{
			if(areaCode.length()>4){
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				result = projectSummaryService.projectSummaryRebake(ids,"2","4");
				ProjectLog projectLog = new ProjectLog();
				projectLog.setProjId(Long.parseLong(ids));
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleType("项目审核");
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】退回【"+projectSummaryService.getById(ids).getTitle()+"】纳入申请");
				projectLogService.save(projectLog);
				String msgIntro = "【"+userNameDecrypt+"】退回【"+projectSummaryService.getById(id).getTitle()+"】纳入申请";
				UnifyMessage unifyMessage = new UnifyMessage();
				unifyMessage.setMsgId(Long.valueOf(ids));//消息主键（业务主键）
				unifyMessage.setMsgTitle("项目退回");//消息标题
				unifyMessage.setMsgType("33");//消息类型，字典编码：web_message_type
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(projectSummaryService.getById(id).getCreateUser().toString());
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem("");//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);
				String xmdl = projectSummaryService.getById(ids).getXmdl();
				String xmbq = projectSummaryService.getById(ids).getProjLabel();
				if (xmdl.equals("1")||xmdl.equals("2")){
					if (xmbq.contains("1")){
						unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
					}else if(xmbq.contains("2")){
						unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
					}else {
						unifyMessage.setMsgSubitem("投资项目清单");//消息分项
					}
				}else if (xmdl.equals("3")||xmdl.equals("4")){
					unifyMessage.setMsgSubitem("前期项目清单");//消息分项
				}else if (xmdl.equals("5")){
					unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
				}else if (xmdl.equals("6")) {
					unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
				}
				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
				unifyMessage.setTwoLevelType("33");//项目退回
				unifyMessageService.sendMessageInfo(unifyMessage);
			}else{
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				result = projectSummaryService.projectSummaryRebake(ids,"12","6");
				ProjectLog projectLog = new ProjectLog();
				projectLog.setProjId(Long.parseLong(ids));
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleType("项目审核");
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】退回【"+projectSummaryService.getById(ids).getTitle()+"】纳入申请");
				projectLogService.save(projectLog);
				String msgIntro = "【"+userNameDecrypt+"】退回【"+projectSummaryService.getById(id).getTitle()+"】纳入申请";
				UnifyMessage unifyMessage = new UnifyMessage();
				unifyMessage.setMsgId(Long.valueOf(ids));//消息主键（业务主键）
				unifyMessage.setMsgTitle("项目退回");//消息标题
				unifyMessage.setMsgType("33");//消息类型，字典编码：web_message_type
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(projectSummaryService.getById(id).getCreateUser().toString());
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem("");//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);
				String xmdl = projectSummaryService.getById(ids).getXmdl();
				String xmbq = projectSummaryService.getById(ids).getProjLabel();
				if (xmdl.equals("1")||xmdl.equals("2")){
					if (xmbq.contains("1")){
						unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
					}else if(xmbq.contains("2")){
						unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
					}else {
						unifyMessage.setMsgSubitem("投资项目清单");//消息分项
					}
				}else if (xmdl.equals("3")||xmdl.equals("4")){
					unifyMessage.setMsgSubitem("前期项目清单");//消息分项
				}else if (xmdl.equals("5")){
					unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
				}else if (xmdl.equals("6")) {
					unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
				}
				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
				unifyMessage.setTwoLevelType("33");//项目退回
				unifyMessageService.sendMessageInfo(unifyMessage);
			}

		}

		return R.status(result);
	}

	/**
	 * 项目删除
	 * @param id
	 * @return
	 */
	@PostMapping("/batchDelete")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "删除", notes = "传入id")
	public R batchDelete(@Valid @RequestParam String id) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		boolean result = false;
		result = projectSummaryService.removeById(id);
		if(result){
			ProjectLog projectLog = new ProjectLog();
			projectLog.setProjId(Long.parseLong(id));
			projectLog.setHandleType("项目删除");
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】将【"+projectSummaryService.getById(id).getTitle()+"】删除");
			projectLogService.save(projectLog);
			result = projectPhasePlanService.projectPhasePlanDelete(id);


			if(result){
				result = projectPhaseRemindService.projectPhaseRemindDelete(id);
			}
		}


		//发送消息
		BladeUser currentUser = AuthUtil.getUser();
		List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
		String receiver="";
		for(int i=0;i<leaders.size();i++){
			if(i==leaders.size()-1){
				receiver+=leaders.get(i).getId().toString();
			}else{
				receiver+=leaders.get(i).getId().toString()+",";
			}
		}
		String deptName = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		String msgIntro = "【"+deptName+"】删除【"+projectSummaryService.getById(id).getTitle()+"】";
		UnifyMessage unifyMessage = new UnifyMessage();
		unifyMessage.setMsgId(Long.valueOf(id));//消息主键（业务主键）
		unifyMessage.setMsgTitle("项目删除");//消息标题
		unifyMessage.setMsgType("32");//消息类型，字典编码：web_message_type
		unifyMessage.setMsgPlatform("web");//平台：web或app
		unifyMessage.setReceiveUser(receiver);
		unifyMessage.setMsgIntro(msgIntro);//消息简介
		unifyMessage.setMsgSubitem("");//消息分项
		unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
		unifyMessage.setCreateTime(new Date());
		unifyMessageService.sendMessageInfo(unifyMessage);
		return R.status(result);
	}

	/**
	 * 项目挂牌
	 * @param param
	 * @return
	 */
	@PostMapping("/batchListing")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目批量挂牌", notes = "传入json")
	public R batchListing(@Valid @RequestBody String param) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		boolean result = false;
		JSONObject paramObj =  JSONObject.parseObject(param);
		String ids = paramObj.getString("ids");
		String autoState = paramObj.getString("autoState");
		String autoStateName = "";
		if(autoState.equals("1")){
			autoStateName = "蓝牌";
		}else if(autoState.equals("2")){
			autoStateName = "黄牌";
		}else{
			autoStateName = "红牌";
		}
		String[] id =ids.split(",");
		if(id.length>0){
			for(int i=0;i<id.length;i++){
				result = projectSummaryService.projectSummaryListing(id[i], autoState);
				ProjectLog projectLog = new ProjectLog();
				projectLog.setProjId(Long.parseLong(id[i]));
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleType("项目挂牌");
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】将【"+projectSummaryService.getById(id[i]).getTitle()+"】挂"+autoStateName);
				projectLogService.save(projectLog);
			}
		}else{
			result = projectSummaryService.projectSummaryListing(ids, autoState);
			ProjectLog projectLog = new ProjectLog();
			projectLog.setProjId(Long.parseLong(ids));
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleType("项目挂牌");
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】将【"+projectSummaryService.getById(id).getTitle()+"】挂"+autoStateName);
			projectLogService.save(projectLog);
		}

		return R.status(result);
	}

	/**
	 * 首页项目地图搜索
	 */
	@GetMapping("/projectMap")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "首页项目地图搜索", notes = "")
	public R<IPage<MapPorjectVO>> queryProjectMap(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		//添加判断权限
		entity = getAuth(entity);
//		entity.put("zrr",AuthUtil.getUserId());
		IPage<MapPorjectVO> pages = projectSummaryService.queryProjectMap(Condition.getPage(query), entity);
		List<MapPorjectVO> records = pages.getRecords();
		for (int i = 0; i < records.size(); i++) {
			MapPorjectVO vo = records.get(i);
			vo.setImageUrl(projectFilesService.getProjectPicByProjId(vo.getId()));
		}
		return R.data(pages);
	}

	/**
	 * 首页项目地图搜索
	 */
	@GetMapping("/projectMapNoPage")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "首页项目地图搜索", notes = "")
	public R<List<MapPorjectVO>> projectMapNoPage(@ApiIgnore @RequestParam Map<String, Object> entity) {
		//添加判断权限
		entity = getAuth(entity);
//		entity.put("zrr",AuthUtil.getUserId());
		List<MapPorjectVO> records = projectSummaryService.queryProjectMapNoPage(entity);
		for (int i = 0; i < records.size(); i++) {
			MapPorjectVO vo = records.get(i);
			vo.setImageUrl(projectFilesService.getProjectPicByProjId(vo.getId()));
		}
		return R.data(records);
	}
	/**
	 * 投资项目基础信息列表分页查询
	 * @return
	 */
	@GetMapping("/getTzProjectSummaryList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "投资项目基础信息列表", notes = "")
	public R<IPage<TzProjectSummaryVO>> getTzProjectSummaryList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		SM4Crypto crypto = SM4Crypto.getInstance();
		String name = crypto.checkDataIsEncrypt(user.getName()) ? crypto.decrypt(user.getName()) : user.getName();
		String phone = crypto.checkDataIsEncrypt(user.getPhone()) ? crypto.decrypt(user.getPhone()) : user.getPhone();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();
		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");
		String reportStatusStr ="";
		if(entity.get("reportStatusPara") == null){
			entity.put("reportStatus","");
		}else{
			reportStatusStr = entity.get("reportStatusPara").toString();
			entity.put("reportStatus",reportStatusStr);
		}
		if(entity.get("sfzg") != null && "1".equals(entity.get("sfzg").toString())){
			entity.put("zgdwzrr",AuthUtil.getDeptId());
		}
		QueryWrapper<ProjectSummary>  queryWrapper  = Condition.getQueryWrapper(entity, ProjectSummary.class);

		//IPage<ProjectSummary> pages = projectSummaryService.page(Condition.getPage(query), queryWrapper);
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<TzProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<TzProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			TzProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, TzProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
//							List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
//							if(!projectPhaseRemind.isEmpty()){
//								if(projectPhaseRemind.get(0).getReportTime()!=null && calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
//									vo.setReportStatus("7");
//									proj.setReportStatus("7");
//									projectSummaryService.updateById(proj);
//								}
//							}
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
	//								List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
	//								if(!projectPhaseRemind.isEmpty()){
	//									if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
	//										vo.setReportStatus("7");
	//										proj.setReportStatus("7");
	//									}
	//								}
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			try{
				switch (month){
					case 1:
						vo.setDyjh(Float.parseFloat(vo.getJhtz01()));
						break;
					case 2:
						vo.setDyjh(Float.parseFloat(vo.getJhtz02()));
						break;
					case 3:
						vo.setDyjh(Float.parseFloat(vo.getJhtz03()));
						break;
					case 4:
						vo.setDyjh(Float.parseFloat(vo.getJhtz04()));
						break;
					case 5:
						vo.setDyjh(Float.parseFloat(vo.getJhtz05()));
						break;
					case 6:
						vo.setDyjh(Float.parseFloat(vo.getJhtz06()));
						break;
					case 7:
						vo.setDyjh(Float.parseFloat(vo.getJhtz07()));
						break;
					case 8:
						vo.setDyjh(Float.parseFloat(vo.getJhtz08()));
						break;
					case 9:
						vo.setDyjh(Float.parseFloat(vo.getJhtz09()));
						break;
					case 10:
						vo.setDyjh(Float.parseFloat(vo.getJhtz10()));
						break;
					case 11:
						vo.setDyjh(Float.parseFloat(vo.getJhtz11()));
						break;
					case 12:
						vo.setDyjh(Float.parseFloat(vo.getJhtz12()));
						break;
				}
			}catch (Exception e){
				System.out.println(e.getMessage());
				vo.setDyjh(0f);
			}
			vo.setJhce(vo.getTotalEconomic()-vo.getDyjh());
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}
			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			//获取项目专员列表
			List<ProjectSpecialist> list = specialistService.list(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, proj.getId()));
			vo.setProjectSpecialistList(list);
			vo.setCanReport(list.stream().anyMatch(special -> special.getProjectSpecialist().equals(name) && special.getProjectSpecialistTel().equals(phone)));
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		return R.data(pageVo);
	}

	/**
	 * 投资项目基础信息列表分页查询
	 * @return
	 */
	@PostMapping("/getTzProjectSummaryListApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "投资项目基础信息列表", notes = "")
	public R getTzProjectSummaryListApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("投资项目基础信息列表分页查询-app",params);
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
		Map<String, Object> entity = new HashMap<>(jsonParams);

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();
		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");
		String reportStatusStr ="";
		if(entity.get("reportStatusPara") == null){
			entity.put("reportStatus","");
		}else{
			reportStatusStr = entity.get("reportStatusPara").toString();
			entity.put("reportStatus",reportStatusStr);
		}
		if(entity.get("sfzg") != null && "1".equals(entity.get("sfzg").toString())){
			entity.put("zgdwzrr",AuthUtil.getDeptId());
		}
		Query query = new Query();
		query.setCurrent(jsonParams.getInteger("current"));
		query.setSize(jsonParams.getInteger("size"));
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<TzProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<TzProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			TzProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, TzProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			try{
				switch (month){
					case 1:
						vo.setDyjh(Float.parseFloat(vo.getJhtz01()));
						break;
					case 2:
						vo.setDyjh(Float.parseFloat(vo.getJhtz02()));
						break;
					case 3:
						vo.setDyjh(Float.parseFloat(vo.getJhtz03()));
						break;
					case 4:
						vo.setDyjh(Float.parseFloat(vo.getJhtz04()));
						break;
					case 5:
						vo.setDyjh(Float.parseFloat(vo.getJhtz05()));
						break;
					case 6:
						vo.setDyjh(Float.parseFloat(vo.getJhtz06()));
						break;
					case 7:
						vo.setDyjh(Float.parseFloat(vo.getJhtz07()));
						break;
					case 8:
						vo.setDyjh(Float.parseFloat(vo.getJhtz08()));
						break;
					case 9:
						vo.setDyjh(Float.parseFloat(vo.getJhtz09()));
						break;
					case 10:
						vo.setDyjh(Float.parseFloat(vo.getJhtz10()));
						break;
					case 11:
						vo.setDyjh(Float.parseFloat(vo.getJhtz11()));
						break;
					case 12:
						vo.setDyjh(Float.parseFloat(vo.getJhtz12()));
						break;
				}
			}catch (Exception e){
				System.out.println(e.getMessage());
				vo.setDyjh(0f);
			}
			vo.setJhce(vo.getTotalEconomic()-vo.getDyjh());
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}
			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		JSONObject jsonObject = objectMapper.convertValue(pageVo, JSONObject.class);
		return R.data(VSTool.encrypt(encryptSign, jsonObject.toJSONString(), VSTool.CHN));
	}

	/**
	 * 导出投资项目excel1
	 * @param
	 * @param
	 */
	@GetMapping("export")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "投资项目-项目信息导出", notes = "传入projectSummary")
	public void exportUser(ProjectSummaryExcel1 projectSummaryExcel1, HttpServletResponse response) {


/*			//sql查询条件
			Object title = entity.get("title");//项目名称
			Object xmType = entity.get("xmType");//项目类型
			Object xqhyzgbmName = entity.get("xqhyzgbmName");//县区行业主管部门
			Object sfnrtj = entity.get("sfnrtj");//是否纳入统计库
			Object projNature = entity.get("projNature");//建设性质
			Object projLabel = entity.get("projLabel");//项目标签
			Object bzzrdwName = entity.get("bzzrdwName");//责任单位
			Object reportStatus = entity.get("reportStatus");//调度情况
			Object xmAddress = entity.get("xmAddress");//所属区域
			Object xjldName = entity.get("xjldName");//县级领导
			Object projMain = entity.get("projMain");//投资类别
			Object xmnf = entity.get("xmnf");//年份
			Object sjbzldName = entity.get("sjbzldName");//市级包抓领导
			Object szhyzgbmName = entity.get("szhyzgbmName");//市直行业主管部门
			Object sfkfg = entity.get("sfkfg");//是否开复工
			Object porjStatus = entity.get("porjStatus");//纳入情况


			LambdaQueryWrapper<ProjectSummary> queryWrapper = Wrappers.<ProjectSummary>query().lambda()
				.like(!StringUtil.isEmpty(title),ProjectSummary::getTitle,title)
				.eq(!StringUtil.isEmpty(xmType),ProjectSummary::getXmType,xmType)
				.like(!StringUtil.isEmpty(xqhyzgbmName),ProjectSummary::getXqhyzgbmName,xqhyzgbmName)
				.eq(!StringUtil.isEmpty(sfnrtj),ProjectSummary::getSfnrtj,sfnrtj)
				.eq(!StringUtil.isEmpty(projNature),ProjectSummary::getProjNature,projNature)
				.eq(!StringUtil.isEmpty(projLabel),ProjectSummary::getProjLabel,projLabel)
				.like(!StringUtil.isEmpty(bzzrdwName),ProjectSummary::getBzzrdwName,bzzrdwName)
				.eq(!StringUtil.isEmpty(reportStatus),ProjectSummary::getReportStatus,reportStatus)
				.eq(!StringUtil.isEmpty(xmAddress),ProjectSummary::getXmAddress,xmAddress)
				.like(!StringUtil.isEmpty(xjldName),ProjectSummary::getXjldName,xjldName)
				.eq(!StringUtil.isEmpty(projMain),ProjectSummary::getProjMain,projMain)
				.eq(!StringUtil.isEmpty(xmnf),ProjectSummary::getXmnf,xmnf)
				.like(!StringUtil.isEmpty(sjbzldName),ProjectSummary::getSjbzldName,sjbzldName)
				.like(!StringUtil.isEmpty(szhyzgbmName),ProjectSummary::getSzhyzgbmName,szhyzgbmName)
				.eq(!StringUtil.isEmpty(sfkfg),ProjectSummary::getSfkfg,sfkfg)
				.eq(!StringUtil.isEmpty(porjStatus),ProjectSummary::getPorjStatus,porjStatus)
				.and(Wrappers -> {
					Wrappers.eq(ProjectSummary::getPorjStatus,porjStatus);
				});*/



				List<ProjectSummaryExcel1> list = projectSummaryService.exportProjectSummary(projectSummaryExcel1);

				ExcelUtil.export(response, "投资项目清单-" + DateUtil.time(), "投资项目清单", list, ProjectSummaryExcel1.class);
			}
	/**
	 * 政府专项债券项目列表
	 * @return
	 */
	@GetMapping("/getZfzqList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "政府专项债券项目列表", notes = "")
	public R<IPage<ZysjProjectSummaryVO>> getZfzqList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();

		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");

		entity.put("xmdl",'6');
		QueryWrapper<ProjectSummary>  queryWrapper  = Condition.getQueryWrapper(entity, ProjectSummary.class);
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<ZysjProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<ZysjProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			ZysjProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, ZysjProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
//							List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
//							if(!projectPhaseRemind.isEmpty()){
//								if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
//									vo.setReportStatus("7");
//									proj.setReportStatus("7");
//									projectSummaryService.updateById(proj);
//								}
//							}
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
									//								List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
									//								if(!projectPhaseRemind.isEmpty()){
									//									if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
									//										vo.setReportStatus("7");
									//										proj.setReportStatus("7");
									//									}
									//								}
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}

			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		return R.data(pageVo);
	}

	/**
	 * 政府专项债券项目列表
	 */
	@PostMapping("/getZfzqListApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "政府专项债券项目列表-app", notes = "")
	public R getZfzqListApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("user-app",params);
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
		Query query = new Query();
		query.setCurrent(jsonParams.getInteger("current"));
		query.setSize(jsonParams.getInteger("size"));
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();
		Map<String, Object> entity = new HashMap<>(jsonParams);
		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");

		entity.put("xmdl",'6');
		QueryWrapper<ProjectSummary>  queryWrapper  = Condition.getQueryWrapper(entity, ProjectSummary.class);
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<ZysjProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<ZysjProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			ZysjProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, ZysjProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}

			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		JSONObject pageJson = objectMapper.convertValue(pageVo, JSONObject.class);
		return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
	}

	/**
	 * 中央和省级预算内项目基础信息列表分页查询
	 * @return
	 */
	@GetMapping("/getZysjysnList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "中央和省级预算内项目基础信息列表分页查询", notes = "")
	public R<IPage<ZfzqProjectSummaryVO>> getZysjysnList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();

		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");

		entity.put("xmdl",'5');
		QueryWrapper<ProjectSummary>  queryWrapper  = Condition.getQueryWrapper(entity, ProjectSummary.class);
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<ZfzqProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<ZfzqProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			ZfzqProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, ZfzqProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
//							List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
//							if(!projectPhaseRemind.isEmpty()){
//								if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
//									vo.setReportStatus("7");
//									proj.setReportStatus("7");
//									projectSummaryService.updateById(proj);
//								}
//							}
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
									//								List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
									//								if(!projectPhaseRemind.isEmpty()){
									//									if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
									//										vo.setReportStatus("7");
									//										proj.setReportStatus("7");
									//									}
									//								}
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}
			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		return R.data(pageVo);
	}

	/**
	 * 中央和省级预算内项目基础信息列表分页查询
	 * @return
	 */
	@PostMapping("/getZysjysnListApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "中央和省级预算内项目基础信息列表分页查询", notes = "")
	public R getZysjysnListApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("中央和省级预算内项目基础信息列表分页查询-app",params);
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

		Query query = new Query();
		query.setCurrent(jsonParams.getInteger("current"));
		query.setSize(jsonParams.getInteger("size"));
		Map<String, Object> entity = new HashMap<>(jsonParams);
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();

		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");

		entity.put("xmdl",'5');
		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<ZfzqProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<ZfzqProjectSummaryVO> tzrecourds = new ArrayList<>();
		for (ProjectSummary proj:recourds) {
			//2024年4月10日21点22分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
				proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
				proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
				proj.setDhhm(sm4.decrypt(proj.getDhhm()));
			}
			if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
				proj.setGddh(sm4.decrypt(proj.getGddh()));
			}
			if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
				proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
				proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
				proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
				proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
				proj.setZrrName(sm4.decrypt(proj.getZrrName()));
			}
			if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
				proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
				proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
			}
			//2024年4月10日21点22分-敏感字段处理结束
			ZfzqProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, ZfzqProjectSummaryVO.class));
			Long projectSummaryId = vo.getId();
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
			if (supervisionSubmitAudit!=null){
				vo.setAuditId(supervisionSubmitAudit.getId());
			}
			String reportStatus = vo.getReportStatus();
			if(StringUtil.isNotBlank(reportStatus)){
				// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
				if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
					ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
					ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
					if(projectPhasePlan!=null){
						if(projectPhaseReport == null)
							projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
						//TODO 1待汇报 向超期转变
						if("1".equals(reportStatus)){
							//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
						}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
							//已汇报向待汇报转化
							if(reportStatus.equals("3") && (day>24 || day<21)){
								ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
								if(test == null){
									vo.setReportStatus("1");
									proj.setReportStatus("1");
									projectSummaryService.updateById(proj);
								}
							}
							//6汇报县级退回 10汇报市级退回
						}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
							if(projectPhaseReport!=null){
								vo.setReportId(projectPhaseReport.getId());
							}
						}
					}
				}
			}
			//如果登录账号是管理员，并且区划一致，则添加下管标识
			if(isAdmin && userArea.contains(proj.getAreaCode())){
				vo.setSfgx("1");
			}else{
				vo.setSfgx("0");
			}

			//是否可调度
			if(String.valueOf(userId).equals(proj.getZrr())){
				vo.setSfdd("1");
			}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
				vo.setSfdd("1");
			}else if(proj.getCreateUser().longValue() == userId.longValue()){
				vo.setSfdd("1");
			}else{
				vo.setSfdd("0");
			}
			int monthTrue = calendar.get(Calendar.MONTH) + 1;
			if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
				vo.setSfdd("0");
			}
			String time = year+"-"+monthTrue;
			if(day >= 25){
				if(monthTrue==12){
					time = (year+1)+"-1";
				}else{
					time = year+"-"+(monthTrue+1);
				}
			}
			List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
			if(switchList!=null && !switchList.isEmpty()){
				if(switchList.get(0).getStatus()==0)
					vo.setSfdd("0");
			}
			tzrecourds.add(vo);
		}
		pageVo.setRecords(tzrecourds);
		JSONObject pageJson = objectMapper.convertValue(pageVo, JSONObject.class);
		return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
	}

	/**
	 * 项目移库
	 * @param param
	 * @return
	 */
	@PostMapping("/batchYk")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目移库", notes = "传入json")
	public R batchYk(@Valid @RequestBody String param) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		boolean result = false;
		JSONObject paramObj =  JSONObject.parseObject(param);
		String ids = paramObj.getString("ids");
		String projLabel = paramObj.getString("projLabel");
		String projLabelName = "";
		if(projLabel.equals("1")){
			projLabelName = "市列";
		}else if(projLabel.equals("2")){
			projLabelName = "省列";
		}else if(projLabel.equals("3")){
			projLabelName = "亿元以上";
		}else{
			projLabelName = "亿元以下";
		}
		String[] id =ids.split(",");
		if(id.length>0){
			for(int i=0;i<id.length;i++){
				result = projectSummaryService.projectSummaryYk(id[i], projLabel);
				ProjectLog projectLog = new ProjectLog();
				projectLog.setProjId(Long.parseLong(id[i]));
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleType("项目移库");
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】将【"+projectSummaryService.getById(id[i]).getTitle()+"】移至"+projLabelName);
				projectLogService.save(projectLog);
			}
		}else{
			result = projectSummaryService.projectSummaryYk(ids, projLabel);
			ProjectLog projectLog = new ProjectLog();
			projectLog.setProjId(Long.parseLong(ids));
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleType("项目移库");
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】将【"+projectSummaryService.getById(id).getTitle()+"】移至"+projLabelName);
			projectLogService.save(projectLog);
		}

		return R.status(result);
	}

	/**
	 * 项目催办
	 * @param projectUrgeList
	 * @return
	 */
	@PostMapping("/batchUrge")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "催办", notes = "传入list集合")
	public R batchUrges(@Valid @RequestBody List<ProjectUrge> projectUrgeList){
		return R.data(projectUrgeService.saveOrUpdateBatch(projectUrgeList));
	}

	/**
	 * 项目编辑
	 * @param id
	 * @return
	 */
	@GetMapping("/batchEdit")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "编辑", notes = "传入id")
	public R batchEdit(@ApiIgnore @RequestParam String id){
		JSONObject jsonObject = new JSONObject();
		QueryWrapper<ProjectSummary> queryWrapper = new QueryWrapper
			<ProjectSummary>();
		queryWrapper.select(" * ");
		queryWrapper.eq(id!="0","id",id);
		ProjectSummary detail = projectSummaryService.getOne(queryWrapper);
		//2024年4月11日8点50分-敏感字段处理开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(detail.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(detail.getSzhyzgbmZrrName())){
			detail.setSzhyzgbmZrrName(sm4.decrypt(detail.getSzhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(detail.getXqhyzgbmZrrName())){
			detail.setXqhyzgbmZrrName(sm4.decrypt(detail.getXqhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getDhhm()) && sm4.checkDataIsEncrypt(detail.getDhhm())){
			detail.setDhhm(sm4.decrypt(detail.getDhhm()));
		}
		if(StringUtils.isNotEmpty(detail.getGddh()) && sm4.checkDataIsEncrypt(detail.getGddh())){
			detail.setGddh(sm4.decrypt(detail.getGddh()));
		}
		if(StringUtils.isNotEmpty(detail.getManagerContact()) && sm4.checkDataIsEncrypt(detail.getManagerContact())){
			detail.setManagerContact(sm4.decrypt(detail.getManagerContact()));
		}
		if(StringUtils.isNotEmpty(detail.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(detail.getBzzgdwZrrName())){
			detail.setBzzgdwZrrName(sm4.decrypt(detail.getBzzgdwZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(detail.getBzzrdwZrrName())){
			detail.setBzzrdwZrrName(sm4.decrypt(detail.getBzzrdwZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getSgfzrName()) && sm4.checkDataIsEncrypt(detail.getSgfzrName())){
			detail.setSgfzrName(sm4.decrypt(detail.getSgfzrName()));
		}
		if(StringUtils.isNotEmpty(detail.getZrrName()) && sm4.checkDataIsEncrypt(detail.getZrrName())){
			detail.setZrrName(sm4.decrypt(detail.getZrrName()));
		}
		if(StringUtils.isNotEmpty(detail.getSjbzldName()) && sm4.checkDataIsEncrypt(detail.getSjbzldName())){
			detail.setSjbzldName(sm4.decrypt(detail.getSjbzldName()));
		}
		if(StringUtils.isNotEmpty(detail.getXjbzldName()) && sm4.checkDataIsEncrypt(detail.getXjbzldName())){
			detail.setXjbzldName(sm4.decrypt(detail.getXjbzldName()));
		}
		//2024年4月11日8点50分-敏感字段处理结束
		jsonObject.put("projectSummary",detail);
		List<ProjectPhasePlan> projectPhasePlanList = projectPhasePlanService.getProjectPhasePlanListByProjId(id);
		jsonObject.put("projectPhasePlanList",projectPhasePlanList);
		List<ProjectPhaseReport> projectPhaseReportList = projectPhaseReportService.getProjectPhaseReportListByProjId(id);
		jsonObject.put("projectPhaseReportList",projectPhaseReportList);

		return R.data(jsonObject);
	}

	/**
	 * 项目汇报信息
	 * @param id
	 * @return
	 */
	@GetMapping("/batchReport")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目汇报", notes = "")
	public R batchReport(@ApiIgnore @RequestParam String id){
		List<ProjectPhaseReport> projectPhaseReportList = projectPhaseReportService.getProjectPhaseReportListByProjId(id);
		return R.data(projectPhaseReportList);
	}

	/**
	 * 项目留言信息
	 * @param id
	 * @return
	 */
	@GetMapping("/batchMessage")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目留言", notes = "")
	public R batchMessage(@ApiIgnore @RequestParam String id){
		List<MessageInformation> messageInformationList = messageInformationService.getMessageInformationListByProjId(id);
		return R.data(messageInformationList);
	}

	/**
	 * 项目文件信息
	 * @param id
	 * @return
	 */
	@GetMapping("/batchFile")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目文件", notes = "")
	public R batchFile(@ApiIgnore @RequestParam String id){
		JSONObject jsonObject = new JSONObject();
		List<ProjectFiles> projectPictureList = projectFilesService.getProjectPictureListByProjId(id);
		jsonObject.put("projectPictureList",projectPictureList);
		List<ProjectFiles> projectVideoList = projectFilesService.getProjectVideoListByProjId(id);
		jsonObject.put("projectVideoList",projectVideoList);
		List<ProjectFiles> projectFileList = projectFilesService.getProjectFileListByProjId(id);
		jsonObject.put("projectFileList",projectFileList);
		return R.data(jsonObject);
	}

	/**
	 * 项目文件筛选
	 * @param id
	 * @param type
	 * @return
	 */
	@GetMapping("/batchFileScreen")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目文件筛选", notes = "")
	public R batchFileScreen(@ApiIgnore @RequestParam String id,@RequestParam String type){
		List<ProjectFiles> projectFileList = new ArrayList<>();
		if(type.equals("1")){
			projectFileList = projectFilesService.getProjectPictureListByProjId(id);
		} else if(type.equals("2")){
			projectFileList = projectFilesService.getProjectVideoListByProjId(id);
		} else if(type.equals("3")){
			projectFileList = projectFilesService.getProjectFileListByProjId(id);
		}
		return R.data(projectFileList);
	}

	/**
	 * 项目文件下载
	 * @param param
	 * @return
	 */
	@PostMapping("/downloadFile")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "文件下载", notes = "传入ids")
	public R downloadFile(@Valid @RequestBody String param){
		JSONObject paramObj =  JSONObject.parseObject(param);
		List<ProjectFiles> projectFileList = new ArrayList<>();
		String ids = paramObj.getString("ids");
		String[] id =ids.split(",");
		for(int i=0;i<id.length;i++){
			ProjectFiles projectFiles = projectFilesService.getProjectFileListById(id[i]);
			projectFileList.add(projectFiles);
		}
		return R.data(projectFileList);
	}

	/**
	 * 导入申报表格
	 */
	@PostMapping("imimportProjectSummary")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "导入投资项目基础信息", notes = "传入excel")
	public R imimportProjectSummary(@ApiIgnore @RequestParam MultipartFile file,@RequestParam String xmType) throws IOException {
		try {
			ProjectReadyExcel projectReadyExcel = new ProjectReadyExcel(projectSummaryService,xmType);
			ExcelUtil.save(file,projectReadyExcel,ProjectSummaryExcel.class);
		}catch (Exception e){
			throw e;
		}
		return R.success("操作成功！");
	}





	/**
	 * 公共方法-批示/留言
	 * @param messageInformation
	 * @return
	 */
	@PostMapping("/saveMsgAndFile")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "公共方法-批示/留言", notes = "传入MessageInformation对象")
	public R save(@Valid @RequestBody MessageInformation messageInformation) throws Exception {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if (currentUser != null) {
			messageInformation.setAppriseUser(currentUser.getRealName());
			messageInformation.setAppriseUserId(currentUser.getId());
			messageInformation.setAppriseuserDeptname(currentUser.getDeptId().toString());
		}

		iMessageInformationService.save(messageInformation);

		if (messageInformation.getAppriseFilesList() != null) {
			List<AppriseFiles> appriseFilesList = messageInformation.getAppriseFilesList();
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setBusinessId(messageInformation.getId());
				iAppriseFilesService.save(appriseFiles);
			}
		}
		String title = "新增批示/留言";
		String businessId = String.valueOf(messageInformation.getId());
		String businessTable = "MessageInformation";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.success("操作成功！");
	}

	/**
	 * 项目编辑
	 * @param id
	 * @return
	 */
	@GetMapping("/batchDetial")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目编辑", notes = "")
	public R batchDetial(@ApiIgnore @RequestParam String id){
		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(Long.parseLong(id));
		projectLog.setHandleType("项目查看");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			projectLog.setHandleUser(user.getRealName());
		}
		projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
		String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
		projectLog.setHandleContent("【"+handleUserDecrypt+"】查看【"+projectSummaryService.getById(id).getTitle()+"】");
		projectLogService.save(projectLog);
		ProjectSummary projectSummary = projectSummaryService.getById(id);
		projectSummary.setProjectSpecialistList(specialistService.list(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, id)));
		String ddAddress = projectSummary.getDdAddress();
		Map baiduMap = new LinkedHashMap();
		Map gaodeMap = new LinkedHashMap();
		if(StringUtil.isNotBlank(ddAddress)){
			String[] ddAddressSr = ddAddress.split("，");
			String bdLon = ddAddressSr[0].split("：")[1];//经度
			String bdLat = ddAddressSr[1].split("：")[1];//纬度
			double x = Double.parseDouble(bdLon) - 0.0065, y = Double.parseDouble(bdLat) - 0.006;
			double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
			double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
			String gdLon = z * Math.cos(theta) + "";
			String gdLat = z * Math.sin(theta) + "";
			baiduMap.put("lat",bdLat);
			baiduMap.put("lon",bdLon);
			gaodeMap.put("lat",gdLat);
			gaodeMap.put("lon",gdLon);
			projectSummary.setBaiDu(baiduMap);
			projectSummary.setGaoDe(gaodeMap);
		}
		List<ProjectFiles> projectFilesList = projectFilesService.getProjectFilesListByProjId(id);
		projectSummary.setProjectFilesList(projectFilesList);
		List<ProjectPhasePlan> projectPhasePlanList = projectPhasePlanService.getProjectPhasePlanListByProjId(id);
		if(projectPhasePlanList!=null){
			for(ProjectPhasePlan projectPhasePlan:projectPhasePlanList){
				List<ProjectPhaseRemind> projectPhaseRemindList = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(projectPhasePlan.getProjId().toString(),projectPhasePlan.getId().toString());
				projectPhasePlan.setProjectPhaseRemindList(projectPhaseRemindList);
			}
		}
		projectSummary.setProjectPhasePlanList(projectPhasePlanList);
		//2024年4月10日20点50分-敏感字段处理开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(projectSummary.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getSzhyzgbmZrrName())){
			projectSummary.setSzhyzgbmZrrName(sm4.decrypt(projectSummary.getSzhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getXqhyzgbmZrrName())){
			projectSummary.setXqhyzgbmZrrName(sm4.decrypt(projectSummary.getXqhyzgbmZrrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getDhhm()) && sm4.checkDataIsEncrypt(projectSummary.getDhhm())){
			projectSummary.setDhhm(sm4.decrypt(projectSummary.getDhhm()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getGddh()) && sm4.checkDataIsEncrypt(projectSummary.getGddh())){
			projectSummary.setGddh(sm4.decrypt(projectSummary.getGddh()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getManagerContact()) && sm4.checkDataIsEncrypt(projectSummary.getManagerContact())){
			projectSummary.setManagerContact(sm4.decrypt(projectSummary.getManagerContact()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getBzzgdwZrrName())){
			projectSummary.setBzzgdwZrrName(sm4.decrypt(projectSummary.getBzzgdwZrrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getBzzrdwZrrName())){
			projectSummary.setBzzrdwZrrName(sm4.decrypt(projectSummary.getBzzrdwZrrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getSgfzrName()) && sm4.checkDataIsEncrypt(projectSummary.getSgfzrName())){
			projectSummary.setSgfzrName(sm4.decrypt(projectSummary.getSgfzrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getZrrName())){
			projectSummary.setZrrName(sm4.decrypt(projectSummary.getZrrName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getSjbzldName()) && sm4.checkDataIsEncrypt(projectSummary.getSjbzldName())){
			projectSummary.setSjbzldName(sm4.decrypt(projectSummary.getSjbzldName()));
		}
		if(StringUtils.isNotEmpty(projectSummary.getXjbzldName()) && sm4.checkDataIsEncrypt(projectSummary.getXjbzldName())){
			projectSummary.setXjbzldName(sm4.decrypt(projectSummary.getXjbzldName()));
		}
		//2024年4月10日20点50分-敏感字段处理结束
		return R.data(projectSummary);
	}

	/**
	 * 项目编辑-app
	 */
	@PostMapping("/batchDetialApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目编辑-app", notes = "")
	public R batchDetialApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("项目编辑-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String id = jsonParams.getString("id");

			ProjectLog projectLog = new ProjectLog();//项目日志
			projectLog.setProjId(Long.parseLong(id));
			projectLog.setHandleType("项目查看");
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			if(user==null){
				projectLog.setHandleUser(AuthUtil.getUserName());
			}else{
				projectLog.setHandleUser(user.getRealName());
			}
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】查看【"+projectSummaryService.getById(id).getTitle()+"】");
			projectLogService.save(projectLog);
			ProjectSummary projectSummary = projectSummaryService.getById(id);
			projectSummary.setProjectSpecialistList(specialistService.list(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, id)));
			String ddAddress = projectSummary.getDdAddress();
			Map baiduMap = new LinkedHashMap();
			Map gaodeMap = new LinkedHashMap();
			if(StringUtil.isNotBlank(ddAddress)){
				String[] ddAddressSr = ddAddress.split("，");
				String bdLon = ddAddressSr[0].split("：")[1];//经度
				String bdLat = ddAddressSr[1].split("：")[1];//纬度
				double x = Double.parseDouble(bdLon) - 0.0065, y = Double.parseDouble(bdLat) - 0.006;
				double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
				double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
				String gdLon = z * Math.cos(theta) + "";
				String gdLat = z * Math.sin(theta) + "";
				baiduMap.put("lat",bdLat);
				baiduMap.put("lon",bdLon);
				gaodeMap.put("lat",gdLat);
				gaodeMap.put("lon",gdLon);
				projectSummary.setBaiDu(baiduMap);
				projectSummary.setGaoDe(gaodeMap);
			}
			List<ProjectFiles> projectFilesList = projectFilesService.getProjectFilesListByProjId(id);
			projectSummary.setProjectFilesList(projectFilesList);
			List<ProjectPhasePlan> projectPhasePlanList = projectPhasePlanService.getProjectPhasePlanListByProjId(id);
			if(projectPhasePlanList!=null){
				for(ProjectPhasePlan projectPhasePlan:projectPhasePlanList){
					List<ProjectPhaseRemind> projectPhaseRemindList = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(projectPhasePlan.getProjId().toString(),projectPhasePlan.getId().toString());
					projectPhasePlan.setProjectPhaseRemindList(projectPhaseRemindList);
				}
			}
			projectSummary.setProjectPhasePlanList(projectPhasePlanList);
			//2024年4月10日20点50分-敏感字段处理开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(projectSummary.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getSzhyzgbmZrrName())){
				projectSummary.setSzhyzgbmZrrName(sm4.decrypt(projectSummary.getSzhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getXqhyzgbmZrrName())){
				projectSummary.setXqhyzgbmZrrName(sm4.decrypt(projectSummary.getXqhyzgbmZrrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getDhhm()) && sm4.checkDataIsEncrypt(projectSummary.getDhhm())){
				projectSummary.setDhhm(sm4.decrypt(projectSummary.getDhhm()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getGddh()) && sm4.checkDataIsEncrypt(projectSummary.getGddh())){
				projectSummary.setGddh(sm4.decrypt(projectSummary.getGddh()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getManagerContact()) && sm4.checkDataIsEncrypt(projectSummary.getManagerContact())){
				projectSummary.setManagerContact(sm4.decrypt(projectSummary.getManagerContact()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getBzzgdwZrrName())){
				projectSummary.setBzzgdwZrrName(sm4.decrypt(projectSummary.getBzzgdwZrrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getBzzrdwZrrName())){
				projectSummary.setBzzrdwZrrName(sm4.decrypt(projectSummary.getBzzrdwZrrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getSgfzrName()) && sm4.checkDataIsEncrypt(projectSummary.getSgfzrName())){
				projectSummary.setSgfzrName(sm4.decrypt(projectSummary.getSgfzrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getZrrName()) && sm4.checkDataIsEncrypt(projectSummary.getZrrName())){
				projectSummary.setZrrName(sm4.decrypt(projectSummary.getZrrName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getSjbzldName()) && sm4.checkDataIsEncrypt(projectSummary.getSjbzldName())){
				projectSummary.setSjbzldName(sm4.decrypt(projectSummary.getSjbzldName()));
			}
			if(StringUtils.isNotEmpty(projectSummary.getXjbzldName()) && sm4.checkDataIsEncrypt(projectSummary.getXjbzldName())){
				projectSummary.setXjbzldName(sm4.decrypt(projectSummary.getXjbzldName()));
			}
			//2024年4月10日20点50分-敏感字段处理结束
			JSONObject pageJson = objectMapper.convertValue(projectSummary, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 获取审核人员
	 * @return
	 */
	@GetMapping("/getShry")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取审核人员", notes = "")
	public R getShry(@ApiIgnore @RequestParam String id){
		JSONObject result = new JSONObject();
		String deptId = AuthUtil.getDeptId();
		try{
			if(StringUtil.isBlank(deptId)){
				if(StringUtil.isBlank(id)){
					return R.fail("找不到用户");
				}else{
					User user = userClient.userInfoById(Long.parseLong(id)).getData();
					if(user==null || StringUtil.isBlank(user.getDeptId())){
						return R.fail("找不到用户或部门");
					}else{
						deptId = user.getDeptId();
					}
				}
			}
			if(StringUtil.isBlank(deptId)){
				return R.fail("找不到部门");
			}else{
				Dept dept = sysClient.getDept(Long.parseLong(deptId)).getData();
				if(dept==null){
					return R.fail("找不到部门");
				}else{
					String areacode = dept.getAreaCode();
					//如果取出的审核人id与登录人一致，则去获取市级信息
					if(String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId(areacode))|| id.equals(PropConstant.getProjectShzhId(areacode))){
						result.put("areacode",areacode);
						result.put("level",4);
						result.put("shrid", PropConstant.getProjectShzhId("6207"));
						result.put("shrname",PropConstant.getProjectShzhName("6207"));
					}else{
						result.put("areacode",areacode);
						result.put("level",6);
						result.put("shrid", PropConstant.getProjectShzhId(areacode));
						result.put("shrname",PropConstant.getProjectShzhName(areacode));
					}

				}
			}
		}catch (Exception e){
			return R.fail("找不到用户");
		}
		return R.data(result);
	}

	/**
	 * 获取审核人员
	 * @return
	 */
	@GetMapping("/getShryByArea")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取审核人员", notes = "")
	public R getShryByArea(@ApiIgnore @RequestParam String areacode){
		JSONObject result = new JSONObject();
		result.put("areacode",areacode);
		result.put("level",areacode.length());
		result.put("shrid", PropConstant.getProjectShzhId(areacode));
		result.put("shrname",PropConstant.getProjectShzhName(areacode));
		return R.data(result);
	}
	/**
	 * 获取项目年份
	 * @return
	 */
	@GetMapping("/getXjXmnf")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取新建项目年份", notes = "")
	public R getXjXmnf(){
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		List<JSONObject> years = new ArrayList<>();
		JSONObject year1 = new JSONObject();
		year1.put("xmnf",year);
		JSONObject year2 = new JSONObject();
		year2.put("xmnf",year+1);
		JSONObject year3 = new JSONObject();
		year3.put("xmnf",year+2);
		years.add(year1);
		years.add(year2);
		years.add(year3);
		return R.data(years);
	}

	/**
	 * 获取新项目年份
	 * @return
	 */
	@GetMapping("/getXmnf")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取项目年份", notes = "")
	public R getXmnf(){
		return R.data(projectSummaryService.getXmnfList());
	}

	/**
	 * 获取简易汇报信息
	 * @return
	 */
	@GetMapping("/getSimpleDetail")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取简易汇报信息", notes = "")
	public R getSimpleDetail(@ApiIgnore @RequestParam String id, @RequestParam(required = false, defaultValue = "") String reportId){
		ProjectSummary projectSummary = projectSummaryService.getById(id);
		if(projectSummary!=null){
			JSONObject result = new JSONObject();
			result.put("title",projectSummary.getTitle());
			result.put("projLabel",projectSummary.getProjLabel());
			result.put("xmdl",projectSummary.getXmdl());
			switch (projectSummary.getXmdl()){
				//投资项目
				case "1":
					result.put("totalEconomic",projectSummary.getTotalEconomic()==null||projectSummary.getTotalEconomic()<0?0f:projectSummary.getTotalEconomic());
					//投资计划
					int month = -1;
					if(StringUtil.isNotBlank(reportId)){
						ProjectPhaseReport report = projectPhaseReportService.getById(reportId);
						ProjectPhasePlan plan = null;
						if (report!=null)
							plan = projectPhasePlanService.getById(report.getJhjdId());
						if(plan!=null)
							month = plan.getPlanMonth();
					}
					Calendar calendar = Calendar.getInstance();
					if(month <1){
						month = calendar.get(Calendar.MONTH) + 1;
						if(calendar.get(Calendar.DAY_OF_MONTH)<24 && month>1){
							month-=1;
						}
					}
					try{
						switch (month){
							case 1:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz01()));
								break;
							case 2:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz02()));
								break;
							case 3:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz03()));
								break;
							case 4:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz04()));
								break;
							case 5:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz05()));
								break;
							case 6:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz06()));
								break;
							case 7:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz07()));
								break;
							case 8:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz08()));
								break;
							case 9:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz09()));
								break;
							case 10:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz10()));
								break;
							case 11:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz11()));
								break;
							case 12:
								result.put("dyjh",Float.parseFloat(projectSummary.getJhtz12()));
								break;
						}
					}catch (Exception e){
						System.out.println(e.getMessage());
						result.put("dyjh",0f);
					}
					break;
				//中央省级预算内项目返回
				case "5":
					result.put("zytzjh",Integer.parseInt(projectSummary.getZytzjh()));
					result.put("sjtzjh",Integer.parseInt(projectSummary.getSjtzjh()));
					result.put("dftzjh",Integer.parseInt(projectSummary.getDftzjh()));
					break;
			}
			return R.data(result);
		}else{
			return R.fail("查无此项目");
		}
	}

	/**
	 * 项目成员
	 * @param id
	 * @return
	 */
	@GetMapping("/batchUser")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目成员", notes = "projectSummary")
	public R batchUser(@ApiIgnore @RequestParam String id){
		return R.data(projectSummaryService.getUserListByProjId(id));
	}

	/**
	 * 项目汇报列表
	 * @param id
	 * @return
	 */
	@GetMapping("/batchReportList")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目汇报列表", notes = "")
	public R batchReportScreen(@ApiIgnore @RequestParam String id){


		return R.data(projectSummaryService.getProjectPhaseReportByHbjdId(Long.valueOf(id)));
	}


	private Map<String, Object> getAuth(Map<String, Object> entity){
//		String roles = AuthUtil.getUserRole();
//		String[] deptIds = AuthUtil.getDeptId().split(",");
//		boolean isAdmin = false;
//		List<String> depts = new ArrayList<>();
//		List<String> deptAreas = new ArrayList<>();
//		for (String id:deptIds){
//			Dept dept = sysClient.getDept(Long.parseLong(id)).getData();
//			if(dept!=null) {
//				depts.add(dept.getDeptName());
//				deptAreas.add(dept.getAreaCode());
//				if(!isAdmin){
//					if(String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId(dept.getAreaCode()))){
//						isAdmin=true;
//					}
//				}
//			}
//		}
//		entity.put("ownArea",deptAreas);
//		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
//		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
//
//		//1.角色为“市级四大班子”
//		if(roles.contains("市级四大班子")){
//			entity.put("isTop","1");
//		}
//		//2.部门为“市委办公室”、“市政府办公室”、“市发改委” 并且 岗位是管理员或部门领导角色
//		if(AuthUtil.getPostId().contains(leadPostId)|| AuthUtil.getPostId().contains(authPostId)){
//			if(depts.contains("市委办公室") || depts.contains("市政府办公室") || depts.contains("市发展改革委") )
//				entity.put("isTop","1");
//		}
//		//3.审核账号
//		if(isAdmin){//是审核账号
//			entity.put("isAdmin","1");
//			if(!deptAreas.contains("6207")){//不是市发改委账号,只能查看管辖区划下的项目
//				entity.put("areaCode",deptAreas);
//			}else{
//				entity.put("isTop","1");
//			}
//		}else{
//			entity.put("isAdmin","0");
//			//如果不是审核账号，则只能查看自己创建的项目
//			entity.put("createUser",AuthUtil.getUserId());
//		}
//		//4.岗位为“管理员”、角色是“领导”有所在部门创建项目列表权限
//		if(AuthUtil.getPostId().contains(leadPostId)|| AuthUtil.getPostId().contains(authPostId)){
//			entity.put("createDept",AuthUtil.getDeptId());
//		}
//		//5.如果登录人岗位是管理员，判断调度单位
//		if(AuthUtil.getPostId().contains(authPostId)){
//			entity.put("dwmc",Arrays.asList(deptIds));
//		}
//		boolean flag = false;
//		for(String dept : depts){
//			if("委领导".equals(dept)){
//				flag = true;
//				break;
//			}
//		}
//		//6.如果是县委或区委领导，且是管理员
//		if (flag && (AuthUtil.getPostId().contains(authPostId) || AuthUtil.getPostId().contains(leadPostId))) {
//			entity.put("areaCode",deptAreas);
//		}

		Dept dept = sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData();
		if(String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId(dept.getAreaCode()))){
			entity.put("isAdmin","1");
		}else{
			entity.put("isAdmin","0");
		}
		if(dept!=null && StringUtil.isNotBlank(dept.getAreaCode())){
			if(!"6207".equals(dept.getAreaCode())){
				entity.put("areaCodeSingle",dept.getAreaCode());
			}
			List<String> deptAreas = new ArrayList<>();
			deptAreas.add(dept.getAreaCode());
			entity.put("ownArea",deptAreas);
		}
		return  entity;
	}


	/**
	 * 项目管理员  -- 投资项目基础信息列表分页查询
	 * @return
	 */
	@GetMapping("/getXMGLTzProjectSummaryList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "投资项目基础信息列表", notes = "")
	public R<IPage<TzProjectSummaryVO>> getXMGLTzProjectSummaryList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		SM4Crypto crypto = SM4Crypto.getInstance();
		String name = crypto.checkDataIsEncrypt(user.getName()) ? crypto.decrypt(user.getName()) : user.getName();
		String phone = crypto.checkDataIsEncrypt(user.getPhone()) ? crypto.decrypt(user.getPhone()) : user.getPhone();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day <24 && month>1){
			month-=1;
		}else if(day < 24){
			month=12;
		}
		Long userId = AuthUtil.getUserId();
		//添加判断权限
		entity = getAuth(entity);
		boolean isAdmin = "1".equals(entity.get("isAdmin").toString());
		List<String> userArea  = (List<String>) entity.get("ownArea");
		String reportStatusStr ="";
		if(entity.get("reportStatusPara") == null){
			entity.put("reportStatus","");
		}else{
			reportStatusStr = entity.get("reportStatusPara").toString();
			entity.put("reportStatus",reportStatusStr);
		}
		if(entity.get("sfzg") != null && "1".equals(entity.get("sfzg").toString())){
			entity.put("zgdwzrr",AuthUtil.getDeptId());
		}
		// 项目管理员查询条件是否存在
		Object projectSpecialistList = entity.get("projectSpecialistList");
//		QueryWrapper<ProjectSummary>  queryWrapper  = Condition.getQueryWrapper(entity, ProjectSummary.class);
		List<String> ids = null;
		LambdaQueryWrapper<ProjectSpecialist> wrapper = Wrappers.<ProjectSpecialist>query().lambda();
		if (projectSpecialistList != null && !projectSpecialistList.toString().isEmpty()) {
			wrapper.like(ProjectSpecialist::getProjectSpecialist, projectSpecialistList.toString());
		}
		List<ProjectSpecialist> projectSpecialists = specialistService.list(wrapper);
		if (projectSpecialists != null && !projectSpecialists.isEmpty()) {
			ids = projectSpecialists.stream().map(ProjectSpecialist::getProjectId).distinct().collect(Collectors.toList());
		}
//		IPage<ProjectSummary> pages = projectSummaryService.selectPage(Condition.getPage(query), entity);
		IPage<ProjectSummary> pages = null;
		if (ids == null || ids.isEmpty()) {
			pages = Condition.getPage(query);
		} else {
			entity.put("ids", ids);
			pages = projectSummaryService.selectXMGLPage(Condition.getPage(query), entity);
		}
		IPage<TzProjectSummaryVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		List<ProjectSummary> recourds = pages.getRecords();
		List<TzProjectSummaryVO> tzrecourds = new ArrayList<>();
		if (recourds != null && !recourds.isEmpty()) {
			for (ProjectSummary proj:recourds) {
				//2024年4月10日21点22分-敏感字段处理开始
				SM4Crypto sm4 = SM4Crypto.getInstance();
				if(StringUtils.isNotEmpty(proj.getSzhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getSzhyzgbmZrrName())){
					proj.setSzhyzgbmZrrName(sm4.decrypt(proj.getSzhyzgbmZrrName()));
				}
				if(StringUtils.isNotEmpty(proj.getXqhyzgbmZrrName()) && sm4.checkDataIsEncrypt(proj.getXqhyzgbmZrrName())){
					proj.setXqhyzgbmZrrName(sm4.decrypt(proj.getXqhyzgbmZrrName()));
				}
				if(StringUtils.isNotEmpty(proj.getDhhm()) && sm4.checkDataIsEncrypt(proj.getDhhm())){
					proj.setDhhm(sm4.decrypt(proj.getDhhm()));
				}
				if(StringUtils.isNotEmpty(proj.getGddh()) && sm4.checkDataIsEncrypt(proj.getGddh())){
					proj.setGddh(sm4.decrypt(proj.getGddh()));
				}
				if(StringUtils.isNotEmpty(proj.getManagerContact()) && sm4.checkDataIsEncrypt(proj.getManagerContact())){
					proj.setManagerContact(sm4.decrypt(proj.getManagerContact()));
				}
				if(StringUtils.isNotEmpty(proj.getBzzgdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzgdwZrrName())){
					proj.setBzzgdwZrrName(sm4.decrypt(proj.getBzzgdwZrrName()));
				}
				if(StringUtils.isNotEmpty(proj.getBzzrdwZrrName()) && sm4.checkDataIsEncrypt(proj.getBzzrdwZrrName())){
					proj.setBzzrdwZrrName(sm4.decrypt(proj.getBzzrdwZrrName()));
				}
				if(StringUtils.isNotEmpty(proj.getSgfzrName()) && sm4.checkDataIsEncrypt(proj.getSgfzrName())){
					proj.setSgfzrName(sm4.decrypt(proj.getSgfzrName()));
				}
				if(StringUtils.isNotEmpty(proj.getZrrName()) && sm4.checkDataIsEncrypt(proj.getZrrName())){
					proj.setZrrName(sm4.decrypt(proj.getZrrName()));
				}
				if(StringUtils.isNotEmpty(proj.getSjbzldName()) && sm4.checkDataIsEncrypt(proj.getSjbzldName())){
					proj.setSjbzldName(sm4.decrypt(proj.getSjbzldName()));
				}
				if(StringUtils.isNotEmpty(proj.getXjbzldName()) && sm4.checkDataIsEncrypt(proj.getXjbzldName())){
					proj.setXjbzldName(sm4.decrypt(proj.getXjbzldName()));
				}
				//2024年4月10日21点22分-敏感字段处理结束
				TzProjectSummaryVO vo = Objects.requireNonNull(BeanUtil.copy(proj, TzProjectSummaryVO.class));
				Long projectSummaryId = vo.getId();
				SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getAuditByservIdAndUserId(projectSummaryId,userId);
				if (supervisionSubmitAudit!=null){
					vo.setAuditId(supervisionSubmitAudit.getId());
				}
				String reportStatus = vo.getReportStatus();
				if(StringUtil.isNotBlank(reportStatus)){
					// 1待汇报 3已汇报 5汇报县级待审核 6汇报县级退回 8超期已汇报 9汇报市级待审核 10汇报市级退回
					if(reportStatus.equals("1")||reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5")||reportStatus.equals("6")||reportStatus.equals("9")||reportStatus.equals("10")){
						ProjectPhasePlan projectPhasePlan = projectPhasePlanService.getProjectPhasePlanByProjIdAndMonth(projectSummaryId,month);
						ProjectPhaseReport projectPhaseReport = projectPhaseReportService.getFirstProjectPhaseReportByProjId(proj.getId());
						if(projectPhasePlan!=null){
							if(projectPhaseReport == null) {
								projectPhaseReport = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
							}
							//TODO 1待汇报 向超期转变
							if("1".equals(reportStatus)){
//							List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
//							if(!projectPhaseRemind.isEmpty()){
//								if(projectPhaseRemind.get(0).getReportTime()!=null && calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
//									vo.setReportStatus("7");
//									proj.setReportStatus("7");
//									projectSummaryService.updateById(proj);
//								}
//							}
								//3已汇报 8超期已汇报 5汇报县级待审核   9汇报市级待审核
							}else if(reportStatus.equals("3")||reportStatus.equals("8")||reportStatus.equals("5") || reportStatus.equals("9")){
								if(projectPhaseReport!=null){
									vo.setReportId(projectPhaseReport.getId());
								}
								//已汇报向待汇报转化
								if(reportStatus.equals("3") && (day>24 || day<21)){
									ProjectPhaseReport test = projectPhaseReportService.getProjectPhaseReportAllByPlanId(projectPhasePlan.getId());
									if(test == null){
										vo.setReportStatus("1");
										proj.setReportStatus("1");
										//								List<ProjectPhaseRemind> projectPhaseRemind = projectPhaseRemindService.getProjectPhaseRemindListByProjIdAndPlanId(String.valueOf(proj.getId()),String.valueOf(projectPhasePlan.getId()));
										//								if(!projectPhaseRemind.isEmpty()){
										//									if(calendar.getTime().compareTo(projectPhaseRemind.get(0).getReportTime())>0){
										//										vo.setReportStatus("7");
										//										proj.setReportStatus("7");
										//									}
										//								}
										projectSummaryService.updateById(proj);
									}
								}
								//6汇报县级退回 10汇报市级退回
							}else  if(reportStatus.equals("6") || reportStatus.equals("10")){
								if(projectPhaseReport!=null){
									vo.setReportId(projectPhaseReport.getId());
								}
							}
						}
					}
				}
				try{
					switch (month){
						case 1:
							vo.setDyjh(Float.parseFloat(vo.getJhtz01()));
							break;
						case 2:
							vo.setDyjh(Float.parseFloat(vo.getJhtz02()));
							break;
						case 3:
							vo.setDyjh(Float.parseFloat(vo.getJhtz03()));
							break;
						case 4:
							vo.setDyjh(Float.parseFloat(vo.getJhtz04()));
							break;
						case 5:
							vo.setDyjh(Float.parseFloat(vo.getJhtz05()));
							break;
						case 6:
							vo.setDyjh(Float.parseFloat(vo.getJhtz06()));
							break;
						case 7:
							vo.setDyjh(Float.parseFloat(vo.getJhtz07()));
							break;
						case 8:
							vo.setDyjh(Float.parseFloat(vo.getJhtz08()));
							break;
						case 9:
							vo.setDyjh(Float.parseFloat(vo.getJhtz09()));
							break;
						case 10:
							vo.setDyjh(Float.parseFloat(vo.getJhtz10()));
							break;
						case 11:
							vo.setDyjh(Float.parseFloat(vo.getJhtz11()));
							break;
						case 12:
							vo.setDyjh(Float.parseFloat(vo.getJhtz12()));
							break;
					}
				}catch (Exception e){
					System.out.println(e.getMessage());
					vo.setDyjh(0f);
				}
				vo.setJhce(vo.getTotalEconomic()-vo.getDyjh());
				//如果登录账号是管理员，并且区划一致，则添加下管标识
				if(isAdmin && userArea.contains(proj.getAreaCode())){
					vo.setSfgx("1");
				}else{
					vo.setSfgx("0");
				}

				//是否可调度
				if(String.valueOf(userId).equals(proj.getZrr())){
					vo.setSfdd("1");
				}else if(AuthUtil.getDeptId().equals(proj.getDwmc()) && AuthUtil.getUser().getPostId().contains(PropConstant.getBmglyPostId())){
					vo.setSfdd("1");
				}else if(proj.getCreateUser().longValue() == userId.longValue()){
					vo.setSfdd("1");
				}else{
					vo.setSfdd("0");
				}
				int monthTrue = calendar.get(Calendar.MONTH) + 1;
				if(StringUtils.isNotBlank(proj.getXmnf()) && Integer.parseInt(proj.getXmnf())<year){
					vo.setSfdd("0");
				}
				String time = year+"-"+monthTrue;
				if(day >= 25){
					if(monthTrue==12){
						time = (year+1)+"-1";
					}else{
						time = year+"-"+(monthTrue+1);
					}
				}
				List<ProjectPhaseReportSwitch> switchList = projectPhaseReportSwitchService.list(Wrappers.<ProjectPhaseReportSwitch>query().lambda().eq(ProjectPhaseReportSwitch::getTime, time));
				if(switchList!=null && !switchList.isEmpty()){
					if(switchList.get(0).getStatus()==0) {
						vo.setSfdd("0");
					}
				}
				//获取项目专员列表
				List<ProjectSpecialist> list = specialistService.list(Wrappers.<ProjectSpecialist>query().lambda().eq(ProjectSpecialist::getProjectId, proj.getId()));
				vo.setProjectSpecialistList(list);
				vo.setCanReport(list.stream().anyMatch(special -> special.getProjectSpecialist().equals(name) && special.getProjectSpecialistTel().equals(phone)));
				tzrecourds.add(vo);
			}
		}
		pageVo.setRecords(tzrecourds);
		return R.data(pageVo);
	}

	/**
	 * 获取审核人员
	 * @return
	 */
	@GetMapping("/getListAuth")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "获取当前人按钮权限", notes = "")
	public R getListAuth(){
		JSONObject result = new JSONObject();

		boolean isAdmin = false;
		String[] deptIds = AuthUtil.getDeptId().split(",");
		List<String> deptAreas = new ArrayList<>();
		List<String> depts = new ArrayList<>();
		for (String id:deptIds){
			Dept dept = sysClient.getDept(Long.parseLong(id)).getData();
			if(dept!=null) {
				deptAreas.add(dept.getAreaCode());
				depts.add(dept.getDeptName());
				if(!isAdmin){
					if(String.valueOf(AuthUtil.getUserId()).equals(PropConstant.getProjectShzhId(dept.getAreaCode()))){
						isAdmin=true;
					}
				}
			}
		}
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
		int sb=1,th=1,nr=1,sc=1,dr=0,dc=0;

//领导账号没有申报删除纳入退回
		result.put("admin",0);
		if(isAdmin){
			result.put("admin",1);
			if(!deptAreas.contains("6207")){//不是市发改委账号,只能查看管辖区划下的项目
				sb=1;th=1;nr=0;sc=0;
			}else{
				sb=1;th=1;nr=1;sc=1;dr=1;dc=1;
			}
		}else if(AuthUtil.getPostId().contains(leadPostId) || AuthUtil.getUserRole().contains("市级四大班子") || AuthUtil.getUserRole().contains("县级四大班子"))
		{
			sb=0;th=0;nr=0;sc=0;
		}else{//只有管理员有申报
			sb=1;th=0;nr=0;sc=0;
		}
		if(AuthUtil.getPostId().contains(authPostId) && depts.contains("市发展改革委")){
			dr=1;dc=1;
		}

		result.put("sb",sb);//申报
//		result.put("th",th);//退回
		result.put("th",0);//退回
		result.put("nr",nr);//纳入
		result.put("sc",sc);//删除
		result.put("dr",dr);//导入
		result.put("dc",dc);//导出
		return R.data(result);
	}
}
