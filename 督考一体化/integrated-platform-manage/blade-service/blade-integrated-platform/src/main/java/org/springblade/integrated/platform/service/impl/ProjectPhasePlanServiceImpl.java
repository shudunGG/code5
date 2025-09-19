package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.ProjectPhasePlan;
import com.vingsoft.entity.ProjectPhaseRemind;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.mapper.ProjectPhasePlanMapper;
import org.springblade.integrated.platform.service.IProjectPhasePlanService;
import org.springblade.integrated.platform.service.IProjectPhaseRemindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectPhasePlanServiceImpl extends BaseServiceImpl<ProjectPhasePlanMapper, ProjectPhasePlan> implements IProjectPhasePlanService {

	@Autowired
	private IProjectPhaseRemindService projectPhaseRemindService;

	@Override
	public ProjectPhasePlan selectDetail(ProjectPhasePlan projectPhasePlan) {
		QueryWrapper<ProjectPhasePlan> queryWrapper = new QueryWrapper<ProjectPhasePlan>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhasePlan.getId()!=0,"id",projectPhasePlan.getId());
		return baseMapper.selectOne(queryWrapper);
	}



	/**
	 * 保存申报项目阶段与提醒信息
	 * @param projectPhasePlanList
	 * @return
	 */
	@Override
	public boolean saveList(List<ProjectPhasePlan> projectPhasePlanList) {
		boolean result = this.saveOrUpdateBatch(projectPhasePlanList);
		if(result){
			for (ProjectPhasePlan projectPhasePlan:projectPhasePlanList){
				List<ProjectPhaseRemind> projectPhaseRemindList = projectPhasePlan.getProjectPhaseRemindList();
				if(ObjectUtil.isNotEmpty(projectPhaseRemindList)){
					for (ProjectPhaseRemind projectPhaseRemind:projectPhaseRemindList){
						projectPhaseRemind.setProjId(projectPhasePlan.getProjId());
						projectPhaseRemind.setPhaseId(projectPhasePlan.getId());
						projectPhaseRemind.setPhaseName(projectPhasePlan.getPhaseName());
					}
					result = projectPhaseRemindService.saveOrUpdateBatch(projectPhaseRemindList);
				}
			}
		}
		return result;
	}

	/**
	 * 项目阶段信息删除
	 * @param id
	 * @return
	 */
	@Override
	public boolean projectPhasePlanDelete(String id){
		return baseMapper.projectPhasePlanDelete(id);
	}

	/**
	 * 项目阶段信息查询
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectPhasePlan> getProjectPhasePlanListByProjId(String id){
		return baseMapper.getProjectPhasePlanListByProjId(id);
	}

	/**
	 * 获取项目本月的阶段信息
	 * @param id
	 * @param planMonth
	 * @return
	 */
	@Override
	public ProjectPhasePlan getProjectPhasePlanByProjIdAndMonth(Long id,int planMonth){
		return  baseMapper.getProjectPhasePlanByProjIdAndMonth(id, planMonth);
	}
}
