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
        targetNamespace = "UpdateAppAcctSoap"// 与接口中的命名空间一致,一般是接口的包名倒
        //targetNamespace = "http://service.cfweb/"// 与接口中的命名空间一致,一般是接口的包名倒
)
public interface WebServiceFor4A {

    @WebMethod(operationName = "UpdateAppAcctSoap")
    String updateAppAcctSoap(@WebParam(name = "RequestInfo") String RequestInfo);

}