package org.springblade.plugin.data.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.plugin.data.entity.QualityTestingLog;
import org.springblade.plugin.data.vo.QualityTestingLogVO;

import java.util.Date;

/**
 * @author MaQiuyun
 * @date 2021/12/29 17:12
 * @description:
 */
public class QualityTestingLogWrapper extends BaseEntityWrapper<QualityTestingLog, QualityTestingLogVO> {
	public static final QualityTestingLogWrapper build() {
		return new QualityTestingLogWrapper();
	}

	@Override
	public QualityTestingLogVO entityVO(QualityTestingLog entity) {
		QualityTestingLogVO vo = BeanUtil.copyProperties(entity, QualityTestingLogVO.class);
		String currentCycleStr = "";
		Date currentCycle = entity.getCurrentCycle();
		//组装统计周期
		switch (entity.getCycleType()) {
			case "year":
				currentCycleStr = DateUtil.format(currentCycle, "yyyy") + "年";
				break;
			case "quarter":
				String dayStr = DateUtil.format(currentCycle, DateUtil.PATTERN_DATE);
				String[] split = dayStr.split("-");
				int quarter = (Integer.parseInt(split[1]) + 2) / 3;
				currentCycleStr = new StringBuffer(split[0]).append("年").append(String.valueOf(quarter)).append("季度").toString();
				break;
			case "month":
				currentCycleStr = DateUtil.format(currentCycle, "yyyy年MM月");
				break;
			default:
				currentCycleStr = DateUtil.format(currentCycle, "yyyy年MM月dd日");
		}
		vo.setCurrentCycleStr(currentCycleStr);
		return vo;
	}
}
