package org.springblade.plugin.data.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.entity.RelationRuleTemplate;
import org.springblade.plugin.data.entity.RuleTemplate;
import org.springblade.plugin.data.mapper.RuleTemplateMapper;
import org.springblade.plugin.data.service.IRelationRuleTemplateService;
import org.springblade.plugin.data.vo.RuleTemplateVO;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IRuleTemplateService;
import org.springframework.transaction.annotation.Transactional;

/**
 * RuleTemplate的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class RuleTemplateServiceImpl extends BaseServiceImpl<RuleTemplateMapper, RuleTemplate> implements IRuleTemplateService {

	private IRelationRuleTemplateService relationRuleTemplateService;

	private final static String LOGIC = "LOGIC";

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeRuleTemplates(String ids) {
		Func.toStrList(ids).forEach(id -> {
			this.removeRuleTemplate(id);
		});
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeRuleTemplate(String id) {
		RuleTemplate template = getById(id);
		if (StringUtil.equals(LOGIC, template.getType())) {
			return relationRuleTemplateService.removeByRuleId(id) && removeById(id);
		}
		return removeById(id);
	}

	@Override
	public RuleTemplateVO getRuleTemplate(String id) {
		RuleTemplate template = getById(id);
		RuleTemplateVO templateVO = BeanUtil.copy(template, RuleTemplateVO.class);
		if(StringUtil.equals(LOGIC,template.getType())){
			templateVO.setRelations(relationRuleTemplateService.selectByRuleId(id));
		}
		return templateVO;
	}
}
