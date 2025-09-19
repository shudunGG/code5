package org.springblade.integrated.platform.controller;


import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.ProjectPhaseReportSwitch;
import com.vingsoft.entity.WorkMonth;
import com.vingsoft.entity.WorkMonthAppraise;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.runtime.WithObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RList;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.excel.WorkMonthExcel;
import org.springblade.integrated.platform.excel.WorkMonthImporter;
import org.springblade.integrated.platform.service.IWorkMonthAppraiseService;
import org.springblade.integrated.platform.service.IWorkMonthService;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 考核评价-领导评价 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
@Slf4j
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("workMonth")
public class WorkMonthController extends BladeController {

	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserClient userClient;
	@Resource
	private IWorkMonthService workMonthService;
	@Resource
	private IWorkMonthAppraiseService workMonthAppraiseService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * 工作月调度列表
	 */
	@GetMapping("/newList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "", notes = "")
	public List<Map<String, Object>> newList(String checkClassify, String month, String stageYear) {

		//获取部门列表
		R<List<Dept>> RDept = sysClient.getDeptByGroup("000000", checkClassify, stageYear);
		List<Dept> deptList = RDept.getData();
		List<Map<String, Object>> list = new ArrayList<>();
		if(deptList != null && deptList.size() > 0){
			for(int i=0; i<deptList.size(); i++){
				Long deptCode = deptList.get(i).getId();
				QueryWrapper queryWrapper = new QueryWrapper();
				queryWrapper.eq("dept_code",deptCode.toString());
				queryWrapper.eq("month",month);
//				queryWrapper.orderByDesc("create_time");
				queryWrapper.orderByDesc("update_time");
				List<WorkMonth> workMonthList = workMonthService.list(queryWrapper);
				//获取所需字段
				Map<String,Object> map = new HashMap();
				if(workMonthList != null && workMonthList.size() > 0){
					map.put("deptName",deptList.get(i).getDeptName());
					map.put("deptCode",deptCode);
					map.put("month",month);
					map.put("planStatus","1"); //0未上报计划  1已上报计划
					map.put("planTime",workMonthList.get(0).getCreateTime());
					if(StringUtil.isNotBlank(workMonthList.get(0).getJhqk()) && workMonthList.get(0).getJhqk().equals("2")){
						//完成情况  汇报
						map.put("finishStatus","1");  //已上传
					}else{
						map.put("finishStatus","0");  //未上传
					}
					map.put("finishTime",workMonthList.get(0).getUpdateTime());  //数据修改时间
				}else{
					map.put("deptName",deptList.get(i).getDeptName());
					map.put("deptCode",deptCode);
					map.put("month",month);
					map.put("planStatus","0");  //0未上报计划  1已上报计划
					map.put("planTime","");
					map.put("finishStatus","");
					map.put("finishTime","");
				}

				list.add(map);
			}
		}

		return list;
	}


	/**
	 * 分页查询
	 *
	 * @param
	 * @param
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "", notes = "")
	public List<Map<String, Object>> list(String checkClassify, String month, String stageYear) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String manageDept = user.getManageDept();  //分管部门
		//查询数据，封装分页参数
		R<List<Dept>> Rdeptids = sysClient.getDeptByGroup("000000", checkClassify, stageYear);
		List<Dept> data = Rdeptids.getData();
		List<Map<String, Object>> list = new ArrayList<>();
		List<Map<String, Object>> workMonths = workMonthService.workListPage(month);
		if (workMonths.size() > 0) {
			for (int i = 0; i < data.size(); i++) {
				Map map = new HashMap();
				map.put("deptName", data.get(i).getDeptName() + "");
				map.put("deptCode", data.get(i).getId());
				int a = 0;
				int b = 1;
				String num = null;
				for (int j = 0; j < workMonths.size(); j++) {
					if (workMonths.get(j).get("deptCode").equals(data.get(i).getId() + "")) {
						a = j;
						if (j == 0) {
							b = j;
						}
						num = data.get(i).getId() + "";
					}
				}

				//评价处理
				if(manageDept.contains(data.get(i).getId().toString())){
					List<WorkMonthAppraise> workMonthAppraiseList = workMonthAppraiseService.list(Wrappers.<WorkMonthAppraise>query().lambda()
						.eq(WorkMonthAppraise::getDeptCode, data.get(i).getId().toString()).eq(WorkMonthAppraise::getMonth,month));
					if(workMonthAppraiseList != null && workMonthAppraiseList.size() > 0){
						map.put("canAppraise","2");  //已评价，修改为2
						map.put("appraiseScore", workMonthAppraiseList.get(0).getAppraiseScore());
						map.put("appraiseExplain", workMonthAppraiseList.get(0).getAppraiseExplain());
						map.put("appraiseId",workMonthAppraiseList.get(0).getId());  //评价主键id
					}else{
						map.put("canAppraise","1"); //匹配到了分管部门，1 可以评价
					}
				}else{
					map.put("canAppraise","0"); //是否可以评价 默认0 不可以评价
				}

				List<Map<String, Object>> list1 = workMonthService.selectTime(month, "1", num);
				List<Map<String, Object>> list2 = workMonthService.selectTime(month, "2", num);
				if (b == 0) {
					map.put("createTime", workMonths.get(0).get("createTime"));
					map.put("jhwcl", workMonths.get(0).get("jhwcl"));
					map.put("month", month);
					map.put("num", workMonths.get(0).get("num"));

					map.put("jhnum", workMonths.get(0).get("jhnum"));
					map.put("sbnum", workMonths.get(0).get("sbnum"));
					map.put("completion", workMonths.get(0).get("completion"));
					if (list1.size() > 0) {
						map.put("jhtime", list1.get(0).get("createTime"));
					} else {
						map.put("jhtime", "");
					}
					if (list2.size() > 0) {
						map.put("sbtime", list2.get(0).get("createTime"));
					} else {
						map.put("sbtime", "");
					}
					list.add(map);
				}
				else if (a > 0) {
					map.put("createTime", workMonths.get(a).get("createTime"));
					map.put("jhwcl", workMonths.get(a).get("jhwcl"));
					map.put("month", month);
					map.put("num", workMonths.get(a).get("num"));
					map.put("jhnum", workMonths.get(a).get("jhnum"));
					map.put("sbnum", workMonths.get(a).get("sbnum"));
					map.put("completion", workMonths.get(a).get("completion"));
					if (list1.size() > 0) {
						map.put("jhtime", list1.get(0).get("createTime"));
					} else {
						map.put("jhtime", "");
					}
					if (list2.size() > 0) {
						map.put("sbtime", list2.get(0).get("createTime"));
					} else {
						map.put("sbtime", "");
					}
					list.add(map);
				}
				else {

					map.put("createTime", "");
					map.put("num", 0);
					map.put("jhwcl", 0);
					map.put("jhtime", "");
					map.put("sbtime", "");
					map.put("month", month);
					map.put("jhnum", 0);
					map.put("sbnum", 0);
					map.put("completion", "");
					list.add(map);
				}
			}
		} else {
			for (int i = 0; i < data.size(); i++) {
				Map map = new HashMap();
				map.put("deptName", data.get(i).getDeptName() + "");
				map.put("deptCode", data.get(i).getId());
				map.put("createTime", "");
				map.put("num", 0);
				map.put("jhwcl", 0);
				map.put("jhtime", "");
				map.put("sbtime", "");
				map.put("month", month);
				map.put("jhnum", 0);
				map.put("sbnum", 0);
				map.put("completion", "");
				list.add(map);
			}
		}
		return list;
	}


	/**
	 * 分页查询
	 *
	 * @param
	 * @param
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "工作月调度分页", notes = "传入workMonth")
	public List<WorkMonth> listPage(String deptCode, String month) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//查询数据，封装分页参数
		List<WorkMonth> detail = workMonthService.detail(month, deptCode);
		SM4Crypto sm4 = SM4Crypto.getInstance();
		detail.forEach(workMonth -> {
			if(StringUtils.isNotEmpty(workMonth.getPersonLiable()) && sm4.checkDataIsEncrypt(workMonth.getPersonLiable())){
				workMonth.setPersonLiable(sm4.decrypt(workMonth.getPersonLiable()));
			}
			if(StringUtils.isNotEmpty(workMonth.getHbPerson()) && sm4.checkDataIsEncrypt(workMonth.getHbPerson())){
				workMonth.setHbPerson(sm4.decrypt(workMonth.getHbPerson()));
			}
		});
		return detail;
	}

	/**
	 * 分页查询
	 */
	@PostMapping("/pageApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "工作月调度分页", notes = "传入workMonth")
	public R pageApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("workMonth-pageApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String deptCode = jsonParams.getString("deptCode");
			String month = jsonParams.getString("month");

			//查询数据，封装分页参数
			List<WorkMonth> detail = workMonthService.detail(month, deptCode);
			SM4Crypto sm4 = SM4Crypto.getInstance();
			detail.forEach(workMonth -> {
				if(StringUtils.isNotEmpty(workMonth.getPersonLiable()) && sm4.checkDataIsEncrypt(workMonth.getPersonLiable())){
					workMonth.setPersonLiable(sm4.decrypt(workMonth.getPersonLiable()));
				}
				if(StringUtils.isNotEmpty(workMonth.getHbPerson()) && sm4.checkDataIsEncrypt(workMonth.getHbPerson())){
					workMonth.setHbPerson(sm4.decrypt(workMonth.getHbPerson()));
				}
			});
			JSONArray jsonArray = objectMapper.convertValue(detail, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	@GetMapping("/getDept")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "", notes = "")
	public R getDept(String deptCode) {

		List<String> list = new ArrayList<>();
		list.add(0, "2");
		list.add(1, "5");
		R<List<Map<String, Object>>> listR = sysClient.selectDeptList(list);
		List<Map<String, Object>> data = listR.getData();

		int num = 0;
		for (int i = 0; i < data.size(); i++) {
			if (deptCode.equals(data.get(i).get("id"))) {
				num++;
			}
		}
		if (num > 0) {
			return R.data("1");
		} else {
			List<String> list2 = new ArrayList<>();
			list2.add(0, "3");
			list2.add(1, "4");
			list2.add(2, "6");
			list2.add(3, "7");
			list2.add(4, "8");
			R<List<Map<String, Object>>> listR2 = sysClient.selectDeptList(list2);
			List<Map<String, Object>> data2 = listR2.getData();

			for (int i = 0; i < data2.size(); i++) {
				if (deptCode.equals(data2.get(i).get("id"))) {
					num++;
				}
			}
			if (num > 0) {
				return R.data("2");
			} else return R.data(false);

		}
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入workMonth")
	public R save(@Valid @RequestBody List<WorkMonth> workMonth) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm");
		Date date = new Date();
		String format = dateFormat.format(date);
		int num = 0;
		for (int i = 0; i < workMonth.size(); i++) {
			workMonth.get(i).setCreateUserCode(String.valueOf(user.getId()));
			workMonth.get(i).setCreateUserName(user.getName());

			boolean save = workMonthService.save(workMonth.get(i));
			if (save == true) {
				num++;
			}
		}
		if (num == workMonth.size() - 1) {
			return R.data(true);
		} else {
			return R.data(false);
		}
	}

	/**
	 * 新增
	 */
	@PostMapping("/saveApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入workMonth")
	public R save(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("workMonth-saveApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONArray jsonParams = JSONObject.parseArray(dataMap.get("extra").toString());
			List<WorkMonth> workMonth = JSON.parseArray(jsonParams.toJSONString(),WorkMonth.class);
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			int num = 0;
			for (int i = 0; i < workMonth.size(); i++) {
				workMonth.get(i).setCreateUserCode(String.valueOf(user.getId()));
				workMonth.get(i).setCreateUserName(user.getName());

				boolean save = workMonthService.save(workMonth.get(i));
				if (save == true) {
					num++;
				}
			}
			if (num == workMonth.size() - 1) {
				return R.data(VSTool.encrypt(encryptSign, "true", VSTool.CHN));
			} else {
				return R.data(VSTool.encrypt(encryptSign, "false", VSTool.CHN));
			}
		}else {
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 编辑
	 *
	 * @param
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "", notes = "传入workMonth")
	public R update(@Valid @RequestBody WorkMonth workMonth) {
		workMonth.setJhqk("2");
		return R.status(workMonthService.updateById(workMonth));
	}

	/**
	 * 编辑
	 */
	@PostMapping("/updateApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "", notes = "传入workMonth")
	public R update(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("workMonth-updateApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			WorkMonth workMonth = objectMapper.convertValue(jsonParams, WorkMonth.class);
			workMonth.setJhqk("2");
			return R.data(VSTool.encrypt(encryptSign, String.valueOf(workMonthService.updateById(workMonth)), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 获取工作月调度的可用性
	 * @param
	 * @return
	 */
	@GetMapping("/getMonthWorkModifyStatus")
	@ApiOperationSupport(order = 5)
	public R getMonthWorkModifyStatus() {
		return R.data(PropConstant.getMonthWorkModifyStatus());
	}

	/**
	 * 导出
	 */
	@GetMapping("export-workMonth")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "工作月调度导出", notes = "传入leaderApprise")
	public void exportWorkMonth(WorkMonth workMonth, HttpServletResponse response) {
		//sql查询条件
		QueryWrapper<WorkMonth> queryWrapper = new QueryWrapper<WorkMonth>();
		queryWrapper.select(" * ");
		queryWrapper.eq(workMonth.getId()!=null,"id",workMonth.getId());
		List<WorkMonthExcel> list = workMonthService.exportWorkMonth(queryWrapper);
		ExcelUtil.export(response, "月工作计划及完成情况表-" + DateUtil.time(), "月工作计划及完成情况", list, WorkMonthExcel.class);

		String title = "导出了月工作计划及完成情况表";
		String businessId = String.valueOf(workMonth.getId());
		String businessTable = "WorkMonth";
		int businessType = BusinessType.EXPORT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
	}

	/**
	 * 导入-工作月调度数据
	 */
	@PostMapping("import-workMonth")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "导入工作月调度", notes = "传入excel")
	public R importRegion(MultipartFile file) throws IOException {
		Long businessId=0L;
		String businessTable="";

		try {
			WorkMonthImporter workMonthImporter = new WorkMonthImporter(workMonthService);
			ExcelUtil.save(file, workMonthImporter, WorkMonthExcel.class);
		} catch (Exception ex) {
			throw ex;
		}

		return R.success("操作成功！");
	}
}
