package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/10 22:11
 */
@Data
@ApiModel(value = "办公邮件",description = "")
@TableName("oa_email")
public class OaEmail extends BaseEntity {
	/** 回复邮件主键 */
	@ApiModelProperty(name = "回复邮件主键",notes = "")
	private Long replyId ;
	/** 标题 */
	@ApiModelProperty(name = "标题",notes = "")
	private String title ;
	/** 收件人 */
	@ApiModelProperty(name = "收件人",notes = "")
	private String receiverUser ;
	@ApiModelProperty(name = "收件人",notes = "")
	private String receiverUserName ;
	/** 是否抄送 */
	@ApiModelProperty(name = "是否抄送",notes = "")
	private Integer isCopied ;
	/** 抄送人 */
	@ApiModelProperty(name = "抄送人",notes = "")
	private String copiedUser ;
	/** 抄送人 */
	@ApiModelProperty(name = "抄送人",notes = "")
	private String copiedUserName ;
	/** 邮件内容 */
	@ApiModelProperty(name = "邮件内容",notes = "")
	private String content ;
	/** 附件 */
	@ApiModelProperty(name = "附件",notes = "")
	private String files ;

	/**
	 * 阅读表主键
	 */
	@TableField(exist = false)
	private Long readId;

	/**
	 * 阅读状态 0未读 1已读
	 */
	@TableField(exist = false)
	private Long readStatus;
}
