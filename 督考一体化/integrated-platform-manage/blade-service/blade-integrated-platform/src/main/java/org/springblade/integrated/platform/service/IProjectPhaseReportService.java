package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhaseRemind;
import com.vingsoft.entity.ProjectPhaseReport;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IProjectPhaseReportService extends BaseService<ProjectPhaseReport> {

	/**
	 * 按条件查询ProjectPhaseReport表中的数据
	 * @param projectPhaseReport
	 * @return
	 */
	ProjectPhaseReport selectDetail(ProjectPhaseReport projectPhaseReport);

	/**
	 * 阶段汇报信息查询
	 * @param id
	 * @return
	 */
	List<ProjectPhaseReport> getProjectPhaseReportListByProjId(String id);

	/**
	 * 获取阶段汇报信息
	 * @param id
	 * @return
	 */
	ProjectPhaseReport getProjectPhaseReportByPlanId(Long id);
	ProjectPhaseReport getProjectPhaseReportAllByPlanId(Long id);
	ProjectPhaseReport getFirstProjectPhaseReportByProjId(Long id);

	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	ProjectPhaseReport getProjectPhaseReportByHbjdId(Long id);


}
