package org.springblade.plugin.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.LW;

import java.io.Serializable;

/**
 * @author MaQiuyun
 * @date 2021/12/28 21:04
 * @description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LWVO extends LW implements Serializable {
	private static final long serialVersionUID = 3806161053106084161L;
	/**
	 * 规则代码
	 */
	private String code;
	/**
	 * 规则描述
	 */
	private String describe;
	/**
	 * 数量
	 */
	private Integer count;
}
