package org.springblade.gateway.utils;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/13 15:29
 */
public class EncryptAesUtil {
	private static Logger logger = LoggerFactory.getLogger(EncryptAesUtil.class);
	/**
	 * 密钥 AES加解密要求key必须要128个比特位（这里需要长度为16，否则会报错）
	 */
	private static final String KEY = "20020101@victory";

	public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

	/**
	 * key的长度
	 */
	private static final Integer KEY_LENGTH = 32;

	/**
	 * 算法
	 */
	private static final String ALGORITHMS = "AES/ECB/PKCS5Padding";
	/**
	 * 静态常量
	 */
	private static final String AES = "AES";

	/**
	 * 无效
	 */
	public static final String INVALID_KEY = "invalid";

	/**
	 * 有效
	 */
	public static final String VALID_KEY = "valid";

	/**
	 * redis key
	 */
	public static final String SYS_VALID_KEY = "sys:valid";

	/**
	 * 将base 64 code 【AES解密】为字符串
	 *
	 * @param key
	 * @param encryptStr 待解密的base 64 code
	 * @return 解密后的String
	 */
	public static String aesDecrypt(String key, String encryptStr) {
		try {
			if(StringUtils.isEmpty(key) || StringUtils.isEmpty(encryptStr) || key.length()!=KEY_LENGTH){
				return null;
			}
			// 将字符串转为byte，返回解码后的byte[]
			byte[] encryptBytes = new BASE64Decoder().decodeBuffer(encryptStr);

			// 初始化为解密模式的密码器
			Cipher cipher = Cipher.getInstance(ALGORITHMS);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), EncryptAesUtil.AES));
			byte[] decryptBytes = cipher.doFinal(encryptBytes);

			return new String(decryptBytes);
		} catch (Exception e) {
			logger.error(e.getMessage() + e);
		}
		return null;
	}

	/**
	 * AES 加密操作
	 *
	 * @param content 待加密内容
	 * @param key     加密密钥
	 * @return 返回Base64转码后的加密数据
	 */
	public static String encrypt(String content, String key) {
		try {
			// 创建密码器
			Cipher cipher = Cipher.getInstance(ALGORITHMS);
			byte[] byteContent = content.getBytes("utf-8");
			// 初始化为加密模式的密码器
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), EncryptAesUtil.AES));
			// 加密
			byte[] result = cipher.doFinal(byteContent);
			//通过Base64转码返回
			return new BASE64Encoder().encode(result);
		} catch (Exception ex) {
			logger.error("加密失败", ex);
		}

		return null;
	}

	/**
	 * Base64解密
	 *
	 * @param content    文本内容
	 * @param aesTextKey 文本密钥
	 * @return {String}
	 */
	@Nullable
	public static String decryptFormBase64ToString(@Nullable String content, String aesTextKey) {
		byte[] hexBytes = decryptFormBase64(content, aesTextKey);
		if (hexBytes == null) {
			return null;
		}
		return new String(hexBytes, DEFAULT_CHARSET);
	}

	/**
	 * Base64解密
	 *
	 * @param content    文本内容
	 * @param aesTextKey 文本密钥
	 * @return byte[]
	 */
	@Nullable
	public static byte[] decryptFormBase64(@Nullable String content, String aesTextKey) {
		if (StringUtils.isBlank(content)) {
			return null;
		}
		return decryptFormBase64(content.getBytes(DEFAULT_CHARSET), aesTextKey);
	}

	/**
	 * Base64解密
	 *
	 * @param content    内容
	 * @param aesTextKey 文本密钥
	 * @return byte[]
	 */
	public static byte[] decryptFormBase64(byte[] content, String aesTextKey) {
		return decrypt(Base64Util.decode(content), aesTextKey);
	}

	/**
	 * 解密
	 *
	 * @param content    内容
	 * @param aesTextKey 文本密钥
	 * @return byte[]
	 */
	public static byte[] decrypt(byte[] content, String aesTextKey) {
		return decrypt(content, Objects.requireNonNull(aesTextKey).getBytes(DEFAULT_CHARSET));
	}

	/**
	 * 加密
	 *
	 * @param encrypted 内容
	 * @param aesKey    密钥
	 * @return byte[]
	 */
	public static byte[] decrypt(byte[] encrypted, byte[] aesKey) {
		return Pkcs7Encoder.decode(aes(encrypted, aesKey, Cipher.DECRYPT_MODE));
	}

	/**
	 * ase加密
	 *
	 * @param encrypted 内容
	 * @param aesKey    密钥
	 * @param mode      模式
	 * @return byte[]
	 */
	private static byte[] aes(byte[] encrypted, byte[] aesKey, int mode) {
		Assert.isTrue(aesKey.length == 32, "IllegalAesKey, aesKey's length must be 32");
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
			cipher.init(mode, keySpec, iv);
			return cipher.doFinal(encrypted);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * 提供基于PKCS7算法的加解密接口.
	 */
	private static class Pkcs7Encoder {
		private static final int BLOCK_SIZE = 32;

		private static byte[] encode(byte[] src) {
			int count = src.length;
			// 计算需要填充的位数
			int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
			// 获得补位所用的字符
			byte pad = (byte) (amountToPad & 0xFF);
			byte[] pads = new byte[amountToPad];
			for (int index = 0; index < amountToPad; index++) {
				pads[index] = pad;
			}
			int length = count + amountToPad;
			byte[] dest = new byte[length];
			System.arraycopy(src, 0, dest, 0, count);
			System.arraycopy(pads, 0, dest, count, amountToPad);
			return dest;
		}

		private static byte[] decode(byte[] decrypted) {
			int pad = decrypted[decrypted.length - 1];
			if (pad < 1 || pad > BLOCK_SIZE) {
				pad = 0;
			}
			if (pad > 0) {
				return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
			}
			return decrypted;
		}
	}
}
