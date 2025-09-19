package org.springblade.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by j.jia
 * Version 3.0.0
 * Edit Date 2024/05/11
 */
public class VSTool {
	private VSTool() {}
	public static final int USA = 0;
	public static final int CHN = 1;
	/**
	 * 解密前端数据
	 * @param privateKey 私钥
	 * @param data 密文
	 * @return Map<String,Object> 结果信息
	 */
	public static Map<String,Object> decrypt(String privateKey, String data) {
		return decrypt(privateKey, data, USA);
	}

	/**
	 * 解密前端数据
	 * @param privateKey 私钥
	 * @param data 密文
	 * @param algorithm 算法
	 * @return Map<String,Object> 结果信息
	 */
	public static Map<String,Object> decrypt(String privateKey, String data, int algorithm) {
		Map<String,Object> map = new HashMap<>(16);
		byte[] bytes = new byte[0];
		try {
			bytes = new BASE64Decoder().decodeBuffer(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = new String(bytes);
		String[] es = data.split("\\|");
		String key = "";
		String extra = "";
		switch (algorithm) {
			case USA:
				key = RSAUtil.decrypt(privateKey,es[0]);
				extra = AESUtil.decrypt(key, es[1]);
				map.put("sign",key);
				break;
			case CHN:
				key = SM2Util.decrypt(privateKey,"04" + es[0]);
				String sign = key.substring(0,16);
				extra = SM4Util.decrypt(sign, es[1]);
				map.put("sign",sign);
				break;
		}
		map.put("extra",extra);

		char[] pts = key.toCharArray();
		if(pts.length != 32) {
			map.put("time","-1");
			return map;
		}
		int[] location = {4,6,12,15,19,20,24,26,28,30};
		StringBuilder builder = new StringBuilder();
		for (int value : location) {
			builder.append(pts[value]);
		}
		String bstr = builder.toString();
		if(bstr.matches("[0-9]+")) {
			long systime = new Date().getTime() / 1000;
			long time = Long.parseLong(builder.toString());
			map.put("time",Math.abs(systime - time));
		}else {
			map.put("time","-1");
		}
		return map;
	}

	/**
	 * 加密数据
	 * @param sign 秘钥
	 * @param data 待加密字符串
	 * @return String 密文
	 */
	public static String encrypt(String sign, String data) {
		return encrypt(sign, data, USA);
	}

	/**
	 * 加密数据
	 * @param sign 秘钥
	 * @param data 待加密字符串
	 * @param algorithm 算法
	 * @return String 密文
	 */
	public static String encrypt(String sign, String data, int algorithm) {
		String text = null;
		switch (algorithm) {
			case USA:
				text = AESUtil.encrypt(sign,data);
				break;
			case CHN:
				text = SM4Util.encrypt(sign,data);
				break;
		}
		if(text != null) {
			byte[] bytes = text.getBytes();
			String base64 = new BASE64Encoder().encode(bytes);
			text = base64.replaceAll(System.lineSeparator(),"");
		}
		return text;
	}

	public static class SM2 {
		public static String encrypt(String key, String data) { return SM2Util.encrypt(key,data); }
		public static String decrypt(String key, String data) { return SM2Util.decrypt(key,data); }
	}

	public static class SM4 {
		public static String generateKey() { return SM4Util.generateKey(); }
		public static String encrypt(String key, String data) { return SM4Util.encrypt(key,data); }
		public static String decrypt(String key, String data) { return SM4Util.decrypt(key,data); }
	}

	public static class RSA {
		public static String encrypt(String key, String data) { return RSAUtil.encrypt(key,data); }
		public static String decrypt(String key, String data) { return RSAUtil.decrypt(key,data); }
	}

	public static class AES {
		public static String generateKey() { return AESUtil.generateKey(); }
		public static String encrypt(String key, String data) { return AESUtil.encrypt(key,data); }
		public static String decrypt(String key, String data) { return AESUtil.decrypt(key,data); }
	}
}
