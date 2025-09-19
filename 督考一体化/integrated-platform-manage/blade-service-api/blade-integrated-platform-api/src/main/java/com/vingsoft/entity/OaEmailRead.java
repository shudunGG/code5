package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;


/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/10 22:13
 */
@Data
@ApiModel(value = "办公邮件-阅读",description = "")
@TableName("oa_email_read")
public class OaEmailRead extends BaseEntity {
	/** 邮件主键 */
	@ApiModelProperty(name = "邮件主键",notes = "")
	private Long emailId ;
	/** 收件人 */
	@ApiModelProperty(name = "收件人",notes = "")
	private Long receiverUser ;
	/** 阅读状态 0未读 1已读 */
	@ApiModelProperty(name = "阅读状态",notes = "")
	private int readStatus ;
}
