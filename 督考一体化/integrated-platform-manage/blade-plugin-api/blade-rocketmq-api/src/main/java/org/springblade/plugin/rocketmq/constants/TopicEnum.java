package org.springblade.plugin.rocketmq.constants;

/**
 * 定义消息队列主题<br/>
 * 注意：MQ消息队列主题，每个主题的子主题，请到tags下面定义
 *
 * @author mrtang
 * @version 1.0
 * @date 2021/9/14 9:33 下午
 */

public enum TopicEnum {
	Demo("DEMO", "测试消息"),
	;

	private String code;
	private String msg;

	private TopicEnum(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public String getCode() {
		return this.code;
	}

	public String getMsg() {
		return this.msg;
	}
}
