package org.springblade.integrated.platform.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.AnnualAssessment;
import com.vingsoft.entity.AnnualAssessment;
import org.springblade.integrated.platform.excel.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zrj
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:22
 */
@Repository
public interface AnnualAssessmentMapper extends BaseMapper<AnnualAssessment> {

	/**
	 * 导出年度评价政治思想建设数据
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

}
