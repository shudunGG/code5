package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.vo.SupervisionDeptPlanReportVO;
import com.vingsoft.vo.SupervisionFollowVO;
import com.vingsoft.vo.SupervisionInfoVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SupervisionInfoMapper extends BaseMapper<SupervisionInfo> {

	List<SupervisionInfo> queryList(@Param("ew") QueryWrapper<SupervisionInfoVO> ew, @Param("deptId") String deptId, @Param("userId")  String userId,
									  @Param("tbBus")  String tbBus);
	List<SupervisionInfoVO> queryListCB(@Param("ew") QueryWrapper<SupervisionInfoVO> ew, @Param("deptId") String deptId, @Param("userId")  String userId,
										@Param("tbBus")  String tbBus);

	Map<String,Object> listStatistics(@Param("servTypeThree") String servTypeThree,@Param("deptId") String deptId,@Param("year") String year);

	List<SupervisionFollowVO> supervisionFollow(@Param("ew") QueryWrapper<SupervisionInfo> ew);

	List<SupervisionSign> supervisionNoSign(@Param("ew") QueryWrapper<SupervisionInfo> ew);

	List<SupervisionInfo> supervisionNoReport(@Param("ew") QueryWrapper<SupervisionInfo> ew);

	List<SupervisionInfo> listStatisticsdhb(@Param("ew") QueryWrapper<SupervisionInfo> ew);

	List<SupervisionDeptPlanReportVO> servDeptPlanReport(@Param("ew") QueryWrapper<SupervisionInfo> ew ,@Param("evaluateType") String evaluateType);

	@InterceptorIgnore(tenantLine = "1")
	@Delete("delete from ${tableName} where ${ew.sqlSegment} ")
	int deleteByQueryWrapper(@Param("tableName") String tableName,@Param("ew") QueryWrapper<T> ew);

	SupervisionInfoVO detailsNew(@Param("ew") QueryWrapper<SupervisionInfo> ew,@Param("deptId") String deptId, @Param("userId")  String userId);

	List<SupervisionInfoVO> queryListfollow(@Param("ew") QueryWrapper<SupervisionInfoVO> ew, @Param("deptId") String deptId, @Param("userId")  String userId);

	@Select("select  from supervision_phase_report where serv_code = #{servCode} oder by end_time desc limit 1")
	SupervisionInfoVO selectplanid(@Param("servCode") String servCode,@Param("deptId")String deptId);

	@Delete("DELETE FROM supervision_sign  where ${ew.sqlSegment}")
	boolean deletedept(@Param("ew") QueryWrapper<SupervisionSign> ew );
}
