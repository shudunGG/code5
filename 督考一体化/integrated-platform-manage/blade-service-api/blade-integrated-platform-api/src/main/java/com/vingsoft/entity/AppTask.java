package com.vingsoft.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
* @Description:    交办信息类
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:00
* @Version:        1.0
*/
@Data
@ApiModel(value = "AppTask对象", description = "交办信息表")
public class AppTask extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/**
	 * 事项名称
	 */
	private String servName;
	/**
	 * 相关要求
	 */
	private String content;
	/**
	 * 发送人id
	 */
	private Long senderId;
	/**
	 * 发送人名称
	 */
	private String senderName;
	/**
	 * 接收人ID
	 */
	private String receiveId;
	/**
	 * 接收人名称
	 */
	private String receiveName;
	/**
	 * 发送时间
	 */
	private Date sendTime;
}
