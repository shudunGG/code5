package org.springblade.integrated.platform.service;

import com.vingsoft.entity.QuarterlyAssessment;
import com.vingsoft.entity.QuarterlyAssessment;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.*;

import java.util.List;


/**
 * 服务类
 *
 * @Author zrj
 * @Create 2022/4/9 17:30
 */
public interface IQuarterlyAssessmentService extends BaseService<QuarterlyAssessment> {

	/**
	 * 导入季度评价数据
	 *
	 * @param data
	 * @return
	 */
	void importQuarterlyAssessment(List<QuarterlyAssessmentExcel> data);

	/**
	 * 导出季度评价-党建工作
	 *
	 * @param quarterlyAssessment
	 * @return
	 */
	//List<QuarterlyAssessmentExcel1> exportQuarterlyAssessment1(QuarterlyAssessment quarterlyAssessment);

	/**
	 * 导出季度评价-工作实绩
	 *
	 * @param quarterlyAssessment
	 * @return
	 */
	//List<QuarterlyAssessmentExcel2> exportQuarterlyAssessment2(QuarterlyAssessment quarterlyAssessment);

	/**
	 * 导出季度评价-党风廉政
	 *
	 * @param quarterlyAssessment
	 * @return
	 */
	//List<QuarterlyAssessmentExcel3> exportQuarterlyAssessment3(QuarterlyAssessment quarterlyAssessment);


	/**
	 * 新增
	 * @param qe
	 * @return
	 */
	boolean saveAssessment(QuarterlyAssessment qe);

	/**
	 * 修改
	 * @param qe
	 * @return
	 */
	boolean uptAssessment(QuarterlyAssessment qe);

	/**
	 * 根据id查询明细
	 *
	 * @param id
	 * @return
	 */
	QuarterlyAssessment details(Long id);

	/**
	 * 修改阶段
	 * @param qe
	 * @return
	 */
	boolean uptStage(QuarterlyAssessment qe);

}
