package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.StructureMetadata;

import java.util.List;

/**
 * StructureMetadata的服务接口
 *
 * @author
 */
public interface IStructureMetadataService extends BaseService<StructureMetadata> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 根据主体主键获取元数据列表
	 * @Date 上午 11:17 2021/10/29 0029
	 * @Param [themeId]
	 **/
	List<StructureMetadata> getByThemeId(Long themeId);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 根据主体主键删除元数据记录
	 * @Date 下午 1:18 2021/10/29 0029
	 * @Param [themeId]
	 **/
	boolean deleteByThemeId(Long themeId);

	/**
	 * @return java.util.List<java.lang.String>
	 * @Author MaQY
	 * @Description 根据主体表ID获取当前主体表下所有字段名称
	 * @Date 下午 1:28 2021/11/4 0004
	 * @Param [themeId]
	 **/
	List<StructureMetadata> getCheckFieldsByThemeId(String themeId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 获取当前主体表下字段类型为数值型、字符型、日期型的字段名称
	 * @Date 下午 3:55 2021/11/4 0004
	 * @Param [themeId]
	 **/
	List<StructureMetadata> getSpecificCheckFieldsByThemeId(String themeId);

	/**
	 * 获取当前主体表下字段类型为字符型、日期型的字段名称
	 * @param themeId
	 * @return
	 */
	List<StructureMetadata> getDateTypeCheckFieldsByThemeId(String themeId);
}
