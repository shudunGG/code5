package org.springblade.plugin.data.service;

import java.util.List;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.FunctionParameter;

/**
 * FunctionParameter的服务接口
 *
 * @author
 */
public interface IFunctionParameterService extends BaseService<FunctionParameter> {

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.FunctionParameter>
	 * @Author MaQY
	 * @Description 根据函数名称获取参数信息
	 * @Date 下午 4:23 2021/11/11 0011
	 * @Param [functionName]
	 **/
	List<FunctionParameter> getByFunctionName(String functionName);

}
