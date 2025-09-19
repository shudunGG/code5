package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 项目阶段信息实体类
 * @author AdamJin 2022-4-9 17:30:50
 */
@Data
@TableName("project_phase_plan")
public class ProjectPhasePlan  extends BaseEntity{

    /**
     * 项目id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long projId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 截止时间
     */
    private Date endTime;

    /**
     * 阶段名称
     */
    private String phaseName;

    /**
     * 阶段要求
     */
    private String requirement;

	/**
	 * 阶段月份
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private int planMonth;

	/**
	 * 阶段提醒信息
	 */
	@TableField(exist = false)
	private List<ProjectPhaseRemind> projectPhaseRemindList;

	/**
	 * 阶段汇报信息
	 */
	@TableField(exist = false)
	private ProjectPhaseReport projectPhaseReport;

	/**
	 * 评价状态
	 */
	@TableField(exist = false)
	private String appraiseStatus; //Y 已评价 N 未评价

	/**
	 * 评价信息id
	 */
	@TableField(exist = false)
	private Long appraiseId;

}
