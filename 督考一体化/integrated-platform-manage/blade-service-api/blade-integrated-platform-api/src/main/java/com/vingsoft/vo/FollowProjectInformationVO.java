package com.vingsoft.vo;

import com.vingsoft.entity.FollowInformation;
import com.vingsoft.entity.ProjectSummary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-13 16:35:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "关注项目信息对象", description = "关注信息对象")
public class FollowProjectInformationVO extends ProjectSummary {


	@ApiModelProperty(value = "关注人id")
	private Long followUserId;
	@ApiModelProperty(value = "关注人单位id")
	private Long followDeptId;
	/**
	 * 关注人
	 */
	@ApiModelProperty(value = "关注人")
	private String followUser;

	@ApiModelProperty("关注时间")
	private Date followDate;



}
