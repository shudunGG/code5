package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhaseRemind;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author Adam
 * @Create 2022-4-9 18:03:05
 */
public interface ProjectPhaseRemindMapper extends BaseMapper<ProjectPhaseRemind> {

	@Update("update project_phase_remind set is_deleted = 1 where proj_id = #{id}")
	boolean projectPhaseRemindDelete(@Param("id") String id);

	/**
	 * 获取项目提醒信息列表
	 * @param id
	 * @param planId
	 * @return
	 */
	@Select("select * from project_phase_remind where proj_id = #{id} and phase_id = #{planId} and is_deleted = 0")
	List<ProjectPhaseRemind> getProjectPhaseRemindListByProjIdAndPlanId(@Param("id") String id,@Param("planId") String planId);

}
