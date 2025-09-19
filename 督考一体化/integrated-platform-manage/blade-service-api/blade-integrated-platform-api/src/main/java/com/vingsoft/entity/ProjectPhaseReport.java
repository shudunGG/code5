package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 项目阶段汇报实体类
 * @author AdamJin 2022-4-9 17:33:28
 */
@Data
@TableName("project_phase_report")
public class ProjectPhaseReport extends BaseEntity{
    /**
     * 项目id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long projId;

    /**
     * 计划阶段id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long jhjdId;

    /**
     * 汇报阶段id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long hbjdId;

    /**
     * 进展状态
     */
    private String jzzt;

    /**
     * 当月调度投资（万元）
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Float dyddtz;

    /**
     * 累计完成投资（万元）
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Float ljddtz;

    /**
     * 形象进度
     */
    private String xxjd;

    /**
     * 是否开复工
     */
    private String sfkfg;

    /**
     * 开复工时间
     */
    private Date kfgsj;

    /**
     * 未开复工原因
     */
    private String wkfgyy;

    /**
     * 是否纳入统计库
     */
    private String sfnrtjk;

    /**
     * 纳入统计库时间
     */
    private Date nrtjksj;

    /**
     * 未纳入统计库原因
     */
    private String wnrtjkyy;

    /**
     * 实际开工时间
     */
    private Date sjkgsj;

    /**
     * 实际竣工时间
     */
    private Date sjjgsj;

    /**
     * 存在的主要困难和问题
     */
    private String czzyknwt;

    /**
     * 图片/视频文字说明
     */
    private String tpspsm;

    /**
     * 审核状态
     */
    private String shzt;

    /**
     * 汇报人名称
     */
    private String hbrmc;

    /**
     * 批示
     */
    private String ps;

    /**
     * 批示人
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long psr;

    /**
     * 批示人名称
     */
    private String psrxm;

    /**
     * 附件
     */
    private String file;

	/**
	 * 阶段名称
	 */
	@TableField(exist = false)
	private String phaseName;

	/**
	 * 阶段截止时间
	 */
	@TableField(exist = false)
	private String endTime;

	/**
	 * 审核人
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Long shrId;

	/**
	 * 项目阶段信息
	 */
	@TableField(exist = false)
	private List<ProjectPhasePlan> projectPhasePlanList;

	/**
	 * 审核意见(市级）
	 */
	private String shyj;
	/**
	 * 审核意见(省级）
	 */
	private String sjshyj;

	/**
	 * 文件附件
	 */
	@TableField(exist = false)
	private List<ProjectFiles> files;


	/**
	 * 累计到位资金-合计
	 */
	private String ljdwzjhj;
	/**
	 * 累计到位资金-中央
	 */
	private String ljdwzjzy;
	/**
	 * 累计到位资金-省级
	 */
	private String	ljdwzjsj;
	/**
	 * 累计到位资金-地方
	 */
	private String ljdwzjdf;

	/**
	 * 完成投资-合计
	 */
	private String 	wctzhj;
	/**
	 * 完成投资-中央
	 */
	private String wctzzy;
	/**
	 * 完成投资-省级
	 */
	private String 	wctzsj;
	/**
	 * 完成投资-地方
	 */
	private String wctzdf;

	/**
	 * 支付资金-合计
	 */
	private String 	zfzjhj;
	/**
	 * 支付资金-中央
	 */
	private String zfzjzy;
	/**
	 * 支付资金-省级
	 */
	private String 	zfzjsj;
	/**
	 * 支付资金-地方
	 */
	private String zfzjdf;


	private String ljdwzjhjlv;

	private String 	ljdwzjzylv;

	private String ljdwzjsjlv;

	private String 	ljdwzjdflv;

	private String wctzhjlv;

	private String 	wctzzylv;

	private String wctzsjlv;

	private String 	wctzdflv;

	private String zfzjhjlv;

	private String 	zfzjzylv;

	private String zfzjsjlv;

	private String 	zfzjdflv;
	/**
	 * 当年完成投资
	 */
	private String 	dnwctz;
	/**
	 * 累计完成投资
	 */
	private String 	ljwctz;
	/**
	 * 专项债券使用情况
	 */
	private String 	zxzqsyqk;
	/**
	 * 当年使用债券情况
	 */
	private String 	dnsyzqqk;
	/**
	 * 累计使用债券情况
	 */
	private String 	ljsyzqqk;
	/**
	 * 债券使用率
	 */
	private String 	zqsyl;
	/**
	 * 计划竣工时间
	 */
	private Date  jhjgsj;
	/**
	 * 计划开复工时间
	 */
	private Date  startDatePlan;

	/**
	 * 是否办理选址/土地预算意见
	 */
	private String 	sfblxztd;
	/**
	 * 是否办理环境影响评价
	 */
	private String 	sfhjxxpj;
	/**
	 * 是否办理可行性研究报告
	 */
	private String 	sfkxxyj;
	/**
	 * 是否办理项目建议书/规划/实施方案
	 */
	private String 	sfblxmjys;
}
