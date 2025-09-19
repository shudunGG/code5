package org.springblade.integrated.platform.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.vingsoft.entity.Submit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springblade.common.constant.HttpUrlConstant;
import org.springblade.core.http.HttpRequest;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.SmsDockingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @className: SmsDockingServiceImpl
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/5/16 10:21 星期二
 * @Version 1.0
 **/
@Slf4j
@Service
public class SmsDockingServiceImpl implements SmsDockingService {

	@Resource
	private HttpUrlConstant httpUrlConstant;

	@Override
	public R send(String mobiles, String content) {

		String url = httpUrlConstant.getUrl();

		String ecName = httpUrlConstant.getEcName();
		String apId = httpUrlConstant.getApId();
		String secretKey = httpUrlConstant.getSecretKey();
		log.info("发送短信开始url:"+url);
		log.info("发送短信开始ecName:"+ecName);
		log.info("发送短信开始apId:"+apId);
		log.info("发送短信开始secretKey:"+secretKey);
		if(StringUtil.isEmpty(mobiles)){
			return R.fail("手机号码不能为空");
		}
		if(StringUtil.isEmpty(content)){
			return R.fail("短信内容不能为空");
		}
		log.info("发送短信开始mobiles:"+mobiles);
		log.info("发送短信开始content:"+content);
		String sign = httpUrlConstant.getSign();
		String addSerial = "";
		String mac = getMacInfo(ecName,apId,secretKey,mobiles,content,sign,addSerial);
		log.info("32位小写MD5加密后："+mac);

		Submit submit = new Submit();
		submit.setEcName(ecName);
		submit.setApId(apId);
		submit.setSecretKey(secretKey);
		submit.setMobiles(mobiles);
		submit.setContent(content);
		submit.setSign(sign);
		submit.setAddSerial(addSerial);
		submit.setMac(mac);

		String param = JSON.toJSONString(submit);
		//Base64加密
		String encode = Base64.encodeBase64String(param.getBytes());
		log.info("encode:"+encode);

		JsonNode data = HttpRequest.post(url).bodyJson(encode)
			.addHeader("Content-Type", "application/json;charset=utf-8")
			.execute().onSuccess(responseSpec -> responseSpec.asJsonNode());
		log.info("发送短信返回结果data:"+data);

		return R.data(data);
	}

	/**
	 * 参数校验序列，生成方法：将ecName、apId、secretKey、mobiles、content、sign、addSerial按序拼接（无间隔符），通过MD5（32位小写）计算得出值。
	 */
	public String getMacInfo(String ecName, String apId, String secretKey, String mobiles, String content, String sign, String addSerial){

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(ecName);
		stringBuffer.append(apId);
		stringBuffer.append(secretKey);
		stringBuffer.append(mobiles);
		stringBuffer.append(content);
		stringBuffer.append(sign);
		stringBuffer.append(addSerial);

		String selfMac = md5(stringBuffer.toString());

		return selfMac;
	}

	private String md5(String value){
		String result = null;
		MessageDigest md5 = null;
		try{
			md5 = MessageDigest.getInstance("MD5");
			md5.update((value).getBytes("UTF-8"));
		}catch (NoSuchAlgorithmException error){
			error.printStackTrace();
		}catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		byte b[] = md5.digest();
		int i;
		StringBuffer buf = new StringBuffer("");

		for(int offset=0; offset<b.length; offset++){
			i = b[offset];
			if(i<0){
				i+=256;
			}
			if(i<16){
				buf.append("0");
			}
			buf.append(Integer.toHexString(i));
		}

		result = buf.toString();
		return result;
	}
}
