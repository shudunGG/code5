package com.om.bo.message.resp;

/**
 * 文本消息
 * 
 * @author lxj
 * @date 2014-10-23
 */
public class TextMessage extends BaseMessage {
	// 回复的消息内容
	private String Content;

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}
}