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
 * @description:任务日志实体
 * @date 2022-04-08 20:15
 */
@Data
@TableName("task_logs")
@ApiModel(value = "task_logs对象", description = "任务日志表")
public class TaskLogs extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 操作人员名称
	 */
	@ApiModelProperty(value = "操作人员名称")
	private String operateUsername;

    /**
     * 操作内容
     */
	@ApiModelProperty(value = "操作内容")
    private String operateContent;

    /**
     * 考核评价操作类型  1：创建，2：删除，3：编辑，4：查看，5：导出，6：导入
     */
	@ApiModelProperty(value = "操作类型")
    private String operateType;

	/**
	 * 操作部门名称
	 */
	@ApiModelProperty(value = "操作部门名称")
	private String operateDeptname;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
