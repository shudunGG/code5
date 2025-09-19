package org.springblade.plugin.data.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.entity.FunctionParameter;

import org.apache.ibatis.annotations.Param;

/**
 * FunctionParameter的Dao接口
 *
 * @author
 */
public interface FunctionParameterMapper extends BaseMapper<FunctionParameter> {

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.FunctionParameter>
	 * @Author MaQY
	 * @Description 获取参数信息
	 * @Date 下午 4:22 2021/11/11 0011
	 * @Param [functionName]
	 **/
	@Select("select * from function_parameter where function_name =#{functionName} order by parameter_sort")
	List<FunctionParameter> selectByFunctionName(@Param("functionName") String functionName);
}
