package org.springblade.integrated.platform.service;

import com.vingsoft.entity.ProjectSpecialist;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表的服务层接口
 */
public interface IProjectSpecialistService extends BaseService<ProjectSpecialist> {

	/**
	 * 通过项目id批量新增修改或删除
	 * @param specialist 项目专员列表
	 * @return boolean 新增修改或删除是否成功
	 */
	boolean listBatchByProjectId(List<ProjectSpecialist> specialist,String projectId);

}
