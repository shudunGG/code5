package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhaseReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author Adam
 * @Create 2022-4-9 18:03:05
 */
public interface ProjectPhaseReportMapper extends BaseMapper<ProjectPhaseReport> {

	/**
	 * 阶段汇报信息查询
	 * @param id
	 * @return
	 */
	@Select("select p.phase_name,p.end_time,r.* from project_phase_plan p left join project_phase_report r on p.id = r.hbjd_id where p.proj_id = #{id} and p.is_deleted = 0")
	List<ProjectPhaseReport> getProjectPhaseReportListByProjId(@Param("id") String id);

	/**
	 * 获取阶段汇报信息(审核通过)
	 * @param id
	 * @return
	 */
	@Select("select * from project_phase_report where jhjd_id = ${id} and is_deleted = 0 and shzt = 3")
	ProjectPhaseReport getProjectPhaseReportByPlanId(@Param("id") Long id);

	/**
	 * 获取阶段汇报信息(所有状态）
	 * @param id
	 * @return
	 */
	@Select("select * from project_phase_report where jhjd_id = ${id} and is_deleted = 0")
	ProjectPhaseReport getProjectPhaseReportAllByPlanId(@Param("id") Long id);

	/**
	 * 根据项目id获取未处理的调度
	 * @param id
	 * @return
	 */
	@Select("select * from project_phase_report where proj_id = ${id} and is_deleted = 0 and (shzt!=3 or shzt is null) order by create_time limit 1")
	ProjectPhaseReport getFirstProjectPhaseReportByProjId(@Param("id") Long id);


	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	@Select("select * from project_phase_report where hbjd_id = ${id} and is_deleted = 0")
	ProjectPhaseReport getProjectPhaseReportByHbjdId(@Param("id") Long id);

}
