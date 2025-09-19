package com.om.bo.base;

public enum ResultEnum {
	SUCCESS(1, "处理成功"),
	ERROR_SYSTEM(-1,"系统处理失败,请联系管理员处理!"),
	NODATA(100, "没有查到数据"),
	NO_DATA_SOURCE(101, "数据源不存在"),
	INSERT_FAIL(1001, "插入数据记录为0"),
	UPDATE_FAIL(1002, "修改数据记录为0"),
	RATE_FEE_EMPTY(2001, "费率金额不能为空"),
	RATE_FEE_EXIST(2002, "该费率已存在"),
	POLICY_EXIST(2003, "策略已存在"),
	CASEID_EMPTY(2004, "测试案例编号不能为空"),
	PARAM_ERROR(2005, "参数错误")

	; //注意这里是分号

	private Integer code;
 	private String message;
	
	public Integer getCode() {
		return code;
	}
 
	public void setCode(Integer code) {
		this.code = code;
	}
 
	public String getMessage() {
		return message;
	}
 
	public void setMessage(String message) {
		this.message = message;
	}
 
	ResultEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
