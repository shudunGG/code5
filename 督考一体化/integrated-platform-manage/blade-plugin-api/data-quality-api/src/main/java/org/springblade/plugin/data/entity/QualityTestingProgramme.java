package org.springblade.plugin.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * quality_testing_programme实体类
 *
 * @author
 */
@Data
@TableName("quality_testing_programme")
@ApiModel(value = "quality_testing_programme对象", description = "质检方案表")
public class QualityTestingProgramme extends BaseEntity {
	private static final long serialVersionUID = -4568705010502174050L;
	/**
	 * 方案名称
	 */
	@ApiModelProperty(value = "方案名称")
	private String name;
	/**
	 * 执行周期
	 */
	@ApiModelProperty(value = "执行周期")
	private String cycle;
	/**
	 * 执行周期的具体时间
	 */
	@ApiModelProperty(value = "执行周期的具体时间")
	private String time;
	/**
	 * 执行周期的星期几
	 */
	@ApiModelProperty(value = "执行周期的星期几")
	private String week;
	/**
	 * 执行周期的具体日期时间
	 */
	@ApiModelProperty(value = "执行周期的具体日期时间")
	private String datetime;
	/**
	 * 执行周期的月份
	 */
	@ApiModelProperty(value = "执行周期的月份")
	private String month;
	/**
	 * 执行周期的日期
	 */
	@ApiModelProperty(value = "执行周期的日期")
	private String date;
	/**
	 * 同步上期例外1是0否
	 */
	@ApiModelProperty(value = "同步上期例外")
	private String sync;
	/**
	 * 计划任务1启用0禁用
	 */
	@ApiModelProperty(value = "计划任务")
	private String planTask;
	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述")
	private String description;
	/**
	 * 是否通知1是0否
	 */
	@ApiModelProperty(value = "是否通知")
	private String notify;
	/**
	 * 通知人
	 */
	@ApiModelProperty(value = "被通知人")
	private String notifyUser;
	/**
	 * 通知内容
	 */
	@ApiModelProperty(value = "通知内容")
	private String noticeContent;
	/**
	 * 业务模型管理表主键
	 */
	@ApiModelProperty(value = "业务模型管理表主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long modelId;
	/**
	 * 定时任务id
	 */
	@ApiModelProperty(value = "定时任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Integer jobId;
}
