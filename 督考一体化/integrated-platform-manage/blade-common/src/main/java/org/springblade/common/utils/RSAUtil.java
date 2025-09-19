package org.springblade.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by j.jia
 * Version 3.0.0
 * Edit Date 2024/05/11
 */
public class RSAUtil {
    private RSAUtil() {}

    /**
     * 加密
     * @param data 普通字符串
     * @return 返回密文
     */
    public static String encrypt(String key,String data){
        PublicKey publicKey = getPublicKey(key);
        byte[] bytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            bytes = cipher.doFinal(new BASE64Decoder().decodeBuffer(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  new BASE64Encoder().encode(bytes);
    }

    /**
     * 解密
     * @param data 待解密字符串
     * @return 返回明文字符串
     */
    public static String decrypt(String key,String data){
        PrivateKey privateKey = getPrivateKey(key);
        byte[] bytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            bytes = cipher.doFinal(new BASE64Decoder().decodeBuffer(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }

    private static PublicKey getPublicKey(String key){
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = new BASE64Decoder().decodeBuffer(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    private static PrivateKey getPrivateKey(String key){
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = new BASE64Decoder().decodeBuffer(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }
}
