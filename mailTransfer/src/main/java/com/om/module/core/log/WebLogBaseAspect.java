package com.om.module.core.log;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.om.config.mybatis.core.DataSourcesName;
import com.om.config.mybatis.core.DynamicDataSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
/**
 * 全局日志输出定义,暂时定义controller里面的日志
 * @author Administrator
 *
 */
@Aspect
@Component
public class WebLogBaseAspect {
    private final static Logger logger = LoggerFactory.getLogger(WebLogBaseAspect.class);
    /**
     * 指定 controller 包下的注解
     * //两个..代表所有子目录，最后括号里的两个..代表所有参数
     * */
    @Pointcut("execution(public * com.om.module.controller..*.*(..))")
    public void logPoint() {

    }

    /**
     * 指定当前执行方法在logPoint之前执行
     * */
    @Before("logPoint()")
    public void doBefore(JoinPoint joinPoint) throws Throwable{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = new Date();
        String dateStr = sdf.format(date);

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        logger.info("WebLogBaseAspect请求地址 : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        // 获取真实的ip地址
        //logger.info("IP : " + IPAddressUtil.getClientIpAddress(request));
        logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."
                + joinPoint.getSignature().getName());
        logger.info("参数 : " + Arrays.toString(joinPoint.getArgs()));
        //在这里将数据源设置为默认数据源,这个做法有点差劲...
        logger.info("请求到来,开始设置默认数据源........");
        DynamicDataSource.setDataSource(DataSourcesName.DEFAULTDB);
        logger.info("请求到来,默认数据源设置完成........");
        //loggger.info("参数 : " + joinPoint.getArgs());
    }

    /**
     * 指定在方法之后返回
     * */
    @AfterReturning(returning = "ret", pointcut = "logPoint()")// returning的值和doAfterReturning的参数名一致
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容(返回值太复杂时，打印的是物理存储空间的地址)
        //logger.info("WebLogBaseAspect请求处理完成返回值为 : " + ret);
        // 处理完请求，清空设置的内容

    }

    @Around("logPoint()")
    public Object doAround(ProceedingJoinPoint procee) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object ob = procee.proceed();// ob 为方法的返回值
        logger.info("WebLogBaseAspect耗时 : " + (System.currentTimeMillis() - startTime));
        return ob;
    }
}
