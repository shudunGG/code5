package org.springblade.integrated.platform.service;


import com.vingsoft.entity.AnnualAssessment;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.*;

import java.util.List;

/**
 * @author zrj
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:12
 */
public interface IAnnualAssessmentService extends BaseService<AnnualAssessment> {

	/**
	 * 新增
	 * @param annualAssessment
	 * @return
	 */
	boolean saveAnnualAssessment(AnnualAssessment annualAssessment);

	/**
	 * 修改
	 * @param annualAssessment
	 * @return
	 */
	boolean uptAnnualAssessment(AnnualAssessment annualAssessment);

	/**
	 * 导出1政治思想建设数据
	 * @param ae
	 * @return
	 */
	List<ZzsxjsExcel> exportZzsxjsAssessment(AnnualAssessment ae);

	/**
	 * 导出2领导能力数据
	 * @param ae
	 * @return
	 */
	List<LdnlExcel> exportLdnlAssessment(AnnualAssessment ae);

	/**
	 * 导出3党的建设数据
	 * @param ae
	 * @return
	 */
	List<DdjsExcel> exportDdjsAssessment(AnnualAssessment ae);

	/**
	 * 导出4市直高质量发展数据
	 * @param ae
	 * @return
	 */
	List<SgzlfzExcel> exportSgzlfzAssessment(AnnualAssessment ae);

	/**
	 * 导出5区县高质量发展数据
	 * @param ae
	 * @return
	 */
	List<QxgzlfzExcel> exportQxgzlfzAssessment(AnnualAssessment ae);




	/**
	 * 导入年度评价数据
	 * @param data
	 * @return
	 */
	void importZzsxjsAssessment(List<AnnualAssessmentExcel> data);

	/**
	 * 根据id查询明细
	 * @param id
	 * @return
	 */
	AnnualAssessment details(Long id);

	/**
	 * 修改阶段
	 * @param ae
	 * @return
	 */
	boolean uptStage(AnnualAssessment ae);
}
