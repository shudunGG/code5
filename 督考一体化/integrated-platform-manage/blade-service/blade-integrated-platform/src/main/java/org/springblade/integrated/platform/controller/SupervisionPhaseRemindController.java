package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionPhaseRemind;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.ISupervisionPhaseRemindService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办阶段汇报提醒控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/SupervisionPhaseRemind")
@Api(value = "督察督办阶段汇报提", tags = "督察督办阶段汇报提")
public class SupervisionPhaseRemindController extends BladeController {

	private final ISupervisionPhaseRemindService supervisionPhaseRemindService;

	/**
	 * 分页查询
	 * @param start
	 * @param limit
	 * @return
	 */
	@GetMapping("/list/{start}/{limit}")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办阶段汇报提", notes = "")
	public R<PageInfo> list(@ApiIgnore @PathVariable Integer start, @PathVariable Integer limit, @RequestParam String columnCode ) {
		QueryWrapper<SupervisionPhaseRemind> ew =new QueryWrapper<>();
		PageHelper.startPage(start,limit).setOrderBy("update_time desc");
		Map<String,Object> param=new HashMap<>();
		param.put("columnCode",columnCode);
		List<SupervisionPhaseRemind> list = supervisionPhaseRemindService.list(ew);
		PageInfo pageInfo = new PageInfo(list);
		return R.data(pageInfo);
	}

	/**
	 * 新增
	 * @param SupervisionPhaseRemind
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionPhaseRemind SupervisionPhaseRemind) {
		return R.status(supervisionPhaseRemindService.save(SupervisionPhaseRemind));
	}

	/**
	 * 修改
	 * @param SupervisionPhaseRemind
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionPhaseRemind SupervisionPhaseRemind) {
		return R.status(supervisionPhaseRemindService.updateById(SupervisionPhaseRemind));
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
		return R.status(supervisionPhaseRemindService.removeById(id));
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
		return R.status(supervisionPhaseRemindService.removeByIds(Arrays.asList(id)));
	}

}
