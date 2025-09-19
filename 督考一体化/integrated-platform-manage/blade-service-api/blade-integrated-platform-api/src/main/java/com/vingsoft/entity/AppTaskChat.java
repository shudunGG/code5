package com.vingsoft.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
* @Description:    交办回复类
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:15
* @Version:        1.0
*/
@Data
@ApiModel(value = "appTaskChat对象", description = "交办信息回复表")
public class AppTaskChat extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/**
	 * 我的交办ID
	 */
	private Long taskId;
	/**
	 * 回复人ID
	 */
	private Long receiveId;
	/**
	 * 回复人名称
	 */
	private String receiveName;
	/**
	 * 回复时间
	 */
	private Date replayTime;
	/**
	 * 回复内容
	 */
	private String content;
}
