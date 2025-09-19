/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.integrated.platform.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vingsoft.entity.*;
import lombok.AllArgsConstructor;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 用户服务Feign实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class SupervisionClient implements ISupervisionClient {

	private final ISupervisionPhaseReportService service;

	private final ISupervisionInfoService supervisionInfoService;

	private final IUnifyMessageService unifyMessageService;

	private final IUserClient userClient;

	private final IDictBizClient dictBizClient;

	private final ISupervisionScoreService supervisionScoreService;

	private final ISupervisionSignService supervisionSignService;

	@Override
	public R<Boolean> overdueNoSign() {
		boolean flag=true;
		List<SupervisionSign> list = supervisionSignService.getOverdueNoSignList();
		for(SupervisionSign sign:list){
			SupervisionInfo supervisionInfo = supervisionInfoService.getById(sign.getServId());
			//超期扣分
			BigDecimal score = this.overdueScore(sign.getWcsx(), DateUtils.getNowDate());
			//获取该事项是否超期扣分
			QueryWrapper<SupervisionScore> wrapperScore=new QueryWrapper();
			wrapperScore.eq("serv_Code",supervisionInfo.getServCode());
			wrapperScore.eq("dept_Id",sign.getSignDept());
			wrapperScore.eq("score_Type","4");
			SupervisionScore supervisionScore = supervisionScoreService.getOne(wrapperScore);
			if(ObjectUtil.isNotEmpty(supervisionScore)){
				if(supervisionScore.getScore().compareTo(score)!=0){
					supervisionScore.setScore(score);
					supervisionScore.setDetails("事项超期未汇报扣分(未签收):"+score);
					flag=supervisionScoreService.updateById(supervisionScore);
				}
			}else{
				SupervisionScore supervisionScoreNew=new SupervisionScore();
				supervisionScoreNew.setScore(score);
				supervisionScoreNew.setScoreType("4");
				supervisionScoreNew.setServCode(supervisionInfo.getServCode());
				supervisionScoreNew.setDeptId(sign.getSignDept());
				supervisionScoreNew.setDetails("事项超期未汇报扣分(未签收):"+score);
				flag=supervisionScoreService.save(supervisionScoreNew);
			}
		}
		return null;
	}

	/**
	 * 获取阶段超期数据
	 * @return
	 */
	@Override
	public R<List<SupervisionPhaseReport>> phaseOverdue() {
		return  R.data(service.phaseOverdue());
	}

	@Override
	public R<Boolean> updatePhaseReport() {
		QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper<>();
		wrapper.in("report_status","0","3","4","6");
		wrapper.lt("remind_report_time", DateUtils.getTime());
		wrapper.ne("Status",2);
		wrapper.isNull("parent_Id");
		List<SupervisionPhaseReport> list = service.list(wrapper);
		boolean flag=true;
		for(SupervisionPhaseReport report:list){

			//2023年2月20日 删除关联事项表状态不为4（已完成）的数据 start。
			SupervisionInfo supervisionInfo = supervisionInfoService.getOne(Wrappers.<SupervisionInfo>query().lambda()
				.eq(SupervisionInfo::getServCode, report.getServCode()).last(" limit 1"));
			if(supervisionInfo != null && supervisionInfo.getServStatus().equals("4")){
				list.remove(report);
				continue;
			}
			//2023年2月20日 删除关联事项表状态不为4（已完成）的数据 end。

			if(report.getReportStatus().equals("0")&&report.getStatus()!=6){
				report.setReportStatus("6");
			}
			report.setStatus(6);
			flag = service.updateById(report);
			//超期扣分
			BigDecimal score = this.overdueScore(report.getRemindReportTime(), DateUtils.getNowDate());
			//获取该事项是否超期扣分
			QueryWrapper<SupervisionScore> wrapperScore=new QueryWrapper();
			wrapperScore.eq("serv_Code",report.getServCode());
			wrapperScore.eq("dept_Id",Long.valueOf(report.getReportDept()));
			wrapperScore.eq("score_Type","4");
			SupervisionScore supervisionScore = supervisionScoreService.getOne(wrapperScore);
			if(ObjectUtil.isNotEmpty(supervisionScore)){
				if(supervisionScore.getScore().compareTo(score)!=0){
					supervisionScore.setScore(score);
					supervisionScore.setDetails("事项超期未汇报扣分:"+score);
					flag=supervisionScoreService.updateById(supervisionScore);
				}
			}else{
				SupervisionScore supervisionScoreNew=new SupervisionScore();
				supervisionScoreNew.setScore(score);
				supervisionScoreNew.setScoreType("4");
				supervisionScoreNew.setServCode(report.getServCode());
				supervisionScoreNew.setDeptId(Long.valueOf(report.getReportDept()));
				supervisionScoreNew.setDetails("事项超期未汇报扣分:"+score);
				flag=supervisionScoreService.save(supervisionScoreNew);
			}
		}
		return R.data(flag);
	}

	@Override
	public R<Boolean> updateSupervisionOverdue() {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		wrapper.ne("flow_status","0");
		wrapper.ne("flow_status","4");
		wrapper.ne("serv_status","3");
		wrapper.lt("date_format(wcsx, '%Y-%m-%d %H:%i:%s')", DateUtils.getTime());
		List<SupervisionInfo> infos = supervisionInfoService.list(wrapper);
		boolean flag=false;
		List<User> userList=new ArrayList<>();
		for(SupervisionInfo info:infos){

			info.setServStatus("3");
			 flag = supervisionInfoService.updateById(info);
			String receiveUser =info.getCreateUser().toString()+"," ;
			//责任领导
			String dutyLeader = info.getDutyLeader();
			if(ObjectUtil.isNotEmpty(dutyLeader)){
				String[] dutyLeadeID = dutyLeader.split(",");
				for(String dID:dutyLeadeID){
					R<User> userR = userClient.userInfoById(Long.valueOf(dID));
					User user=userR.getData();
					if(ObjectUtil.isNotEmpty(user)){
						userList.add(user);
					}
				}
			}
			//牵头单位
			String leadUnit = info.getLeadUnit();
			String[] leadUnits = leadUnit.split(",");
			for (String lead : leadUnits) {
				//发遂消息  岗位:"管理员ID"1516056792837869570
				R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
				if(ObjectUtil.isNotEmpty(userLeader.getData())){
					userList.addAll(userLeader.getData());
				}
			}
			//责任单位
			String dutyUnit = info.getDutyUnit();
			String[] dutyUnits = null;
			if(ObjectUtil.isNotEmpty(dutyUnit)){
				dutyUnits = dutyUnit.split(",");
			}
			if(ObjectUtil.isNotEmpty(dutyUnits)){
				for (String duty : dutyUnits) {
					//获取需要发送消息的人员
					R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
					if(ObjectUtil.isNotEmpty(userLeader.getData())){
						userList.addAll(userLeader.getData());
					}
				}
			}
			userList =  userList.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
			for (User user1 : userList) {
				receiveUser += user1.getId()+",";
			}

			//督查督办人
			User userdb = userClient.userInfoById(info.getCreateUser()).getData();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(info.getId());
			unifyMessage.setMsgTitle("事项超期");
			unifyMessage.setMsgType("62");
			unifyMessage.setMsgPlatform("web");
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro("【"+info.getServName()+"】事项已超期!");
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);
			//sysClie
			R<String> value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo());
			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType("8");
			unifyMessage.setMsgSubitem(value.getData());
			unifyMessage.setTwoLevelType("62");
			unifyMessageService.sendMessageInfo(unifyMessage);
		}



		return R.data(flag);
	}

	@Override
	public R<Boolean> updateSupervisionNamely() {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		wrapper.ne("flow_status","0");
		wrapper.ne("flow_status","4");
//		wrapper.eq("date_format(wcsx, '%Y-%m-%d')",DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,DateUtils.getTomorrow()) );
		//1:正常推进;2:已完成;3:已超期;4:即将超期;5:超级已完成
		wrapper.notIn("serv_status","3","4","5");
		wrapper.apply("timestampdiff(HOUR, date_format(wcsx, '%Y-%m-%d'), date_format(now(), '%Y-%m-%d'))= 24");

		List<SupervisionInfo> list = supervisionInfoService.list(wrapper);
		boolean flag=true;
		for(SupervisionInfo info:list){
			info.setServStatus("4");
			flag = supervisionInfoService.update(info,wrapper);
			//发送消息
			List<User> userList=new ArrayList<>();
			String receiveUser =info.getCreateUser().toString()+"," ;
			//责任领导
			String dutyLeader = info.getDutyLeader();
			if(ObjectUtil.isNotEmpty(dutyLeader)){
				String[] dutyLeadeID = dutyLeader.split(",");
				for(String dID:dutyLeadeID){
					R<User> userR = userClient.userInfoById(Long.valueOf(dID));
					User user=userR.getData();
					if(ObjectUtil.isNotEmpty(user)){
						userList.add(user);
					}
				}
			}
			//牵头单位
			String leadUnit = info.getLeadUnit();
			String[] leadUnits = leadUnit.split(",");
			for (String lead : leadUnits) {
				//发遂消息  岗位:"管理员ID"1516056792837869570
				R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
				if(ObjectUtil.isNotEmpty(userLeader.getData())){
					userList.addAll(userLeader.getData());
				}
			}
			//责任单位
			String dutyUnit = info.getDutyUnit();
			String[] dutyUnits = null;
			if(ObjectUtil.isNotEmpty(dutyUnit)){
				dutyUnits = dutyUnit.split(",");
			}
			if(ObjectUtil.isNotEmpty(dutyUnits)){
				for (String duty : dutyUnits) {
					//获取需要发送消息的人员
					R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
					if(ObjectUtil.isNotEmpty(userLeader.getData())){
						userList.addAll(userLeader.getData());
					}
				}
			}
			userList =  userList.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
			for (User user1 : userList) {
				receiveUser += user1.getId()+",";
			}

			//督查督办人
			User userdb = userClient.userInfoById(info.getCreateUser()).getData();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(info.getId());
			unifyMessage.setMsgTitle("事项即将超期");
			unifyMessage.setMsgType("64");
			unifyMessage.setMsgPlatform("web");
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro("【"+info.getServName()+"】即将超期!");
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);
			//sysClie
			R<String> value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo());
			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType("8");
			unifyMessage.setMsgSubitem(value.getData());
			unifyMessage.setTwoLevelType("63");
			unifyMessageService.sendMessageInfo(unifyMessage);

		}
		return R.data(flag);
	}
	static <T> Predicate<T> distinctByKey1(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private BigDecimal overdueScore(Date startDate,Date endDate){
		Long day = DateUtils.getDatePoorToDay(DateUtils.dateTime(DateUtils.YYYY_MM_DD,DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,endDate)),
			DateUtils.dateTime(DateUtils.YYYY_MM_DD,DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,startDate))
			);
		BigDecimal score= BigDecimal.valueOf(0);
		if(day>=0&&day<3){
			score=Constants.OVERDUE_SCORE_ONE.setScale(2,BigDecimal.ROUND_HALF_UP);
		}else if(day>=3&&day<5){
			score=Constants.OVERDUE_SCORE_THREE.setScale(2,BigDecimal.ROUND_HALF_UP);
		}else if(day>=5&&day<8){
			score=Constants.OVERDUE_SCORE_FIVE.setScale(2,BigDecimal.ROUND_HALF_UP);
		}else if(day>=8){
			score=Constants.OVERDUE_SCORE_EIGHT.setScale(2,BigDecimal.ROUND_HALF_UP);
		}

		return score;

	}
}
