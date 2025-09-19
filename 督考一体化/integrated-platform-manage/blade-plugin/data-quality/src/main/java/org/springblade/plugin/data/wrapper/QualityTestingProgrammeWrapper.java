package org.springblade.plugin.data.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.plugin.data.entity.QualityTestingProgramme;
import org.springblade.plugin.data.vo.QualityTestingProgrammeVO;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:11 2021/12/2 0002
 * @ Description：
 */
public class QualityTestingProgrammeWrapper extends BaseEntityWrapper<QualityTestingProgramme, QualityTestingProgrammeVO> {
	public static final QualityTestingProgrammeWrapper build() {
		return new QualityTestingProgrammeWrapper();
	}

	@Override
	public QualityTestingProgrammeVO entityVO(QualityTestingProgramme entity) {
		QualityTestingProgrammeVO programmeVO = BeanUtil.copy(entity, QualityTestingProgrammeVO.class);
		programmeVO.setCycleStr(getCycleStr(entity));
		//拼接周期字符串
		return programmeVO;
	}

	/**
	 * 获取周期字符串
	 * @param entity
	 * @return
	 */
	public static String getCycleStr(QualityTestingProgramme entity){
		StringBuffer cycleStr = new StringBuffer();
		switch (entity.getCycle()) {
			case "1":
				cycleStr.append("一次").append(entity.getDatetime());
				break;
			case "2":
				cycleStr.append("每日").append(entity.getTime());
				break;
			case "3":
				cycleStr.append("每").append(stringToWeek(entity.getWeek())).append(entity.getTime());
				break;
			case "4":
				cycleStr.append("每月").append(entity.getDate()).append("日").append(entity.getTime());
				break;
			case "5":
				cycleStr.append("每年").append(entity.getMonth()).append("月").append(entity.getDate()).append("日").append(entity.getTime());
				break;
		}
		return cycleStr.toString();
	}

	private static String stringToWeek(String i) {
		switch (i) {
			case "1":
				return "周一";
			case "2":
				return "周二";
			case "3":
				return "周三";
			case "4":
				return "周四";
			case "5":
				return "周五";
			case "6":
				return "周六";
			case "7":
				return "周日";
			default:
				return "";
		}
	}
}
