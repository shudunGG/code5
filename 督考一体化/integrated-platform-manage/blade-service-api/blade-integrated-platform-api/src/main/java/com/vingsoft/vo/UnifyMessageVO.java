package com.vingsoft.vo;

import com.vingsoft.entity.UnifyMessage;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/24 21:49
 */
@Data
public class UnifyMessageVO extends UnifyMessage {
	/**
	 * 消息状态：0未读	1已读
	 */
	private String msgStatusName;

	/**
	 * 消息类型，属于哪个模块：evaluation 考核评价
	 */
	private String msgTypeName;

	/**
	 * 接收人，多个逗号隔开
	 */
	private String receiveUserName;
}
