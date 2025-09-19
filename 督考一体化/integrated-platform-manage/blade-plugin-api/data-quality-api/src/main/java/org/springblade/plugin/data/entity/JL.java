package org.springblade.plugin.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author MaQiuyun
 * @date 2021/12/23 16:34
 * @description:结果记录表
 */
@Data
public class JL implements Serializable {
	private static final long serialVersionUID = 3822608352550950639L;
	/**
	 * 主键
	 */
	private String id;
	/**
	 * 检查字段
	 */
	private String check_column;
	/**
	 * 检查字段值
	 */
	private String check_column_value;
	/**
	 * 数据表主键字段
	 */
	private String key_column;
	/**
	 * 数据表主键说明
	 */
	private String key_comment;
	/**
	 * 数据表主键值
	 */
	private String key_value;
	/**
	 * 记录时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date record_time;
	/**
	 * 是否例外1是0否
	 */
	private String if_exception;
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
	/**
	 * 统计表Id
	 */
	private String tj_id;
	/**
	 * 周期类型
	 */
	private String cycle_type;
	/**
	 * 主题表ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long theme_id;
}
