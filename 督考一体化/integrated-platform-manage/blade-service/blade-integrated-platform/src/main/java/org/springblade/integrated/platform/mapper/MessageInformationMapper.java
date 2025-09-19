package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.MessageInformation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
public interface MessageInformationMapper extends BaseMapper<MessageInformation> {

	/**
	 * 项目留言
	 * @param id
	 * @return
	 */
	@Select("select * from message_information where business_id = #{id} and is_deleted = 0")
	List<MessageInformation> getMessageInformationListByProjId(@Param("id") String id);

}
