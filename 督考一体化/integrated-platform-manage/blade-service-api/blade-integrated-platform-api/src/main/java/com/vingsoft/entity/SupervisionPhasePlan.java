package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * supervision_phase_plan
 * @author
 */
@Data
@TableName("supervision_phase_plan")
public class SupervisionPhasePlan extends BaseEntity implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

    /**
     * 开始时间
     */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 截止时间
     */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 阶段名称
     */
    private String phaseName;

	/**
	 * 阶段编号
	 */
    private String phaseCode;


    /**
     * 更新部门
     */
    private String updateDept;

    /**
     * 阶段要求
     */
    private String requirement;

	/**
	 * 阶段状态
	 */
	private String phaseStatus;

    private static final long serialVersionUID = 1L;

	/**
	 * 阶段汇报提醒信息
	 */
	@TableField(exist = false)
	private List<SupervisionPhaseRemind> supervisionPhaseRemindList;

	/**
	 *  汇报信息
	 */
	@TableField(exist = false)
	private List<SupervisionPhaseReport> supervisionPhaseReportList;

	@TableField(exist = false)
	private  SupervisionPhaseReport supervisionPhaseReport;


	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;

	/**
	 * 是否当前阶段
	 */
	private String isEnable;

	/**
	 * 上报状态
	 */
	private String reportStatus;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 阶段排序
	 */
	private String sort;

	@TableField(exist = false)
	private Integer huizong;//汇总是否已分派
}
