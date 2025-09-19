package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.entity.ThemeTable;

import java.util.List;
import java.util.Map;

/**
 * ThemeTable的Dao接口
 *
 * @author
 */
public interface ThemeTableMapper extends BaseMapper<ThemeTable> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ThemeTable>
	 * @Author MaQY
	 * @Description 根据模型ID查询主题列表
	 * @Date 上午 11:14 2021/10/29 0029
	 * @Param [modelId]
	 **/
	@Select("select * from theme_table where model_id = #{modelId} and is_deleted=0")
	List<ThemeTable> getByModelId(@Param("modelId") Long modelId);

	/**
	 * 物理删除
	 *
	 * @param id
	 * @return
	 */
	@Delete("delete from theme_table where id = #{id}")
	int deleteByThemeId(@Param("id") Long id);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ThemeTable>
	 * @Author MaQY
	 * @Description 根据模型ID删除主题记录
	 * @Date 上午 11:14 2021/10/29 0029
	 * @Param [modelId]
	 **/
	@Delete("delete from theme_table where model_id = #{modelId}")
	int deleteByModelId(@Param("modelId") Long modelId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ThemeTable>
	 * @Author MaQY
	 * @Description 根据当前主题表ID获取其所在模型下的所有其他未删除的主题表信息
	 * @Date 下午 3:22 2021/11/5 0005
	 * @Param [themeId]
	 **/
	@Select("select * from theme_table where model_id in (select model_id from theme_table where id= #{themeId} and is_deleted=0) and id != #{themeId}")
	List<ThemeTable> selectOtherThemeTablesByThemeId(@Param("themeId") String themeId);

	/**
	 * @return java.util.List<java.util.Map < java.lang.String, java.lang.String>>
	 * @Author MaQY
	 * @Description 获取主题表名称、主键字段
	 * @Date 上午 9:28 2021/11/24 0024
	 * @Param []
	 **/
	@Select("select title,id from theme_table where is_deleted=0")
	List<Map<String, String>> getTitleIdList();
}
