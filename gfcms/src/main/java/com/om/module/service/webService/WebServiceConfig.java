package com.om.module.service.webService;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * @Title: WebServiceConfig.java
 * @Description: TODO(WebService配置)
 * @Author: liuxinjia  上午9:39
 * @Date: 2020年6月16日 09点39分
 */
@Configuration
public class WebServiceConfig {

    @Autowired
    private WebServiceDemoService webServiceDemoService;

    @Autowired
    private WebServiceFor4A webServiceFor4A;

    /**
     * 注入servlet  bean name不能dispatcherServlet 否则会覆盖dispatcherServlet
     * @return
     */
    @Bean(name = "cxfServlet")
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(),"/webservice/*");
    }


    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    /**
     * 注册WebServiceDemoService接口到webservice服务
     * @return
     */
    @Bean(name = "WebServiceDemoEndpoint")
    public Endpoint sweptPayEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), webServiceDemoService);
        endpoint.publish("/webservice");
        return endpoint;
    }


    /**
     * 注册WebServiceFor4A接口到webservice服务
     * @return
     */
    @Bean(name = "WebServiceFor4AEndpoint")
    public Endpoint sweptPayEndpoint2() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), webServiceFor4A);
        endpoint.publish("/updateAppAcctService");
        return endpoint;
    }
}