package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ProjectPhasePlan;
import com.vingsoft.entity.ProjectPhaseReport;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.ProjectPhaseReportMapper;
import org.springblade.integrated.platform.service.IProjectPhaseReportService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectPhaseReportServiceImpl extends BaseServiceImpl<ProjectPhaseReportMapper, ProjectPhaseReport> implements IProjectPhaseReportService {

	/**
	 * 阶段汇报信息查询
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectPhaseReport> getProjectPhaseReportListByProjId(String id){
		return baseMapper.getProjectPhaseReportListByProjId(id);
	}
	@Override
	public ProjectPhaseReport selectDetail(ProjectPhaseReport projectPhaseReport) {
		QueryWrapper<ProjectPhaseReport> queryWrapper = new QueryWrapper<ProjectPhaseReport>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhaseReport.getId()!=0,"id",projectPhaseReport.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	/**
	 * 获取阶段汇报信息（已审核）
	 * @param id
	 * @return
	 */
	@Override
	public ProjectPhaseReport getProjectPhaseReportByPlanId(Long id){
		return baseMapper.getProjectPhaseReportByPlanId(id);
	}

	/**
	 * 获取阶段汇报信息（全部）
	 * @param id
	 * @return
	 */
	@Override
	public ProjectPhaseReport getProjectPhaseReportAllByPlanId(Long id){
		return baseMapper.getProjectPhaseReportAllByPlanId(id);
	}
	/**
	 * 根据项目id获取未处理的调度
	 * @param id
	 * @return
	 */
	@Override
	public ProjectPhaseReport getFirstProjectPhaseReportByProjId(Long id){
		return baseMapper.getFirstProjectPhaseReportByProjId(id);
	}

	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	@Override
	public ProjectPhaseReport getProjectPhaseReportByHbjdId(Long id){
		return baseMapper.getProjectPhaseReportByHbjdId(id);
	}

}
