package org.springblade.integrated.platform.wrapper;

import com.alibaba.fastjson.JSON;
import com.vingsoft.entity.Feedback;
import com.vingsoft.vo.FeedbackVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.system.user.cache.UserCache;

import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 18:16
 */
public class FeedbackWrapper extends BaseEntityWrapper<Feedback, FeedbackVO> {

	public static FeedbackWrapper build(){
		return new FeedbackWrapper();
	}

	@Override
	public FeedbackVO entityVO(Feedback entity) {
		FeedbackVO vo = Objects.requireNonNull(BeanUtil.copy(entity,FeedbackVO.class));
		if(StringUtils.isNotEmpty(vo.getFiles())){
			vo.setFilesJson(JSON.parseArray(vo.getFiles()));
		}
		vo.setCreateUserName(UserCache.getUser(vo.getCreateUser()).getName());
		return vo;
	}
}
