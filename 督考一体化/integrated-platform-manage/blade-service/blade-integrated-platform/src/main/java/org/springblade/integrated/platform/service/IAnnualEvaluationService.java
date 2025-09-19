package org.springblade.integrated.platform.service;/**
 * @author TangYanXing
 * @date 2022-04-09 14:12
 */

import com.vingsoft.entity.AnnualEvaluation;
import com.vingsoft.entity.QuarterlyEvaluation;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:12
 */
public interface IAnnualEvaluationService extends BaseService<AnnualEvaluation> {

	/**
	 * 新增
	 * @param annualEvaluation
	 * @return
	 */
	boolean saveAnnualEvaluation(AnnualEvaluation annualEvaluation);

	/**
	 * 修改
	 * @param annualEvaluation
	 * @return
	 */
	boolean uptAnnualEvaluation(AnnualEvaluation annualEvaluation);

	/**
	 * 导出1政治思想建设数据
	 * @param ae
	 * @return
	 */
	List<ZzsxjsExcel> exportZzsxjsEvaluation(AnnualEvaluation ae);

	/**
	 * 导出2领导能力数据
	 * @param ae
	 * @return
	 */
	List<LdnlExcel> exportLdnlEvaluation(AnnualEvaluation ae);

	/**
	 * 导出3党的建设数据
	 * @param ae
	 * @return
	 */
	List<DdjsExcel> exportDdjsEvaluation(AnnualEvaluation ae);

	/**
	 * 导出4市直高质量发展数据
	 * @param ae
	 * @return
	 */
	List<SgzlfzExcel> exportSgzlfzEvaluation(AnnualEvaluation ae);

	/**
	 * 导出5区县高质量发展数据
	 * @param ae
	 * @return
	 */
	List<QxgzlfzExcel> exportQxgzlfzEvaluation(AnnualEvaluation ae);




	/**
	 * 导入年度评价数据
	 * @param data
	 * @return
	 */
	void importZzsxjsEvaluation(List<AnnualEvaluationExcel> data);

	/**
	 * 根据id查询明细
	 * @param id
	 * @return
	 */
	AnnualEvaluation details(Long id);

	/**
	 * 修改阶段
	 * @param ae
	 * @return
	 */
	boolean uptStage(AnnualEvaluation ae);
}
