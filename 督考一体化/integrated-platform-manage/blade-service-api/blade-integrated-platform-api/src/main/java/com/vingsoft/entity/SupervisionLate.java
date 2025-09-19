package com.vingsoft.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
* @Description:    事项申请延期表
* @Author:         WangRJ
* @CreateDate:     2022/5/20 22:56
* @Version:        1.0
*/
@Data
@ApiModel(value = "SupervisionLate对象", description = "SupervisionLate对象")
public class SupervisionLate extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 事项名称
	 */
	private String servName;
	/**
	 * 事项id
	 */
	private Long servId;
	/**
	 * 事项编码
	 */
	private String servCode;
	/**
	 * 延期天数
	 */
	private Integer lateDay;
	/**
	 * 延期状态
	 */
	private Integer lateStatus;
	/**
	 * 申请时间
	 */
	private Date lateDate;
	/**
	 * 延期说明
	 */
	private String lateContent;
	/**
	 * 申请单位名称
	 */
	private String lateDeptName;
	/**
	 * 申请单位id
	 */
	private Long lateDeptId;
	/**
	 * 申请人名称
	 */
	private String lateUserName;
	/**
	 * 申请人id
	 */
	private Long lateUserId;
	/**
	 * 接收人名称
	 */
	private String receiveName;
	/**
	 * 接收人id
	 */
	private Long receiveId;
	/**
	 * 退回原因
	 */
	private String backContent;
}
