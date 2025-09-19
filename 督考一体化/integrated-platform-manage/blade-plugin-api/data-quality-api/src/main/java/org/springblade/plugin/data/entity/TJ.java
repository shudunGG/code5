package org.springblade.plugin.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author MaQiuyun
 * @date 2021/12/23 16:43
 * @description:结果统计表
 */
@Data
public class TJ {
	/**
	 * 统计表Id
	 */
	private String id;
	/**
	 * 统计时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date statistics_time;
	/**
	 * 错误合计
	 */
	private Integer total_error;
	/**
	 * 例外合计
	 */
	private Integer total_exception;
	/**
	 * 已修复合计
	 */
	private Integer total_repaired;
	/**
	 * 统计类型
	 */
	private String statistical_type;
	/**
	 * 主题表ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long theme_id;
	/**
	 * 规则管理表ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long manage_rule_id;
	/**
	 * 质检方案表ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long quality_testing_programme_id;
	/**
	 * 业务模型管理表主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long model_id;
	/**
	 * 执行期数
	 */
	private Integer period;
	/**
	 * 统计周期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date current_cycle;
}
