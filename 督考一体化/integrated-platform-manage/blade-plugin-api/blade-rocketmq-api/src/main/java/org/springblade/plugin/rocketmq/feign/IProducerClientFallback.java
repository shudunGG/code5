package org.springblade.plugin.rocketmq.feign;

import org.springblade.core.tool.api.R;
import org.springblade.plugin.rocketmq.constants.TopicEnum;
import org.springblade.plugin.rocketmq.entity.MQSendResult;
import org.springframework.stereotype.Component;

/**
 * @author mrtang
 * @version 1.0
 * @description: feign失败配置
 * @date 2021/12/21 6:14 下午
 */
@Component
public class IProducerClientFallback implements IProducerClient {
	@Override
	public R<MQSendResult> sendMsg(String msg) {
		return R.fail("消息发送失败");
	}

	@Override
	public R<MQSendResult> sendMsgToUser(String topic, String userId, String msg) {
		return R.fail("用户消息发送失败");
	}
}
