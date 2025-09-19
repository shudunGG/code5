package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.ReminderRecord;
import com.vingsoft.vo.ReminderRecordVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author JG🧸
 * @Create 2022/4/18 13:33
 */
public class ReminderRecordWrapper extends BaseEntityWrapper<ReminderRecord, ReminderRecordVO> {

	public static ReminderRecordWrapper build() {
		return new ReminderRecordWrapper();
	}


	@Override
	public ReminderRecordVO entityVO(ReminderRecord reminderRecord) {
		ReminderRecordVO reminderRecordVO = Objects.requireNonNull(BeanUtil.copy(reminderRecord, ReminderRecordVO.class));
		return reminderRecordVO;
	}
}
