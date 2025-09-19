package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.DateFormatUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionLog;
import com.vingsoft.entity.SupervisionPhasePlan;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.excel.SupervisionInfoExcel;
import com.vingsoft.vo.SupervisionDeptPlanReportVO;
import com.vingsoft.vo.SupervisionInfoVO;
import com.vingsoft.vo.SupervisionSignVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springblade.integrated.platform.wrapper.SupervisionSignWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.*;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办信息控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionInfo")
@Api(value = "督察督办信息", tags = "督察督办信息")
public class SupervisionInfoController extends BladeController {

	private final ISupervisionInfoService supervisionInfoService;

	private final ISupervisionSignService supervisionSignService;

	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final IDictBizClient dictClient;
	private final ISupervisionLogService supervisionLogService;
	private IUnifyMessageService unifyMessageService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 分页查询
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办信息List", notes = "")
	public R<PageInfo> list(@ApiIgnore Query query, @RequestParam Map<String, Object> entity) {
		PageInfo pageInfo = supervisionInfoService.queryListPage(query,entity);
		return R.data(pageInfo);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办信息List-app", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("督察督办信息List-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			Map<String, Object> entity = new HashMap<>(jsonParams);
			PageInfo pageInfo = supervisionInfoService.queryListPage(query,entity);
			JSONObject pageJson = objectMapper.convertValue(pageInfo, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 分页查询
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/cblist")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "承办信息List", notes = "")
	public R<PageInfo> cblist(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ) {
		QueryWrapper<SupervisionInfo> ew =new QueryWrapper<>();
		PageInfo pageInfo = supervisionInfoService.cbqueryListPage( query,entity);
		return R.data(pageInfo);
	}

	/**
	 * 导出
	 * @param entity
	 * @param response
	 */
	@GetMapping("exportInfo")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "导出", notes = "vo")
	public void exportInfo(@RequestParam Map<String, Object> entity, HttpServletResponse response) {
		List<SupervisionInfo> list = supervisionInfoService.queryList(entity);

		List<SupervisionInfoExcel> export = new ArrayList<>();
		list.forEach(info -> {
			SupervisionInfoExcel excel = new SupervisionInfoExcel();

			String servType = "";
			if("serv_type_sw_zdgz".equals(info.getServTypeTwo())){//重点工作
				if("serv_type_sw_ypscwq".equals(info.getServTypeThree())){//一屏四城五区
					servType = dictClient.getValue("serv_type_sw_ypscwq",info.getServTypeFour()).getData();
				}else if("serv_type_sw_qtzdgz".equals(info.getServTypeThree())){//其他重点工作
					servType = dictClient.getValue("serv_type_sw_qtzdgz",info.getServTypeFour()).getData();
				}
			}else if("serv_type_sw_zyhy".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_zyhy",info.getServTypeThree()).getData();
			}else if("serv_type_sw_ldps".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_ldps",info.getServTypeThree()).getData();
			}else if("serv_type_sw_lybl".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_lybl",info.getServTypeThree()).getData();
			}

			excel.setServType(servType);
			excel.setServName(info.getServName());
			excel.setDutyLeader(info.getDutyLeaderName());
			excel.setDutyUnit(info.getDutyUnitName());
			excel.setLeadUnit(info.getLeadUnitName());
			excel.setSupervisor(info.getSupervisorName());
			excel.setEvaluator(info.getEvaluatorName());
			excel.setRequirement(info.getRequirement());

			List<SupervisionPhasePlan> plans = info.getSupervisionPhasePlanList();
			if (plans!=null&&plans.size()>0) {
				StringBuilder planInfo = new StringBuilder();
				for (SupervisionPhasePlan plan : plans) {
					planInfo.append(plan.getPhaseName()).append("[").append(DateFormatUtils.format(plan.getStartTime(), "yyyy-MM-dd HH:mm:ss")).append("-").append(DateFormatUtils.format(plan.getEndTime(), "yyyy-MM-dd HH:mm:ss")).append("]\n");
				}
				excel.setPhaseList(planInfo.toString());
			}else{
				excel.setPhaseList("");
			}
			export.add(excel);
		});

		ExcelUtil.export(response, "事项数据", "事项数据", export, SupervisionInfoExcel.class);
	}

	/**
	 * 导出
	 * @param entity
	 * @param response
	 */
	@GetMapping("exportWord")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "导出", notes = "vo")
	public void exportWord(@RequestParam Map<String, Object> entity, HttpServletResponse response) {
		Map<String, Object> datas = new HashMap<>();
		List<SupervisionInfo> list = supervisionInfoService.queryList(entity);

		List<SupervisionInfoExcel> export = new ArrayList<>();
		list.forEach(info -> {
			SupervisionInfoExcel excel = new SupervisionInfoExcel();

			String servType = "";
			if("serv_type_sw_zdgz".equals(info.getServTypeTwo())){//重点工作
				if("serv_type_sw_ypscwq".equals(info.getServTypeThree())){//一屏四城五区
					servType = dictClient.getValue("serv_type_sw_ypscwq",info.getServTypeFour()).getData();
				}else if("serv_type_sw_qtzdgz".equals(info.getServTypeThree())){//其他重点工作
					servType = dictClient.getValue("serv_type_sw_qtzdgz",info.getServTypeFour()).getData();
				}
			}else if("serv_type_sw_zyhy".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_zyhy",info.getServTypeThree()).getData();
			}else if("serv_type_sw_ldps".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_ldps",info.getServTypeThree()).getData();
			}else if("serv_type_sw_lybl".equals(info.getServTypeTwo())){
				servType = dictClient.getValue("serv_type_sw_lybl",info.getServTypeThree()).getData();
			}

			excel.setServType(servType);
			excel.setServName(info.getServName());
			excel.setDutyLeader(info.getDutyLeaderName());
			excel.setDutyUnit(info.getDutyUnitName());
			excel.setLeadUnit(info.getLeadUnitName());
			excel.setSupervisor(info.getSupervisorName());
			excel.setEvaluator(info.getEvaluatorName());
			excel.setRequirement(info.getRequirement());

			List<SupervisionPhasePlan> plans = info.getSupervisionPhasePlanList();
			if (plans!=null&&plans.size()>0) {
				StringBuilder planInfo = new StringBuilder();
				for (SupervisionPhasePlan plan : plans) {
					planInfo.append(plan.getPhaseName()).append("[").append(DateFormatUtils.format(plan.getStartTime(), "yyyy-MM-dd HH:mm:ss")).append("-").append(DateFormatUtils.format(plan.getEndTime(), "yyyy-MM-dd HH:mm:ss")).append("]\n");
				}
				excel.setPhaseList(planInfo.toString());
			}else{
				excel.setPhaseList("");
			}

			export.add(excel);
		});

		try {
			LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy();
			Configure config = Configure.builder().bind("lists", policy).build();//设置列表配置，如果有多个列表时需加.bind("list1", policy) 新列表配置即可
			datas.put("lists", export);//将列表保存到渲染的map中
			//3.创建XWPFTemplate对象，并设置读取模板路径和要渲染的数据
//			ResourceLoader resourceLoader = new DefaultResourceLoader();
//			Resource resource = resourceLoader.getResource("classpath*:src/main/resources/word/事项数据.docx");
//			new File("blade-service/blade-integrated-platform/src/main/resources/word").getAbsolutePath()+"/事项数据.docx"
			File file = ResourceUtils.getFile("classpath:word/事项数据.docx");
			String path = file.getAbsolutePath();
			System.out.println(path);
			XWPFTemplate template = XWPFTemplate.compile(path, config).render(datas);
			//compile(模板路径,对应的配置)方法是设置模板路径和模板配置的，如果不设置配置时可不传config
			//render(datas)方法是用来渲染数据，将准备好的map数据方进去渲染

			//=================⽣成word到设置浏览默认下载地址=================
			// 设置强制下载不打开
			response.setContentType("application/force-download");
			// 设置⽂件名
			response.addHeader("Content-Disposition", "attachment;filename=事项数据.docx");

			OutputStream out = response.getOutputStream();//创建文件输出流并指定位置
			template.write(out);
			out.flush();
			out.close();
			template.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/listUnfinished")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办超期List", notes = "")
	public R<PageInfo> listUnfinished(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ) {
		QueryWrapper<SupervisionInfo> ew =new QueryWrapper<>();
		PageHelper.startPage(query.getCurrent(),query.getSize());
		ew.and(i->i.ne("flow_Status","4").ne("flow_Status","0"));
		ew.apply("date_format( wcsx , '%Y-%m-%d' )> date_format( now( ) , '%Y-%m-%d' )");
		List<SupervisionInfo> records = supervisionInfoService.list(ew);
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SupervisionInfo> details(@Valid @RequestParam Long id) {
		return R.data(supervisionInfoService.details(id));
	}

	/**
	 * 详情
	 * @return
	 */
	@PostMapping("/detailsApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情-app", notes = "vo")
	public R detailsApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("详情-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Long id = jsonParams.getLong("id");
			SupervisionInfo supervisionInfo = supervisionInfoService.details(id);
			JSONObject entityInfo = objectMapper.convertValue(supervisionInfo, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, entityInfo.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 获取事项成事
	 * @param id
	 * @return
	 */
	@GetMapping("/member")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<List<User>> member(@Valid @RequestParam Long id) {
		List<User> userList = supervisionInfoService.getMember(id);
		return R.data(userList);
	}

	/**
	 * 获取事项成事
	 */
	@PostMapping("/memberApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R member(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("memberApp-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Long id = jsonParams.getLong("id");
			List<User> userList = supervisionInfoService.getMember(id);
			JSONArray jsonArray = objectMapper.convertValue(userList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 详情
	 * @param servId
	 * @return
	 */
	@GetMapping("/detailsNew")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SupervisionInfoVO> detailsNew(@Valid  Long servId,String servCode,String tbBus,BladeUser user) {

		return R.data(supervisionInfoService.detailsNew(servId,servCode,tbBus,user));
	}

	/**
	 * 详情
	 * @return
	 */
	@PostMapping("/detailsNewApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情-app", notes = "vo")
	public R detailsNewApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("详情New-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Long servId = jsonParams.getLong("servId");
			String servCode = jsonParams.getString("servCode");
			String tbBus = jsonParams.getString("tbBus");
			SupervisionInfoVO supervisionInfoVO = supervisionInfoService.detailsNew(servId,servCode,tbBus,AuthUtil.getUser());
			JSONObject entityInfo = objectMapper.convertValue(supervisionInfoVO, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, entityInfo.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	@GetMapping("/detailsbyCode")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SupervisionInfo> details(@Valid @RequestParam String servCode) {
		SupervisionInfo serviceOne = supervisionInfoService.getOne(new QueryWrapper<SupervisionInfo>().eq("serv_code", servCode));
		if(ObjectUtil.isNotEmpty(serviceOne)){
			return R.data(supervisionInfoService.details(serviceOne.getId()));
		}else{
			return R.data(null);
		}

	}

	/**
	 * 新增
	 * @param SupervisionInfo
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionInfo SupervisionInfo, String userIds,  String sync,String title) {
		return R.status(supervisionInfoService.savebus(SupervisionInfo,userIds,sync,title));
	}

	/**
	 * 导入数据
	 * @param list
	 * @return
	 */
	@PostMapping("/saveList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R saveList(@Valid @RequestBody List<SupervisionInfo> list) {

		for (SupervisionInfo info : list) {

			if(StringUtils.isNotEmpty(info.getLeadUnit())){
				info.setLeadUnitName(info.getLeadUnit().replace("，",",").replace("、",","));
				String leadUnit = sysClient.getDeptIds("000000", info.getLeadUnitName()).getData();
				info.setLeadUnit(leadUnit);
			}
			if(StringUtils.isNotEmpty(info.getDutyUnit())){
				info.setDutyUnitName(info.getDutyUnit().replace("，",",").replace("、",","));
				String dutyUnit = sysClient.getDeptIds("000000", info.getDutyUnitName()).getData();
				info.setDutyUnit(dutyUnit);
			}

			if(StringUtils.isNotEmpty(info.getDutyLeader())){
				info.setDutyLeaderName(info.getDutyLeader().replace("，",",").replace("、",","));
				String dutyLeader = userClient.getUserIds(info.getDutyLeaderName()).getData();
				info.setDutyLeader(dutyLeader);
			}

			if(StringUtils.isNotEmpty(info.getSupervisor())){
				info.setSupervisorName(info.getSupervisor().replace("，",",").replace("、",","));
				String supervisor = userClient.getUserIds(info.getSupervisorName()).getData();
				info.setSupervisor(supervisor);
			}

			if(StringUtils.isNotEmpty(info.getEvaluator())){
				info.setEvaluatorName(info.getEvaluator().replace("，",",").replace("、",","));
				String evaluator = userClient.getUserIds(info.getEvaluatorName()).getData();
				info.setEvaluator(evaluator);
			}

			String servTypeThree = "";
			String servTypeFour = "";
			if("serv_type_sw_zdgz".equals(info.getServTypeTwo())){//重点工作
				if("serv_type_sw_ypscwq".equals(info.getServTypeThree())){//一屏四城五区
					servTypeFour = dictClient.getKey("serv_type_sw_ypscwq",info.getServTypeFour()).getData();
				}else if("serv_type_sw_qtzdgz".equals(info.getServTypeThree())){//其他重点工作
					servTypeFour = dictClient.getKey("serv_type_sw_qtzdgz",info.getServTypeFour()).getData();
				}else if("serv_type_sw_cyfzldxd".equals(info.getServTypeThree())){//产业发展六大行动
					servTypeFour = dictClient.getKey("serv_type_sw_cyfzldxd",info.getServTypeFour()).getData();
				}else if("serv_type_sw_sjswsc".equals(info.getServTypeThree())){//乡村振兴十二项行动
					servTypeFour = dictClient.getKey("serv_type_sw_sjswsc",info.getServTypeFour()).getData();
				}
				info.setServTypeFour(servTypeFour);
				servTypeThree = info.getServTypeThree();
			}else if("serv_type_sw_zyhy".equals(info.getServTypeTwo())){
				servTypeThree = dictClient.getKey("serv_type_sw_zyhy",info.getServTypeThree()).getData();
			}else if("serv_type_sw_ldps".equals(info.getServTypeTwo())){
				servTypeThree = dictClient.getKey("serv_type_sw_ldps",info.getServTypeThree()).getData();
			}else if("serv_type_sw_lybl".equals(info.getServTypeTwo())){
				servTypeThree = dictClient.getKey("serv_type_sw_lybl",info.getServTypeThree()).getData();
			}
			info.setServTypeThree(servTypeThree);
			info.setFlowStatus("0");
		}
		return R.status(supervisionInfoService.saveAll(list));
	}

	/**
	 * 修改
	 * @param SupervisionInfo
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionInfo SupervisionInfo,String userIds, String sync,String title) {
		return R.status(supervisionInfoService.updatebus(SupervisionInfo,userIds,sync,title));
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@GetMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String ids, @RequestParam String servCodes) {
		return R.status(supervisionInfoService.deletebus(ids,servCodes));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(supervisionInfoService.removeByIds(Arrays.asList(id)));
	}

	/**
	 * 办结
	 * @param id
	 * @return
	 */
	@PostMapping("/over")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "督察督办-办结", notes = "督察督办-办结")
	public R over(@ApiParam(value = "主键", required = true) @RequestParam String id){
		SupervisionInfo supervisionInfo = new SupervisionInfo();
		supervisionInfo.setId(Long.parseLong(id));
		supervisionInfo.setServStatus(StatusConstant.SERV_STATUS_4);
		supervisionInfo.setFlowStatus(StatusConstant.SERV_STATUS_4);
		return R.status(supervisionInfoService.updateById(supervisionInfo));
	}

	/**
	 * 下发
	 * @param id
	 * @param leadUnit
	 * @param dutyUnit
	 * @return
	 */
	@PostMapping("/issue")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "督察督办-下发", notes = "督察督办-下发")
	@Transactional
	public R issue(@ApiParam(value = "主键", required = true) @RequestParam String id,
				   @ApiParam(value = "牵头单位", required = true) @RequestParam String leadUnit,
				   @ApiParam(value = "责任单位", required = false) @RequestParam(required = false) String dutyUnit){

		//处理签收单位记录
		SupervisionInfo supervisionInfo = new SupervisionInfo();
		supervisionInfo.setId(Long.parseLong(id));
		supervisionInfo.setLeadUnit(leadUnit);
		supervisionInfo.setDutyUnit(dutyUnit);

		this.supervisionSignService.saveSignInfo(supervisionInfo);

		return R.status(this.supervisionInfoService.updateFlowStatus(Long.parseLong(id),StatusConstant.FLOW_STATUS_1));
	}

	/**
	 * 立项送审
	 * @param id		事项id主键
	 * @param title		送审标题
	 * @param userIds	用户主键，多个逗号隔开
	 * @param sync		同步还是异步 1同步；0异步
	 * @return
	 */
	@PostMapping("/submitAudit")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "督察督办-立项送审", notes = "督察督办-立项送审")
	@Transactional
	public R submitAudit(@RequestParam String id,@RequestParam String title,@RequestParam String userIds,@RequestParam String sync){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync,StatusConstant.OPERATION_TYPE_INFO);

			//修改状态
			this.supervisionInfoService.updateFlowStatus(Long.parseLong(id),StatusConstant.FLOW_STATUS_5);

			//发消息
			SupervisionInfo supervisionInfo = this.supervisionInfoService.getById(id);
			String value = DictBizCache.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo());

			String users = userIds;
			if("0".equals(sync)){
				users = userIds.split(",")[0];
			}
			this.unifyMessageService.sendDcSsMsg(UserCache.getUser(AuthUtil.getUserId()).getRealName(),id,users,value,supervisionInfo.getServName());

			//任务日志
			User user = UserCache.getUser(AuthUtil.getUserId());
			SupervisionLog log = new SupervisionLog();
			log.setServCode(supervisionInfo.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("7");
			log.setOperationTime(new Date());
			log.setContent("【"+supervisionInfo.getServName()+"】已送审");
			supervisionLogService.save(log);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}
	/**
	 * 事项办结
	 * @param id
	 * @return
	 */
	@PostMapping("/finish")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "办结", notes = "vo")
	public R Finish(@Valid @RequestParam Long  id, BladeUser user) {
		return R.status(supervisionInfoService.finish(id,user));
	}

	/**
	 * 部门办结
	 * @param deptIds
	 * @param servId
	 * @return
	 */
	@PostMapping("/finishDept")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "部门办结", notes = "vo")
	public R finishDept(@Valid @RequestParam String deptIds,@RequestParam Long servId, BladeUser user) {

		return R.status(supervisionInfoService.finishDept(deptIds,servId,user));
	}

	/**
	 * 获取能办结的部门
	 * @param servId
	 * @return
	 */
	@GetMapping("/finishDeptList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取能办结的部门", notes = "")
	public R<List<SupervisionSignVO>> finishDeptList(@ApiIgnore  @RequestParam Long servId ) {
		QueryWrapper<SupervisionSign> wrapper=new QueryWrapper();
		wrapper.eq("serv_Id",servId);
		wrapper.eq("over_Status","0");
		List<SupervisionSign> supervisionSigns = supervisionSignService.list(wrapper);
		return R.data(SupervisionSignWrapper.build().listVO(supervisionSigns));
	}

	/**
	 * 督查督办统计
	 * @return
	 */
	@GetMapping("/listStatistics")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督查督办统计", notes = "")
	public R<Map<String,Object>> listStatistics(@ApiIgnore String servTypeThree,String deptId,String year) {
		Map<String,Object> aa= supervisionInfoService.listStatistics(servTypeThree,deptId,year);
		return R.data(aa);
	}

	/**
	 * 督查督办统计-app
	 * @return
	 */
	@PostMapping("/listStatisticsApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督查督办统计-app", notes = "")
	public R listStatisticsApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("督查督办统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String servTypeThree = jsonParams.getString("servTypeThree");
			String deptId = jsonParams.getString("deptId");
			String year = jsonParams.getString("year");
			Map<String,Object> resultMap = supervisionInfoService.listStatistics(servTypeThree,deptId,year);
			JSONObject pageJson = objectMapper.convertValue(resultMap, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 获取释统计列表
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/supervisionInfoList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取释统计列表", notes = "")
	public R<PageInfo> supervisionInfoList(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ,BladeUser user) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper();
		List<SupervisionInfo> records=new ArrayList<>();

		if(ObjectUtil.isNotEmpty(entity.get("flowStatus"))){
			if(entity.get("flowStatus").equals("dhb")){
				records = supervisionInfoService.listStatisticsdhb(entity);
			}else if(entity.get("flowStatus").equals("ycq")){
				records = supervisionInfoService.listStatisticsycq(entity);
			}else {
				if(entity.get("flowStatus").equals("3")){
					wrapper.in("flow_Status","1","2","3");
					wrapper.apply("date_format( wcsx , '%Y-%m-%d %H:%i:%s' )>= date_format( now( ) , '%Y-%m-%d %H:%i:%s' )");
				}else if(entity.get("flowStatus").equals("4")) {
//					wrapper.notIn("serv_status","3","5");
					wrapper.eq("flow_Status",entity.get("flowStatus"));
				}
				if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
					wrapper.eq("serv_Type_Three",entity.get("servTypeThree"));
				}
				if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
					wrapper.eq("create_dept",entity.get("deptId"));
				}
				if (ObjectUtil.isNotEmpty(entity.get("searchYear"))){
					wrapper.eq("year(create_time)",entity.get("searchYear"));
				}

				wrapper.orderByDesc("create_time");
				records = supervisionInfoService.list(wrapper);
			}
		}else {
			if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
				wrapper.eq("serv_Type_Three",entity.get("servTypeThree"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
				wrapper.eq("create_dept",entity.get("deptId"));
			}
			wrapper.notIn("ifnull(flow_status,'0')","0","5");

			if (ObjectUtil.isNotEmpty(entity.get("searchYear"))){
				wrapper.eq("year(create_time)",entity.get("searchYear"));
			}
			wrapper.orderByDesc("create_time");
			records = supervisionInfoService.list(wrapper);
		}
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 获取释统计列表
	 * @return
	 */
	@PostMapping("/supervisionInfoListApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取释统计列表", notes = "")
	public R supervisionInfoListApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("user-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			PageHelper.startPage(query.getCurrent(),query.getSize());
			QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper();
			List<SupervisionInfo> records;
			Map<String, Object> entity = new HashMap<>(jsonParams);
			if(ObjectUtil.isNotEmpty(entity.get("flowStatus"))){
				if(entity.get("flowStatus").equals("dhb")){
					records = supervisionInfoService.listStatisticsdhb(entity);
				}else if(entity.get("flowStatus").equals("ycq")){
					records = supervisionInfoService.listStatisticsycq(entity);
				}else {
					if(entity.get("flowStatus").equals("3")){
						wrapper.in("flow_Status","1","2","3");
						wrapper.apply("date_format( wcsx , '%Y-%m-%d %H:%i:%s' )>= date_format( now( ) , '%Y-%m-%d %H:%i:%s' )");
					}else if(entity.get("flowStatus").equals("4")) {
						wrapper.eq("flow_Status",entity.get("flowStatus"));
					}
					if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
						wrapper.eq("serv_Type_Three",entity.get("servTypeThree"));
					}
					if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
						wrapper.eq("create_dept",entity.get("deptId"));
					}
					if (ObjectUtil.isNotEmpty(entity.get("searchYear"))){
						wrapper.eq("year(create_time)",entity.get("searchYear"));
					}

					wrapper.orderByDesc("create_time");
					records = supervisionInfoService.list(wrapper);
				}
			}else {
				if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
					wrapper.eq("serv_Type_Three",entity.get("servTypeThree"));
				}
				if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
					wrapper.eq("create_dept",entity.get("deptId"));
				}
				wrapper.notIn("ifnull(flow_status,'0')","0","5");

				if (ObjectUtil.isNotEmpty(entity.get("searchYear"))){
					wrapper.eq("year(create_time)",entity.get("searchYear"));
				}
				wrapper.orderByDesc("create_time");
				records = supervisionInfoService.list(wrapper);
			}
			PageInfo pageInfo = new PageInfo(records);
			JSONObject resultJson = objectMapper.convertValue(pageInfo, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, resultJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	@GetMapping("/mySupervisionCon")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "我的督查督办", notes = "")
	public R<Map<String,Object>> mySupervisionCon(@RequestParam Map<String, Object> entity, BladeUser user) {
		//未签收
		List<SupervisionSign> supervisionInfos = supervisionInfoService.supervisionNoSign(entity, user);
		//未汇总
		List<SupervisionInfo> supervisionInfo1 = supervisionInfoService.supervisionNoReport(entity, user);

		//超期未办结
		List<SupervisionInfo> supervisionInfo2 = supervisionInfoService.supervisionOverdue(entity, user);

		Map<String,Object> aa=new HashMap<>();
		aa.put("wqs",supervisionInfos.size());
		aa.put("whb",supervisionInfo1.size());
		aa.put("cqwbj",supervisionInfo2.size());
		aa.put("sum",supervisionInfos.size()+supervisionInfo1.size()+supervisionInfo2.size());
		return R.data(aa);
	}

	@GetMapping("/mySupervision")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "我的督查督办", notes = "")
	public R<Map<String,Object>> mySupervision(@RequestParam Map<String, Object> entity, BladeUser user) {
		//未签收
		List<SupervisionSign> supervisionInfos = supervisionInfoService.supervisionNoSign(entity, user);
		//未汇总
		List<SupervisionInfo> supervisionInfo1 = supervisionInfoService.supervisionNoReport(entity, user);

		//超期未办结
		List<SupervisionInfo> supervisionInfo2 = supervisionInfoService.supervisionOverdue(entity, user);
		Map<String,Object> map=new HashMap<>();
		map.put("wqsList",supervisionInfos);
		map.put("whbList",supervisionInfo1);
		map.put("cqwbjList",supervisionInfo2);
		return R.data(map);
	}

	/**
	 * 未签收
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/supervisionNoSign")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "未签收", notes = "")
	public R<PageInfo> supervisionNoSign(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ,BladeUser user) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionSign> records = supervisionInfoService.supervisionNoSign(entity,user);
		PageInfo pageInfo = new PageInfo(SupervisionSignWrapper.build().listVO(records));
		return R.data(pageInfo);
	}

	/**
	 * 未汇总
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/supervisionNoReport")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "未汇总", notes = "")
	public R<PageInfo> supervisionNoReport(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ,BladeUser user) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionInfo> records = supervisionInfoService.supervisionNoReport(entity,user);
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 超期未办结
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/supervisionOverdue")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "超期未办结", notes = "")
	public R<PageInfo> supervisionOverdue(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ,BladeUser user) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionInfo> records = supervisionInfoService.supervisionOverdue(entity,user);
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 领导关注
	 * @param entity
	 * @param user
	 * @return
	 */
	@GetMapping("/supervisionFollow")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "领导关注", notes = "")
	public R<PageInfo> supervisionFollow(@ApiIgnore Query query,@RequestParam Map<String, Object> entity, BladeUser user) {
		PageInfo aa= supervisionInfoService.supervisionFollow(query,entity,user);
		return R.data(aa);
	}

	/**
	 * 我的关注
	 * @param entity
	 * @param user
	 * @return
	 */
	@GetMapping("/supervisionMyFollow")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "我的关注", notes = "")
	public R<PageInfo> supervisionMyFollow(@ApiIgnore Query query,@RequestParam Map<String, Object> entity, BladeUser user) {
		PageInfo aa= supervisionInfoService.supervisionMyFollow(query,entity,user);
		return R.data(aa);
	}

	/**
	 * 获取事项各部门汇报评价情况
	 * @param entity
	 * @return
	 */
	@GetMapping("/servDeptPlanReport")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取能办结的部门", notes = "")
	public R<List<SupervisionDeptPlanReportVO>> servDeptPlanReport(@ApiIgnore @RequestParam Map<String, Object> entity) {
		List<SupervisionDeptPlanReportVO> deptPlanReportVOS = supervisionInfoService.servDeptPlanReport(entity);
		for(SupervisionDeptPlanReportVO vo:deptPlanReportVOS){
			R<String> deptNameR = sysClient.getDeptName(Long.valueOf(vo.getDeptId()));
			String deptName=deptNameR.getData();
			if(ObjectUtil.isNotEmpty(deptName)){
				vo.setDeptName(deptName);
			}
		}
		return R.data(deptPlanReportVOS);
	}


	/**
	 * 修改责任单位
	 * @param id
	 * @param servCode
	 * @param dutyUnit
	 * @param dutyUnitName
	 * @return
	 */
	@GetMapping("/updateDuty")
	public R updateDuty(@RequestParam long id,String servCode,String dutyUnit,String dutyUnitName){
		return R.status(supervisionInfoService.updateDuty(id,servCode,dutyUnit,dutyUnitName));
	}

}
