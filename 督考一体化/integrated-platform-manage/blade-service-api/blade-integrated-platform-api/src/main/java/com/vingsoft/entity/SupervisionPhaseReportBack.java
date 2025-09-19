package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vingsoft.support.mybatis.SM4EncryptHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * supervision_phase_report_back
 * @author
 */
@Data
@TableName("supervision_phase_report_back")
@EqualsAndHashCode(callSuper = true)
public class SupervisionPhaseReportBack extends BaseEntity {

	private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

	/**
	 * 事项名称
	 */
	@TableField(exist = false)
	private String servName;

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
     * 汇报单位
     */
    private String reportDept;

    /**
     * 汇报单位名称
     */
    private String reportDeptName;

    /**
     * 汇报人员
     */
    private String reportUser;

    /**
     * 汇报人员名称
     */
    private String reportUserName;

    /**
     * 进展状态
     */
    private String progressStatus;

    /**
     * 汇报时间
     */
    private Date reportTime;

	/**
	 * 计划汇报时间
	 */
	private Date remindReportTime;

	/**
	 * 完成情况
	 */
	private String situation;

	/**
	 * 未完成原因
	 */
	private String unfinishedRemark;

	/**
	 * 整改措施
	 */
	private String rectificationMeasures;

	/**
	 * 汇报留言内容
	 */
	private String reportMessage;
	/**
	 * 汇报状态(1.责任单位汇报2.牵头单位汇总)
	 */
	private String reportStatus;
	/**
	 * 联系人
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String linkedName;
	/**
	 * 联系电话
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String linkedPhone;
	/**
	 * 监管单位
	 */
	private String jgDept;
	/**
	 * 是否已读
	 */
	private String isSee;

}
