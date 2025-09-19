package com.om.module.service.webService;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;


public class TestService {
    public static void main(String[] args){
        try {
//            JaxWsDynamicClientFactory factroy = JaxWsDynamicClientFactory.newInstance();
//            Client client = factroy.createClient("http://localhost:9445/om/webservice/webservice?wsdl");
//            Object[] results = client.invoke("hello", "lxj");
//            System.out.println("client方式:" + results[0]);

            JaxWsDynamicClientFactory factroy2 = JaxWsDynamicClientFactory.newInstance();
            Client client2 = factroy2.createClient("http://localhost:9445/om/webservice/updateAppAcctService?wsdl");
            Object[] results2 = client2.invoke("updateAppAcctSoap", "lxj");
            System.out.println("client方式:" + results2[0]);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
