package org.springblade.plugin.data.service;


import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.ThemeTable;
import org.springblade.plugin.data.vo.ThemeTableTree;

import java.util.List;
import java.util.Map;

/**
 * ThemeTable的服务接口
 *
 * @author
 */
public interface IThemeTableService extends BaseService<ThemeTable> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ThemeTable>
	 * @Author MaQY
	 * @Description 根据模型ID查询主题列表
	 * @Date 上午 11:18 2021/10/29 0029
	 * @Param [modelId]
	 **/
	List<ThemeTable> getByModelId(Long modelId);

	/**
	 * 根据主题表物理删除
	 *
	 * @param id
	 * @return
	 */
	boolean deleteByThemeId(Long id);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 根据模型ID删除主题记录
	 * @Date 下午 1:17 2021/10/29 0029
	 * @Param [modelId]
	 **/
	boolean deleteByModelId(Long modelId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ThemeTable>
	 * @Author MaQY
	 * @Description 根据当前主题表ID获取其所在模型下的所有其他未删除的主题表信息
	 * @Date 下午 3:21 2021/11/5 0005
	 * @Param [themeId]
	 **/
	List<ThemeTable> getOtherThemeTablesByThemeId(String themeId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.vo.ThemeTableTree>
	 * @Author MaQY
	 * @Description 根据主题表ID获取表数据和其所有元数据信息，并返回树形结构
	 * @Date 上午 11:07 2021/11/8 0008
	 * @Param [themeIds]
	 **/
	List<ThemeTableTree> getThemeTableTree(List<String> themeIds);

	/**
	 * @return java.util.List<java.util.Map < java.lang.String, java.lang.String>>
	 * @Author MaQY
	 * @Description 主题表名称和ID清单
	 * @Date 上午 9:27 2021/11/24 0024
	 * @Param []
	 **/
	List<Map<String, String>> getTitleIdList();
}
