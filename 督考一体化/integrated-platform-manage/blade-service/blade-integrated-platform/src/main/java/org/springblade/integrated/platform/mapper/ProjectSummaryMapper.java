package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vingsoft.entity.ProjectPhaseReport;
import com.vingsoft.entity.ProjectSummary;
import com.vingsoft.vo.MapPorjectVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel1;

import java.util.List;
import java.util.Map;

/**
 * Mapper 接口
 *
 * @Author Adam
 * @Create 2022-4-9 18:03:05
 */
public interface ProjectSummaryMapper extends BaseMapper<ProjectSummary> {

	/**
	 * 导出数据
	 *
	 * @param
	 * @return
	 */
	List<ProjectSummaryExcel1> exportProjectSummary(ProjectSummaryExcel1 projectSummaryExcel1);


	/**
	 * 项目入库
	 * @param id
	 * @return
	 */
	@Update("UPDATE project_summary SET sfrk=1,porj_status=2 where id=#{id}")
	boolean projectSummaryRk(@Param("id") String id,@Param("reportStatus") String reportStatus);

	/**
	 * 项目送审
	 * @param id
	 * @return
	 */
	@Update("UPDATE project_summary SET report_status=#{reportStatus} where id=#{id}")
	boolean projectSummaryWare(@Param("id") String id,@Param("reportStatus") String reportStatus);

	/**
	 * 更新项目状态
	 * @param id
	 * @return
	 */
	@Update("UPDATE project_summary SET porj_status=#{projStatus} where id=#{id}")
	boolean updateProjStatus(@Param("id") String id,@Param("projStatus") String projStatus);

	/**
	 * 项目退回
	 * @param id
	 * @return
	 */
	@Update("UPDATE project_summary SET report_status=#{reportStatus}, porj_status=#{projStatus} where id=#{id}")
	boolean projectSummaryRebake(@Param("id") String id,@Param("reportStatus") String reportStatus,@Param("projStatus") String projStatus);

	/**
	 * 项目挂牌
	 * @param id
	 * @param autoState
	 * @return
	 */
	@Update("UPDATE project_summary SET auto_state=#{autoState} where id=#{id}")
	boolean projectSummaryListing(@Param("id") String id,@Param("autoState") String autoState);

	@Update("update project_summary set proj_label=#{projLabel} where id=#{id}")
	boolean projectSummaryYk(@Param("id") String id,@Param("projLabel") String projLabel);

	@Select("select count(1) from project_summary where is_deleted='0' and xmdl='1' and xmnf = #{year}")
	int getTotalCount(@Param("year") String year);

	@Select("select count(1) from project_summary where sfkfg='1' and is_deleted='0' and xmdl='1' and xmnf = #{year}")
	int getStartedCount(@Param("year") String year);

	@Select("select ifnull(sum(total_investment),0)  from project_summary where is_deleted='0' and xmdl='1' and xmnf = #{year}")
	float getTotalInvestment(@Param("year") String year);

	@Select("select ifnull(sum(year_economic),0)  from project_summary where is_deleted='0' and xmdl='1' and xmnf = #{year}")
	float getYearInvestment(@Param("year") String year);

	@Select("select ifnull(sum(total_economic),0)  wctz01 from project_summary where is_deleted='0' and xmdl='1' and xmnf = #{year}")
	float getDoneInvestment(@Param("year") String year);

	@Select("select count(1) from project_summary where is_deleted='0' and xmdl='1' and proj_label like CONCAT('%',#{projLabel},'%') and xmnf = #{year}")
	int getTZTotalCount(@Param("projLabel") String projLabel,@Param("year") String year);

	@Select("select count(1) from project_summary where sfkfg='1' and is_deleted='0' and xmdl='1' and proj_label like CONCAT('%',#{projLabel},'%') and xmnf = #{year}")
	int getTZStartedCount(@Param("projLabel") String projLabel,@Param("year") String year);

	@Select("select ifnull(sum(total_investment),0)  from project_summary where is_deleted='0' and xmdl='1' and proj_label like CONCAT('%',#{projLabel},'%') and xmnf = #{year}")
	float getTZTotalInvestment(@Param("projLabel") String projLabel,@Param("year") String year);

	@Select("select ifnull(sum(year_economic),0)  from project_summary where is_deleted='0' and xmdl='1' and proj_label like CONCAT('%',#{projLabel},'%') and xmnf = #{year}")
	float getTZYearInvestment(@Param("projLabel") String projLabel,@Param("year") String year);

	@Select("select ifnull(sum(total_economic),0)  wctz01 from project_summary where is_deleted='0' and xmdl='1' and proj_label like CONCAT('%',#{projLabel},'%') and xmnf = #{year}")
	float getTZDoneInvestment(@Param("projLabel") String projLabel,@Param("year") String year);

	IPage<MapPorjectVO> queryProjectMap(IPage<MapPorjectVO> page, @Param("entity") Map<String, Object> entity);
	List<MapPorjectVO> queryProjectMapNoPage(@Param("entity") Map<String, Object> entity);

	@Select("select xmnf from project_summary where is_deleted='0' and xmnf is not null and xmnf != '' GROUP BY xmnf")
	List<Map<String,Object>> getXmnfList();


	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	@Select("select * from project_summary where id = ${id} and is_deleted = 0")
	ProjectSummary getProjectPhaseReportByHbjdId(@Param("id") Long id);

	IPage<ProjectSummary> selectPage(IPage<ProjectSummary> page, @Param("entity") Map<String, Object> entity);

	@Select("select id, porj_status, report_status, xm_type, xmdl , auto_state, qqcylx, title, proj_main, proj_content , xm_address, dd_address, " +
		"total_investment, year_economic, month_economic , proj_code, proj_label, start_date_plan, complete_date, sgdw , sgdw_name, wctz01, wctz02, " +
		"wctz03, wctz04 , wctz05, wctz06, wctz07, wctz08, wctz09 , wctz10, wctz11, wctz12, jhtz01, jhtz02 , jhtz03, jhtz04, jhtz05, jhtz06, jhtz07 ," +
		" jhtz08, jhtz09, jhtz10, jhtz11, jhtz12 , sjld, sjld_name, xjld, xjld_name, szhyzgbm , szhyzgbm_name, szhyzgbm_zrr, szhyzgbm_zrr_name," +
		" xqhyzgbm, xqhyzgbm_name , xqhyzgbm_zrr, xqhyzgbm_zrr_name, dhhm, gddh, manager_contact , dwmc, dwmc_name, file, year_goal, qqgzjhwcsj ," +
		" bzzgdw, bzzgdw_name, bzzgdw_zrr, bzzgdw_zrr_name, bzzrdw , bzzrdw_name, bzzrdw_zrr, bzzrdw_zrr_name, xmnf, sfkfg , sfnrtj, create_user," +
		" create_dept, create_time, update_user , update_time, status, zxmc, proj_scale, sgfzr , sgfzr_name, subordination, proj_nature, xdzjwh, xdzj_time ," +
		" tzjhhj, zytzjh, sjtzjh, dftzjh, fxzq_time , fxzqje, sfrk, cy_type, wcqk, zrr , zrr_name, area_code, total_economic, sjbzld, sjbzld_name ," +
		" is_deleted, bg_file_name, bg_file_url from project_summary " +
		"where is_deleted = 0 and xmdl like CONCAT('%', '1', '%') and xmnf = #{searchYear} order by create_time desc")
	List<ProjectSummary> selectListByYear(@Param("searchYear") String searchYear);


	IPage<ProjectSummary> selectXMGLPage(IPage<ProjectSummary> page, @Param("entity") Map<String, Object> entity);
}
