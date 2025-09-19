package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.MessageInformation;
import com.vingsoft.entity.ProjectLog;
import com.vingsoft.entity.ProjectPhasePlan;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.IProjectPhasePlanService;
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
 *  @Description: 项目管理计划阶段
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectPhasePlan")
@Api(value = "项目管理计划阶段", tags = "项目管理计划阶段")
public class ProjectPhasePlanController extends BladeController {

	private final IProjectPhasePlanService projectPhasePlanService;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目计划阶段", notes = "传入projectPhasePlan")
	public R<List<ProjectPhasePlan>> detail(ProjectPhasePlan projectPhasePlan) {
		//sql查询条件
		QueryWrapper<ProjectPhasePlan> queryWrapper = new QueryWrapper<ProjectPhasePlan>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhasePlan.getId()!=null,"id",projectPhasePlan.getId());
		List<ProjectPhasePlan> detail = projectPhasePlanService.list(queryWrapper);
		return R.data(detail);
	}



	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理计划阶段", notes = "")
	public R<IPage<ProjectPhasePlan>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<ProjectPhasePlan> pages = projectPhasePlanService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectPhasePlan.class));
		return R.data(pages);
	}


	/**
	 * 新增
	 * @param projectPhasePlan
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectPhasePlan projectPhasePlan) {
		return R.status(projectPhasePlanService.save(projectPhasePlan));
	}

	/**
	 * 修改
	 * @param projectPhasePlan
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectPhasePlan projectPhasePlan) {
		return R.status(projectPhasePlanService.updateById(projectPhasePlan));
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
		return R.status(projectPhasePlanService.removeById(id));
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
		return R.status(projectPhasePlanService.removeByIds(Arrays.asList(id)));
	}

	@GetMapping("/reportMessage")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "阶段信息", notes = "")
	public R reportMessage(@ApiIgnore @RequestParam String id){
		return  R.data(projectPhasePlanService.getProjectPhasePlanListByProjId(id));
	}
}
