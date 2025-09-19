package com.om.module.service.webService;

import org.springframework.stereotype.Service;

import javax.jws.WebService;

/**
 * @Title: WebServiceDemoServiceImpl.java
 * @Description: TODO()
 * @Author: liuxinjia  上午9:39
 * @Date: 2020年6月16日 09点39分
 */
@Service
@WebService(serviceName = "WebServiceDemoService", // 与接口中指定的name一致
        targetNamespace = "http://service.om/", // 与接口中的命名空间一致,一般是接口的包名倒
        endpointInterface = "com.om.module.service.webService.WebServiceDemoService" // 接口地址
)
public class WebServiceDemoServiceImpl implements WebServiceDemoService {

    @Override
    public String hello(String name) {
        return "hello"+name;
    }


}