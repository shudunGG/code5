package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ScoreAdd;
import com.vingsoft.entity.ScoreMinus;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.excel.ScoreAddExcel;
import org.springblade.integrated.platform.excel.ScoreMinusExcel;
import org.springblade.integrated.platform.mapper.ScoreAddMapper;
import org.springblade.integrated.platform.mapper.ScoreMinusMapper;
import org.springblade.integrated.platform.service.IScoreAddService;
import org.springblade.integrated.platform.service.IScoreMinusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/9 12:15
 */
@Service
public class ScoreMinusServiceImpl extends BaseServiceImpl<ScoreMinusMapper, ScoreMinus> implements IScoreMinusService {

	@Override
	public ScoreMinus selectDetail(ScoreMinus scoreMinus) {
		QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
		queryWrapper.select(" * ");
		queryWrapper.eq(scoreMinus.getId()!=0,"id",scoreMinus.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	public List<ScoreMinusExcel> exportScoreMinus(Wrapper<ScoreMinus> queryWrapper) {
		List<ScoreMinusExcel> scoreList = baseMapper.exportScoreMinus(queryWrapper);
		return scoreList;
	}


	@Override
	public int updateScoreMinusIsSend(String year){
		return baseMapper.updateScoreMinusIsSend(year);
	}
}
