package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mrtang
 * @version 1.0
 * @description: 统一消息
 * @date 2022/4/24 15:33
 */
@Data
@TableName("unify_message")
public class UnifyMessage extends BaseEntity {

	/**
	 * 消息主键
	 */
	private Long msgId;

	/**
	 * 消息标题
	 */
	private String msgTitle;

	/**
	 * 消息类型，属于哪个模块
	 */
	private String msgType;

	/**
	 * 二级消息类型，属于哪个模块
	 */
	private String twoLevelType;

	/**
	 * 消息状态：0未读	1已读
	 */
	private Integer msgStatus;

	/**
	 * 平台：app or web
	 */
	private String msgPlatform;

	/**
	 * 消息简介
	 */
	private String msgIntro;

	/**
	 * 消息分项
	 */
	private String msgSubitem;

	/**
	 * 接收人，多个逗号隔开
	 */
	private String receiveUser;
}
