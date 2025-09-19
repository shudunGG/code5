package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.QuarterlySumScore;
import com.vingsoft.entity.SingleCheckAppraise;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.mapper.SingleCheckAppraiseMapper;
import org.springblade.integrated.platform.service.ISingleCheckAppraiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @className: SingleCheckAppraiseServiceImpl
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 15:13 星期二
 * @Version 1.0
 **/
@Service
public class SingleCheckAppraiseServiceImpl extends BaseServiceImpl<SingleCheckAppraiseMapper, SingleCheckAppraise> implements ISingleCheckAppraiseService {


	@Override
	public R<List<SingleCheckAppraise>> getIndexStatistics(SingleCheckAppraise singleCheckAppraise) {
		QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
		queryWrapper.select(" area_type,\n" +
							"appraise_object_id,\n" +
							"appraise_object_name,\n" +
							"sum(score) as score");
		//指标名称模糊搜索
		queryWrapper.like(singleCheckAppraise.getTargetName() !=null && !Objects.equals(singleCheckAppraise.getTargetName(), ""),"target_name",singleCheckAppraise.getTargetName());
		//年度搜索
		queryWrapper.eq(singleCheckAppraise.getYear() !=null && !Objects.equals(singleCheckAppraise.getYear(), ""),"year",singleCheckAppraise.getYear());
		//季度搜索
		queryWrapper.eq(singleCheckAppraise.getQuarter() !=null && !Objects.equals(singleCheckAppraise.getQuarter(), ""),"quarter",singleCheckAppraise.getQuarter());
		//县区、市值搜索
		queryWrapper.eq(singleCheckAppraise.getAreaType() !=null && !Objects.equals(singleCheckAppraise.getAreaType(), ""),"area_type",singleCheckAppraise.getAreaType());
		//考核对象搜索
		queryWrapper.eq(singleCheckAppraise.getAppraiseObjectId() !=null && !Objects.equals(singleCheckAppraise.getAppraiseObjectId(), ""),"appraise_object_id",singleCheckAppraise.getAppraiseObjectId());
		queryWrapper.eq("is_deleted",0);
		queryWrapper.eq("is_send",1);
		queryWrapper.groupBy("appraise_object_id");
		queryWrapper.orderByDesc("score");

		List<SingleCheckAppraise> singleCheckAppraiseList = this.list(queryWrapper);

		return R.data(singleCheckAppraiseList);
	}
}
