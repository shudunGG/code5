package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * supervision_log
 * @author
 */
@Data
@TableName("supervision_log")
public class SupervisionLog extends BaseEntity implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

    /**
     * 操作人员
     */
    private String operationUser;

    /**
     * 操作人员名称
     */
    private String operationUserName;

    /**
     * 单位
     */
    private String operationDept;

    /**
     * 单位名称
     */
    private String operationDeptName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 时间
     */
    private Date operationTime;

    /**
     * 操作内容
     */
    private String content;

    private static final long serialVersionUID = 1L;

	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;


}
