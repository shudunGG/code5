package org.springblade.plugin.rocketmq.producer.processor;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.plugin.rocketmq.constants.RocketMQErrorEnum;
import org.springblade.plugin.rocketmq.constants.TopicEnum;
import org.springblade.plugin.rocketmq.entity.MQSendResult;
import org.springblade.plugin.rocketmq.exception.AppException;
import org.springblade.plugin.rocketmq.producer.MQProducerCondition;
import org.springblade.plugin.rocketmq.vo.BaseMQMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author mrtang
 * @version 1.0
 * @description: 生产者发送消息处理器
 * @date 2021/12/21 12:17 上午
 */
@Conditional(MQProducerCondition.class)
@Component
public class MQProducerSendMsgProcessor {
	private static final Logger logger = LoggerFactory.getLogger(MQProducerSendMsgProcessor.class);

	@Autowired
	private DefaultMQProducer defaultMQProducer;

	/**
	 * 发送消息,仅发送一次，不关心是否发送成功
	 * @param topic	主题
	 * @param tag 	消息标签，只支持设置一个Tag（服务端消息过滤使用）
	 * @param keys 	消息关键词，多个Key用MessageConst.KEY_SEPARATOR隔开（查询消息使用）
	 * @param msg	消息
	 */
	public void sendOneway(TopicEnum topic, String tag, String keys, BaseMQMessageVO msg){
		this.sendOneway(topic, tag, keys, JSON.toJSONString(msg));
	}

	/**
	 * 发送消息,仅发送一次，不关心是否发送成功
	 * @param topic	主题
	 * @param tag 	消息标签，只支持设置一个Tag（服务端消息过滤使用）
	 * @param keys 	消息关键词，多个Key用MessageConst.KEY_SEPARATOR隔开（查询消息使用）
	 * @param msg	消息
	 */
	public void sendOneway(TopicEnum topic, String tag, String keys, String msg){
		logger.info(String.format("发送信息到消息队列(只发送一次，不关心是否成功)。topic:%s,tag:%s,keys:%s,msg:%s",topic==null?"":topic.getCode()+"["+topic.getMsg()+"]",
			tag==null?"":tag,keys.toString(),msg));
		try {
			validateSendMsg(topic, tag, msg);
			Message sendMsg = new Message(topic.getCode(),tag==null?null:tag, StringUtils.isEmpty(keys)?null:keys,msg.getBytes());
			//默认3秒超时
			defaultMQProducer.sendOneway(sendMsg);
		} catch (MQClientException | RemotingException | InterruptedException e) {
			logger.error("消息发送失败",e);
		}
	}

	/**
	 * 同步发送消息
	 * @param topic	主题
	 * @param tag 	消息标签，只支持设置一个Tag（服务端消息过滤使用）
	 * @param msg	消息
	 * @return
	 */
	public MQSendResult send(TopicEnum topic, String tag, String msg){
		return this.send(topic, tag, null, msg);
	}

	/**
	 * 同步发送消息
	 * @param topic	主题
	 * @param tag 	消息标签，只支持设置一个Tag（服务端消息过滤使用）
	 * @param keys 	消息关键词，多个Key用MessageConst.KEY_SEPARATOR隔开（查询消息使用）
	 * @param msg	消息
	 * @return
	 */
	public MQSendResult send(TopicEnum topic,String tag,String keys,String msg){
		logger.info(String.format("发送信息到消息队列。topic:%s,tag:%s,keys:%s,msg:%s",topic==null?"":topic.getCode()+"["+topic.getMsg()+"]",
			tag==null?"":tag,keys,msg));
		MQSendResult mqSendResult = null;
		try {
			validateSendMsg(topic, tag, msg);
			SendResult sendResult = null;
			Message sendMsg = new Message(topic.getCode(),tag==null?null:tag,StringUtils.isEmpty(keys)?null:keys,msg.getBytes());
			//默认3秒超时
			sendResult = defaultMQProducer.send(sendMsg);
			mqSendResult = new MQSendResult(sendResult);
		}catch(AppException e){
			logger.error(e.getErrMsg());
			mqSendResult = new MQSendResult(e.getErrMsg(), null);
		} catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
			logger.error("消息发送失败",e);
			mqSendResult = new MQSendResult("消息发送失败", e);
		}
		logger.info("发送消息到消息队列的响应信息为："+mqSendResult.toString());
		return mqSendResult==null?new MQSendResult():mqSendResult;
	}
	/**
	 * 校验参数
	 * @param topic
	 * @param tag
	 * @param msg
	 */
	private void validateSendMsg(TopicEnum topic, String tag, String msg) {
		if(topic==null){
			throw new AppException(RocketMQErrorEnum.PARAMM_NULL,"topic为空",false);
		}
		if(tag == null){
			throw new AppException(RocketMQErrorEnum.PARAMM_NULL,"tag为空",false);
		}
		if(msg==null){
			throw new AppException(RocketMQErrorEnum.PARAMM_NULL,"msg为空",false);
		}
	}
}
