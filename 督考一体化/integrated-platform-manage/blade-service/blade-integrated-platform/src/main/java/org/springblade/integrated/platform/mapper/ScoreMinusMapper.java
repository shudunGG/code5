package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ScoreMinus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.ScoreMinusExcel;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */

public interface ScoreMinusMapper extends BaseMapper<ScoreMinus> {

	/**
	 * 导出加分数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<ScoreMinusExcel> exportScoreMinus(@Param("qw") Wrapper<ScoreMinus> queryWrapper);

	//改变is_send发布状态
	int updateScoreMinusIsSend(String year);

}
