package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ScoreAdd;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.excel.ScoreAddExcel;
import org.springblade.integrated.platform.mapper.ScoreAddMapper;
import org.springblade.integrated.platform.service.IScoreAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
@Service
public class ScoreAddServiceImpl extends BaseServiceImpl<ScoreAddMapper, ScoreAdd> implements IScoreAddService {

	@Override
	public ScoreAdd selectDetail(ScoreAdd score_add) {
		QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
		queryWrapper.select(" * ");
		queryWrapper.eq(score_add.getId()!=0,"id",score_add.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	public List<ScoreAddExcel> exportScoreAdd(Wrapper<ScoreAdd> queryWrapper) {
		return baseMapper.exportScoreAdd(queryWrapper);
	}

	@Override
	public boolean updateScoreAddIsSend(String year) {
		return baseMapper.updateScoreAddIsSend(year);
	}

}
