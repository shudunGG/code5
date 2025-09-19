package org.springblade.plugin.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springblade.plugin.data.service.IStructureMetadataService;

import java.util.List;

/**
 * StructureMetadata的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/structureMetadata")
@Api(value = "主题信息表表结构元数据", tags = "主题信息表表结构元数据表接口")
public class StructureMetadataController extends BladeController {

	/**
	 * StructureMetadataService服务
	 */
	private IStructureMetadataService structureMetadataService;

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < java.lang.String>>
	 * @Author MaQY
	 * @Description 根据主体表ID获取当前主体表下所有字段名称
	 * @Date 下午 1:37 2021/11/4 0004
	 * @Param [themeId]
	 **/
	@GetMapping("/getCheckFieldsByThemeId")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取当前主体表下所有字段名称", notes = "传入themeId")
	public R<List<StructureMetadata>> getCheckFieldsByThemeId(@RequestParam("themeId") String themeId) {
		return R.data(structureMetadataService.getCheckFieldsByThemeId(themeId));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.entity.StructureMetadata>>
	 * @Author MaQY
	 * @Description 获取当前主体表下字段类型为数值型、字符型、日期型的字段名称
	 * @Date 下午 3:53 2021/11/4 0004
	 * @Param [themeId]
	 **/
	@GetMapping("/getSpecificCheckFieldsByThemeId")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "获取当前主体表下字段类型为数值型、字符型、日期型的字段名称", notes = "传入themeId")
	public R<List<StructureMetadata>> getSpecificCheckFieldsByThemeId(@RequestParam("themeId") String themeId) {
		return R.data(structureMetadataService.getSpecificCheckFieldsByThemeId(themeId));
	}

	/**
	 * 获取当前主体表下字段类型为字符型、日期型的字段名称
	 *
	 * @param themeId
	 * @return
	 */
	@GetMapping("/getDateTypeCheckFieldsByThemeId")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "获取当前主体表下字段类型为字符型、日期型的字段名称", notes = "传入themeId")
	public R<List<StructureMetadata>> getDateTypeCheckFieldsByThemeId(@RequestParam("themeId") String themeId) {
		return R.data(structureMetadataService.getDateTypeCheckFieldsByThemeId(themeId));
	}
}
