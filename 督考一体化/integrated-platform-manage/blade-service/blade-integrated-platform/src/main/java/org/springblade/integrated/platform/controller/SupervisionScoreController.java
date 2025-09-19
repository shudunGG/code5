package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.vo.SupervisionDeptScoretVO;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.ISupervisionScoreService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author mrtang
 * @version 1.0
 * @description: 事项部门签收控制层
 * @date 2022/4/18 10:27
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionScore")
public class SupervisionScoreController {

	private final ISupervisionScoreService supervisionScoreService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 部门分组得分统计
	 * @param deptGroup
	 * @return
	 */
	@GetMapping("/deptScoreStatistics")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "部门分组得分统计", notes = "部门分组得分统计")
	public R<List<SupervisionDeptScoretVO>> deptScoreStatistics(@RequestParam String deptGroup,@RequestParam String supYear,@RequestParam String startTime,@RequestParam String endTime){
		List<SupervisionDeptScoretVO> supervisionDeptScoretVOS = supervisionScoreService.deptScoreStatistics(deptGroup,supYear, startTime,endTime);
		return R.data(supervisionDeptScoretVOS);
	}

	/**
	 * 部门分组得分统计
	 * @return
	 */
	@PostMapping("/deptScoreStatisticsApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "部门分组得分统计-app", notes = "部门分组得分统计-app")
	public R deptScoreStatisticsApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("部门分组得分统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String deptGroup = jsonParams.getString("deptGroup");
			String supYear = jsonParams.getString("supYear");
			String startTime = jsonParams.getString("startTime");
			String endTime = jsonParams.getString("endTime");
			List<SupervisionDeptScoretVO> supervisionDeptScoretVOS = supervisionScoreService.deptScoreStatistics(deptGroup,supYear, startTime,endTime);
			JSONArray jsonArray = objectMapper.convertValue(supervisionDeptScoretVOS, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}


	/**
	 * 部门分组得分统计
	 * @param deptGroup
	 * @return
	 */
	@GetMapping("/deptScoreDetails")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "部门分组得分统计", notes = "部门分组得分统计")
	public R<List<SupervisionDeptScoretVO>> deptScoreDetails(@RequestParam String deptGroup,@RequestParam Long deptId,@RequestParam String startTime,@RequestParam String endTime){
		List<SupervisionDeptScoretVO> supervisionDeptScoretVOS = supervisionScoreService.deptScoreDetails(deptGroup,deptId,startTime,endTime);
		return R.data(supervisionDeptScoretVOS);
	}

	/**
	 * 部门分组得分统计-app
	 * @return
	 */
	@PostMapping("/deptScoreDetailsApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "部门分组得分统计", notes = "部门分组得分统计")
	public R deptScoreDetailsApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("部门分组得分统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String deptGroup = jsonParams.getString("deptGroup");
			Long deptId = jsonParams.getLong("deptId");
			String startTime = jsonParams.getString("startTime");
			String endTime = jsonParams.getString("endTime");
			List<SupervisionDeptScoretVO> supervisionDeptScoretVOS = supervisionScoreService.deptScoreDetails(deptGroup,deptId,startTime,endTime);
			JSONArray jsonArray = objectMapper.convertValue(supervisionDeptScoretVOS, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}

	}


}

