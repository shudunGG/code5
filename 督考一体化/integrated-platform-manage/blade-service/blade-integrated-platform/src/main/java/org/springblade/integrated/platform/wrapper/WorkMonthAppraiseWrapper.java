package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.TaskFiles;
import com.vingsoft.entity.WorkMonthAppraise;
import com.vingsoft.vo.TaskFilesVO;
import com.vingsoft.vo.WorkMonthAppraiseVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;

/**
 * @className: WorkMonthAppraiseWrapper
 * @author: Waston.FR
 * @date: 2023/2/15 10:26 星期三
 * @Version 1.0
 **/
public class WorkMonthAppraiseWrapper extends BaseEntityWrapper<WorkMonthAppraise, WorkMonthAppraiseVO> {

	public static WorkMonthAppraiseWrapper build() {
		return new WorkMonthAppraiseWrapper();
	}


	@Override
	public WorkMonthAppraiseVO entityVO(WorkMonthAppraise workMonthAppraise) {
		WorkMonthAppraiseVO workMonthAppraiseVO = Objects.requireNonNull(BeanUtil.copy(workMonthAppraise, WorkMonthAppraiseVO.class));
		return workMonthAppraiseVO;
	}
}
