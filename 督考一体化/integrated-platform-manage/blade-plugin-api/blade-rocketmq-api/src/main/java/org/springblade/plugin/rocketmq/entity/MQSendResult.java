package org.springblade.plugin.rocketmq.entity;


import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 返回信息
 * @date 2021/12/21 9:16 上午
 */
public class MQSendResult implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 是否发送成功
	 */
	private boolean isSendSuccess;
	/**
	 * 错误信息
	 */
	private String errMsg;
	/**
	 * 错误堆栈信息
	 */
	private Throwable e;
	/**
	 * 消息ID
	 */
	private String msgId;
	/**
	 * sendResult对象
	 */
	private SendResult sendResult;

	public MQSendResult() {
		super();
		this.isSendSuccess = false;
	}

	public MQSendResult(SendResult sendResult) {
		super();
		this.sendResult = sendResult;
		//判断是否发送成功
		if (sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK) {
			//失败
			this.isSendSuccess=false;
			this.errMsg="发送失败";
		}else{
			//成功
			this.isSendSuccess=true;
			this.msgId = sendResult.getMsgId();
		}
	}

	public MQSendResult(String errMsg, Throwable e) {
		super();
		this.errMsg = errMsg;
		this.e = e;
		this.isSendSuccess = false;
	}
	public boolean isSendSuccess() {
		return isSendSuccess;
	}
	public void setSendSuccess(boolean isSendSuccess) {
		this.isSendSuccess = isSendSuccess;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	public Throwable getE() {
		return e;
	}
	public void setE(Throwable e) {
		this.e = e;
	}
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public SendResult getSendResult() {
		return sendResult;
	}
	public void setSendResult(SendResult sendResult) {
		this.sendResult = sendResult;
	}

	@Override
	public String toString() {
		return "RocketMQSendResult [isSendSuccess=" + isSendSuccess + ", errMsg=" + errMsg + ", e=" + e + ", msgId="
			+ msgId + ", sendResult=" + sendResult + "]";
	}

}
