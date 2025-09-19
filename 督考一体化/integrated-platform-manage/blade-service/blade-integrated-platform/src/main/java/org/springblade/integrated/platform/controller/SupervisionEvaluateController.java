package org.springblade.integrated.platform.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionEvaluateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import org.springblade.core.mp.support.Query;


import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办评价控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/SupervisionEvaluate")
@Api(value = "督察督办评价", tags = "督察督办评价")
public class SupervisionEvaluateController extends BladeController {

	private final ISupervisionEvaluateService supervisionEvaluateService;
	private final ISupervisionInfoService iSupervisionInfoService;
	private final ISupervisionScoreService iSupervisionScoreService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private ISupervisionScoreService supervisionScoreService;
	@Resource
	private IUserClient userClient;
	@Resource
	private  ISupervisionInfoService supervisionInfoService;
	@Resource
	private IUnifyMessageService unifyMessageService;
	@Resource
	private IDictBizClient dictBizClient;
	@Resource
	private  ISupervisionLogService supervisionLogService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 详细信息
	 */
	@GetMapping("/SupervisionCount")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督查评价首页统计", notes = "传入 supervisionEvaluate")
	public R<List<SupervisionEvaluate>> supervisionCount(SupervisionEvaluate supervisionEvaluate) {
		LambdaQueryWrapper<SupervisionEvaluate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
		lambdaQueryWrapper.eq(supervisionEvaluate.getAppraiseClassify()!=null,SupervisionEvaluate::getAppraiseClassify,supervisionEvaluate.getAppraiseClassify())
			.like(supervisionEvaluate.getSupervisionYear()!=null,SupervisionEvaluate::getCreateTime,supervisionEvaluate.getSupervisionYear())
			.orderByDesc(SupervisionEvaluate::getResult);
		List<SupervisionEvaluate> supervisionEvaluateList = supervisionEvaluateService.list(lambdaQueryWrapper);

		//TODO 把其他未评价的部门添加进去
		//拿到 supervisionEvaluateList 中所有的【被评价单位】
		String[] strings = new String[supervisionEvaluateList.size()];
		for (int i = 0; i < supervisionEvaluateList.size(); i++) {
			strings[i] = supervisionEvaluateList.get(i).getEvaluatedDeptName();
		}
		//去重
		List<String> list1 = new ArrayList<String>();
		for (String v : strings) {
			if (!list1.contains(v)) {
				list1.add(v);
			}
		}
		//获取分组后的单位id
		R<String> Rdeptids = sysClient.getDeptIdsByGroup("000000",supervisionEvaluate.getAppraiseClassify());
		String deptids = Rdeptids.getData();
		if (deptids != null) {
			String[] arr = deptids.split(",");
			List<String> deptId = Arrays.asList(arr);
			for (int i = 0; i < deptId.size(); i++) {
				String did = deptId.get(i);
				String deptNamestr = SysCache.getDeptName(Long.valueOf(did));
				String listStr = list1.toString();
				if (!deptNamestr.contains(listStr)) {
					SupervisionEvaluate supervisionEvaluate1 = new SupervisionEvaluate();
					supervisionEvaluate1.setEvaluatedDeptName(deptNamestr);
					supervisionEvaluate1.setResult("0");
					supervisionEvaluate1.setSCount(0);
					supervisionEvaluateList.add(supervisionEvaluate1);
				}
			}
		}

		//给总得分排名次
		double[] annualscore = new double[supervisionEvaluateList.size()];
		for (int i = 0; i < supervisionEvaluateList.size(); i++) {
			annualscore[i] = Double.parseDouble(supervisionEvaluateList.get(i).getResult());
		}
		//去重
		List<Double> list = new ArrayList<Double>();
		for (double v : annualscore) {
			if (!list.contains(v)) {
				list.add(v);
			}
		}
		//先升序排列
		Collections.sort(list);
		//然后降序排列，得到list[0]最大
		Collections.reverse(list);
		for (int i = 0; i < list.size(); i++) {
			for (SupervisionEvaluate supervisionEvaluate1: supervisionEvaluateList) {
				if (Double.parseDouble(supervisionEvaluate1.getResult()) == list.get(i)) {
					//排序
					supervisionEvaluate1.setSCount(i + 1);
				}
			}
		}


		return R.data(supervisionEvaluateList);
	}

	/**
	 * 督查评价首页统计详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督查评价首页统计详情", notes = "传入 supervisionEvaluate")
	public R<List<Map<String, Object>>> detail(SupervisionScore supervisionScore, String supYear) {
		//需要传参year
		//把所有县区的单位查出来
		List<Dept> gDeptids = sysClient.getDeptByGroup("000000","1", supYear).getData();
		String deptIds = "";
		if (gDeptids.size() > 0) {
			for (int i = 0; i < gDeptids.size(); i++) {
				deptIds+=gDeptids.get(i).getId()+",";
			}
		}

		String deptId = "";
		if (StringUtils.isNotNull(supervisionScore.getDeptId())) {
			deptId = String.valueOf(supervisionScore.getDeptId());

			//如果当前单位的id属于县区，那就查找子部门的分数
			if (deptIds.contains(String.valueOf(supervisionScore.getDeptId()))) {
				R<List<Dept>> deptChildR = sysClient.getDeptChild(supervisionScore.getDeptId());
				List<Dept> deptChildRData = deptChildR.getData();
				if (deptChildRData.size() > 0) {
					for (int i = 0; i < deptChildRData.size(); i++) {
						deptId+= "," +deptChildRData.get(i).getId();
					}
				}
			}
		}
		String[] deptNames = deptId.split(",");

		QueryWrapper<SupervisionScore> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("serv_code,dept_id,score_type,score score,create_user,create_dept");
		queryWrapper.like(supervisionScore.getSuperYear()!=null,"create_time",supervisionScore.getSuperYear());
		queryWrapper.in("dept_id",deptNames);
		queryWrapper.groupBy("dept_id,serv_code");
		List<SupervisionScore> supervisionScores = iSupervisionScoreService.list(queryWrapper);

		List<Map<String, Object>> mapList =new ArrayList<>();
		for (int i = 0; i < supervisionScores.size(); i++) {
			QueryWrapper<SupervisionInfo> queryWrapper1 = new QueryWrapper<>();
			queryWrapper1.eq(supervisionScores.get(i).getServCode()!=null,"serv_code",supervisionScores.get(i).getServCode());
			List<SupervisionInfo> list = iSupervisionInfoService.list(queryWrapper1);
			String deptName = SysCache.getDeptName(supervisionScores.get(i).getCreateDept());
			Map<String, Object> map = new HashMap<>();
			if (list.size() > 0) {
				map.put("ServName", list.get(0).getServName());
				map.put("deptName", deptName);
				map.put("Score", supervisionScores.get(i).getScore());
				mapList.add(map);
			} else {
				map.put("ServName", "无");
				map.put("deptName", deptName);
				map.put("Score",supervisionScores.get(i).getScore());
				mapList.add(map);
			}
		}

		return R.data(mapList);
	}



	/**
	 * 分页查询
	 * @param start
	 * @param limit
	 * @return
	 */
	@GetMapping("/listPage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办评价List", notes = "")
	public R<PageInfo> listPage(@ApiIgnore @PathVariable Integer start, @PathVariable Integer limit, @RequestParam Map<String, Object> entity ) {
		QueryWrapper<SupervisionEvaluate> ew =new QueryWrapper<>();
		List<SupervisionEvaluate> list = supervisionEvaluateService.list(ew);
		PageInfo pageInfo = new PageInfo(list);
		return R.data(pageInfo);
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办评价List", notes = "")
	public R<List<SupervisionEvaluate>> list(@ApiIgnore  @RequestParam Map<String, Object> entity ) {
		QueryWrapper<SupervisionEvaluate> queryWrapper = Condition.getQueryWrapper(entity, SupervisionEvaluate.class);
		List<SupervisionEvaluate> records = supervisionEvaluateService.list(queryWrapper);
		return R.data(records);
	}

	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办评价List-app", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("详情New-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Map<String, Object> entity = new HashMap<>(jsonParams);
			QueryWrapper<SupervisionEvaluate> queryWrapper = Condition.getQueryWrapper(entity, SupervisionEvaluate.class);
			List<SupervisionEvaluate> records = supervisionEvaluateService.list(queryWrapper);
			JSONArray jsonArray = objectMapper.convertValue(records, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param supervisionEvaluate
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionEvaluate supervisionEvaluate ,BladeUser user) {
		Dept dept = null;
		supervisionEvaluate.setEvaluateTime(DateUtils.getNowDate());
		if (supervisionEvaluate.getAppraiseDeptid() != null) {
			dept = SysCache.getDept(supervisionEvaluate.getAppraiseDeptid());
		} else {
			System.out.println("评价部门为空！");
		}
		if(ObjectUtil.isNotEmpty(dept)){
			supervisionEvaluate.setAppraiseClassify(dept.getDeptGroup());
		}
		boolean isok = supervisionEvaluateService.save(supervisionEvaluate);
		if(isok){
			supervisionEvaluate.setId(null);
			isok = supervisionScoreService.calculationScore(supervisionEvaluate);
		}
		if(isok){
			QueryWrapper<SupervisionInfo> wrapperI=new QueryWrapper();
			wrapperI.eq("serv_code",supervisionEvaluate.getServCode());
			SupervisionInfo infoServiceOne = supervisionInfoService.getOne(wrapperI);
			final R<User> userR1 = userClient.userInfoById(user.getUserId());
			User userR1Data = userR1.getData();
			String receiveUser=infoServiceOne.getCreateUser().toString()+",";
            String msgIntro="";
			if(supervisionEvaluate.getEvaluateType().equals("1")){
				//责任领导评价  获取事项的牵头单位和责任单位管理员
				//当前登录是否库市领导
				Set leaderDeptId=new HashSet();
				leaderDeptId.add(PropConstant.getSwldDeptId());
				leaderDeptId.add(PropConstant.getSzfldDeptId());
				String[] deptIds = user.getDeptId().split(",");
				boolean isleader=false;
				for(String id:deptIds){
					if(leaderDeptId.contains(id)){
						isleader=true;
						break;
					}else{
						isleader=false;
					}
				}
				if(isleader) {
					msgIntro = "市领导【" + userR1Data.getRealName() + "】已评价【" + infoServiceOne.getServName() + "】";
				}else{
					msgIntro = "【" + userR1Data.getRealName() + "】已评价【" + infoServiceOne.getServName() + "】";

				}

				List<User> userList=new ArrayList<>();
				//牵头单位
				String leadUnit = infoServiceOne.getLeadUnit();
				String[] leadUnits = leadUnit.split(",");
				for (String lead : leadUnits) {
					//发遂消息  岗位:"管理员ID"1516056792837869570
					R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
					if(org.springblade.core.tool.utils.ObjectUtil.isNotEmpty(userLeader.getData())){
						userList.addAll(userLeader.getData());
					}
				}
				//责任单位
				String dutyUnit = infoServiceOne.getDutyUnit();
				String[] dutyUnits = null;
				if(ObjectUtil.isNotEmpty(dutyUnit)){
					dutyUnits = dutyUnit.split(",");
				}
				if(ObjectUtil.isNotEmpty(dutyUnits)){
					for (String duty : dutyUnits) {
						//获取需要发送消息的人员
						R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
						if(org.springblade.core.tool.utils.ObjectUtil.isNotEmpty(userLeader.getData())){
							userList.addAll(userLeader.getData());
						}
					}
				}
				userList =  userList.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
				for (User user1 : userList) {
					receiveUser += user1.getId()+",";
				}
			}else if(supervisionEvaluate.getEvaluateType().equals("2")||supervisionEvaluate.getEvaluateType().equals("3")){
				//督查督办评价/牵头单位评价
				R<List<User>> userG= userClient.getUserLeader(supervisionEvaluate.getEvaluatedDept(), Constants.USER_POST_GLY_id);
				List<User> userGly=userG.getData();
				for(User u:userGly){
					receiveUser += u.getId()+",";
				}
				msgIntro = "【" + userR1Data.getRealName() + "】已评价【" + infoServiceOne.getServName() + "】";
			}

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(infoServiceOne.getId());
			message.setMsgTitle("督查督办事项评价");
			message.setMsgType("53");
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(msgIntro);
			message.setCreateTime(new Date());
			message.setReceiveUser(receiveUser);
			unifyMessageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(infoServiceOne.getServTypeOne(), infoServiceOne.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType("9");
			message.setMsgSubitem(value);
			message.setTwoLevelType("53");
			unifyMessageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(infoServiceOne.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(userR1Data.getId().toString());
			log.setOperationUserName(userR1Data.getRealName());
			log.setOperationType("1");
			log.setOperationTime(new Date());
			log.setContent("【"+infoServiceOne.getServName()+"】已评价");
			supervisionLogService.save(log);

		}
		return R.status(isok);
	}

	/**
	 * 修改
	 * @param supervisionEvaluate
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionEvaluate supervisionEvaluate) {
		return R.status(supervisionEvaluateService.updateById(supervisionEvaluate));
	}
	/**
	 * 详情
	 * @param entity
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SupervisionEvaluate> details(@Valid @RequestParam Map<String, Object> entity) {
		QueryWrapper<SupervisionEvaluate> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("evaluateUser"))){
			wrapper.eq("evaluate_User",entity.get("evaluateUser"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("evaluateType"))){
			wrapper.eq("evaluate_Type",entity.get("evaluateType"));
			if(!entity.get("evaluateType").equals("1")){
				if(ObjectUtil.isNotEmpty(entity.get("evaluatedDept"))){
					wrapper.eq("evaluated_Dept",entity.get("evaluatedDept"));
				}
			}
		}
		if(ObjectUtil.isNotEmpty(entity.get("servCode"))){
			wrapper.eq("serv_Code",entity.get("servCode"));
		}
		SupervisionEvaluate  evaluate= supervisionEvaluateService.getOne(wrapper);
		if(ObjectUtil.isNotEmpty(evaluate)){
			Date startDate = evaluate.getEvaluateTime();
			int hours = DateUtils.getTtimeDifferenceToHours(startDate, DateUtils.getNowDate());
			if(hours>24){
				evaluate.setIsOverTime(Long.valueOf(1));
			}
		}
		return R.data(evaluate);
	}



	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String id,BladeUser user) {
		SupervisionEvaluate supervisionEvaluate = supervisionEvaluateService.getById(id);
		boolean flag = supervisionEvaluateService.removeById(id);
		if(flag){
			flag = supervisionScoreService.calculationScore(supervisionEvaluate);
		}
		if(flag){


			QueryWrapper<SupervisionInfo> wrapperI=new QueryWrapper();
			wrapperI.eq("serv_code",supervisionEvaluate.getServCode());
			SupervisionInfo infoServiceOne = supervisionInfoService.getOne(wrapperI);
			final R<User> userR1 = userClient.userInfoById(user.getUserId());
			User userR1Data = userR1.getData();
			String receiveUser=infoServiceOne.getCreateUser().toString()+",";
			String msgIntro="";
			if(supervisionEvaluate.getEvaluateType().equals("1")){
				//责任领导评价  获取事项的牵头单位和责任单位管理员
				//当前登录是否库市领导
				Set leaderDeptId=new HashSet();
				leaderDeptId.add(PropConstant.getSwldDeptId());
				leaderDeptId.add(PropConstant.getSzfldDeptId());
				String[] deptIds = user.getDeptId().split(",");
				boolean isleader=false;
				for(String idE:deptIds){
					if(leaderDeptId.contains(idE)){
						isleader=true;
						break;
					}else{
						isleader=false;
					}
				}
				if(isleader) {
					msgIntro = "市领导【" + userR1Data.getRealName() + "】已撤销【" + infoServiceOne.getServName() + "】评价";
				}else{
					msgIntro = "【" + userR1Data.getRealName() + "】已撤销【" + infoServiceOne.getServName() + "】评价";

				}

				List<User> userList=new ArrayList<>();
				//牵头单位
				String leadUnit = infoServiceOne.getLeadUnit();
				String[] leadUnits = leadUnit.split(",");
				for (String lead : leadUnits) {
					//发遂消息  岗位:"管理员ID"1516056792837869570
					R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
					if(org.springblade.core.tool.utils.ObjectUtil.isNotEmpty(userLeader.getData())){
						userList.addAll(userLeader.getData());
					}
				}
				//责任单位
				String dutyUnit = infoServiceOne.getDutyUnit();
				String[] dutyUnits = null;
				if(ObjectUtil.isNotEmpty(dutyUnit)){
					dutyUnits = dutyUnit.split(",");
				}
				if(ObjectUtil.isNotEmpty(dutyUnits)){
					for (String duty : dutyUnits) {
						//获取需要发送消息的人员
						R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
						if(org.springblade.core.tool.utils.ObjectUtil.isNotEmpty(userLeader.getData())){
							userList.addAll(userLeader.getData());
						}
					}
				}
				userList =  userList.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
				for (User user1 : userList) {
					receiveUser += user1.getId()+",";
				}
			}else if(supervisionEvaluate.getEvaluateType().equals("2")||supervisionEvaluate.getEvaluateType().equals("3")){
				//督查督办评价/牵头单位评价
				R<List<User>> userG= userClient.getUserLeader(supervisionEvaluate.getEvaluatedDept(), Constants.USER_POST_GLY_id);
				List<User> userGly=userG.getData();
				for(User u:userGly){
					receiveUser += u.getId()+",";
				}
				msgIntro = "【" + userR1Data.getRealName() + "】已撤销【" + infoServiceOne.getServName() + "】评价";
			}

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(infoServiceOne.getId());
			message.setMsgTitle("督查督办事项撤销评价");
			message.setMsgType("54");
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(msgIntro);
			message.setCreateTime(new Date());
			message.setReceiveUser(receiveUser);
			unifyMessageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(infoServiceOne.getServTypeOne(), infoServiceOne.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType("9");
			message.setMsgSubitem(value);
			message.setTwoLevelType("54");
			unifyMessageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(infoServiceOne.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(userR1Data.getId().toString());
			log.setOperationUserName(userR1Data.getRealName());
			log.setOperationType("1");
			log.setOperationTime(new Date());
			log.setContent("【"+infoServiceOne.getServName()+"】已撤销评价");
			supervisionLogService.save(log);









			//-------------------------




		}
		return R.status(flag);
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(supervisionEvaluateService.removeByIds(Arrays.asList(id)));
	}

	/**
	 * 督查评价年度统计（分管部门、县区、其他部门）
	 * @return
	 */
	@GetMapping("/dcdbList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督查督办年度统计信息", notes = "")
	public R<List<SupervisionEvaluateVo>> dcdbList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String type= entity.get("type").toString();//查询类型：1 分管部门 2 区县 3 其他部门
		String year= entity.get("year").toString();//年份
		List<SupervisionEvaluateVo> list =supervisionEvaluateService.getDcdb(type,year);
		if (list.size() > 0 && !"3".equals(type)) {
			for (int i = 0; i < list.size(); i++) {
				Dept dept = SysCache.getDept(Long.valueOf(list.get(i).getDeptId()));
				if (dept != null) {
					if (StringUtils.isNotNull(dept.getSort())) {
						list.get(i).setSort(dept.getSort().toString());
					} else {
						list.get(i).setSort("100");
					}
				} else {
					list.get(i).setSort("100");
				}
			}
		} else if("3".equals(type)) {
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setSort("1");
			}
		}
		//按照部门的sort排序
		list.sort(new Comparator<SupervisionEvaluateVo>() {
			@Override
			public int compare(SupervisionEvaluateVo o1, SupervisionEvaluateVo o2) {
				Integer sort1 = Integer.parseInt(o1.getSort());
				Integer sort2 = Integer.parseInt(o2.getSort());
				return sort1.compareTo(sort2);
			}
		});
		return R.data(list);
	}
	static <T> Predicate<T> distinctByKey1(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
