package com.vingsoft.vo;

import com.vingsoft.entity.FollowInformation;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-13 16:35:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "关注信息对象", description = "关注信息对象")
public class FollowInformationVO extends FollowInformation {

	//督查督办相关--------------
	//事项名称
	private String servName;
	//事项类型，先写上三级分类
	private String servTypeThree;

	//考核评价相关--------------
	//评价分类
	private String checkClassifyName;
	//评价要点
	private String majorTarget;

	//项目管理相关---------------
	//项目类型
	private String projectType;
	//项目名称
	private String projectName;


}
