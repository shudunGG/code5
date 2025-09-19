package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目阶段提醒实体类
 * @author AdamJin 2022-4-9 17:33:28
 */
@Data
@TableName("project_phase_remind")
public class ProjectPhaseRemind  extends BaseEntity{

    /**
     * 项目id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long projId;

    /**
     * 阶段ID
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long phaseId;

    /**
     * 阶段名称j
     */
    private String phaseName;

    /**
     * 汇报时间
     */
    private Date reportTime;

    /**
     * 提前提醒时间
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long reminderTime;

	/**
	 * 阶段编号
	 */
	private String phaseCode;

	/**
	 * 事项编号
	 */
	private  String servCode;

}
