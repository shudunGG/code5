package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * supervision_urge
 * @author
 */
@Data
@TableName("supervision_urge")
public class SupervisionUrge extends BaseEntity implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

    /**
     * 催办人员
     */
    private Long urgeUser;

    /**
     * 催办时间
     */
    private Date urgeTime;

    /**
     * 被催办单位
     */
    private Long urgedUnit;

    /**
     * 被催办单位名称
     */
    private String urgedUnitName;

    /**
     * 催办内容
     */
    private String content;

    private static final long serialVersionUID = 1L;

	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;
	/**
	 * 事项附件
	 */
	@TableField(exist = false)
	private List<SupervisionFiles> supervisionFilesList;

}
