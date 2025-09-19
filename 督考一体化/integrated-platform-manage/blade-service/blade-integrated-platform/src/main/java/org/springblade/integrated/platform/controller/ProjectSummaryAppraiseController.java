package org.springblade.integrated.platform.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.ProjectSummary;
import com.vingsoft.entity.ProjectSummaryAppraise;
import com.vingsoft.vo.ProjectSummaryAppraiseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.IProjectSummaryAppraiseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @className: ProjectSummaryAppraiseController
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/3/16 15:49 星期四
 * @Version 1.0
 **/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectSummaryAppraise")
@Api(value = "项目管理评价信息", tags = "项目管理评价")
public class ProjectSummaryAppraiseController {

	@Resource
	IProjectSummaryAppraiseService projectSummaryAppraiseService;

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "新增", notes = "传入vo")
	public R save(@Valid @RequestBody ProjectSummaryAppraiseVO projectSummaryAppraiseVO) {
		return R.status(projectSummaryAppraiseService.save(projectSummaryAppraiseVO));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectSummaryAppraise projectSummaryAppraise) {
		return R.status(projectSummaryAppraiseService.updateById(projectSummaryAppraise));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<ProjectSummaryAppraise> detail(ProjectSummaryAppraise entity) {
		ProjectSummaryAppraise detail = projectSummaryAppraiseService.getOne(Condition.getQueryWrapper(entity));
		return R.data(detail);
	}
}
