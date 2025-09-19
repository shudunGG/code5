package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.entity.StructureMetadata;

import java.util.List;

/**
 * StructureMetadata的Dao接口
 *
 * @author
 */
public interface StructureMetadataMapper extends BaseMapper<StructureMetadata> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 根据主体主键获取元数据列表
	 * @Date 上午 11:16 2021/10/29 0029
	 * @Param [themeId]
	 **/
	@Select("select * from structure_metadata where theme_id = #{themeId}")
	List<StructureMetadata> getByThemeId(@Param("themeId") Long themeId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 根据主体主键删除元数据记录
	 * @Date 上午 11:16 2021/10/29 0029
	 * @Param [themeId]
	 **/
	@Delete("delete from structure_metadata where theme_id = #{themeId}")
	int deleteByThemeId(@Param("themeId") Long themeId);

	/**
	 * @return java.util.List<java.lang.String>
	 * @Author MaQY
	 * @Description 根据主体表ID获取当前主体表下所有字段名称
	 * @Date 下午 1:27 2021/11/4 0004
	 * @Param [themeId]
	 **/
	@Select("select * from structure_metadata where theme_id = #{themeId}")
	List<StructureMetadata> getCheckFieldsByThemeId(@Param("themeId") String themeId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 获取当前主体表下字段类型为数值型、字符型、日期型的字段名称
	 * @Date 下午 4:47 2021/11/4 0004
	 * @Param [themeId]
	 **/
	@Select("select name,type,data_size,digits from structure_metadata where theme_id = #{themeId} and `type` in ('CHAR','VARCHAR','BINARY','VARBINARY','BLOB','TEXT','ENUM','SET', 'VARCHAR2','NVARCHAR2','NCHAR','INTEGER','SMALLINT','DECIMAL','NUMERIC','FLOAT','REAL','DOUBLE','DEC','INT','TINYINT','MEDIUMINT','BIGINT','NUMBER','BINARY_FLOAT','BINARY_DOUBLE','DATETIME','DATE','TIMESTAMP','TIME','YEAR')")
	List<StructureMetadata> getSpecificCheckFieldsByThemeId(@Param("themeId") String themeId);

	/**
	 * 获取当前主体表下字段类型为字符型、日期型的字段名称
	 *
	 * @param themeId
	 * @return
	 */
	@Select("select name,type,data_size,digits from structure_metadata where theme_id = #{themeId} and `type` in ('CHAR','VARCHAR','VARCHAR2','NVARCHAR2','NCHAR','DATETIME','DATE','TIMESTAMP')")
	List<StructureMetadata> getDateTypeCheckFieldsByThemeId(@Param("themeId") String themeId);
}
