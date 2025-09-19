package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:汇报信息实体
 * @date 2022-04-08 20:15
 */
@Data
@TableName("reports")
@ApiModel(value = "reports对象", description = "汇报信息表")
public class Reports extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 阶段信息表主键id
	 */
	@ApiModelProperty(value = "阶段信息表主键id")
	private Long stageInformationId;

    /**
     * 阶段名称
     */
	@ApiModelProperty(value = "阶段名称")
    private String stage;

    /**
     * 单位名称
     */
	@ApiModelProperty(value = "单位名称")
    private String deptName;

    /**
     * 单位id
     */
	@ApiModelProperty(value = "单位id")
    private String deptId;

    /**
     * 进展状态：1全面完成 2正常推进 3进展缓慢 4没有进展
     */
	@ApiModelProperty(value = "进展状态")
    private String processStatus;

    /**
     * 进展情况
     */
	@ApiModelProperty(value = "进展情况")
    private String process;

    /**
     * 汇报留言内容
     */
	@ApiModelProperty(value = "汇报留言内容")
    private String reportMessage;

    /**
     * 状态：0 暂存 1 提交 2送审 3送审通过 4送审不通过
     */
	@ApiModelProperty(value = "状态")
    private String reportStatus;

	/**
	 * 评价指标分类：1年度考核 2季度考核
	 */
	@ApiModelProperty(value = "评价指标分类")
	private String evaluationType;

	/**
	 * 评价指标表主键
	 */
	@ApiModelProperty(value = "评价指标表主键")
	private Long evaluationId;

	/**
	 * 附件信息
	 */
	@TableField(exist = false)
	private List<AppriseFiles> appriseFilesList;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;

	/**
	 * 汇报基本信息表主键id
	 */
	@ApiModelProperty(value = "汇报基本信息表主键id")
	private Long baseid;

	/**
	 *送审标题
	 */
	@TableField(exist = false)
	private String title;

	/**
	 *送审用户id
	 */
	@TableField(exist = false)
	private String userIds;

	/**
	 *是否异步
	 */
	@TableField(exist = false)
	private String sync;


}
