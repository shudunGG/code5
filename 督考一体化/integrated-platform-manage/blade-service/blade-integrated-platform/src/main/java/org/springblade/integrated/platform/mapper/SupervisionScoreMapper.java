package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionScore;
import com.vingsoft.vo.SupervisionDeptScoretVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SupervisionScoreMapper extends BaseMapper<SupervisionScore> {


//	@Select("SELECT ifnull(ROUND(avg(score),2),0.00) from supervision_score  where ${ew.sqlSegment} ")
	@Select("SELECT ifnull(ROUND(avg(t.score),2),0.00) from (" +
		"SELECT SUM(score) score ,s.serv_code,s.dept_id  from supervision_score s left join supervision_info i on s.serv_code = i.serv_code where i.flow_status ='4' and s.create_time BETWEEN '${startTime}' AND '${endTime}' GROUP by s.serv_code,s.dept_id"+
		")t  where ${ew.sqlSegment} ")
	BigDecimal deptScoreStatistics( @Param("ew") QueryWrapper<SupervisionScore> ew,@Param("startTime")String startTime,@Param("endTime") String endTime);

//	@Select("SELECT ifnull(ROUND(avg(score),2),0.00) score,dept_id from supervision_score  where ${ew.sqlSegment} group by dept_id ")
	@Select("SELECT avg(t.score) score,t.serv_code,t.dept_id from (" +
		      "SELECT CASE WHEN sum(score) > 0 THEN sum(score) ELSE 0 END  as score ,s.serv_code,s.dept_id  from supervision_score s left JOIN supervision_info i on s.serv_code = i.serv_code where i.flow_status ='4' and s.create_time BETWEEN '${startTime}' AND '${endTime}' GROUP by s.serv_code,s.dept_id"+
		     ")t  where ${ew.sqlSegment} GROUP BY t.dept_id")
	List<SupervisionDeptScoretVO> deptScoreStatisticsGroupby(@Param("ew") QueryWrapper<SupervisionScore> ew,@Param("startTime")String startTime,@Param("endTime") String endTime);

	@Select("SELECT CASE WHEN sum(score) > 0 THEN sum(score) ELSE 0 END  as score,s.dept_id,s.serv_Code,GROUP_CONCAT(details) details from supervision_score s left JOIN supervision_info i on s.serv_code = i.serv_code where i.flow_status ='4' and  ${ew.sqlSegment}   group by dept_id,serv_Code")
	List<SupervisionDeptScoretVO> deptScoreServ(@Param("ew") QueryWrapper<SupervisionScore> ew);

}
