package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.plugin.data.dto.ManageRuleDTO;
import org.springblade.plugin.data.entity.ManageRule;

import java.util.List;

/**
 * ManageRule的Dao接口
 *
 * @author
 */
public interface ManageRuleMapper extends BaseMapper<ManageRule> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.ManageRule>
	 * @Author MaQY
	 * @Description 自定义分页查询
	 * @Date 下午 4:28 2021/11/3 0003
	 * @Param [rule]
	 **/
	List<ManageRule> selectPageList(IPage page, @Param("rule") ManageRuleDTO rule);

	/**
	 * @return java.lang.Integer
	 * @Author MaQY
	 * @Description 获取当前主题表所在的模型下当前类型的规则数量+1
	 * @Date 下午 3:02 2021/11/4 0004
	 * @Param [type, themeId]
	 **/
	@Select("select COUNT(`type`)+1 from manage_rule where theme_id in (select id from theme_table where model_id = (select model_id from theme_table where id = #{themeId} and is_deleted = 0) and is_deleted = 0) and `type`=#{type}")
	Integer selectTypeCountInOneModel(@Param("type") String type, @Param("themeId") String themeId);

	/**
	 * 获取质检方案关联规则
	 *
	 * @param programmeId
	 * @return
	 */
	@Select("select * from manage_rule where enable_status='1' and id in (select manage_rule_id from quality_testing_programme_rule where quality_testing_programme_id = #{programmeId})")
	List<ManageRule> getTestingDataQualityRules(@Param("programmeId") String programmeId);

}
