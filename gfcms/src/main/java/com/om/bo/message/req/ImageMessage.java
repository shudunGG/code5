package com.om.bo.message.req;

/**
 * 图片消息
 * 
 * @author lxj
 * @date 2014-10-23
 */
public class ImageMessage extends BaseMessage {
	// 图片链接
	private String PicUrl;

	public String getPicUrl() {
		return PicUrl;
	}

	public void setPicUrl(String picUrl) {
		PicUrl = picUrl;
	}
}

