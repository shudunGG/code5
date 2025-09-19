package org.springblade.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;


@Data
@TableName("api_request_data")
public class ApiRequestData {
	/**
	 * 主键id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/** 请求时间 */
	private Date reqTime ;
	/** 客户端表的ID */
	private Long clientId ;
	/** 接口ID */
	private Long interfaceId ;
	/** 请求数据 */
	private String reqData ;
	/** 响应数据 */
	private String respData ;
}
