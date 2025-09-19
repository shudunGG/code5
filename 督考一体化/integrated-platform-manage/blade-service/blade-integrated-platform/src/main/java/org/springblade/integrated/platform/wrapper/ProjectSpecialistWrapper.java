package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.ProjectSpecialist;
import com.vingsoft.vo.ProjectSpecialistVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表包装类,返回视图层所需的字段
 * @author 11489
 */
public class ProjectSpecialistWrapper extends BaseEntityWrapper<ProjectSpecialist, ProjectSpecialistVO>  {

    public static ProjectSpecialistWrapper build() {
        return new ProjectSpecialistWrapper();
    }

	@Override
	public ProjectSpecialistVO entityVO(ProjectSpecialist projectSpecialist) {
		ProjectSpecialistVO projectSpecialistVO = new ProjectSpecialistVO();
		BeanUtil.copy(projectSpecialist, projectSpecialistVO);

		return projectSpecialistVO;
	}

}
