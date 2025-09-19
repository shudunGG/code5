package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * @description:汇报时间实体
 * @date 2022-04-08 20:15
 */
@Data
@TableName("report_time")
@ApiModel(value = "report_time对象", description = "汇报时间表")
public class ReportTime extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 阶段信息表主键id
	 */
	@ApiModelProperty(value = "阶段信息表主键id")
	private Long stageInformationId;

    /**
     * 阶段名称
     */
	@ApiModelProperty(value = "阶段名称")
    private String stage;

    /**
     * 汇报时间
     */
	@ApiModelProperty(value = "汇报时间")
    private Date reportTime;

    /**
     * 提前提醒时间（小时）
     */
	@ApiModelProperty(value = "提前提醒时间（小时）")
    private String reminderTime;
	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
