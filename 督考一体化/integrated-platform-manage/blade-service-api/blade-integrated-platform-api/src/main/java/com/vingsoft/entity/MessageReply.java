package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.List;

/**
 * 公共方法-批示/留言回复
 *
 * @Author JG🧸
 * @Create 2022/05/14 14:40
 */
@Data
@TableName("message_reply")
@ApiModel(value = "message_reply 对象", description = "留言信息回复表")
public class MessageReply extends BaseEntity{

	/**
	 * 关联批示留言表id
	 */
	@ApiModelProperty(value = "关联批示留言表id")
	private String messageInformationId;

    /**
     * 回复内容
     */
	@ApiModelProperty(value = "回复内容")
    private String replyContent;

	/**
	 * 评价人
	 */
	@ApiModelProperty(value = "评价人")
	private String appriseUser;

	/**
	 * 评价人id
	 */
	@ApiModelProperty(value = "评价人id")
	private Long appriseUserId;

	/**
	 * 评价人部门
	 */
	@ApiModelProperty(value = "评价人部门")
	private String appriseuserDeptname;

	/**
	 * 回复类型：1回复批示留言 2回复批示留言下的回复
	 */
	@ApiModelProperty(value = "回复类型")
	private String reolyType;

	/**
	 * 如果回复类型为2，这个字段就是当前表的id，否则为空
	 */
	@ApiModelProperty(value = "当前表id")
	private String reolyMsgId;


	/**
	 * 考核评价文件信息
	 */
	@TableField(exist = false)
	List<AppriseFiles> appriseFilesList;



	/**
	 * 业务大类（1督查督办2考核评价3项目管理）
	 */
	@TableField(exist = false)
	private String businessType;

	/**
	 * 绩效考核类型 1年度 2季度
	 */
	@TableField(exist = false)
	private String evaluationType;

}
