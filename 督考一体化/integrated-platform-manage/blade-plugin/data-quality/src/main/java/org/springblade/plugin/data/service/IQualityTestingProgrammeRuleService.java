package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.QualityTestingProgrammeRule;

import java.util.List;

/**
 * @author MaQiuyun
 * @date 2021/12/16 14:56
 * @description:
 */
public interface IQualityTestingProgrammeRuleService extends BaseService<QualityTestingProgrammeRule> {
	/**
	 * 根据方案主键获取规则主键
	 *
	 * @param programmeId
	 * @return
	 */
	List<String> getRuleIdByProgrammeId(String programmeId);

	/**
	 * 根据质检方案主键删除记录
	 *
	 * @param programmeId
	 * @return
	 */
	boolean removeByProgrammeId(String programmeId);

	/**
	 * 根据质检方案主键和规则主键删除
	 * @param ruleId
	 * @return
	 */
	boolean removeByIds(String programmeId,String ruleId);
}
