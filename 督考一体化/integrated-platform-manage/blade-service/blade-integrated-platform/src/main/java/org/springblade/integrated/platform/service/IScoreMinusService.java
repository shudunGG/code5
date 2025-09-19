package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ScoreMinus;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.ScoreMinusExcel;

import java.util.List;

/**
 *  服务类
 *
 * @Author JG🧸
 * @Create 2022/4/9 12:08
 */
public interface IScoreMinusService extends BaseService<ScoreMinus> {

	/**
	 * 按条件查询ScoreMinus表中的数据
	 * @param scoreMinus
	 * @return
	 */
	ScoreMinus selectDetail(ScoreMinus scoreMinus);



	/**
	 * 导出加分数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<ScoreMinusExcel> exportScoreMinus(Wrapper<ScoreMinus> queryWrapper);

	int updateScoreMinusIsSend(String year);


}
