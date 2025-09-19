package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.ReportsBaseinfo;
import com.vingsoft.entity.ScoreMinus;
import com.vingsoft.vo.ReportsBaseinfoVo;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.ScoreMinusExcel;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-20 10:24
 */
public interface ReportsBaseinfoMapper extends BaseMapper<ReportsBaseinfo> {

	/**
	 * 查询单位阶段汇报信息
	 * @param
	 * @return
	 */
	List<ReportsBaseinfoVo> findList(ReportsBaseinfo reportsBaseinfo);
	List<ReportsBaseinfoVo> findListAnnual(ReportsBaseinfo reportsBaseinfo);

}
