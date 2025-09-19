package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.entity.RelationRuleTemplate;

import java.util.List;

/**
 * RelationRuleTemplate的Dao接口
 *
 * @author
 */
public interface RelationRuleTemplateMapper extends BaseMapper<RelationRuleTemplate> {

	/**
	 * @return int
	 * @Author MaQY
	 * @Description 根据规则模板表主键删除关联关系
	 * @Date 上午 11:34 2021/11/22 0022
	 * @Param [ruleId]
	 **/
	@Delete("delete from relation_rule_template where rule_id = #{ruleId}")
	int deleteByRuleId(@Param("ruleId") String ruleId);

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.RelationRuleTemplate>
	 * @Author MaQY
	 * @Description 根据规则模板ID查询关联关系
	 * @Date 下午 3:49 2021/11/22 0022
	 * @Param [ruleId]
	 **/
	@Select("select * from relation_rule_template where rule_id = #{ruleId}")
	List<RelationRuleTemplate> selectByRuleId(@Param("ruleId") String ruleId);
}
