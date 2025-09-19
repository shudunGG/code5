package org.springblade.plugin.data.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.dto.BusinessModelDTO;
import org.springblade.plugin.data.dto.ThemeTableDTO;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.BusinessModel;
import org.springblade.plugin.data.vo.DataTreeNode;
import org.springframework.web.bind.annotation.*;

import org.springblade.plugin.data.service.IBusinessModelService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BusinessModel的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/businessModel")
@Api(value = "业务模型管理表", tags = "业务模型管理表接口")
public class BusinessModelController extends BladeController {

	/**
	 * BusinessModelService服务
	 */
	private IBusinessModelService businessModelService;

	@GetMapping("/pageList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "传入businessModel")
	public R<IPage<BusinessModel>> pageList(BusinessModel businessModel, Query query) {
		IPage<BusinessModel> page = businessModelService.page(Condition.getPage(query), Condition.getQueryWrapper(businessModel));
		return R.data(page);
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < java.util.Map < java.lang.String, java.lang.String>>>
	 * @Author MaQY
	 * @Description 根据数据源信息获取表名和注释
	 * @Date 上午 10:56 2021/10/29 0029
	 * @Param [id]
	 **/
	@GetMapping("/getTableNames")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "数据库表名列表", notes = "传入datasourceId")
	public R<List<Map<String, String>>> getTableNames(@RequestParam("id") String id) {
		return R.data(businessModelService.getTableNames(id));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.entity.StructureMetadata>>
	 * @Author MaQY
	 * @Description 根据数据源信息和表名获取表结构元数据（包括唯一索引和主键这种唯一的）
	 * @Date 上午 10:55 2021/10/29 0029
	 * @Param [id, tableName]
	 **/
	@GetMapping("/getTableStructure")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "获取表结构", notes = "传入datasourceId，tableName")
	public R<List<StructureMetadata>> getTableStructure(@RequestParam("id") String id, @RequestParam("tableName") String tableName) {
		return R.data(businessModelService.getTableStructure(id, tableName));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 保存业务模型的信息，包括所属的主题表和各主题表对应的元数据
	 * @Date 上午 10:25 2021/10/29 0029
	 * @Param [businessModel]
	 **/
	@PostMapping("/saveBusinessModelInfo")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "保存业务模型的信息", notes = "传入businessModel")
	public R saveBusinessModelInfo(@RequestBody BusinessModelDTO businessModel) {
		return R.status(businessModelService.saveBusinessModelInfo(businessModel));
	}

	/**
	 * @return org.springblade.core.tool.api.R<org.springblade.plugin.data.dto.BusinessModelDTO>
	 * @Author MaQY
	 * @Description 根据id获取业务模型信息，包括所属主题表和对应的元数据
	 * @Date 上午 10:59 2021/10/29 0029
	 * @Param [id]
	 **/
	@GetMapping("/modelDetail")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "根据id获取业务模型信息", notes = "传入id")
	public R<BusinessModelDTO> modelDetail(@RequestParam("id") String id) {
		return R.data(businessModelService.modelDetail(id));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 修改业务模型信息、主题表信息及元数据信息
	 * @Date 上午 11:04 2021/10/29 0029
	 * @Param [businessModel]
	 **/
	@PostMapping("/updateModelInfo")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "修改业务模型信息", notes = "传入businessModel")
	public R updateModelInfo(@RequestBody BusinessModelDTO businessModel) {
		return R.status(businessModelService.updateModelInfo(businessModel));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除业务模型信息, 包括所包含的主题表及元数据记录
	 * @Date 下午 1:21 2021/10/29 0029
	 * @Param [ids]
	 **/
	@PostMapping("/deleteModelInfos")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "删除业务模型信息", notes = "传入主键集合")
	public R deleteModelInfos(@RequestParam("ids") String ids) {
		return R.status(businessModelService.deleteModelInfo(ids));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除一个业务模型信息, 包括所包含的主题表及元数据记录
	 * @Date 下午 1:21 2021/10/29 0029
	 * @Param [ids]
	 **/
	@PostMapping("/deleteOneModelInfo")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "删除业务模型信息", notes = "传入主键")
	public R deleteOneModelInfo(@RequestParam("id") String id) {
		return R.status(businessModelService.deleteOneModelInfo(id));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.dto.ThemeTableDTO>>
	 * @Author MaQY
	 * @Description 根据模型ID和表名获取元数据（元数据表已经有的就去元数据表和主题表找，元数据表没有的，就连接数据源查询）
	 * @Date 下午 4:46 2021/10/29 0029
	 * @Param [id, tableName]
	 **/
	@GetMapping("/getThemeInfosByTables")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "根据模型ID和表名获取元数据", notes = "传入model_id,tableNames")
	public R<List<ThemeTableDTO>> getThemeInfosByTables(@RequestParam Map<String, Object> businessModel) {
		return R.data(businessModelService.getThemeInfosByTables(JSONObject.parseObject(JSONObject.toJSONString(businessModel), BusinessModel.class)));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.vo.DataTreeNode>>
	 * @Author MaQY
	 * @Description 模型和主题表树形结构
	 * @Date 下午 3:22 2021/11/3 0003
	 * @Param []
	 **/
	@GetMapping("/getModelThemeTree")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "模型和主题表树形结构", notes = "无参")
	public R<List<DataTreeNode>> getModelThemeTree() {
		return R.data(businessModelService.getModelThemeTree());
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.lang.String>
	 * @Author MaQY
	 * @Description 根据模型ID获取数据库类型
	 * @Date 下午 2:21 2021/11/8 0008
	 * @Param [modelId]
	 **/
	@GetMapping("/getDataBaseType")
	@ApiOperationSupport(order = 11)
	@ApiOperation(value = "根据模型ID获取数据库类型", notes = "传入modelId")
	public R<String> getDataBaseType(@RequestParam("modelId") String modelId) {
		return R.data(businessModelService.getDataBaseType(modelId));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.entity.BusinessModel>>
	 * @Author MaQY
	 * @Description 获取业务模型列表
	 * @Date 下午 5:19 2021/12/1 0001
	 * @Param []
	 **/
	@GetMapping("/getListInfo")
	@ApiOperationSupport(order = 12)
	@ApiOperation(value = "获取业务模型列表", notes = "无参")
	public R<List<BusinessModel>> getListInfo() {
		return R.data(businessModelService.list(Wrappers.<BusinessModel>query().lambda().eq(BaseEntity::getIsDeleted, 0)));
	}

	/**
	 * 根据模型ID获取当前模型下的主题表和规则的树形结构
	 * @param modelId
	 * @return
	 */
	@GetMapping("/getThemeRuleTree")
	@ApiOperationSupport(order = 13)
	@ApiOperation(value = "获取主题表和规则的树形结构", notes = "传入modelId")
	public R<List<DataTreeNode>> getThemeRuleTree(@RequestParam("modelId") String modelId){
		return R.data(businessModelService.getThemeRuleTree(modelId));
	}

	/**
	 * 获取统计类型
	 * @param modelId
	 * @return
	 */
	@GetMapping("/getStatisticalType")
	@ApiOperationSupport(order = 14)
	@ApiOperation(value = "获取统计类型", notes = "传入modelId")
	public R<String> getStatisticalType(@RequestParam("modelId") String modelId){
		return R.data(businessModelService.getStatisticalType(modelId));
	}
}
