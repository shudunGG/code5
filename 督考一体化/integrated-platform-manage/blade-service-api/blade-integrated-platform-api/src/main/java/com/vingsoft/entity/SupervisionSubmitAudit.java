package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mrtang
 * @version 1.0
 * @description: 立项送审
 * @date 2022/4/18 17:11
 */
@Data
@TableName("supervision_submit_audit")
public class SupervisionSubmitAudit implements Serializable {
	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "主键")
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	@ApiModelProperty("创建人")
	private Long createUser;
	@DateTimeFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@JsonFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@ApiModelProperty("创建时间")
	private Date createTime;

	/**
	 * 事项id
	 */
	private Long servId;

	/**
	 * 汇报id
	 */
	private Long reportId;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 接收人
	 */
	private Long userId;

	/**
	 * 审核状态：0待审核；1通过；2不通过；3冻结，上一个人审核通过后转为0待审核状态
	 */
	private Integer status;

	/**
	 * 同步还是异步 1同步；0异步
	 */
	private Integer sync;

	/**
	 * 审核顺序，异步下按顺序审核
	 */
	private Integer sort;

	/**
	 * 审核消息
	 */
	private String msg;

	/**
	 * 批次号，区分是哪一批送审的
	 */
	private String batchNumber;

	/**
	 * 附件地址
	 */
	private String fileUrl;

	/**
	 * 附件名称
	 */
	private String fileName;

	/**
	 * 业务类型 info——督察督办；plan——上报计划；report——项目汇报；reportChi——项目下发汇报；reportAll——项目汇总；ware——项目入库; apprise——考核汇报；appriseScore——考核汇报改分申请
	 */
	private String operationType;


	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 审批人
	 */
	private Long approvalUser;

	/**
	 * 审批时间
	 */
	private Date approvalTime;
}
