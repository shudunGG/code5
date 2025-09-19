package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vingsoft.entity.OaEmail;
import com.vingsoft.vo.OaEmailVO;
import org.apache.ibatis.annotations.Param;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/11 09:40
 */
public interface OaEmailMapper extends BaseMapper<OaEmail> {

	/**
	 * 收件箱分页查询
	 * @param page
	 * @param userId
	 * @param title
	 * @param readStatus
	 * @return
	 */
	IPage<OaEmail> findInboxPage(IPage<OaEmail> page, @Param("userId") Long userId, @Param("title") String title, @Param("readStatus") Integer readStatus);
}
