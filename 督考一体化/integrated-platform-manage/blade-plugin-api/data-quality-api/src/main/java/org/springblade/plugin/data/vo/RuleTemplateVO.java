package org.springblade.plugin.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.RelationRuleTemplate;
import org.springblade.plugin.data.entity.RuleTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 2:00 2021/11/22 0022
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleTemplateVO extends RuleTemplate {
	private static final long serialVersionUID = -8890115170704987745L;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 逻辑检查模板携带的关联关系
	 * @Date 下午 2:01 2021/11/22 0022
	 * @Param
	 **/
	private List<RelationRuleTemplate> relations = new ArrayList<>();
}
