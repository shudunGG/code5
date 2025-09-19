package org.springblade.plugin.data.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.plugin.data.entity.RelationRuleTemplate;
import org.springblade.plugin.data.mapper.RelationRuleTemplateMapper;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IRelationRuleTemplateService;

import java.util.List;

/**
 * RelationRuleTemplate的服务接口的实现类
 *
 * @author
 */
@Service
public class RelationRuleTemplateServiceImpl extends BaseServiceImpl<RelationRuleTemplateMapper, RelationRuleTemplate> implements IRelationRuleTemplateService {

	@Override
	public boolean removeByRuleId(String ruleId) {
		return baseMapper.deleteByRuleId(ruleId) > 0;
	}

	@Override
	public List<RelationRuleTemplate> selectByRuleId(String id) {
		return baseMapper.selectByRuleId(id);
	}
}
