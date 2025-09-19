package org.springblade.plugin.workflow.design.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springblade.plugin.workflow.design.entity.WfSerial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 流程流水号 Mapper 接口
 *
 * @author ssc
 */
public interface WfSerialMapper extends BaseMapper<WfSerial> {

	@Update("UPDATE BLADE_WF_SERIAL SET sequence = start_sequence WHERE type = #{type}")
	void resetSN(@Param("type") String type);
}
