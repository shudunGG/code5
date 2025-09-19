package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.*;
import com.vingsoft.vo.MapPorjectVO;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel1;
import org.springblade.system.user.entity.User;


import java.util.List;
import java.util.Map;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IProjectSummaryService extends BaseService<ProjectSummary> {

	int getTotalCount(String year);
	int getStartedCount(String year);
	float getTotalInvestment(String year);
	float getYearInvestment(String year);
	float getDoneInvestment(String year);

	int getTZTotalCount(String projLabel,String year);
	int getTZStartedCount(String projLabel,String year);
	float getTZTotalInvestment(String projLabel,String year);
	float getTZYearInvestment(String projLabel,String year);
	float getTZDoneInvestment(String projLabel,String year);

	/**
	 * 导出数据
	 *
	 * @param projectSummaryExcel1
	 * @return
	 */
	List<ProjectSummaryExcel1> exportProjectSummary(ProjectSummaryExcel1 projectSummaryExcel1);

	/**
	 * 按条件查询ProjectSummary表中的数据
	 * @param projectSummary
	 * @return
	 */
	ProjectSummary selectDetail(ProjectSummary projectSummary);

	/**
	 * 保存申报项目基础信息
	 * @param projectSummary
	 * @return
	 */
	boolean saveProjectSummary(ProjectSummary projectSummary);

	/**
	 * 修改申报项目基础信息
	 * @param projectSummary
	 * @return
	 */
	boolean updateProjectSummary(ProjectSummary projectSummary);

	/**
	 * 项目入库
	 * @param id
	 * @return
	 */
	boolean projectSummaryRk(String id,String reportStatus);

	/**
	 * 项目送审
	 * @param id
	 * @return
	 */
	boolean projectSummaryWare(String id,String reportStatus);

	/**
	 * 更新项目状态
	 * @param id
	 * @param projStatus
	 * @return
	 */
	boolean updateProjStatus(String id,String projStatus);

	/**
	 * 项目退回
	 * @param id,reportStatus
	 * @return
	 */
	boolean projectSummaryRebake(String id,String reportStatus,String projStatus);
	IPage<MapPorjectVO> queryProjectMap(IPage<MapPorjectVO> page, Map<String, Object> entity);
	List<MapPorjectVO> queryProjectMapNoPage(Map<String, Object> entity);

	/**
	 * 项目挂牌
	 * @param id
	 * @param autoState
	 * @return
	 */
	boolean projectSummaryListing(String id,String autoState);

	/**
	 * 项目移库
	 * @param id
	 * @return
	 */
	boolean projectSummaryYk(String id,String projLabel);

	/**
	 * 导入投资项目
	 * @param data
	 */
	void imimportProjectSummary(List<ProjectSummaryExcel> data);

	List<Map<String,Object>> getXmnfList();


	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	ProjectSummary getProjectPhaseReportByHbjdId(Long id);

	/**
	 * 查询项目人员
	 * @param id
	 * @return
	 */
	List<User> getUserListByProjId(String id);
	String getUserIdListByProjId(Long id,Long loginId);
	String getUserIdListByDeptId(String deptId,String loginId);


	IPage<ProjectSummary> selectPage(IPage<ProjectSummary> page, Map<String, Object> entity);

	List<ProjectSummary> selectListByYear(String searchYear);

	IPage<ProjectSummary> selectXMGLPage(IPage<ProjectSummary> page, Map<String, Object> entity);
}
