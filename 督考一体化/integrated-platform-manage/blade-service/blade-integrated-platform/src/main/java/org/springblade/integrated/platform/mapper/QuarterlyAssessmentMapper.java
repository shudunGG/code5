package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.QuarterlyAssessment;
import com.vingsoft.entity.QuarterlyEvaluation;
import org.springblade.integrated.platform.excel.QuarterlyEvaluationExcel1;
import org.springblade.integrated.platform.excel.QuarterlyEvaluationExcel2;
import org.springblade.integrated.platform.excel.QuarterlyEvaluationExcel3;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 17:15
 */
public interface QuarterlyAssessmentMapper extends BaseMapper<QuarterlyAssessment> {

	/**
	 * 导出季度评价-党建工作
	 *
	 * @param quarterlyEvaluation
	 * @return
	 */
	List<QuarterlyEvaluationExcel1> exportQuarterlyEvaluation1(QuarterlyEvaluation quarterlyEvaluation);


	/**
	 * 导出季度评价-工作实绩
	 *
	 * @param quarterlyEvaluation
	 * @return
	 */
	List<QuarterlyEvaluationExcel2> exportQuarterlyEvaluation2(QuarterlyEvaluation quarterlyEvaluation);



	/**
	 * 导出季度评价-党风廉政
	 *
	 * @param quarterlyEvaluation
	 * @return
	 */
	List<QuarterlyEvaluationExcel3> exportQuarterlyEvaluation3(QuarterlyEvaluation quarterlyEvaluation);




}
