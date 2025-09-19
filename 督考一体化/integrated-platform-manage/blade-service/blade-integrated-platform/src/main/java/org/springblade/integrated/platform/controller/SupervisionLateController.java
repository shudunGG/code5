package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionLate;
import com.vingsoft.vo.SupervisionLateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.service.ISupervisionLateService;
import org.springblade.integrated.platform.wrapper.SupervisionLateWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @Description:    事项申请延期
* @Author:         WangRJ
* @CreateDate:     2022/5/20 23:53
* @Version:        1.0
*/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionLate")
@Api(value = "交办信息", tags = "交办信息")
public class SupervisionLateController extends BladeController {

	private final ISupervisionLateService supervisionLateService;

	private final ISupervisionFilesService filesService;

	/**
	 * 分页
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<SupervisionLateVO>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<SupervisionLate> pages = supervisionLateService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SupervisionLate.class));
		return R.data(SupervisionLateWrapper.build().pageVO(pages));
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入vo")
	public R save(@Valid @RequestBody SupervisionLateVO vo) {
		return R.status(supervisionLateService.saveOrUpdate(vo));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入entity")
	public R update(@Valid @RequestBody SupervisionLate entity) {
		return R.status(supervisionLateService.updateById(entity));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<SupervisionLateVO> detail(SupervisionLate entity) {
		SupervisionLate detail = supervisionLateService.getOne(Condition.getQueryWrapper(entity));
		SupervisionLateVO vo = Objects.requireNonNull(BeanUtil.copy(detail, SupervisionLateVO.class));

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setPhaseId(entity.getId());
		fileDel.setServCode(detail.getServCode());
		fileDel.setFileFrom("延期");
		fileDel.setCreateDept(detail.getLateDeptId());
		List<SupervisionFiles> list = filesService.list(Condition.getQueryWrapper(fileDel));

		vo.setFilesList(list);
		return R.data(vo);
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(supervisionLateService.deleteLogic(Func.toLongList(ids)));
	}
}
