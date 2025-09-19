package com.om.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源访问映射路径,例如使用文件下载时可以不通过流的方式读取,而是直接通过地址下载
 */

@Configuration
public class WebMvcStaticResourceConfAdapter {
    protected final Logger logger = LoggerFactory.getLogger(WebMvcStaticResourceConfAdapter.class);
    @Autowired
    private Environment env;
    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        /**
         * 前台访问示例，支持动态设置二级访问目录
         * http://127.0.0.1:9443/om/cfFileRes/down/tmpl/t1/1234.txt   tmpl下有子目录，里面有个文件1234.txt
         * http://127.0.0.1:9443/om/cfFileRes/down/tmpl/tp01.xls      直接访问tmpl下的文件
         */
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                //logger.info("tmplDonwReqPath:"+env.getProperty("tmplDonwReqPath")+";"+"tmplDownPath:"+env.getProperty("tmplDownPath"));
                //logger.info("tmplImportExeclDonwReqPath:"+env.getProperty("tmplImportExeclDonwReqPath")+";"+"tmplImportPath:"+env.getProperty("tmplImportPath"));
                //logger.info("needFileDonwReqPath:"+env.getProperty("needFileDonwReqPath")+";"+"needUploadPath:"+env.getProperty("needUploadPath"));
                //registry.addResourceHandler("/cfFileDown/**").addResourceLocations("file:/home/uploads/") ;
                registry.addResourceHandler(env.getProperty("tmplDonwReqPath")).addResourceLocations("file:"+env.getProperty("tmplDownPath")) ;
                registry.addResourceHandler(env.getProperty("tmplImportExeclDonwReqPath")).addResourceLocations("file:"+env.getProperty("tmplImportPath")) ;
                registry.addResourceHandler(env.getProperty("needFileDonwReqPath")).addResourceLocations("file:"+env.getProperty("needUploadPath")) ;
                registry.addResourceHandler(env.getProperty("autoTestDownReqPath")).addResourceLocations("file:"+env.getProperty("autoTestUploadPath")) ;

                registry.addResourceHandler(env.getProperty("autoTestImgReqPath")).addResourceLocations("file:"+env.getProperty("autoTestImgPath")) ;
            }
        };
    }

}
