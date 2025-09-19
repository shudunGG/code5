package org.springblade.integrated.platform.excel;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springblade.core.excel.support.ExcelImporter;
import org.springblade.integrated.platform.service.IQuarterlyAssessmentService;
import org.springblade.integrated.platform.service.IQuarterlyEvaluationService;

import java.util.List;

/**
 * 季度评价数据导入类
 *
 * @Author zrj
 * @Create 2023/3/29 17:30
 */
@RequiredArgsConstructor
public class QuarterlyAssessmentImporter implements ExcelImporter<QuarterlyAssessmentExcel> {

	private final IQuarterlyAssessmentService iQuarterlyAssessmentService;
	private final String type;
	private final String name;

	@Override
	public void save(List<QuarterlyAssessmentExcel> data) {
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setJdzbType(type);
			data.get(i).setJdzbName(name);
		}
		iQuarterlyAssessmentService.importQuarterlyAssessment(data);
	}


}
