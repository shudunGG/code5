package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.ScoreMinus;
import com.vingsoft.vo.ScoreMinusVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author JG🧸
 * @Create 2022/4/9 12:07
 */
public class ScoreMinusWrapper extends BaseEntityWrapper<ScoreMinus, ScoreMinusVO> {

	public static ScoreMinusWrapper build() {
		return new ScoreMinusWrapper();
	}


	@Override
	public ScoreMinusVO entityVO(ScoreMinus scoreMinus) {
		ScoreMinusVO scoreMinusVO = Objects.requireNonNull(BeanUtil.copy(scoreMinus, ScoreMinusVO.class));
		return scoreMinusVO;
	}
}
