package org.springblade.plugin.rocketmq.constants;

import java.io.Serializable;

/**
 * 错误编码公共接口
 *
 * @author mrtang
 * @version 1.0
 * @date 2021/9/14 9:40 下午
 */

public interface ErrorCode extends Serializable {
	/**
	 * 错误码
	 * @return
	 */
	String getCode();
	/**
	 * 错误信息
	 * @return
	 */
	String getMsg();
}
