package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;


import org.springblade.core.mp.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ Date       ：Created in 2025年03月19日10时53分15秒
 * @ Description：项目管理系统汇报信息表实体类
 * @author 11489
 */
@Data
@TableName("project_management_report")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ProjectManagementReport对象", description = "项目管理系统汇报信息表")
public class ProjectManagementReport extends BaseEntity {

    private static final long serialVersionUID = 4409645942145276184L;

	/**
	* 项目基本信息表id
	*/
	@ApiModelProperty(value = "项目基本信息表id")
	private String projectId;

	/**
	* 标题
	*/
	@ApiModelProperty(value = "标题")
	private String reportTitle;

	/**
	* 汇报内容
	*/
	@ApiModelProperty(value = "汇报内容")
	private String reportContent;

	/**
	* 附件名称
	*/
	@ApiModelProperty(value = "附件名称")
	private String fileName;

	/**
	* 附件地址
	*/
	@ApiModelProperty(value = "附件地址")
	private String fileUrl;

}
