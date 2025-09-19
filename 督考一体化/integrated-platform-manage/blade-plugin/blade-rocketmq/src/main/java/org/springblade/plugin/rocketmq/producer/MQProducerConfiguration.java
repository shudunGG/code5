package org.springblade.plugin.rocketmq.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.plugin.rocketmq.constants.RocketMQErrorEnum;
import org.springblade.plugin.rocketmq.exception.RocketMQException;
import org.springblade.plugin.rocketmq.properties.MQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.util.StringUtils;

/**
 * @author mrtang
 * @version 1.0
 * @description: 生产者配置
 * @date 2021/12/21 12:11 上午
 */
@Conditional(MQProducerCondition.class)
@SpringBootConfiguration
public class MQProducerConfiguration {
	public static final Logger LOGGER = LoggerFactory.getLogger(MQProducerConfiguration.class);

	@Autowired
	private MQProperties mqProperties;

	@Bean
	public DefaultMQProducer getRocketMQProducer() throws RocketMQException {
		if (StringUtils.isEmpty(mqProperties.getProducer().getGroupName())) {
			throw new RocketMQException(RocketMQErrorEnum.PARAMM_NULL,"groupName is blank",false);
		}
		if (StringUtils.isEmpty(mqProperties.getProducer().getNamesrvAddr())) {
			throw new RocketMQException(RocketMQErrorEnum.PARAMM_NULL,"nameServerAddr is blank",false);
		}
		DefaultMQProducer producer;
		producer = new DefaultMQProducer(mqProperties.getProducer().getGroupName());
		producer.setNamesrvAddr(mqProperties.getProducer().getNamesrvAddr());
		//如果需要同一个jvm中不同的producer往不同的mq集群发送消息，需要设置不同的instanceName
		//producer.setInstanceName(instanceName);
		if(mqProperties.getProducer().getMaxMessageSize()!=null){
			producer.setMaxMessageSize(mqProperties.getProducer().getMaxMessageSize());
		}
		if(mqProperties.getProducer().getSendMsgTimeout()!=null){
			producer.setSendMsgTimeout(mqProperties.getProducer().getSendMsgTimeout());
		}
		//如果发送消息失败，设置重试次数，默认为2次
		if(mqProperties.getProducer().getRetryTimesWhenSendFailed()!=null){
			producer.setRetryTimesWhenSendFailed(mqProperties.getProducer().getRetryTimesWhenSendFailed());
		}

		try {
			producer.start();

			LOGGER.info(String.format("producer is start ! groupName:[%s],namesrvAddr:[%s]"
				, mqProperties.getProducer().getGroupName(), mqProperties.getProducer().getNamesrvAddr()));
		} catch (MQClientException e) {
			LOGGER.error(String.format("producer is error {}"
				, e.getMessage(),e));
			throw new RocketMQException(e);
		}
		return producer;
	}
}
