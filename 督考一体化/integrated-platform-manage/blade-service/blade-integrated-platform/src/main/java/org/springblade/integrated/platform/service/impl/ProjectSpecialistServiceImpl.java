package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.ProjectSpecialist;
import org.springblade.integrated.platform.mapper.ProjectSpecialistMapper;
import org.springblade.integrated.platform.service.IProjectSpecialistService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表的服务层接口实现类
 */
@Service
public class ProjectSpecialistServiceImpl extends BaseServiceImpl<ProjectSpecialistMapper, ProjectSpecialist> implements IProjectSpecialistService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean listBatchByProjectId(List<ProjectSpecialist> specialist,String projectId) {

		if (specialist != null && !specialist.isEmpty()) {
			//首先取出所有的项目id
			List<String> projectIds = specialist.stream().map(ProjectSpecialist::getProjectId).distinct().collect(Collectors.toList());
			//先删除
			if (projectIds != null && !projectIds.isEmpty()) {
				baseMapper.deleteData(projectIds);
			}
			//再添加
			return this.saveBatch(specialist);
		} else {
			List<String> list = new ArrayList<>();
			list.add(projectId);
			baseMapper.deleteData(list);
		}
		return true;
	}
}
