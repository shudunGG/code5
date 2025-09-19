package com.om.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import java.util.Base64;

public class EncryptUtils {


    static {
        // 添加安全提供者（SM2，SM3，SM4等加密算法，CBC、CFB等加密模式，PKCS7Padding等填充方式，不在Java标准库中，由BouncyCastleProvider实现）
        //Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * SM4，商业密码（Shang Mi4）是中华人民共和国政府采用的一种分组密码标准，由国家密码管理局于2012年3月21日发布，相关标准为“GM/T 0002-2012《SM4分组密码算法》”。
     * <p>
     * 输入：待加密的字符串，16或24或32位字符串密码
     * 输出：16进制字符串或Base64编码的字符串密文（常用）
     * 应用：密码管理、数字签名、文件完整性校验
     * 安全性：★★★★☆
     *
     * @param plainString 明文
     * @param key         秘钥
     * @return cipherString 密文
     */
    public static String sm4Encrypt(String plainString, String key) {
        String cipherString = null;
        try {
            // 指定加密算法
            String algorithm = "SM4";
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            // 获取Cipher对象实例（BC中SM4默认使用ECB模式和PKCS5Padding填充方式，因此下列模式和填充方式无需指定）
            Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
            // 初始化Cipher为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            // 获取加密byte数组
            byte[] cipherBytes = cipher.doFinal(plainString.getBytes(StandardCharsets.UTF_8));
            // 输出为Base64编码
            cipherString = Base64.getEncoder().encodeToString(cipherBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherString;
    }






    /**
     * SM4，商业密码（Shang Mi4）是中华人民共和国政府采用的一种分组密码标准，由国家密码管理局于2012年3月21日发布，相关标准为“GM/T 0002-2012《SM4分组密码算法》”。
     * <p>
     * 输入：密文，16或24或32位字符串密码
     * 输出：明文
     * 应用：密码管理、数字签名、文件完整性校验
     * 安全性：★★★★☆
     *
     * @param cipherString 密文
     * @param key          秘钥
     * @return plainString 明文
     */
    public static String sm4Decrypt(String cipherString, String key) {
        String plainString = null;
        try {
            // 指定加密算法
            String algorithm = "SM4";
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            // 获取Cipher对象实例（BC中SM4默认使用ECB模式和PKCS5Padding填充方式，因此下列模式和填充方式无需指定）
            Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
            // 初始化Cipher为解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            // 获取加密byte数组
            byte[] cipherBytes = cipher.doFinal(Base64.getDecoder().decode(cipherString));
            // 输出为字符串
            plainString = new String(cipherBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainString;
    }

}