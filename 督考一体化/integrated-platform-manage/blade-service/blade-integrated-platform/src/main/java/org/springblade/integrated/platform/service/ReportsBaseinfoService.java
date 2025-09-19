package org.springblade.integrated.platform.service;

import com.vingsoft.entity.*;
import com.vingsoft.vo.ReportsBaseinfoVo;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-19 22:12
 */
public interface ReportsBaseinfoService extends BaseService<ReportsBaseinfo> {
	void saveForQuarter(QuarterlyEvaluation qe);

	void saveForQuarter(QuarterlyAssessment qe);

	void saveForAnnual(AnnualEvaluation ae);

	void saveForAnnualAssessment(AnnualAssessment ae);

	List<ReportsBaseinfoVo> findList(ReportsBaseinfo reportsBaseinfo);
	List<ReportsBaseinfoVo> findListAnnual(ReportsBaseinfo reportsBaseinfo);
}
