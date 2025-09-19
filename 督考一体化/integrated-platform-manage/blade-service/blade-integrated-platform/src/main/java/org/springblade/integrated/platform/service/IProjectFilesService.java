package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ProjectFiles;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IProjectFilesService extends BaseService<ProjectFiles> {
	String getProjectPicByProjId(Long id);

	/**
	 * 查询项目文件-图片列表
	 * @param id
	 * @return
	 */
	List<ProjectFiles> getProjectPictureListByProjId(String id);

	/**
	 * 查询项目文件-视频列表
	 * @param id
	 * @return
	 */
	List<ProjectFiles> getProjectVideoListByProjId(String id);

	/**
	 * 查询项目文件-文件列表
	 * @param id
	 * @return
	 */
	List<ProjectFiles> getProjectFileListByProjId(String id);

	/**
	 * 项目文件下载
	 * @param id
	 * @return
	 */
	ProjectFiles getProjectFileListById(String id);

	/**
	 * 项目材料列表
	 * @param id
	 * @return
	 */
	List<ProjectFiles> getProjectFilesListByProjId(String id);

	/**
	 * 根据项目id删除附件
	 * @param id
	 * @return
	 */
	boolean deleteProjectFilesByProjId(String id);
}
