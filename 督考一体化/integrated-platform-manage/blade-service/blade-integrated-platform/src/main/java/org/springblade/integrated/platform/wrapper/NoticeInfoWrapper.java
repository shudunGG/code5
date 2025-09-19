package org.springblade.integrated.platform.wrapper;

import com.alibaba.fastjson.JSON;
import com.vingsoft.entity.NoticeInfo;
import com.vingsoft.vo.NoticeInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;

import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 01:02
 */
@Slf4j
public class NoticeInfoWrapper extends BaseEntityWrapper<NoticeInfo, NoticeInfoVO> {

	public static NoticeInfoWrapper build(){
		return new NoticeInfoWrapper();
	}

	@Override
	public NoticeInfoVO entityVO(NoticeInfo entity) {
		NoticeInfoVO vo = Objects.requireNonNull(BeanUtil.copy(entity,NoticeInfoVO.class));

		try {
			if(StringUtils.isNotEmpty(vo.getFilesUrl())){
				vo.setFilesUrlJson(JSON.parseArray(vo.getFilesUrl()));
			}
			if(StringUtils.isNotEmpty(vo.getTextFileUrl())){
				vo.setTextFileUrlJson(JSON.parseArray(vo.getTextFileUrl()));
			}
		} catch (Exception e){
			log.error(e.getMessage());
		}

		return vo;
	}
}
