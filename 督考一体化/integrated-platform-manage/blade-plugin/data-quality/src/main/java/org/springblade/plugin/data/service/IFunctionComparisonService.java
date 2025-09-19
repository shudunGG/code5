package org.springblade.plugin.data.service;

import java.util.List;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.dto.FunctionComparisonDTO;
import org.springblade.plugin.data.entity.FunctionComparison;

/**
 * FunctionComparison的服务接口
 *
 * @author
 */
public interface IFunctionComparisonService extends BaseService<FunctionComparison> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.dto.FunctionComparisonDTO>
	 * @Author MaQY
	 * @Description 获取函数树
	 * @Date 下午 4:04 2021/11/11 0011
	 * @Param []
	 **/
	List<FunctionComparisonDTO> getFunctionTree();

	/**
	 * @return org.springblade.plugin.data.entity.FunctionComparison
	 * @Author MaQY
	 * @Description 根据校验名称获取公式
	 * @Date 上午 10:32 2021/11/12 0012
	 * @Param [inspectionName]
	 **/
	FunctionComparison getByInspectionName(String inspectionName);
}
