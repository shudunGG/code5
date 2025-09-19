package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectSummary;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author Adam
 * @Create 2022-4-9 18:03:05
 */
public interface ProjectFilesMapper extends BaseMapper<ProjectFiles> {

	/**
	 * 项目文件-图片列表
	 * @param id
	 * @return
	 */
	@Select("select * from project_files where proj_id = #{id} and is_deleted = 0 and file_format = 1")
	List<ProjectFiles> getProjectPictureListByProjId(@Param("id") String id);

	/**
	 * 项目文件-视频列表
	 * @param id
	 * @return
	 */
	@Select("select * from project_files where proj_id = #{id} and is_deleted = 0 and file_format = 2")
	List<ProjectFiles> getProjectVideoListByProjId(@Param("id") String id);

	/**
	 * 项目文件-文件列表
	 * @param id
	 * @return
	 */
	@Select("select * from project_files where proj_id = #{id} and is_deleted = 0 and file_format = 3")
	List<ProjectFiles> getProjectFileListByProjId(@Param("id") String id);

	/**
	 * 项目文件下载
	 * @param id
	 * @return
	 */
	@Select("select file_name,file_url from project_files where id =#{id} and is_deleted = 0")
	ProjectFiles getProjectFileListById(@Param("id") String id);

	/**
	 * 项目材料列表
	 * @param id
	 * @return
	 */
	@Select("select * from project_files where proj_id =#{id} and is_deleted = 0")
	List<ProjectFiles> getProjectFilesListByProjId(@Param("id") String id);

	/**
	 * 根据项目id删除附件
	 * @param id
	 * @return
	 */
	@Update("delete from project_files where proj_id =#{id}")
	boolean deleteProjectFilesByProjId(@Param("id") String id);
}
