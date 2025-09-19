package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-08 20:12
 */
@Data
@TableName("follow_information")
@ApiModel(value = "follow_information对象", description = "关注信息表")
public class FollowInformation extends BaseEntity {
    /**
     * 业务大类（1督查督办2考核评价3项目管理）
     */
	@ApiModelProperty(value = "业务大类（1督查督办2考核评价3项目管理）")
    private String businessType;

    /**
     * 业务主键（事项id，评价id，项目id）
     */
	@ApiModelProperty(value = "业务主键（事项id，评价id，项目id）")
    private Long businessId;

	@ApiModelProperty(value = "业务数据表")
	private String businessTable;

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
