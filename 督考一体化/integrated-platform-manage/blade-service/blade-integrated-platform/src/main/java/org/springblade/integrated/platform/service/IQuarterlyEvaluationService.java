package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.AnnualEvaluation;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.QuarterlyEvaluation;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.*;

import java.util.List;


/**
 * 服务类
 *
 * @Author JG🧸
 * @Create 2022/4/9 17:30
 */
public interface IQuarterlyEvaluationService extends BaseService<QuarterlyEvaluation> {

	/**
	 * 导入季度评价数据
	 *
	 * @param data
	 * @return
	 */
	void importQuarterlyEvaluation(List<QuarterlyEvaluationExcel> data);

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


	/**
	 * 新增
	 * @param qe
	 * @return
	 */
	boolean saveEvaluation(QuarterlyEvaluation qe);

	/**
	 * 修改
	 * @param qe
	 * @return
	 */
	boolean uptEvaluation(QuarterlyEvaluation qe);

	/**
	 * 根据id查询明细
	 * @param id
	 * @return
	 */
	QuarterlyEvaluation details(Long id);

	/**
	 * 修改阶段
	 * @param qe
	 * @return
	 */
	boolean uptStage(QuarterlyEvaluation qe);

}
