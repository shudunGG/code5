package org.springblade.integrated.platform.excel;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.vingsoft.entity.StageInformation;
import lombok.RequiredArgsConstructor;
import org.springblade.core.excel.support.ExcelImporter;
import org.springblade.integrated.platform.service.IQuarterlyEvaluationService;
import org.springblade.integrated.platform.service.IStageInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 季度评价数据导入类
 *
 * @Author JG🧸
 * @Create 2022/4/9 17:30
 */
@RequiredArgsConstructor
public class QuarterlyEvaluationImporter implements ExcelImporter<QuarterlyEvaluationExcel> {

	private final IQuarterlyEvaluationService iQuarterlyEvaluationService;
	private final String type;
	private final String name;

	@Override
	public void save(List<QuarterlyEvaluationExcel> data) {
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setJdzbType(type);
			data.get(i).setJdzbName(name);
			try {
				data.get(i).setFinishDate(DateUtil.date(data.get(i).getFinishDate()));
			} catch (Exception e) {
				System.out.println("异常来自第" + (i + 2) + "行:"+e.toString());
			}
		}
		iQuarterlyEvaluationService.importQuarterlyEvaluation(data);
	}


}
