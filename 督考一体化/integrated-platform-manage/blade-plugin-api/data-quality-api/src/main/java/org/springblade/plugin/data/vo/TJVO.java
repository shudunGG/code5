package org.springblade.plugin.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.TJ;

/**
 * @author MaQiuyun
 * @date 2021/12/27 17:23
 * @description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TJVO extends TJ {
	/**
	 * 规则代码
	 */
	private String code;
	/**
	 * 规则描述
	 */
	private String describe;
}
