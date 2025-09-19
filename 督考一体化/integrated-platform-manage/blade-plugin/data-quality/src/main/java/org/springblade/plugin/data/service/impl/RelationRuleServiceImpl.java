package org.springblade.plugin.data.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IRelationRuleService;
import org.springblade.plugin.data.mapper.RelationRuleMapper;
import org.springblade.plugin.data.entity.RelationRule;

/**
 * RelationRule的服务接口的实现类
 *
 * @author
 */
@Service
public class RelationRuleServiceImpl extends BaseServiceImpl<RelationRuleMapper, RelationRule> implements IRelationRuleService {


	@Override
	public boolean deleteByRuleId(String ruleId) {
		return baseMapper.deleteByRuleId(ruleId) > 0;
	}
}
