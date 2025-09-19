package org.springblade.plugin.rocketmq.feign;

import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.rocketmq.constants.TopicEnum;
import org.springblade.plugin.rocketmq.entity.MQSendResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mrtang
 * @version 1.0
 * @description: 生产者客户端 供声明式调用，发送消息
 * @date 2021/12/21 6:12 下午
 */
@FeignClient(
	value = AppNameConstant.APPLICATION_ROCKETMQ_NAME,
	fallback = IProducerClientFallback.class
)
public interface IProducerClient {

	String API_PREFIX = "/client";
	String SEND_MSG = API_PREFIX + "/send-msg";
	String SEND_MSG_TO_USER = API_PREFIX + "/send-msg-to-user";

	/**
	 * 测试demo
	 *
	 * @param msg
	 * @return
	 */
	@PostMapping(SEND_MSG)
	R<MQSendResult> sendMsg(@RequestParam("msg") String msg);

	/**
	 * 给用户发消息
	 *
	 * @param topic
	 * @param userId
	 * @param msg
	 * @return
	 */
	@PostMapping(SEND_MSG_TO_USER)
	R<MQSendResult> sendMsgToUser(@RequestParam("topic") String topic, @RequestParam("userId") String userId, @RequestParam("msg") String msg);
}
