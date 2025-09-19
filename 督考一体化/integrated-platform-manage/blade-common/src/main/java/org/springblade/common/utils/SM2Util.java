package org.springblade.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import sun.misc.BASE64Encoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by j.jia
 * Version 3.0.0
 * Edit Date 2024/05/11
 */
public class SM2Util {
    private SM2Util() {}

    /**
     * 加密
     * @param data 普通字符串
     * @return 返回密文
     */
    public static String encrypt(String key,String data){
        SM2 sm2 = SmUtil.sm2(null,key);
        return sm2.encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * 解密
     * @param data 待解密字符串
     * @return 返回明文字符串
     */
    public static String decrypt(String key,String data){
        SM2 sm2 = SmUtil.sm2(key,null);
        return StrUtil.utf8Str(sm2.decrypt(data, KeyType.PrivateKey));
    }

    public static Map<String, String> keyPairGenerator() {
        return keyPairGenerator(false);
    }

    public static Map<String, String> keyPairGenerator(boolean isHex) {
        SM2 sm2 = SmUtil.sm2();
        BCECPublicKey publicKey = (BCECPublicKey)sm2.getPublicKey();
        BCECPrivateKey privateKey = (BCECPrivateKey)sm2.getPrivateKey();
        byte[] uks = publicKey.getQ().getEncoded(false);
        byte[] rks = BCUtil.encodeECPrivateKey(privateKey);
        Map<String, String> map = new HashMap<>();
        if(isHex) {
            map.put("publicKey",HexUtil.encodeHexStr(uks));
            map.put("privateKey",HexUtil.encodeHexStr(rks));
        }else {
            map.put("publicKey",new BASE64Encoder().encodeBuffer(uks));
            map.put("privateKey",new BASE64Encoder().encodeBuffer(rks));
        }
        return map;
    }
}
