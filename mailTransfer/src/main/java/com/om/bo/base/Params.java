package com.om.bo.base;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Params {
    public static String prePublicAddrSend;
    public static String prePublicResultQuery;
    public static String allowAutoGenerateMac;

    @Value("${pzparams.prePublicResultQuery}")
    public void setPrePublicResultQuery(String param) {
        prePublicResultQuery = param;
    }

    @Value("${pzparams.prePublicAddrSend}")
    public void setPrePublicAddrSend(String param) {
        prePublicAddrSend = param;
    }

    @Value("${pzparams.allowAutoGenerateMac}")
    public void setAllowAutoGenerateMac(String param) {
        allowAutoGenerateMac = param;
    }
}
