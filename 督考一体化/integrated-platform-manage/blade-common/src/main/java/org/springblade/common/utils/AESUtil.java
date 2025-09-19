package org.springblade.common.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Random;

/**
 * Created by j.jia
 * Version 3.0.0
 * Edit Date 2024/05/11
 */
public class AESUtil {
    private AESUtil() {}

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String generateKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789"
                + "!@#$%^&*";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        while (builder.length() < 32) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * AES加密
     * @param key 秘钥
     * @param originalText 源文本
     * @return 加密结果
     */
    public static String encrypt(String key,String originalText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(originalText.getBytes(StandardCharsets.UTF_8));
            String enText = new BASE64Encoder().encode(encrypted);
            return enText.replaceAll(System.lineSeparator(),"");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * AES解密
     * @param key 秘钥
     * @param cipherText 密文
     * @return 文明文本
     */
    public static String decrypt(String key, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] encrypted = new BASE64Decoder().decodeBuffer(cipherText);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
