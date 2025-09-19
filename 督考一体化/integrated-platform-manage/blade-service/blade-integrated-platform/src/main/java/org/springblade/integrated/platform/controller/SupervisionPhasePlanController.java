package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionPhasePlan;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.ISupervisionPhasePlanService;
import org.springblade.integrated.platform.wrapper.SupervisionSubmitAuditWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办阶段控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/SupervisionPhasePlan")
@Api(value = "督察督办阶段", tags = "督察督办阶段")
public class SupervisionPhasePlanController extends BladeController {

	private final ISupervisionPhasePlanService supervisionPhasePlanService;
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
	@ApiOperation(value = "督察督办阶段List", notes = "")
	public R<PageInfo> list(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ) {
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionPhasePlan> records = supervisionPhasePlanService.queryList(entity);
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	@GetMapping("/listAll")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办阶段ListAll", notes = "")
	public R<List<SupervisionPhasePlan>> listAll(@ApiIgnore @RequestParam Map<String, Object> entity ) {
		List<SupervisionPhasePlan> records = supervisionPhasePlanService.queryList(entity);

		return R.data(records);
	}

	@GetMapping("/listHB")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "listHB", notes = "")
	public R<List<SupervisionPhasePlan>> listHB(@ApiIgnore @RequestParam Map<String, Object> entity , BladeUser user) {
		List<SupervisionPhasePlan> records = supervisionPhasePlanService.queryListHB(entity,user);
		return R.data(records);
	}

	@PostMapping("/listHBApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "listHBApp", notes = "")
	public R listHBApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("listHBApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Map<String, Object> entity = new HashMap<>(jsonParams);
			List<SupervisionPhasePlan> records = supervisionPhasePlanService.queryListHB(entity, AuthUtil.getUser());
			JSONArray jsonArray = objectMapper.convertValue(records, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	@GetMapping("/listHBAll")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "listHBAll", notes = "")
	public R<List<SupervisionPhasePlan>> listHBAll(@ApiIgnore @RequestParam Map<String, Object> entity , BladeUser user) {
		List<SupervisionPhasePlan> records = supervisionPhasePlanService.queryListHBAll(entity,user);
		return R.data(records);
	}


	/**
	 * 新增
	 * @param SupervisionPhasePlan
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionPhasePlan SupervisionPhasePlan) {
		return R.status(supervisionPhasePlanService.save(SupervisionPhasePlan));
	}

	/**
	 * 修改
	 * @param supervisionPhasePlanList
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody List<SupervisionPhasePlan> supervisionPhasePlanList,String servCode,SupervisionInfo supervisionInfo) {
		return R.status(supervisionPhasePlanService.updateList(supervisionPhasePlanList,servCode,supervisionInfo));
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
		return R.status(supervisionPhasePlanService.removeById(id));
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
		return R.status(supervisionPhasePlanService.removeByIds(Arrays.asList(id)));
	}

}
