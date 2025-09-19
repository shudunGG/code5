package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 项目操作日志表
 * @author AdamJin 2022-4-9 17:28:13
 */
@Data
@TableName("project_phase_report_swtich")
public class ProjectPhaseReportSwitch extends BaseEntity {
	/**
	 * 操作内容
	 */
	private String time;
}
