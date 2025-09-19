package org.springblade.integrated.platform.excel;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springblade.core.excel.support.ExcelImporter;
import org.springblade.integrated.platform.service.IWorkMonthService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @className: WorkMonthImporter
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/3/2 14:45 星期四
 * @Version 1.0
 **/
@RequiredArgsConstructor
public class WorkMonthImporter implements ExcelImporter<WorkMonthExcel> {

	private final IWorkMonthService workMonthService;

	@Override
	public void save(List<WorkMonthExcel> data) {
		for (int i = 0; i < data.size(); i++) {
			try {
				data.get(i).setPlanTime(DateUtil.date(data.get(i).getPlanTime()));
			} catch (Exception e) {
				System.out.println("异常来自第" + (i + 2) + "行:"+e.toString());
			}
		}
		workMonthService.importWorkMonth(data);
	}
}
