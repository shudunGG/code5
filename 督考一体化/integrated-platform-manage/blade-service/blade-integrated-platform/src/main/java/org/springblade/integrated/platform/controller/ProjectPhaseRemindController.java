package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.ProjectPhasePlan;
import com.vingsoft.entity.ProjectPhaseRemind;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.IProjectPhaseRemindService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 项目管理汇报提醒
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectPhaseRemind")
@Api(value = "项目管理汇报提醒", tags = "项目管理汇报提醒")
public class ProjectPhaseRemindController extends BladeController {

	private final IProjectPhaseRemindService projectPhaseRemindService;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目汇报提醒", notes = "传入projectPhaseRemind")
	public R<List<ProjectPhaseRemind>> detail(ProjectPhaseRemind projectPhaseRemind) {
		//sql查询条件
		QueryWrapper<ProjectPhaseRemind> queryWrapper = new QueryWrapper<ProjectPhaseRemind>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhaseRemind.getId()!=null,"id",projectPhaseRemind.getId());
		List<ProjectPhaseRemind> detail = projectPhaseRemindService.list(queryWrapper);
		return R.data(detail);
	}


	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理汇报提醒", notes = "")
	public R<IPage<ProjectPhaseRemind>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<ProjectPhaseRemind> pages = projectPhaseRemindService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectPhaseRemind.class));
		return R.data(pages);
	}


	/**
	 * 新增
	 * @param projectPhaseRemind
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectPhaseRemind projectPhaseRemind) {
		return R.status(projectPhaseRemindService.save(projectPhaseRemind));
	}

	/**
	 * 修改
	 * @param projectPhaseRemind
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectPhaseRemind projectPhaseRemind) {
		return R.status(projectPhaseRemindService.updateById(projectPhaseRemind));
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
		return R.status(projectPhaseRemindService.removeById(id));
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
		return R.status(projectPhaseRemindService.removeByIds(Arrays.asList(id)));
	}

}
