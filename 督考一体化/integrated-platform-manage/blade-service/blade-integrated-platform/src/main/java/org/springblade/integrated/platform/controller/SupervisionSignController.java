package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionLog;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.entity.UnifyMessage;
import com.vingsoft.vo.SupervisionSignVO;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.service.ISupervisionInfoService;
import org.springblade.integrated.platform.service.ISupervisionLogService;
import org.springblade.integrated.platform.service.ISupervisionSignService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.integrated.platform.wrapper.SupervisionSignWrapper;
import org.springblade.system.entity.Post;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mrtang
 * @version 1.0
 * @description: 事项部门签收控制层
 * @date 2022/4/18 10:27
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionSign")
public class SupervisionSignController {

	private final ISupervisionSignService supervisionSignService;
	@Resource
	private ISupervisionInfoService supervisionInfoService;
	private final ISupervisionLogService supervisionLogService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserClient userClient;
	@Resource
	private IUnifyMessageService unifyMessageService;
	@Resource
	private IDictBizClient dictBizClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 签收任务列表
	 * @param deptId
	 * @return
	 */
	@GetMapping("/myTaskList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "签收列表", notes = "传事项id，获取签收列表")
	public R<List<SupervisionSignVO>> myTaskList(@RequestParam("deptId") String deptId){
		List<SupervisionSign> signs = this.supervisionSignService.list(new QueryWrapper<>(new SupervisionSign()).eq("sign_dept",deptId).eq("sign_status",0));
		return R.data(SupervisionSignWrapper.build().listVO(signs));
	}

	/**
	 * 签收列表
	 * @param servId
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "签收列表", notes = "传事项id，获取签收列表")
	public R<List<SupervisionSignVO>> list(@RequestParam("servId") String servId){
		QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
		wrapper.eq("serv_id",servId);
		List<SupervisionSign> signs = this.supervisionSignService.list(wrapper);
		return R.data(SupervisionSignWrapper.build().listVO(signs));
	}

	/**
	 * 签收列表-app
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "签收列表-app", notes = "传事项id，获取签收列表")
	public R listApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("签收列表-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String servId = jsonParams.getString("servId");
			QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
			wrapper.eq("serv_id",servId);
			List<SupervisionSign> signs = this.supervisionSignService.list(wrapper);
			List<SupervisionSignVO> supervisionSignVOList = SupervisionSignWrapper.build().listVO(signs);
			JSONArray jsonArray = objectMapper.convertValue(supervisionSignVOList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 当前登录人未签收列表
	 * @param entity
	 * @param user
	 * @return
	 */
	@GetMapping("/listNotSigned")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "当前登录人未签收列表", notes = "当前登录人未签收列表")
	public R<List<SupervisionSignVO>> listNotSigned(@RequestParam Map<String, Object> entity, BladeUser user){
		QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("signStatus"))){
			wrapper.eq("sign_Status",entity.get("signStatus"));
		}
		if(ObjectUtil.isNotEmpty(user)){
			R<Post> postR = sysClient.getPost(Long.valueOf(user.getPostId()));
			Post data = postR.getData();
			//当前用户所在部门
			String deptId=user.getDeptId();
			//当前登录用户岗位
			String post=data.getPostCode();
			wrapper.eq("sign_Dept",deptId);
			wrapper.apply("'"+ Constants.USER_POST_GLY+"'='"+post+"'");
		}
		wrapper.orderByDesc("create_Time");
		List<SupervisionSign> signs = this.supervisionSignService.list(wrapper);
		return R.data(SupervisionSignWrapper.build().listVO(signs));
	}

	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@RequestBody SupervisionSign entity) {
		return R.status(supervisionSignService.save(entity));
	}

	/**
	 * 修改
	 * @param id
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@RequestParam String id, BladeUser user,@RequestParam String servId, @RequestParam String isPushPlan,@RequestParam String deptType) {
		SupervisionSign sign = supervisionSignService.getById(id);
		sign.setSignStatus(1);
		sign.setSignTime(DateUtils.getNowDate());
		sign.setSignUser(user.getUserId());
		boolean flag = supervisionSignService.updateById(sign);
		SupervisionInfo info = supervisionInfoService.getById(servId);
		if(flag&&deptType.equals("lead")){
			if(isPushPlan.equals("1")){
				info.setFlowStatus("2");
			}else{
				info.setFlowStatus("3");
			}
			flag = supervisionInfoService.updateById(info);
			if(ObjectUtil.isNotEmpty(info)&&ObjectUtil.isEmpty(info.getDutyUnit())){
				//当无责任单位时 牵头单位汇报
				flag = supervisionSignService.saveReportInfo("lead",sign.getSignDept(),servId);
			}
		}else if(flag&&deptType.equals("duty")){
			flag = supervisionSignService.saveReportInfo(deptType,sign.getSignDept(),servId);
		}

		if(flag){
			User user1 = userClient.userInfoById(AuthUtil.getUserId()).getData();
//			String receiveUser = supervisionInfoService.getMagUser(info.getId(), user);
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user1.getRealName());
			UnifyMessage message = new UnifyMessage();
			message.setMsgId(info.getId());
			message.setMsgTitle("督查督办事项已签收");
			message.setMsgType("53");
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro("【"+userNameDecrypt+"】已签收【"+info.getServName()+"】。");
			message.setCreateTime(new Date());
			message.setReceiveUser(info.getCreateUser().toString());
			unifyMessageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType("9");
			message.setMsgSubitem(value);
			message.setTwoLevelType("53");
			unifyMessageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(info.getServCode());
			log.setOperationDept(user1.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user1.getDeptId())).getData());
			log.setOperationUser(user1.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("4");
			log.setOperationTime(new Date());
			log.setContent("【"+userNameDecrypt+"】已签收【"+info.getServName()+"】。");
			supervisionLogService.save(log);

		}
		return R.status(flag);
	}

	/**
	 * 修改
	 * @param ids
	 * @return
	 */
	@PostMapping("/signBatch")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "批量签收", notes = "vo")
	public R signBatch(@RequestParam String ids, BladeUser user) {
		String[] ide = ids.split(",");
		Boolean flag=false;
		for(String id:ide){
			flag = this.updateBatch(id, user);
		}
		return  R.status(flag);
	}
	/**
	 * 修改
	 * @param id
	 * @return
	 */
	public Boolean updateBatch( String id, BladeUser user) {
		SupervisionSign sign = supervisionSignService.getById(id);
		String servId= sign.getServId().toString();
		SupervisionInfo info = supervisionInfoService.getById(servId);
		String isPushPlan=info.getIsPushPlan();
		String deptType=sign.getDeptType();
		sign.setSignStatus(1);
		sign.setSignTime(DateUtils.getNowDate());
		sign.setSignUser(user.getUserId());
		boolean flag = supervisionSignService.updateById(sign);
		if(flag&&deptType.equals("lead")){
			if(isPushPlan.equals("1")){
				info.setFlowStatus("2");
			}else{
				info.setFlowStatus("3");
			}
			flag = supervisionInfoService.updateById(info);
			if(ObjectUtil.isNotEmpty(info)&&ObjectUtil.isEmpty(info.getDutyUnit())){
				//当无责任单位时 牵头单位汇报
				flag = supervisionSignService.saveReportInfo("lead",sign.getSignDept(),servId);
			}
		}else if(flag&&deptType.equals("duty")){
			flag = supervisionSignService.saveReportInfo(deptType,sign.getSignDept(),servId);
		}

		if(flag){
			User user1 = userClient.userInfoById(AuthUtil.getUserId()).getData();
//			String receiveUser = supervisionInfoService.getMagUser(info.getId(), user);
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user1.getRealName());
			UnifyMessage message = new UnifyMessage();
			message.setMsgId(info.getId());
			message.setMsgTitle("督查督办事项已签收");
			message.setMsgType("53");
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro("【"+userNameDecrypt+"】已签收【"+info.getServName()+"】。");
			message.setCreateTime(new Date());
			message.setReceiveUser(info.getCreateUser().toString());
			unifyMessageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType("9");
			message.setMsgSubitem(value);
			message.setTwoLevelType("53");
			unifyMessageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(info.getServCode());
			log.setOperationDept(user1.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user1.getDeptId())).getData());
			log.setOperationUser(user1.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("4");
			log.setOperationTime(new Date());
			log.setContent("【"+userNameDecrypt+"】已签收【"+info.getServName()+"】。");
			supervisionLogService.save(log);

		}
		return flag;
	}
}
