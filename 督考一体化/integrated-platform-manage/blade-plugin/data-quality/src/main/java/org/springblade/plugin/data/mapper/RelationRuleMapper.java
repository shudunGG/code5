package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springblade.plugin.data.entity.RelationRule;

/**
 * RelationRule的Dao接口
 *
 * @author
 */
public interface RelationRuleMapper extends BaseMapper<RelationRule> {
	/**
	 * @return int
	 * @Author MaQY
	 * @Description 根据规则ID删除
	 * @Date 下午 1:14 2021/11/18 0018
	 * @Param [ruleId]
	 **/
	@Delete("delete from relation_rule where rule_id = #{ruleId}")
	int deleteByRuleId(@Param("ruleId") String ruleId);

}
