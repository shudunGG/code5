package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * supervision_phase_remind
 * @author
 */
@Data
@TableName("supervision_phase_remind")
public class SupervisionPhaseRemind extends BaseEntity implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

    /**
     * 阶段ID
     */
    private Long phaseId;

    /**
     * 阶段名称j
     */
    private String phaseName;

    /**
     * 汇报时间
     */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;

    /**
     * 提前提醒时间
     */
    private Long reminderTime;

    private static final long serialVersionUID = 1L;

	/**
	 * 阶段编号
	 */
	private String phaseCode;

	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;


}
