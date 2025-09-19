package org.springblade.plugin.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.plugin.data.entity.ThemeTable;
import org.springblade.plugin.data.vo.ThemeTableTree;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springblade.plugin.data.service.IThemeTableService;

import java.util.List;
import java.util.Map;

/**
 * ThemeTable的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/themeTable")
@Api(value = "主题信息表数据", tags = "主题信息表数据表接口")
public class ThemeTableController extends BladeController {

	/**
	 * ThemeTableService服务
	 */
	private IThemeTableService themeTableService;

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.entity.ThemeTable>>
	 * @Author MaQY
	 * @Description 根据当前主题表ID获取其所在模型下的所有其他未删除的主题表信息
	 * @Date 下午 3:19 2021/11/5 0005
	 * @Param [themeId]
	 **/
	@GetMapping("/getOtherThemeTablesByThemeId")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "根据当前主题表ID获取其所在模型下的所有其他未删除的主题表信息", notes = "传入themeId")
	public R<List<ThemeTable>> getOtherThemeTablesByThemeId(@ApiParam("themeId") String themeId) {
		return R.data(themeTableService.getOtherThemeTablesByThemeId(themeId));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.vo.ThemeTableTree>>
	 * @Author MaQY
	 * @Description 根据主题表ID获取表数据和其所有元数据信息，并返回树形结构
	 * @Date 上午 11:04 2021/11/8 0008
	 * @Param [themeIds]
	 **/
	@GetMapping("/getThemeTableTree")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "根据主题表ID获取其所有元数据信息，并返回树形结构", notes = "传入themeIds")
	public R<List<ThemeTableTree>> getThemeTableTree(@RequestParam("themeIds") String themeIds) {
		return R.data(themeTableService.getThemeTableTree(Func.toStrList(themeIds)));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < java.util.Map < java.lang.String, java.lang.String>>>
	 * @Author MaQY
	 * @Description 主题表名称和ID清单
	 * @Date 上午 9:24 2021/11/24 0024
	 * @Param []
	 **/
	@GetMapping("/getTitleIdList")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "主题表名称和ID清单构", notes = "无参")
	public R<List<Map<String, String>>> getTitleIdList() {
		return R.data(themeTableService.getTitleIdList());
	}
}
