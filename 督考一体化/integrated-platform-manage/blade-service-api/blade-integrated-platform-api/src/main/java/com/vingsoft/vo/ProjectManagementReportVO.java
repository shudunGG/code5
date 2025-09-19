package com.vingsoft.vo;

import com.vingsoft.entity.ProjectManagementReport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;

/**
 * @ Date       ：Created in 2025年03月19日10时53分15秒
 * @ Description：项目管理系统汇报信息表视图实体类
 * @author 11489
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ProjectManagementReportVO对象", description = "项目管理系统汇报信息表")
public class ProjectManagementReportVO extends ProjectManagementReport {
	private static final long serialVersionUID = -2687170665123408179L;

}
