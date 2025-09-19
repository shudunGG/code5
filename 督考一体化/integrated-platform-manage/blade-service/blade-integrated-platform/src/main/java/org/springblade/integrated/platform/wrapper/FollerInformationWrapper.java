package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.FollowInformation;
import com.vingsoft.vo.FollowInformationVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author AdamJin
 * @Create 2022-4-13 16:41:40
 */
public class FollerInformationWrapper extends BaseEntityWrapper<FollowInformation, FollowInformationVO> {

	public static FollerInformationWrapper build() {
		return new FollerInformationWrapper();
	}


	@Override
	public FollowInformationVO entityVO(FollowInformation followInformation) {
		FollowInformationVO followInformationVO = Objects.requireNonNull(BeanUtil.copy(followInformation, FollowInformationVO.class));
		return followInformationVO;
	}
}
