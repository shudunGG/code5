package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhasePlan;
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
public interface ProjectPhasePlanMapper extends BaseMapper<ProjectPhasePlan> {

	/**
	 * 项目阶段信息删除
	 * @param id
	 * @return
	 */
	@Update("update project_phase_plan set is_deleted = 1 where proj_id = #{id}")
	boolean projectPhasePlanDelete(@Param("id") String id);

	/**
	 * 项目阶段信息查询
	 * @param id
	 * @return
	 */
	@Select("select * from project_phase_plan where proj_id = #{id} and is_deleted = 0 order by plan_month")
	List<ProjectPhasePlan> getProjectPhasePlanListByProjId(@Param("id") String id);

	/**
	 * 获取项目本月的阶段信息
	 * @param id
	 * @param planMonth
	 * @return
	 */
	@Select("select * from project_phase_plan where proj_id = #{id} and is_deleted = 0 and plan_month = ${planMonth}")
	ProjectPhasePlan getProjectPhasePlanByProjIdAndMonth(@Param("id") Long id,@Param("planMonth") int planMonth);
}
