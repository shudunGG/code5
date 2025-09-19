package com.om.common.util;

import com.om.bo.base.EncryptDES;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密算法：DES AES DESede
 * 加密模式：ECB CBC CTR OFB CFB
 * 填充方式：PKCS5Padding PKCS7Padding
 */
public class EncryptUtils {

    /**
     * 通用加密方法
     *
     * @param type 加密算法
     * @param encryptString 加密算法/加密模式/填充方式
     * @param encryptKey 密钥
     * @param content 明文
     * @return 密文
     */
    public static String encrypt(EncryptDES type, String encryptString, String encryptKey, String content) {
        try {
            //EncryptType type = EncryptType.getEncryptType(encryptType);

            KeyGenerator kgen = KeyGenerator.getInstance(type.getDesc());
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(encryptKey.getBytes());
            kgen.init(type.getSize(), secureRandom);
            SecretKey secretKey = kgen.generateKey();
            // 生成实例(加解密算法/工作方式/填充方式)
            Cipher cipher = Cipher.getInstance(encryptString);
            // (模式(加密模式)，转换的key)
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), type.getDesc()));
            // 加密
            byte[] bytes = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通用加密方法
     *
     * @param type 加密算法
     * @param encryptString 加密算法/加密模式/填充方式
     * @param encryptKey 密钥
     * @param content 密文
     * @return 明文
     */
    public static String decrypt(EncryptDES type, String encryptString, String encryptKey, String content) {
        try {
            //EncryptType type = EncryptType.getEncryptType(encryptType);

            KeyGenerator kgen = KeyGenerator.getInstance(type.getDesc());
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(encryptKey.getBytes());
            kgen.init(type.getSize(), secureRandom);
            SecretKey secretKey = kgen.generateKey();
            // 生成实例(加解密算法/工作方式/填充方式)
            Cipher cipher = Cipher.getInstance(encryptString);
            // (模式(解密模式)，转换的key)
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), type.getDesc()));
            // 解密
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content));
            return new String(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String makeLicense(String mac,String validDay){
        EncryptDES aes=new EncryptDES(2,"AES",128);

        String encryptKey = "91620103MA71XXLG59/LXJMAKE";
        String content = mac+"|"+validDay;

        String encryptString = "AES/ECB/PKCS5Padding";
        String encode = EncryptUtils.encrypt(aes, encryptString, encryptKey, content);
        System.out.println(encode);
       /* decode = EncryptUtils.decrypt(aes, encryptString, encryptKey, encode);
        System.out.println(decode);
        */
        return encode;
    }


    public static void main(String[] args){
        String mac = "54060500FFFB8B1FDBE1C3DB-6DC1-41DA-9E2A-19FADF48F33B";
        String encode2 = EncryptUtils.makeLicense(mac,"20280101");
        System.out.println("111111111111111111:"+encode2);

        EncryptDES aes2 = new EncryptDES(2, "AES", 128);
        String encryptString2 = "AES/ECB/PKCS5Padding";
        String gfCmsLicense="Z1Txf2Ba2i+bXK3HLKP0XkeD0GhU8+eihqcR5w843otGCBMy2f8K3zVdAmzM8tp3nHVKjU6O1bXJmHRcEbuTMg==";
        String decode2 = EncryptUtils.decrypt(aes2, encryptString2, EncryptDES.encryptKey, gfCmsLicense);
        System.out.println("22222222222:"+decode2);






        EncryptDES des=new EncryptDES(1,"DES",56);
        EncryptDES aes=new EncryptDES(2,"AES",128);
        EncryptDES des3=new EncryptDES(3,"DESede",168);


        String encryptKey = "123456789123456789123456789123456789";
        String content = "qwertyuiopasdfgghjklzxcvvbnm";

        String encryptString = "DES/ECB/PKCS5Padding";
        String encode = EncryptUtils.encrypt(des, encryptString, encryptKey, content);
        System.out.println(encode);
        String decode = EncryptUtils.decrypt(des, encryptString, encryptKey, encode);
        System.out.println(decode);

        encryptString = "AES/ECB/PKCS5Padding";
        encode = EncryptUtils.encrypt(aes, encryptString, encryptKey, content);
        System.out.println(encode);
        decode = EncryptUtils.decrypt(aes, encryptString, encryptKey, encode);
        System.out.println(decode);

        encryptString = "DESede/ECB/PKCS5Padding";
        encode = EncryptUtils.encrypt(des3, encryptString, encryptKey, content);
        System.out.println(encode);
        decode = EncryptUtils.decrypt(des3, encryptString, encryptKey, encode);

    }
}


