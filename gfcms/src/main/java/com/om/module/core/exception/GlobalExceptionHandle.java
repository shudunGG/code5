package com.om.module.core.exception;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.om.bo.base.Const;

/** 
 * 全局异常处理器,拦截发生在Controller层的异常信息
 **/
@ControllerAdvice
public class GlobalExceptionHandle {
	
    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandle.class);
    
    /**
      * 用来处理所有不可预知的异常信息
     * @param e 异常
     * @return json结果
     */
    @ExceptionHandler(value=Exception.class)
    public @ResponseBody HashMap<String,Object> defaultExceptionHandler(HttpServletRequest request,HttpServletResponse response,
    		Exception e){
    	LOGGER.info("全局异常处理异常信息,------------>处理未知异常信息");
    	HashMap<String,Object> result = new HashMap<String,Object>();
    	result.put("code", Const.ErrCode);
    	result.put("msg", Const.ErrSystem);
    	result.put("exception", e.getMessage());
    	//LOGGER.info(e.getMessage());
		e.printStackTrace();
    	return result;
    }
    /**
     * 自定义业务异常处理器,业务运行是产生的捕获处理
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(BusiException.class)
    public @ResponseBody HashMap<String,Object> busiRuntimeException(HttpServletRequest request,HttpServletResponse response,
    		BusiException e) {
    	LOGGER.info("全局异常处理异常信息,------------>处理业务异常信息");
    	HashMap<String,Object> result = new HashMap<String,Object>();
    	result.put("code", e.getCode());
    	result.put("msg", e.getMsg());
    	result.put("exception", e.getMessage());
    	e.printStackTrace();
		//LOGGER.info(e.getMessage());
    	return result;
    }
}
