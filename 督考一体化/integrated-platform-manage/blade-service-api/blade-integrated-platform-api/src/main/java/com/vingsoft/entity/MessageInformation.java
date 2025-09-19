package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-08 20:15
 */
@Data
@TableName("message_information")
@ApiModel(value = "message_information对象", description = "留言信息表")
public class MessageInformation extends BaseEntity{

    /**
     * 留言内容
     */
	@ApiModelProperty(value = "留言内容")
    private String messageContent;

	/**
	 * 工作月调度的业务主键
	 */
	@ApiModelProperty(value = "工作月调度的业务主键")
	private String monthBusinessId;

    /**
     * 业务大类（1督查督办2考核评价3项目管理4绩效考核-领导评价5工作月调度）
     */
	@ApiModelProperty(value = "业务大类")
    private String businessType;

    /**
     * 业务主键（事项id，评价id，项目id）
     */
	@ApiModelProperty(value = "业务主键")
    private Long businessId;


	/**
	 * 业务数据表名
	 */
	@ApiModelProperty(value = "业务数据表名")
	private String businessTable;

	/**
	 * 事项名称
	 */
	@ApiModelProperty(value = "事项名称")
	private String servName;

	/**
	 * 绩效考核类型 1年度 2季度
	 */
	@ApiModelProperty(value = "绩效考核类型")
	private String evaluationType;

	/**
	 * 督查督办子ID
	 */
	@ApiModelProperty(value = "督查督办子ID")
	private Long childId;

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

/*	*//**
	 * 关联文件路径
	 *//*
	@ApiModelProperty(value = "关联文件路径")
	private String aboutfilePath;

	*//**
	 * 文件真实名称
	 *//*
	@ApiModelProperty(value = "文件真实名称")
	private String fileTrueName;*/

	/**
	 * 留言信息类型：1批示，2留言
	 */
	@ApiModelProperty(value = "留言信息类型")
	private Integer psOrLy;

	/**
	 * 艾特@的人员id
	 */
	@ApiModelProperty(value = "艾特@的人员id")
	private String atUserId;

	/**
	 * 艾特@的人员名称
	 */
	@ApiModelProperty(value = "艾特@的人员名称")
	private String atUserName;


	/**
	 * 考核评价文件信息
	 */
	@TableField(exist = false)
	List<AppriseFiles> appriseFilesList;



}
