package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.RuleTemplate;
import org.springblade.plugin.data.vo.RuleTemplateVO;

/**
 * RuleTemplate的服务接口
 *
 * @author
 */
public interface IRuleTemplateService extends BaseService<RuleTemplate> {
	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 批量删除
	 * @Date 上午 11:27 2021/11/22 0022
	 * @Param [ids]
	 **/
	boolean removeRuleTemplates(String ids);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 删除
	 * @Date 上午 11:27 2021/11/22 0022
	 * @Param [id]
	 **/
	boolean removeRuleTemplate(String id);

	/**
	 * @return org.springblade.plugin.data.vo.RuleTemplateVO
	 * @Author MaQY
	 * @Description 根据规则模板ID获取详情
	 * @Date 下午 2:03 2021/11/22 0022
	 * @Param [id]
	 **/
	RuleTemplateVO getRuleTemplate(String id);
}
