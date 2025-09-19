package org.springblade.plugin.rocketmq.properties;

import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: 生产者配置类
 * @date 2021/12/21 4:26 下午
 */
@Data
public class ProducerProperties {
	/**
	 * 发送同一类消息的设置为同一个group，保证唯一,默认不需要设置，rocketmq会使用ip@pid(pid代表jvm名字)作为唯一标示
	 */
	private String groupName;
	/**
	 * mq的nameserver地址
	 */
	private String namesrvAddr;
	/**
	 * 消息最大长度 默认1024*4(4M)
	 */
	private Integer maxMessageSize;
	/**
	 * 发送消息超时时间,默认3000
	 */
	private Integer sendMsgTimeout;
	/**
	 * 发送消息失败重试次数，默认2
	 */
	private Integer retryTimesWhenSendFailed;
}
