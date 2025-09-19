package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * @className: ProjectSummaryAppraise
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/3/16 11:38 星期四
 * @Version 1.0
 **/
@Data
@TableName("project_summary_appraise")
public class ProjectSummaryAppraise extends BaseEntity {

	/**
	 * 项目名称
	 */
	private String title;

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
	 * 投资完成率
	 */
	private String tzwcl;

	/**
	 * 数据产生时间
	 */
	private Date createTime;

	/**
	 * 计划投资月份
	 */
	private String jhtzyf;

	/**
	 * 1-X计划投资额度（万元）
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float jhtzed;

	/**
	 * 评价分数
	 */
	private String score;

	/**
	 * 评价等级（91-100（优秀）、81-90（良好）、71-80（较好）、70分以下（一般））
	 */
	private String level;

}
