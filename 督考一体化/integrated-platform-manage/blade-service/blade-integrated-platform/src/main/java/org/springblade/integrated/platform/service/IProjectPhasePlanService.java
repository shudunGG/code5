package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhasePlan;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IProjectPhasePlanService extends BaseService<ProjectPhasePlan> {


	/**
	 * 按条件查询ProjectPhasePlan表中的数据
	 * @param projectPhasePlan
	 * @return
	 */
	ProjectPhasePlan selectDetail(ProjectPhasePlan projectPhasePlan);


	/**
	 * 保存申报阶段信息
	 * @param projectPhasePlanList
	 * @return
	 */
	boolean saveList(List<ProjectPhasePlan> projectPhasePlanList);

	/**
	 * 项目阶段信息删除
	 * @param id
	 * @return
	 */
	boolean projectPhasePlanDelete(String id);

	/**
	 * 项目阶段信息查询
	 * @param id
	 * @return
	 */
	List<ProjectPhasePlan> getProjectPhasePlanListByProjId(String id);

	/**
	 * 获取项目本月的阶段信息
	 * @param id
	 * @param planMonth
	 * @return
	 */
	ProjectPhasePlan getProjectPhasePlanByProjIdAndMonth(Long id,int planMonth);
}
