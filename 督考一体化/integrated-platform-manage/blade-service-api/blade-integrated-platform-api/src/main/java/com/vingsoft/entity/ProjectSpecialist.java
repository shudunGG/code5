package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;


import org.springblade.core.mp.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表实体类
 */
@Data
@TableName("project_specialist")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ProjectSpecialist对象", description = "项目专员和项目关联表")
public class ProjectSpecialist extends BaseEntity {

    private static final long serialVersionUID = 4851403792192375275L;

	/**
	* 项目基本信息表id
	*/
	@ApiModelProperty(value = "项目基本信息表id")
	private String projectId;

	/**
	* 项目专员
	*/
	@ApiModelProperty(value = "项目专员")
	private String projectSpecialist;

	/**
	* 项目专员联系电话
	*/
	@ApiModelProperty(value = "项目专员联系电话")
	private String projectSpecialistTel;




}
