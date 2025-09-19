package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.ProjectManagementReport;
import com.vingsoft.vo.ProjectManagementReportVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

/**
 * @ Date       ：Created in 2025年03月19日10时53分15秒
 * @ Description：项目管理系统汇报信息表包装类,返回视图层所需的字段
 * @author 11489
 */
public class ProjectManagementReportWrapper extends BaseEntityWrapper<ProjectManagementReport, ProjectManagementReportVO> {

    public static ProjectManagementReportWrapper build() {
        return new ProjectManagementReportWrapper();
    }

	@Override
	public ProjectManagementReportVO entityVO(ProjectManagementReport projectManagementReport) {
		ProjectManagementReportVO projectManagementReportVO = new ProjectManagementReportVO();
		BeanUtil.copy(projectManagementReport, projectManagementReportVO);

		return projectManagementReportVO;
	}

}
