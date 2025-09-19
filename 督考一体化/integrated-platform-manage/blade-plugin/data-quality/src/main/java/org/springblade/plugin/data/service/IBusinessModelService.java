package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.dto.BusinessModelDTO;
import org.springblade.plugin.data.dto.ThemeTableDTO;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.BusinessModel;
import org.springblade.plugin.data.vo.DataTreeNode;

import java.util.List;
import java.util.Map;

/**
 * BusinessModel的服务接口
 *
 * @author
 */
public interface IBusinessModelService extends BaseService<BusinessModel> {
	/**
	 * @return java.util.List<java.lang.String>
	 * @Author MaQY
	 * @Description 查询当前数据库的所有表名
	 * @Date 上午 11:48 2021/10/27 0027
	 * @Param [datasource]
	 **/
	List<Map<String, String>> getTableNames(String id);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 获取表结构
	 * @Date 下午 5:00 2021/10/27 0027
	 * @Param [datasourceId, tableName]
	 **/
	List<StructureMetadata> getTableStructure(String datasourceId, String tableName);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 保存业务模型的信息，包括所属的主题表和各主题表对应的元数据
	 * @Date 上午 10:28 2021/10/29 0029
	 * @Param [businessModel]
	 **/
	boolean saveBusinessModelInfo(BusinessModelDTO businessModel);

	/**
	 * @return org.springblade.plugin.data.dto.BusinessModelDTO
	 * @Author MaQY
	 * @Description 根据id获取业务模型信息，包括所属主题表和对应的元数据
	 * @Date 上午 11:09 2021/10/29 0029
	 * @Param [id]
	 **/
	BusinessModelDTO modelDetail(String id);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 修改业务模型信息、主题表信息及元数据信息
	 * @Date 上午 11:10 2021/10/29 0029
	 * @Param [businessModel]
	 **/
	boolean updateModelInfo(BusinessModelDTO businessModel);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 删除业务模型信息, 包括所包含的主题表及元数据记录
	 * @Date 下午 1:25 2021/10/29 0029
	 * @Param [ids]
	 **/
	boolean deleteModelInfo(String ids);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 删除一个业务模型信息, 包括所包含的主题表及元数据记录
	 * @Date 下午 1:25 2021/10/29 0029
	 * @Param [ids]
	 **/
	boolean deleteOneModelInfo(String id);

	/**
	 * @return java.util.List<org.springblade.plugin.data.dto.ThemeTableDTO>
	 * @Author MaQY
	 * @Description 根据模型ID和表名获取元数据（元数据表已经有的就去元数据表和主题表找，元数据表没有的，就连接数据源查询）
	 * @Date 下午 4:54 2021/10/29 0029
	 * @Param [id, tableNames]
	 **/
	List<ThemeTableDTO> getThemeInfosByTables(BusinessModel businessModel);

	/**
	 * @return java.util.List<org.springblade.plugin.data.vo.DataTreeNode>
	 * @Author MaQY
	 * @Description 模型和主题表树形结构
	 * @Date 下午 3:17 2021/11/3 0003
	 * @Param []
	 **/
	List<DataTreeNode> getModelThemeTree();

	/**
	 * @return java.lang.String
	 * @Author MaQY
	 * @Description 根据模型ID获取数据库类型
	 * @Date 下午 2:22 2021/11/8 0008
	 * @Param [modelId]
	 **/
	String getDataBaseType(String modelId);

	/**
	 * 根据模型Id获取树形结构（主题表和其下规则）
	 *
	 * @param modelId
	 * @return
	 */
	List<DataTreeNode> getThemeRuleTree(String modelId);

	/**
	 * 获取统计类型
	 *
	 * @param modelId
	 * @return
	 */
	String getStatisticalType(String modelId);
}
