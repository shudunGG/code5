package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectPhasePlan;
import com.vingsoft.entity.ProjectPhaseRemind;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IProjectPhaseRemindService extends BaseService<ProjectPhaseRemind> {


	/**
	 * 按条件查询ProjectPhaseRemind表中的数据
	 * @param projectPhaseRemind
	 * @return
	 */
	ProjectPhaseRemind selectDetail(ProjectPhaseRemind projectPhaseRemind);

	/**
	 * 项目提醒信息删除
	 * @param id
	 * @return
	 */
	boolean projectPhaseRemindDelete(String id);

	/**
	 * 获取项目提醒信息列表
	 * @param id
	 * @param planId
	 * @return
	 */
	List<ProjectPhaseRemind> getProjectPhaseRemindListByProjIdAndPlanId(String id, String planId);
}
