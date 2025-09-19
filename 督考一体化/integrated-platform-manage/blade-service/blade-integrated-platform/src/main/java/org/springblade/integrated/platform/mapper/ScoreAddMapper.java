package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.ScoreAdd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.ScoreAddExcel;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */

public interface ScoreAddMapper extends BaseMapper<ScoreAdd> {

	/**
	 * 导出加分数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<ScoreAddExcel> exportScoreAdd(@Param("qw") Wrapper<ScoreAdd> queryWrapper);

	boolean updateScoreAddIsSend(@Param("year") String year);


}
