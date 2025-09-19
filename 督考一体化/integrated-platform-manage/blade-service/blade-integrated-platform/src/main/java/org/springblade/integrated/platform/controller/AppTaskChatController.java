package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.AppTaskChat;
import com.vingsoft.vo.AppTaskChatVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.service.IAppTaskChatService;
import org.springblade.integrated.platform.service.IAppTaskService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.integrated.platform.wrapper.AppTaskChatWrapper;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

/**
* @Description:    交办回复
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:25
* @Version:        1.0
*/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/appTaskChat")
@Api(value = "交办回复", tags = "交办回复")
public class AppTaskChatController extends BladeController {

	private final IAppTaskChatService appTaskChatService;

	/**
	 * 分页
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<AppTaskChatVO>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<AppTaskChat> pages = appTaskChatService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, AppTaskChat.class));
		return R.data(AppTaskChatWrapper.build().pageVO(pages));
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入vo")
	public R save(@Valid @RequestBody AppTaskChatVO vo) {
		return R.status(appTaskChatService.saveAll(vo));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入entity")
	public R update(@Valid @RequestBody AppTaskChat entity) {
		return R.status(appTaskChatService.updateById(entity));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<AppTaskChat> detail(AppTaskChat entity) {
		AppTaskChat detail = appTaskChatService.getOne(Condition.getQueryWrapper(entity));
		return R.data(detail);
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(appTaskChatService.deleteLogic(Func.toLongList(ids)));
	}
}
