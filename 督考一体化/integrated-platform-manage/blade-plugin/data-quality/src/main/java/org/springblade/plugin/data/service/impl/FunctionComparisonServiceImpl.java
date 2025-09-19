package org.springblade.plugin.data.service.impl;

import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.plugin.data.dto.FunctionComparisonDTO;
import org.springblade.plugin.data.service.IFunctionComparisonService;
import org.springblade.plugin.data.service.IFunctionParameterService;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.mapper.FunctionComparisonMapper;
import org.springblade.plugin.data.entity.FunctionComparison;

import java.util.List;

/**
 * FunctionComparison的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class FunctionComparisonServiceImpl extends BaseServiceImpl<FunctionComparisonMapper, FunctionComparison> implements IFunctionComparisonService {

	private IFunctionParameterService parameterService;

	@Override
	public List<FunctionComparisonDTO> getFunctionTree() {
		List<FunctionComparisonDTO> topParent = baseMapper.getTopParent();
		topParent.forEach(parent -> {
			List<FunctionComparisonDTO> childNode = baseMapper.getChildNode(parent.getInspectionName());
			childNode.forEach(child -> {
				child.setArgs(parameterService.getByFunctionName(child.getInspectionName()));
			});
			parent.setChildren(childNode);
		});
		return topParent;
	}

	@Override
	public FunctionComparison getByInspectionName(String inspectionName) {
		return baseMapper.selectByInspectionName(inspectionName);
	}
}
