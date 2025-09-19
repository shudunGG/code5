package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.RelationRule;

/**
 * RelationRule的服务接口
 *
 * @author
 */
public interface IRelationRuleService extends BaseService<RelationRule> {
	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 根据规则ID删除关联关系
	 * @Date 上午 11:55 2021/11/18 0018
	 * @Param [ruleId]
	 **/
	boolean deleteByRuleId(String ruleId);
}
