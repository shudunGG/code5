package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.QuarterlySumScore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
public interface QuarterlySumScoreMapper extends BaseMapper<QuarterlySumScore> {


    QuarterlySumScore avgQuarterlySumScore(Map<String, Object> map);

	/*@Select("select ifnull(sum(weight), 0) totalWeight from quarterly_evaluation " +
		"where check_object_id like CONCAT('%',#{responsibleUnitId},'%') " +
		"and DATE_FORMAT(create_time, '%Y-%m-%d') like CONCAT('%',#{stageYear},'%') " +
		"and jdzb_type = #{jdzbType} " +
		"and to_quarter = #{stage} " +
		"and is_deleted = '0'")*/

    Map<String, Object> getTotalWeight(@Param("responsibleUnitId")String responsibleUnitId,@Param("stageYear")String stageYear,@Param("stage")String stage, @Param("jdzbType")String jdzbType);
}
