package org.springblade.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 视图实体
 * @date 2022/5/1 10:47
 */
@Data
@ApiModel(value = "SubsystemVO", description = "SubsystemVO对象")
public class SubSystemVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 标签名
	 */
	private String label;
	/**
	 * 子系统ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long value;
}
