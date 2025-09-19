package org.springblade.integrated.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.vingsoft.entity.WorkMonth;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.excel.WorkMonthExcel;
import org.springblade.integrated.platform.mapper.WorkMonthMapper;
import org.springblade.integrated.platform.service.IWorkMonthService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/9 13:45
 */
@Service
public class WorkMonthServiceImpl extends BaseServiceImpl<WorkMonthMapper, WorkMonth> implements IWorkMonthService {

	@Resource
	private IUserClient userClient;
	@Override
	public List<Map<String, Object>> workListPage(String month) {
		return baseMapper.workListPage(month);
	}

	@Override
	public List<Map<String, Object>> selectTime(String month, String jhqk, String deptCode) {
		return baseMapper.selectTime(month,jhqk,deptCode);
	}

	@Override
	public List<WorkMonth> detail(String month, String deptCode) {
		return baseMapper.detail(month, deptCode);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importWorkMonth(List<WorkMonthExcel> data) {
		List<WorkMonth> list = new ArrayList<>();
		AtomicInteger index  = new AtomicInteger();
		index.set(1);
		data.forEach(regionExcel -> {
			index.getAndIncrement();
			String plantTime = regionExcel.getPlanTime().toString();
			System.out.println("工作月调度导入：当前行："+index.get());
			WorkMonth region = BeanUtil.copy(regionExcel, WorkMonth.class);
			region.setPlanTime(plantTime);
			if (StringUtils.isEmpty(region.getMonth())) {
				throw new RuntimeException("计划所属月份不能为空！");
			}
			if (StringUtils.isEmpty(region.getConcent())) {
				throw new RuntimeException("工作内容不能为空！");
			}
			if (StringUtils.isEmpty(region.getPersonLiable())) {
				throw new RuntimeException("责任人不能为空！");
			}
			if (StringUtils.isEmpty(region.getPlanTime())) {
				throw new RuntimeException("计划完成时间不能为空！");
			}

			list.add(region);
		});

		QueryWrapper<WorkMonth> queryWrapper;
		StringBuilder sb = new StringBuilder();
		for (int t = 0; t < list.size(); t++) {
			queryWrapper = new QueryWrapper<>();
			queryWrapper.eq(list.get(t).getConcent()!=null,"concent",list.get(t).getConcent());
			List<WorkMonth> mapList = baseMapper.selectList(queryWrapper);
			WorkMonth workMonth;
			if (mapList.size() < 1) {
				workMonth = list.get(t);
				User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
				workMonth.setDeptCode(currentUser.getDeptId());
				String deptName = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
				workMonth.setDeptName(deptName);
				workMonth.setCompletion("2");  //默认未完成
				//当前时间戳-毫秒级
				String timeStamp = String.valueOf(System.currentTimeMillis());
				workMonth.setSort(timeStamp);
				this.save(workMonth);

			} else {
				sb.append("重复：>>>>>>>>>>>>>>第"+ (t+2)+"行数据已存在！\n");
			}
		}
		System.out.println("工作月调度sb:"+sb);
	}

	@Override
	public List<WorkMonthExcel> exportWorkMonth(Wrapper<WorkMonth> queryWrapper) {
		List<WorkMonthExcel> list = baseMapper.exportWorkMonth(queryWrapper);
		return list;
	}

}
