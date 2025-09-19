package com.om.module.service.webService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @Title: WebServiceDemoService.java
 * @Description: TODO()
 * @Author: liuxinjia  上午9:39
 * @Date: 2020年6月16日 09点39分
 */
@WebService(
        targetNamespace = "http://service.om/"// 与接口中的命名空间一致,一般是接口的包名倒
)
public interface WebServiceDemoService {

    @WebMethod
    String hello(@WebParam(name = "name")String name);

}