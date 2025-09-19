package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.entity.QualityTestingProgrammeRule;

import java.util.List;

/**
 * @author MaQiuyun
 * @date 2021/12/16 14:52
 * @description:
 */
public interface QualityTestingProgrammeRuleMapper extends BaseMapper<QualityTestingProgrammeRule> {
	/**
	 * 根据质检方案主键查询规则主键
	 *
	 * @param programmeId
	 * @return
	 */
	@Select("select manage_rule_id from quality_testing_programme_rule where quality_testing_programme_id = #{programmeId}")
	List<String> selectRuleIdByProgrammeId(@Param("programmeId") String programmeId);

	/**
	 * 根据质检方案ID删除记录
	 *
	 * @param programmeId
	 * @return
	 */
	@Delete("delete from quality_testing_programme_rule where quality_testing_programme_id = #{programmeId}")
	Integer deleteByProgrammeId(@Param("programmeId") String programmeId);

	/**
	 * 根据质检方案主键和规则主键删除记录
	 *
	 * @param programmeId
	 * @param ruleId
	 * @return
	 */
	@Delete("delete from quality_testing_programme_rule where quality_testing_programme_id = #{programmeId} and manage_rule_id = #{ruleId}")
	Integer deleteByIds(@Param("programmeId") String programmeId, @Param("ruleId") String ruleId);
}
