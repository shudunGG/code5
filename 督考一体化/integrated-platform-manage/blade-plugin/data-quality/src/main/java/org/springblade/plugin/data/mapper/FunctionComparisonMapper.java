package org.springblade.plugin.data.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.dto.FunctionComparisonDTO;
import org.springblade.plugin.data.entity.FunctionComparison;

import org.apache.ibatis.annotations.Param;

/**
 * FunctionComparison的Dao接口
 *
 * @author
 */
public interface FunctionComparisonMapper extends BaseMapper<FunctionComparison> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.dto.FunctionComparisonDTO>
	 * @Author MaQY
	 * @Description 获取最高级别的父节点
	 * @Date 下午 4:14 2021/11/11 0011
	 * @Param []
	 **/
	@Select("select function_category `expression`,function_category `inspection_name`,COUNT(1)>0 `hasChildren` from function_comparison GROUP BY function_category")
	List<FunctionComparisonDTO> getTopParent();

	/**
	 * @return java.util.List<org.springblade.plugin.data.dto.FunctionComparisonDTO>
	 * @Author MaQY
	 * @Description 获取子节点
	 * @Date 下午 4:19 2021/11/11 0011
	 * @Param [functionCategory]
	 **/
	@Select("select *,0 `hasChildren` from function_comparison where function_category = #{functionCategory}")
	List<FunctionComparisonDTO> getChildNode(@Param("functionCategory") String functionCategory);

	/**
	 * @return org.springblade.plugin.data.entity.FunctionComparison
	 * @Author MaQY
	 * @Description 根据函数校验名称获取函数信息
	 * @Date 上午 10:35 2021/11/12 0012
	 * @Param [inspectionName]
	 **/
	@Select("select * from function_comparison where inspection_name = #{inspectionName}")
	FunctionComparison selectByInspectionName(@Param("inspectionName") String inspectionName);
}
