package org.springblade.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 第三方接口
 * 实现配置文件修改，自动刷新功能
 */
@Component
@RefreshScope
public class HttpUrlConstant {

	//短信对接url
	String url;

	//短信对接ecName
	String ecName;

	//短信对接apId
	String apId;

	//短信对接secretKey
	String secretKey;

	//短信对接sign
	String sign;



	public String getUrl() {
		return url;
	}
	@Value("${http_url.url:}")
	public void setUrl(String url) {
		this.url = url;
	}

	public String getEcName() {
		return ecName;
	}
	@Value("${http_url.ecName:}")
	public void setEcName(String ecName) {
		this.ecName = ecName;
	}

	public String getApId() {
		return apId;
	}
	@Value("${http_url.apId:}")
	public void setApId(String apId) {
		this.apId = apId;
	}

	public String getSecretKey() {
		return secretKey;
	}
	@Value("${http_url.secretKey:}")
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getSign() {
		return sign;
	}
	@Value("${http_url.sign:}")
	public void setSign(String sign) {
		this.sign = sign;
	}

}
