package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.RelationRuleTemplate;

import java.util.List;

/**
 * RelationRuleTemplate的服务接口
 *
 * @author
 */
public interface IRelationRuleTemplateService extends BaseService<RelationRuleTemplate> {
	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 根据规则ID删除关联关系
	 * @Date 上午 11:36 2021/11/22 0022
	 * @Param [ruleId]
	 **/
	boolean removeByRuleId(String ruleId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.RelationRuleTemplate>
	 * @Author MaQY
	 * @Description 根据规则模板ID查询关联关系
	 * @Date 下午 3:47 2021/11/22 0022
	 * @Param [id]
	 **/
	List<RelationRuleTemplate> selectByRuleId(String id);
}
