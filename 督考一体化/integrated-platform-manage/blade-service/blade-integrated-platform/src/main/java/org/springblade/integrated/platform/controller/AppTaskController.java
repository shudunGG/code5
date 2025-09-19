package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.AppTask;
import com.vingsoft.vo.AppTaskVO;
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
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.service.IAppTaskService;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
* @Description:    交办信息
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:24
* @Version:        1.0
*/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/appTask")
@Api(value = "交办信息", tags = "交办信息")
public class AppTaskController extends BladeController {

	private final IAppTaskService appTaskService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<AppTaskVO>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<AppTask> pages = appTaskService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, AppTask.class).and(wrapper -> wrapper.eq("create_user", AuthUtil.getUserId()).or().like("receive_id",AuthUtil.getUserId())).orderByDesc("send_time"));
		return R.data(AppTaskWrapper.build().pageVO(pages));
	}

	/**
	 * 分页
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R list(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("appTask-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			Map<String, Object> entity = new HashMap<>(jsonParams);
			IPage<AppTask> pages = appTaskService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, AppTask.class).and(wrapper ->
				wrapper.eq("create_user", AuthUtil.getUserId()).or().like("receive_id",AuthUtil.getUserId())).orderByDesc("send_time"));
			JSONObject pageJson = objectMapper.convertValue(AppTaskWrapper.build().pageVO(pages), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入vo")
	public R save(@Valid @RequestBody AppTaskVO vo) {
		return R.status(appTaskService.saveAll(vo));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入entity")
	public R update(@Valid @RequestBody AppTask entity) {
		return R.status(appTaskService.updateById(entity));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<AppTask> detail(AppTask entity) {
		AppTask detail = appTaskService.getOne(Condition.getQueryWrapper(entity));
		return R.data(detail);
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(appTaskService.deleteLogic(Func.toLongList(ids)));
	}
}

