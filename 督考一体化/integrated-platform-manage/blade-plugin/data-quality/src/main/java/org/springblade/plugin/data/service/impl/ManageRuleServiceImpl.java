package org.springblade.plugin.data.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.dto.ManageRuleDTO;
import org.springblade.plugin.data.entity.*;
import org.springblade.plugin.data.service.*;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.mapper.ManageRuleMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ManageRule的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class ManageRuleServiceImpl extends BaseServiceImpl<ManageRuleMapper, ManageRule> implements IManageRuleService {

	private IRelationRuleService relationRuleService;
	private IRuleTemplateService ruleTemplateService;
	private IRelationRuleTemplateService relationRuleTemplateService;
	private IThemeTableService themeTableService;
	private final static String LOGIC = "LOGIC";
	private final static String TIMELY = "TIMELY";

	@Override
	public IPage<ManageRule> selectPageList(IPage page, ManageRuleDTO rule) {
		return page.setRecords(baseMapper.selectPageList(page, rule));
	}

	@Override
	public String generateCode(String type, String themeId) {
		//根据themeId获取model下所有的themeId
		Integer integer = baseMapper.selectTypeCountInOneModel(type, themeId);
		return type + integer;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveRule(ManageRuleDTO rule) {
		if (StringUtil.equals(LOGIC, rule.getType()) || StringUtil.equals(TIMELY, rule.getType())) {
			rule.setCheckFormula(HtmlUtils.htmlUnescape(rule.getCheckFormula()));
		}
		ManageRule manageRule = BeanUtil.copy(rule, ManageRule.class);
		boolean save = save(manageRule);
		if (StringUtil.equals(LOGIC, rule.getType())) {
			//逻辑类型，要保存关联关系
			List<RelationRule> relations = rule.getRelations();
			relations.forEach(relationRule -> {
				relationRule.setRuleId(manageRule.getId());
				relationRule.setId(null);
				relationRuleService.save(relationRule);
			});
		}
		return save;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteRules(String ids) {
		Func.toStrList(ids).forEach(id -> {
			this.deleteRule(id);
		});
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteRule(String id) {
		ManageRule manageRule = getById(id);
		boolean delete = true;
		if (StringUtil.equals(LOGIC, manageRule.getType())) {
			//删除关联关系
			delete = relationRuleService.deleteByRuleId(id);
		}
		return delete && removeById(id);
	}

	@Override
	public boolean updateRule(ManageRuleDTO rule) {
		if (StringUtil.equals(LOGIC, rule.getType())) {
			//根据ruleID删除后，再新增
			relationRuleService.deleteByRuleId(rule.getId().toString());
			rule.getRelations().forEach(relationRule -> {
				relationRule.setRuleId(rule.getId());
				relationRule.setId(null);
				relationRuleService.save(relationRule);
			});
		}
		if (StringUtil.equals(LOGIC, rule.getType()) || StringUtil.equals(TIMELY, rule.getType())) {
			rule.setCheckFormula(HtmlUtils.htmlUnescape(rule.getCheckFormula()));
		}
		return updateById(rule);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveRuleAndTemplate(ManageRuleDTO rule) {
		boolean s = this.saveRule(rule);
		//模板和模板的关联关系
		RuleTemplate template = BeanUtil.copy(rule, RuleTemplate.class);
		boolean b = ruleTemplateService.save(template);
		if (StringUtil.equals(LOGIC, rule.getType())) {
			rule.getRelations().forEach(relationRule -> {
				RelationRuleTemplate ruleTemplate = BeanUtil.copy(relationRule, RelationRuleTemplate.class);
				ruleTemplate.setRuleId(template.getId());
				ruleTemplate.setId(null);
				relationRuleTemplateService.save(ruleTemplate);
			});
		}
		return s & b;
	}

	@Override
	public List<ManageRule> getTestingDataQualityRules(String programmeId) {
		return baseMapper.getTestingDataQualityRules(programmeId);
	}

	@Override
	public ManageRuleDTO getRuleDetail(String id) {
		ManageRule manageRule = getById(id);
		ManageRuleDTO manageRuleDTO = BeanUtil.copyProperties(manageRule, ManageRuleDTO.class);
		if (StringUtil.equals(LOGIC, manageRuleDTO.getType())) {
			//逻辑检查 查询关联关系
			List<RelationRule> relationRuleList = relationRuleService.list(Wrappers.<RelationRule>query().lambda().eq(RelationRule::getRuleId, id));
			manageRuleDTO.setRelations(relationRuleList);
		}
		ThemeTable themeTable = themeTableService.getById(manageRule.getThemeId());
		if (Func.isNotEmpty(themeTable)) {
			manageRuleDTO.setThemeTableName(themeTable.getName());
		}
		return manageRuleDTO;
	}
}
