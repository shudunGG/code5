package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.vingsoft.support.mybatis.SM4EncryptHandler;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/14 14:34
* @Version:        1.0
*/
@Data
@TableName("supervision_phase_report_all")
public class SupervisionPhaseReportAll extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * id
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
	 * 下发人员id
	 */
	private String downUserId;
	/**
	 * 下发人员名称
	 */
	private String downUserName;
	/**
	 * 下发状态(0待下发1已下发2提交3通过4驳回)默认0
	 */
	private String downStatus;

	/**
	 * 联系人
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String linkedName ;
	/**
	 * 联系电话
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String linkedPhone ;

	/**
	 * 事项附件
	 */
	@TableField(exist = false)
	private List<SupervisionFiles> supervisionFilesList;

	/**
	 * 汇报送审信息
	 */
	@TableField(exist = false)
	private SupervisionSubmitAudit supervisionSubmitAudit;

	@TableField(exist = false)
	private String userId;
	@TableField(exist = false)
	private String sync;
	@TableField(exist = false)
	private String title;
}
