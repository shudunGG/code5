package org.springblade.integrated.platform.controller;

import com.vingsoft.entity.ProjectManagementReport;
import com.vingsoft.vo.ProjectManagementReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import javax.validation.Valid;

import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.integrated.platform.wrapper.ProjectManagementReportWrapper;
import org.springblade.integrated.platform.service.IProjectManagementReportService;
import org.springblade.core.boot.ctrl.BladeController;

/**
 * @ Date       ：Created in 2025年03月19日10时53分15秒
 * @ Description：项目管理系统汇报信息表路由控制器
 */
@RestController
@AllArgsConstructor
@RequestMapping("/projectManagementReport")
@Api(value = "项目管理系统汇报信息表", tags = "项目管理系统汇报信息表接口")
public class ProjectManagementReportController extends BladeController {

	private IProjectManagementReportService projectManagementReportService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入projectManagementReport")
	public R<ProjectManagementReportVO> detail(ProjectManagementReport projectManagementReport) {
		ProjectManagementReport detail = projectManagementReportService.getOne(Condition.getQueryWrapper(projectManagementReport));
		return R.data(ProjectManagementReportWrapper.build().entityVO(detail));
	}

	/**
	 * 分页 项目管理系统汇报信息表
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入projectManagementReport")
	public R<IPage<ProjectManagementReportVO>> list(ProjectManagementReport projectManagementReport, Query query) {
		IPage<ProjectManagementReport> pages = projectManagementReportService.page(Condition.getPage(query), Condition.getQueryWrapper(projectManagementReport));
		return R.data(ProjectManagementReportWrapper.build().pageVO(pages));
	}


	/**
	 * 新增 项目管理系统汇报信息表
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入projectManagementReport")
	public R save(@Valid @RequestBody ProjectManagementReport projectManagementReport) {
		return R.status(projectManagementReportService.save(projectManagementReport));
	}

	/**
	 * 修改 项目管理系统汇报信息表
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入projectManagementReport")
	public R update(@Valid @RequestBody ProjectManagementReport projectManagementReport) {
		return R.status(projectManagementReportService.updateById(projectManagementReport));
	}

	/**
	 * 新增或修改 项目管理系统汇报信息表
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入projectManagementReport")
	public R submit(@Valid @RequestBody ProjectManagementReport projectManagementReport) {
		return R.status(projectManagementReportService.saveOrUpdate(projectManagementReport));
	}


	/**
	 * 删除 项目管理系统汇报信息表
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(projectManagementReportService.deleteLogic(Func.toLongList(ids)));
	}


}
