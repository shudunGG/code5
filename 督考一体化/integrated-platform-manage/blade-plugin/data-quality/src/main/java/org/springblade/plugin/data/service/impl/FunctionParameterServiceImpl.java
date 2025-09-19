package org.springblade.plugin.data.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IFunctionParameterService;
import org.springblade.plugin.data.mapper.FunctionParameterMapper;
import org.springblade.plugin.data.entity.FunctionParameter;

import java.util.List;

/**
 * FunctionParameter的服务接口的实现类
 *
 * @author
 */
@Service
public class FunctionParameterServiceImpl extends BaseServiceImpl<FunctionParameterMapper, FunctionParameter> implements IFunctionParameterService {

	@Override
	public List<FunctionParameter> getByFunctionName(String functionName) {
		return baseMapper.selectByFunctionName(functionName);
	}
}
