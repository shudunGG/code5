package org.springblade.plugin.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.QualityTestingLog;

/**
 * @author MaQiuyun
 * @date 2021/12/29 17:11
 * @description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QualityTestingLogVO extends QualityTestingLog {
	private static final long serialVersionUID = -5784158689378034972L;
	/**
	 * 统计周期
	 */
	private String currentCycleStr;
}
