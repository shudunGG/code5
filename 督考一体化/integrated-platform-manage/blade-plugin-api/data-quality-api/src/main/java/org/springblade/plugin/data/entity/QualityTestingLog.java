package org.springblade.plugin.data.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * quality_testing_log实体类
 *
 * @author
 */
@Data
@TableName("quality_testing_log")
@ApiModel(value = "quality_testing_log对象", description = "质检日志表")
public class QualityTestingLog extends BaseEntity {
	private static final long serialVersionUID = 4351284238371422193L;
	/**
	 * 质检方案表ID
	 */
	@ApiModelProperty(value = "质检方案表ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long qualityTestingProgrammeId;
	/**
	 * 质检结果1成功2失败
	 */
	@ApiModelProperty(value = "质检结果")
	private String qualityTestingResult;
	/**
	 * 统计时间（统计的是哪一周期，这里就是那个时间）
	 */
	@ApiModelProperty(value = "统计时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date statisticalTime;
	/**
	 * 数据量
	 */
	@ApiModelProperty(value = "数据量")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long dataVolume;
	/**
	 * 错误数
	 */
	@ApiModelProperty(value = "错误数")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long errorVolume;
	/**
	 * 分值
	 */
	@ApiModelProperty(value = "分值")
	private Integer score;
	/**
	 * 耗时(毫秒)
	 */
	@ApiModelProperty(value = "耗时")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long timeConsuming;
	/**
	 * 执行者
	 */
	@ApiModelProperty(value = "执行者")
	private String executor;
	/**
	 * 质检时间
	 */
	@ApiModelProperty(value = "质检时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date qualityTestingTime;
	/**
	 * 统计周期
	 */
	@ApiModelProperty(value = "统计周期")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date currentCycle;
	/**
	 * 周期类型
	 */
	@ApiModelProperty(value = "周期类型")
	private String cycleType;
	/**
	 * 业务模型管理表主键
	 */
	@ApiModelProperty(value = "业务模型管理表主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long modelId;

	@TableField(exist = false)
	private Long createUser;
	@TableField(exist = false)
	private Date createTime;
	@TableField(exist = false)
	private Long updateUser;
	@TableField(exist = false)
	private Date updateTime;
	@TableField(exist = false)
	private Long createDept;
	@TableField(exist = false)
	private Integer status;
	@TableField(exist = false)
	private Integer isDeleted;
}
