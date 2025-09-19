package org.springblade.plugin.data.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.dto.QualityTestingProgrammeDTO;
import org.springblade.plugin.data.entity.JL;
import org.springblade.plugin.data.entity.LW;
import org.springblade.plugin.data.entity.QualityTestingProgramme;
import org.springblade.plugin.data.vo.DataTreeNode;
import org.springblade.plugin.data.vo.LWVO;
import org.springblade.plugin.data.vo.QualityTestingProgrammeVO;
import org.springblade.plugin.data.vo.TJVO;
import org.springblade.plugin.data.wrapper.QualityTestingProgrammeWrapper;
import org.springframework.web.bind.annotation.*;

import org.springblade.plugin.data.service.IQualityTestingProgrammeService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * QualityTestingProgramme的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/qualityTestingProgramme")
@Api(value = "QualityTestingProgramme的路由接口服务", tags = "质检方案表接口")
public class QualityTestingProgrammeController extends BladeController {

	/**
	 * QualityTestingProgrammeService服务
	 */
	private IQualityTestingProgrammeService qualityTestingProgrammeService;

	/**
	 * @return org.springblade.core.tool.api.R<com.baomidou.mybatisplus.core.metadata.IPage < org.springblade.plugin.data.entity.QualityTestingProgramme>>
	 * @Author MaQY
	 * @Description 分页查询
	 * @Date 下午 5:29 2021/12/1 0001
	 * @Param [programme, query]
	 **/
	@GetMapping("/selectPageList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页查询", notes = "传入programme")
	public R<IPage<QualityTestingProgrammeVO>> selectPageList(@RequestParam @ApiIgnore Map<String, Object> programme, Query query) {
		IPage<QualityTestingProgramme> page = qualityTestingProgrammeService.page(Condition.getPage(query), Condition.getQueryWrapper(programme, QualityTestingProgramme.class));
		return R.data(QualityTestingProgrammeWrapper.build().pageVO(page));
	}

	/**
	 * 保存质检方案
	 *
	 * @param qualityTestingProgrammeDTO
	 * @return
	 */
	@PostMapping("/saveQualityTestingProgramme")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "保存质检方案", notes = "传入qualityTestingProgrammeDTO")
	public R saveQualityTestingProgramme(@RequestBody QualityTestingProgrammeDTO qualityTestingProgrammeDTO) {
		boolean b = qualityTestingProgrammeService.saveQualityTestingProgramme(qualityTestingProgrammeDTO);
		if (!b) {
			return R.fail("保存失败！");
		}
		if (StringUtil.equals("0", qualityTestingProgrammeDTO.getPlanTask())) {
			//不需要启动定时任务则直接返回成功
			return R.success("保存成功！");
		}
		//需要启动定时任务
		boolean startJob = qualityTestingProgrammeService.startJob(BeanUtil.copyProperties(qualityTestingProgrammeDTO, QualityTestingProgramme.class));
		if (startJob) {
			return R.success("操作成功！");
		} else {
			return R.success("质检方案保存成功，计划任务启动失败，请稍后再次尝试启动！");
		}
	}

	/**
	 * 启动任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	@PostMapping("/startJob")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "启动定时任务", notes = "传入qualityTestingProgramme")
	public R startJob(@RequestBody QualityTestingProgramme qualityTestingProgramme) {
		return qualityTestingProgrammeService.startJob(qualityTestingProgramme) ? R.success("启动成功！") : R.fail("启动失败，请稍后重试！");
	}

	/**
	 * 停止定时任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	@PostMapping("/stopJob")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "停止定时任务", notes = "传入qualityTestingProgramme")
	public R stopJob(@RequestBody QualityTestingProgramme qualityTestingProgramme) {
		return qualityTestingProgrammeService.stopJob(qualityTestingProgramme) ? R.success("停止定时任务成功！") : R.fail("停止定时任务失败，请稍后充实！");
	}

	/**
	 * 触发定时任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	@PostMapping("/trigger")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "触发定时任务", notes = "传入qualityTestingProgramme")
	public R trigger(@RequestBody QualityTestingProgrammeDTO qualityTestingProgramme) {
		return qualityTestingProgrammeService.trigger(qualityTestingProgramme) ? R.success("执行成功，请稍后查看执行结果！") : R.fail("执行失败，请稍后重试！");
	}

	/**
	 * 根据ID获取详情
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getDetailById")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "根据主键获取详情", notes = "传入质检方案主键")
	public R<QualityTestingProgrammeVO> getDetailById(@RequestParam("id") String id) {
		return R.data(qualityTestingProgrammeService.getDetail(id));
	}

	/**
	 * 根据主键集合批量删除
	 *
	 * @param ids
	 * @return
	 */
	@PostMapping("/removeQualityTestingProgrammes")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "根据主键集合批量删除", notes = "传入主键集合")
	public R removeQualityTestingProgrammes(@RequestParam String ids) {
		return R.data(qualityTestingProgrammeService.removeQualityTestingProgrammes(ids));
	}

	/**
	 * 根据主键删除
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/removeQualityTestingProgramme")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "根据主键删除", notes = "传入主键")
	public R removeQualityTestingProgramme(@RequestParam String id) {
		return R.data(qualityTestingProgrammeService.removeQualityTestingProgramme(id));
	}

	/**
	 * 修改质检方案
	 *
	 * @param qualityTestingProgrammeDTO
	 * @return
	 */
	@PostMapping("/updateQualityTestingProgramme")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "修改质检方案", notes = "传入质检方案")
	public R updateQualityTestingProgramme(@RequestBody QualityTestingProgrammeDTO qualityTestingProgrammeDTO) {
		return R.status(qualityTestingProgrammeService.updateQualityTestingProgramme(qualityTestingProgrammeDTO));
	}

	/**
	 * 分页获取统计结果表的记录
	 *
	 * @param tjParam
	 * @param query
	 * @return
	 */
	@GetMapping("/getTJPageList")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "分页获取统计结果表的记录", notes = "传入tjParam")
	public R<IPage<TJVO>> getTJPageList(@RequestParam @ApiIgnore Map<String, String> tjParam, Query query) {
		return R.data(qualityTestingProgrammeService.getTJPageList(tjParam, query));
	}

	/**
	 * 分页获取统计结果记录表的记录
	 *
	 * @param jlParam
	 * @param query
	 * @return
	 */
	@GetMapping("/getJLPageList")
	@ApiOperationSupport(order = 11)
	@ApiOperation(value = "分页获取统计记录表的记录", notes = "传入jlParam")
	public R<IPage<JL>> getJLPageList(@RequestParam @ApiIgnore Map<String, String> jlParam, Query query) {
		return R.data(qualityTestingProgrammeService.getJLPageList(jlParam, query));
	}

	/**
	 * 设为例外
	 *
	 * @param jlStr
	 * @return
	 */
	@PostMapping("/setAsException")
	@ApiOperationSupport(order = 12)
	@ApiOperation(value = "设为例外", notes = "传入jl")
	public R setAsException(@RequestParam String jlStr) {
		return R.status(qualityTestingProgrammeService.setAsException(jlStr));
	}

	/**
	 * 分页查看例外表的数据
	 *
	 * @param lwParam
	 * @param query
	 * @return
	 */
	@GetMapping("/getLWPageList")
	@ApiOperationSupport(order = 13)
	@ApiOperation(value = "分页查看例外表的数据", notes = "传入lwParam")
	public R<IPage<LW>> getLWPageList(@RequestParam @ApiIgnore Map<String, String> lwParam, Query query) {
		return R.data(qualityTestingProgrammeService.getLWPageList(lwParam, query));
	}
	/**
	 * 分页获取统计例外表的记录
	 *
	 * @param lwParam
	 * @param query
	 * @return
	 */
	@GetMapping("/getLWStatisticPageList")
	@ApiOperationSupport(order = 14)
	@ApiOperation(value = "分页获取统计例外表的记录", notes = "传入lwParam")
	public R<IPage<LWVO>> getLWStatisticPageList(@RequestParam @ApiIgnore Map<String, String> lwParam, Query query){
		return R.data(qualityTestingProgrammeService.getLWStatisticPageList(lwParam, query));
	}

	/**
	 * 获取模型和方案树
	 * @return
	 */
	@GetMapping("/getModelProgrammeTree")
	@ApiOperationSupport(order = 15)
	@ApiOperation(value = "获取模型和方案树", notes = "无参")
	public R<List<DataTreeNode>> getModelProgrammeTree(){
		return R.data(qualityTestingProgrammeService.getModelProgrammeTree());
	}
}
