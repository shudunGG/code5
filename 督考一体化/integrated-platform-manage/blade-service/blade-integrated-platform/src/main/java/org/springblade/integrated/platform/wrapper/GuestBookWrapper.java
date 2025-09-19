package org.springblade.integrated.platform.wrapper;

import com.alibaba.fastjson.JSON;
import com.vingsoft.entity.GuestBook;
import com.vingsoft.vo.GuestBookVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;

import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 18:16
 */
public class GuestBookWrapper extends BaseEntityWrapper<GuestBook, GuestBookVO> {

	public static GuestBookWrapper build(){
		return new GuestBookWrapper();
	}

	@Override
	public GuestBookVO entityVO(GuestBook entity) {
		GuestBookVO vo = Objects.requireNonNull(BeanUtil.copy(entity,GuestBookVO.class));
		if(StringUtils.isNotEmpty(vo.getFiles())){
			vo.setFilesJson(JSON.parseArray(vo.getFiles()));
		}
		return vo;
	}
}
