package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ProjectFiles;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.integrated.platform.mapper.ProjectFilesMapper;
import org.springblade.integrated.platform.service.IProjectFilesService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectFilesServiceImpl extends BaseServiceImpl<ProjectFilesMapper, ProjectFiles> implements IProjectFilesService {

	@Override
	public String getProjectPicByProjId(Long id) {
		Map<String,Object> entity = new HashMap<>();
		entity.put("proj_id",id);
		List<ProjectFiles> files = this.list(Condition.getQueryWrapper(entity, ProjectFiles.class));
		for (ProjectFiles file : files) {
			if(file.getFileName()==null) continue;
			if(file.getFileName().contains("png")||file.getFileName().contains("jpg")||file.getFileName().contains("jepg")){
				return file.getFileUrl();
			}
		}
		return "";
	}

	/**
	 * 查询项目文件-图片列表
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectFiles> getProjectPictureListByProjId(String id){
		return baseMapper.getProjectPictureListByProjId(id);
	}

	/**
	 * 查询项目文件-视频列表
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectFiles> getProjectVideoListByProjId(String id){
		return baseMapper.getProjectVideoListByProjId(id);
	}

	/**
	 * 查询项目文件-文件列表
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectFiles> getProjectFileListByProjId(String id){
		return baseMapper.getProjectFileListByProjId(id);
	}

	/**
	 * 项目文件下载
	 * @param id
	 * @return
	 */
	@Override
	public ProjectFiles getProjectFileListById(String id){
		return baseMapper.getProjectFileListById(id);
	}

	/**
	 * 项目材料列表
	 * @param id
	 * @return
	 */
	@Override
	public List<ProjectFiles> getProjectFilesListByProjId(String id){
		return baseMapper.getProjectFilesListByProjId(id);
	}

	/**
	 * 根据项目id删除附件
	 * @param id
	 * @return
	 */
	@Override
	public boolean deleteProjectFilesByProjId(String id){
		return baseMapper.deleteProjectFilesByProjId(id);
	}
}
