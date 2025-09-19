package org.springblade.common.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;

/**
 * Created by j.jia
 * Version 3.0.0
 * Edit Date 2024/05/11
 */
public class SM4Util {
    private SM4Util() {}

    public static String generateKey() {
        return RandomUtil.randomString(16);
    }

    /**
     * SM4加密
     * @param key 秘钥
     * @param originalText 源文本
     * @return 加密结果
     */
    public static String encrypt(String key,String originalText) {
        SM4 sm4 = SmUtil.sm4(key.getBytes());
        return sm4.encryptBase64(originalText);
    }

    /**
     * SM4解密
     * @param key 秘钥
     * @param cipherText 密文
     * @return 文明文本
     */
    public static String decrypt(String key, String cipherText) {
        SM4 sm4 = SmUtil.sm4(key.getBytes());
        return sm4.decryptStr(cipherText);
    }
}
