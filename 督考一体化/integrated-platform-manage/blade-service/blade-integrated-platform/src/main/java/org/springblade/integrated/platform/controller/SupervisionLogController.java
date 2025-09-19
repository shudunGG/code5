package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.ISupervisionLogService;
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
 *  @Description: 督察督办任务日志控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionLog")
@Api(value = "督察督办任务日志", tags = "督察督办任务日志")
public class SupervisionLogController extends BladeController {

	private final ISupervisionLogService supervisionLogService;
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
	@ApiOperation(value = "督察督办日志", notes = "")
	public R<PageInfo> list(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ) {
		PageHelper.startPage(query.getCurrent(),query.getSize()).setOrderBy("operation_Time desc");
		QueryWrapper<SupervisionLog> queryWrapper = Condition.getQueryWrapper(entity, SupervisionLog.class);
		List<SupervisionLog> records = supervisionLogService.list(queryWrapper);
		PageInfo pageInfo = new PageInfo(records);
		return R.data(pageInfo);
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办日志-app", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("supervisionLog-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			PageHelper.startPage(query.getCurrent(),query.getSize()).setOrderBy("operation_Time desc");
			Map<String, Object> entity = new HashMap<>(jsonParams);
			QueryWrapper<SupervisionLog> queryWrapper = Condition.getQueryWrapper(entity, SupervisionLog.class);
			List<SupervisionLog> records = supervisionLogService.list(queryWrapper);
			PageInfo pageInfo = new PageInfo(records);
			JSONObject resultJson = objectMapper.convertValue(pageInfo, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, resultJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 分页查询
	 * @param entity
	 * @return
	 */
	@GetMapping("/listDT")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办日志", notes = "")
	public R<List<SupervisionLog>> listDT(@ApiIgnore @RequestParam Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionLog> queryWrapper =new QueryWrapper<>();
		queryWrapper.orderByDesc("operation_Time");
//		List<SupervisionLog> records = supervisionLogService.list(queryWrapper);
		List<SupervisionLog> records = supervisionLogService.listQueryWrapper(entity,user);
		return R.data(records);
	}

	/**
	 * 新增
	 * @param SupervisionLog
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionLog SupervisionLog) {
		return R.status(supervisionLogService.save(SupervisionLog));
	}

	/**
	 * 修改
	 * @param SupervisionLog
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionLog SupervisionLog) {
		return R.status(supervisionLogService.updateById(SupervisionLog));
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
		return R.status(supervisionLogService.removeById(id));
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
		return R.status(supervisionLogService.removeByIds(Arrays.asList(id)));
	}

}
