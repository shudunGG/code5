package com.om.module.core.exception;


import com.om.bo.base.Result;

/** 
 * @ClassName: ResultUtil <br/> 
 * @date: 2018年1月16日 下午12:28:39 <br/> 
 * @Description: 
 * @author hck
 * @version  
 **/
public class ResultUtil {

    /**
     * 返回成功，传入返回体具体出參
     * @param object
     * @return
     */
    public static Result success(Object object){
        Result result = new Result();
        result.setReturn_code("0");
        result.setReturn_message("success");
        result.setResult(object);
        return result;
    }

    /**
     * 提供给部分不需要出參的接口
     * @return
     */
    public static Result success(){
        return success(null);
    }

    /**
     * 自定义错误信息
     * @param code
     * @param msg
     * @return
     */
    public static Result error(String code,String msg){
        Result result = new Result();
        result.setReturn_code(code);
        result.setReturn_message(msg);
        result.setResult(null);
        return result;
    }
    
    /**
     * 自定义错误信息
     * @param code
     * @param msg
     * @return
     */
    public static Result error(String code,String msg,Object object){
        Result result = new Result();
        result.setReturn_code(code);
        result.setReturn_message(msg);
        result.setResult(object);
        return result;
    }

    /**
     * 返回异常信息，在已知的范围内
     * @param exceptionEnum
     * @return
     */
    public static Result error(ExceptionEnum exceptionEnum){
        Result result = new Result();
        result.setReturn_code(exceptionEnum.getCode());
        result.setReturn_message(exceptionEnum.getMsg());
        result.setResult(null);
        return result;
    }
    
}
