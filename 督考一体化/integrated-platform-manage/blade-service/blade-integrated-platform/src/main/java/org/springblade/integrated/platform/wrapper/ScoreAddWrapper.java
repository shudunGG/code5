package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.ScoreAdd;
import com.vingsoft.vo.ScoreAddVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:43
 */
public class ScoreAddWrapper extends BaseEntityWrapper<ScoreAdd, ScoreAddVO> {

	public static ScoreAddWrapper build() {
		return new ScoreAddWrapper();
	}


	@Override
	public ScoreAddVO entityVO(ScoreAdd score_add) {
		ScoreAddVO scoreAddVO = Objects.requireNonNull(BeanUtil.copy(score_add, ScoreAddVO.class));
		return scoreAddVO;
	}
}
