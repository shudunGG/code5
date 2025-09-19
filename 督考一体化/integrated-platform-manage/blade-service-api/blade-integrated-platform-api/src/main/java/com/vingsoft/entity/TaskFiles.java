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
 * @description:任务附件实体
 * @date 2022-04-08 20:15
 */
@Data
@TableName("task_files")
@ApiModel(value = "task_files对象", description = "任务附件表")
public class TaskFiles extends BaseEntity{

	/**
	 * 文件路径
	 */
	@ApiModelProperty(value = "文件路径")
	private String filePath;

    /**
     * 文件名称
     */
	@ApiModelProperty(value = "文件名称")
    private String fileName;

    /**
     * 文件大小
     */
	@ApiModelProperty(value = "文件大小")
    private String fileSize;

    /**
     * 文件来源
     */
	@ApiModelProperty(value = "文件来源")
    private String fileFrom;

    /**
     * 指标分类：1年度指标 2季度指标
     */
	@ApiModelProperty(value = "指标分类")
    private String evaluationType;

    /**
     * 指标表id
     */
	@ApiModelProperty(value = "指标表id")
    private Long evaluationId;


	/**
	 * 上传人
	 */
	@ApiModelProperty(value = "上传人")
	private String uploadUser;

	/**
	 * 上传时间
	 */
	@ApiModelProperty(value = "上传时间")
	private Date uploadTime;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
