package org.springblade.plugin.rocketmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * rocketmq队列配置
 *
 * @author mrtang
 * @version 1.0
 * @date 2021/9/14 10:13 下午
 */
@Data
@Component
@ConfigurationProperties(prefix = "mq")
@RefreshScope
public class MQProperties {

	/**
	 * 生产者配置
	 */
	private ProducerProperties producer = new ProducerProperties();
}
