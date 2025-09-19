package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.vo.LeaderAppriseVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:33
 */
public class LeaderAppriseWrapper extends BaseEntityWrapper<LeaderApprise, LeaderAppriseVO> {

	public static LeaderAppriseWrapper build() {
		return new LeaderAppriseWrapper();
	}


	@Override
	public LeaderAppriseVO entityVO(LeaderApprise leaderApprise) {
		LeaderAppriseVO leaderAppriseVO = Objects.requireNonNull(BeanUtil.copy(leaderApprise, LeaderAppriseVO.class));
		return leaderAppriseVO;
	}
}
