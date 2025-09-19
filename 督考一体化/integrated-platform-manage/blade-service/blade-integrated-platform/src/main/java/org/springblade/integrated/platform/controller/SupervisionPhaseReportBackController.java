package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionPhaseReportBack;
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
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.ISupervisionInfoService;
import org.springblade.integrated.platform.service.ISupervisionPhaseReportBackService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Description: SupervisionPhaseReportBackController
 * @Author: WangRJ
 * @CreateDate: 2022/7/27 15:45
 * @Version: 1.0
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionPhaseReportBack")
@Api(value = "交办信息", tags = "交办信息")
public class SupervisionPhaseReportBackController extends BladeController {

	private final ISupervisionPhaseReportBackService baseService;

	private final ISupervisionInfoService supervisionInfoService;

	/**
	 * 分页
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<SupervisionPhaseReportBack>> page(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<SupervisionPhaseReportBack> pages = baseService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SupervisionPhaseReportBack.class));
		pages.getRecords().forEach(back -> {
			back.setServName(supervisionInfoService.getOne(Wrappers.<SupervisionInfo>query().lambda().eq(SupervisionInfo::getServCode,back.getServCode())).getServName());
			//20240411131234处理脱敏字段开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(back.getLinkedName()) && sm4.checkDataIsEncrypt(back.getLinkedName())){
				back.setLinkedName(sm4.decrypt(back.getLinkedName()));
			}
			if(StringUtils.isNotEmpty(back.getLinkedPhone()) && sm4.checkDataIsEncrypt(back.getLinkedPhone())){
				back.setLinkedPhone(sm4.decrypt(back.getLinkedPhone()));
			}
			//20240411131234处理脱敏字段结束
		});
		return R.data(pages);
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入entity")
	public R save(@Valid @RequestBody SupervisionPhaseReportBack entity) {
		return R.status(baseService.save(entity));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入entity")
	public R update(@Valid @RequestBody SupervisionPhaseReportBack entity) {
		return R.status(baseService.updateById(entity));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<SupervisionPhaseReportBack> detail(SupervisionPhaseReportBack entity) {
		SupervisionPhaseReportBack detail = baseService.getOne(Condition.getQueryWrapper(entity));
		detail.setServName(supervisionInfoService.getOne(Wrappers.<SupervisionInfo>query().lambda().eq(SupervisionInfo::getServCode,detail.getServCode())).getServName());
		//20240411131234处理脱敏字段开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(detail.getLinkedName()) && sm4.checkDataIsEncrypt(detail.getLinkedName())){
			detail.setLinkedName(sm4.decrypt(detail.getLinkedName()));
		}
		if(StringUtils.isNotEmpty(detail.getLinkedPhone()) && sm4.checkDataIsEncrypt(detail.getLinkedPhone())){
			detail.setLinkedPhone(sm4.decrypt(detail.getLinkedPhone()));
		}
		//20240411131234处理脱敏字段结束
		return R.data(detail);
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(baseService.deleteLogic(Func.toLongList(ids)));
	}

	/**
	 * 获取集合
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "集合", notes = "传入entity")
	public R<List<SupervisionPhaseReportBack>> list(SupervisionPhaseReportBack entity) {
		List<SupervisionPhaseReportBack> list = baseService.list(Condition.getQueryWrapper(entity));
		return R.data(list);
	}
}
