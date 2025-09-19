package com.om.common.util.wx;

/**
 * 微信通用接口凭证
 */
public class AccessToken {
    // 获取到的凭证
    private String token;
    // 凭证有效时间，单位：秒
    private int expiresIn;

    private long expireTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        this.expireTime = System.currentTimeMillis() + ((long)expiresIn)*800L;//打上8折，留点空间
    }
    public long getExpireTime() {
        return expireTime;
    }

}