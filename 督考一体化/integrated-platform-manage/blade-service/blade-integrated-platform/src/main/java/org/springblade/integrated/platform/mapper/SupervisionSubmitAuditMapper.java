package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionSubmitAudit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/18 17:31
 */
public interface SupervisionSubmitAuditMapper extends BaseMapper<SupervisionSubmitAudit> {

	/**
	 * 修改下一个人的状态
	 * @param servId
	 * @param sort
	 * @return
	 */
	@Update("update supervision_submit_audit set `status` = 0 where serv_id = #{servId} and sort = #{sort} and batch_number = #{batchNumber}")
	int updateNextUserStatus(@Param("servId") Long servId, @Param("sort") Integer sort, @Param("batchNumber") String batchNumber);

	/**
	 * 统计当前事项是否有待审核或未通过的送审数据
	 * @param servId
	 * @param batchNumber
	 * @return
	 */
	@Select("select count(1) from supervision_submit_audit where serv_id = ${servId} and batch_number = #{batchNumber} and (`status` = 0 or `status` = 2)")
	int countAuditNumber(@Param("servId") Long servId,@Param("batchNumber") String batchNumber);

	/**
	 * 根据项目id和登录人信息获取待审核信息
	 * @param servId
	 * @param userId
	 * @return
	 */
	@Select("select * from supervision_submit_audit where serv_id = ${servId} and user_id = ${userId} and status = 0 order by sort asc LIMIT 1 ")
	SupervisionSubmitAudit getAuditByservIdAndUserId(@Param("servId") Long servId,@Param("userId") Long userId);

}
