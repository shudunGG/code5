package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ScoreAdd;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.excel.ScoreAddExcel;

import java.util.List;

/**
 *  服务类
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
public interface IScoreAddService extends BaseService<ScoreAdd> {

	/**
	 * 按条件查询Score_add表中的数据
	 * @param score_add
	 * @return
	 */
	ScoreAdd selectDetail(ScoreAdd score_add);



	/**
	 * 导出加分数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<ScoreAddExcel> exportScoreAdd(Wrapper<ScoreAdd> queryWrapper);

	boolean updateScoreAddIsSend(String year);


}
