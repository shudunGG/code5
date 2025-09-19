package com.om.module.core.exception;

/**
 * 自定义业务异常,主要返回异常错误代码和异常描述信息
 * @author Administrator
 *
 */
public class BusiException extends RuntimeException{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**异常错误代码*/
    private String code;

    /**异常错误描述*/
    private String msg;
    public BusiException(String code,String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}    
}
