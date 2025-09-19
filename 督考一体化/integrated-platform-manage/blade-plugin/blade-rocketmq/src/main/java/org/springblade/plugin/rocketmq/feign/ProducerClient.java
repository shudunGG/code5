package org.springblade.plugin.rocketmq.feign;

import lombok.AllArgsConstructor;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.rocketmq.constants.TagConstants;
import org.springblade.plugin.rocketmq.constants.TopicEnum;
import org.springblade.plugin.rocketmq.entity.MQSendResult;
import org.springblade.plugin.rocketmq.producer.processor.MQProducerSendMsgProcessor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mrtang
 * @version 1.0
 * @description: 生产者feign实现
 * @date 2021/12/21 6:19 下午
 */
@RestController
@AllArgsConstructor
public class ProducerClient implements IProducerClient {

	private MQProducerSendMsgProcessor mqProducerSendMsgProcessor;

	private DefaultMQProducer defaultMQProducer;

	@Override
	@PostMapping(SEND_MSG)
	public R<MQSendResult> sendMsg(String msg) {
		return R.data(this.mqProducerSendMsgProcessor.send(TopicEnum.Demo, TagConstants.Demo.msg, msg));
	}

	@Override
	@PostMapping(SEND_MSG_TO_USER)
	public R<MQSendResult> sendMsgToUser(String topic, String userId, String msg) {
		try {
			Message sendMsg = new Message(topic,userId, null,msg.getBytes());
			SendResult sendResult = defaultMQProducer.send(sendMsg);
			MQSendResult mqSendResult = new MQSendResult(sendResult);
			return R.data(mqSendResult);
		} catch (MQClientException e) {
			e.printStackTrace();
		} catch (RemotingException e) {
			e.printStackTrace();
		} catch (MQBrokerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return R.fail("消息发送失败...");
	}


}
