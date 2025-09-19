package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.ScoreAdd;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;
import org.springblade.integrated.platform.excel.ScoreAddExcel;
import org.springblade.integrated.platform.mapper.LeaderAppriseMapper;
import org.springblade.integrated.platform.mapper.ScoreAddMapper;
import org.springblade.integrated.platform.service.ILeaderAppriseService;
import org.springblade.integrated.platform.service.IScoreAddService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/9 13:45
 */
@Service
public class LeaderAppriseServiceImpl extends BaseServiceImpl<LeaderAppriseMapper, LeaderApprise> implements ILeaderAppriseService {


	@Override
	public LeaderApprise selectDetail(LeaderApprise leaderApprise) {
		QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
		queryWrapper.select(" * ");
		queryWrapper.eq(leaderApprise.getId()!=0,"id",leaderApprise.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	public List<LeaderAppriseExcel> exportLeaderApprise(Wrapper<LeaderApprise> queryWrapper) {
		List<LeaderAppriseExcel> scoreList = baseMapper.exportLeaderApprise(queryWrapper);
		return scoreList;
	}


}
