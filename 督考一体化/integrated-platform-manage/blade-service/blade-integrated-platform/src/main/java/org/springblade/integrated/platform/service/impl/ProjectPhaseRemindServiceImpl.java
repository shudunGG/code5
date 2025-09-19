package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ProjectPhasePlan;
import com.vingsoft.entity.ProjectPhaseRemind;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.ProjectPhaseRemindMapper;
import org.springblade.integrated.platform.service.IProjectPhaseRemindService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectPhaseRemindServiceImpl extends BaseServiceImpl<ProjectPhaseRemindMapper, ProjectPhaseRemind> implements IProjectPhaseRemindService {

	/**
	 * 项目提醒信息删除
	 * @param id
	 * @return
	 */
	@Override
	public boolean projectPhaseRemindDelete(String id){
		return baseMapper.projectPhaseRemindDelete(id);
	}

	@Override
	public ProjectPhaseRemind selectDetail(ProjectPhaseRemind projectPhaseRemind) {
		QueryWrapper<ProjectPhaseRemind> queryWrapper = new QueryWrapper<ProjectPhaseRemind>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectPhaseRemind.getId()!=0,"id",projectPhaseRemind.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	/**
	 * 获取项目提醒信息列表
	 * @param id
	 * @param planId
	 * @return
	 */
	@Override
	public List<ProjectPhaseRemind> getProjectPhaseRemindListByProjIdAndPlanId(String id,String planId){
		return baseMapper.getProjectPhaseRemindListByProjIdAndPlanId(id, planId);
	}
}
