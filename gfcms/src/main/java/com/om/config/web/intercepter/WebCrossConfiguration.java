package com.om.config.web.intercepter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.om.bo.base.Const;

/**
 * 跨域请求拦截器配置,如果服务器端使用Nginx处理,则不需要此配置
 */
@Configuration
//@Component
@WebFilter(urlPatterns = "/*", filterName = "CORSFilter")
//public class WebCrossConfiguration implements Filter {
public class WebCrossConfiguration implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(WebCrossConfiguration.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        //logger.info("===============================WebCrossConfiguration初始化=====================================");
    }


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String url = request.getRequestURI();
        logger.info("========全局filter=======请求资源路径:"+url);
        HttpSession session =request.getSession();
        //logger.info("=============================每次请求的session为:"+session.getId());
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));//设置允许跨域访问的地址
        //这里设置客户端可以取到的数据(从header里面获取)
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With,token_code,token");
        response.setHeader("Access-Control-Allow-Methods", "HEAD, POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600"); //跨域的预检请求时间
        response.setHeader("Access-Control-Allow-Credentials","true"); //是否支持cookie跨域
        // 配置客户端通过header里面可以直接获取到的属性
        response.setHeader("Access-Control-Expose-Headers","token_code,token");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("XDomainRequestAllowed", "1");
        //不需要登录拦截的所有请求都放到这个里面来
        String[] urls = new String[]{"loginSystem","testLog", "testUCR_PARAM","testBase",
                "search","testSec","fileUpload","Ord","uploadFile",
                "testRes","testPz","wxEnter"} ;
        Object opMap = session.getAttribute("OP_INFO");
        boolean isFilter = false;
        //开发阶段先屏蔽掉，后面再启用
        /*for(String u:urls){
            if(url.indexOf(u)>-1){
                isFilter = false;
                break;
            }
        }*/
        if(isFilter){
            if(opMap == null){
            	response.setHeader("token_code", "-1") ;
            	response.setHeader("token", "invalid token.......") ;
                //response.setCharacterEncoding("utf-8");
                response.getWriter().write("invalid request.......");
            	return;
            }else{
            	response.setHeader("token_code", "1") ;
            	response.setHeader("token", "FKEPB37PZ+nsyAnFXsAVMvPzkrrtMa7iUBvxBvwMAzOwHdG1ZiPMa=") ;
            }
        }else{
        	response.setHeader("token_code", "1") ;
        	response.setHeader("token", "BCEBB67PZ-nsyAMFXsAVMwPzkrrtGa7hUBvxBvwMAzOwhFG1ZiPMa-") ;
        }
        logger.info("========全局filter=======允许访问资源:"+url);
        filterChain.doFilter(request, response);
    }

    public void destroy() {

    }
}
