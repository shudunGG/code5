package com.om.bo.base;


/** 
 * @ClassName: Result <br/> 
 * @date: 2018年1月16日 上午11:39:48 <br/> 
 * @Description:  
 * @author 
 * @version @param <T> 
 **/
public class Result<T> {

    //    return_code 状态值：0 极为成功，其他数值代表失败
    private String return_code;

    public String getReturn_code() {
		return return_code;
	}

	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}

	public String getReturn_message() {
		return return_message;
	}

	public void setReturn_message(String return_message) {
		this.return_message = return_message;
	}

	//    return_message 错误信息，若status为0时，为success
    private String return_message;

    //    content 返回体报文的出参，使用泛型兼容不同的类型
    private T result;

  

    public T getResult(Object object) {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "Result{" +
                "return_code=" + return_code +
                ", return_message='" + return_message + '\'' +
                ", result=" + result +
                '}';
    }
}
