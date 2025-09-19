package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ProjectSpecialist;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表的持久化接口
 * @author 11489
 */
public interface ProjectSpecialistMapper extends BaseMapper<ProjectSpecialist> {

	int deleteData(@Param("projectIds") List<String> projectIds);

}
