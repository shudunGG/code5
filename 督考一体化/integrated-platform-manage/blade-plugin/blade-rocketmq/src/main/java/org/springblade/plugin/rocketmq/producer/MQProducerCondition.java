package org.springblade.plugin.rocketmq.producer;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author mrtang
 * @version 1.0
 * @description: 判断是否需要开启消息生产者配置
 * @date 2021/12/21 12:10 上午
 */
public class MQProducerCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		//判断当前环境开关是否开启
		Boolean isEnable = Boolean.valueOf(context.getEnvironment().getProperty("rocketmq.producer.isEnable"));
		return isEnable;
	}
}
