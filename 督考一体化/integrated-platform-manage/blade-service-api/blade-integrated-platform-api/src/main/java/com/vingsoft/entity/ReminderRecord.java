package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-08 20:15
 */
@Data
@TableName("reminder_record")
@ApiModel(value = "reminder_record对象", description = "催办记录表")
public class ReminderRecord extends BaseEntity{

    /**
     * 催办人员
     */
	@ApiModelProperty(value = "催办人员")
    private String reminderUser;

    /**
     * 催办时间
     */
	@ApiModelProperty(value = "催办时间")
    private Date reminderDate;

    /**
     * 催办内容
     */
	@ApiModelProperty(value = "催办内容")
    private String reminderContent;

    /**
     * 被催办单位名称
     */
	@ApiModelProperty(value = "被催办单位名称")
    private String reminderDeptname;

    /**
     * 被催办单位id
     */
	@ApiModelProperty(value = "被催办单位id")
    private String reminderDeptid;

    /**
     * 文件的路径
     */
	@ApiModelProperty(value = "文件的路径")
    private String filePath;

	/**
	 * 文件真实名称
	 */
	@ApiModelProperty(value = "文件真实名称")
	private String fileTrueName;
	/**
	 * 评价指标分类：1年度考核 2季度考核
	 */
	@ApiModelProperty(value = "评价指标分类")
	private String evaluationType;

	/**
	 * 年度、季度评价主键id
	 */
	@ApiModelProperty(value = "年度、季度评价主键id")
	private Long evaluationId;


	/**
	 * 考核评价文件信息
	 */
	@TableField(exist = false)
	List<AppriseFiles> appriseFilesList;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
