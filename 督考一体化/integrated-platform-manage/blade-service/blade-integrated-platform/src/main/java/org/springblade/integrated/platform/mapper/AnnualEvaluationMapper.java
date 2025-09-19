package org.springblade.integrated.platform.mapper;/**
 * @author TangYanXing
 * @date 2022-04-09 14:22
 */

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.AnnualEvaluation;
import com.vingsoft.entity.LeaderApprise;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.*;
import org.springblade.system.entity.Tenant;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:22
 */
@Repository
public interface AnnualEvaluationMapper extends BaseMapper<AnnualEvaluation> {

	/**
	 * 导出年度评价政治思想建设数据
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

}
