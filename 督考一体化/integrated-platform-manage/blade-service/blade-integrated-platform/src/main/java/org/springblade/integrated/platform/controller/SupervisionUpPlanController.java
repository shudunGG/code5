package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionUpPlanVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionUpPlanWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.*;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/4/18 11:58
* @Version:        1.0
*/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionUpPlan")
@Api(value = "接口注册", tags = "接口注册")
public class SupervisionUpPlanController extends BladeController {

	private final ISupervisionUpPlanService supervisionUpPlanService;

	@Resource
	private ISupervisionInfoService supervisionInfoService;

	private final ISupervisionFilesService filesService;

	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;

	private final ISupervisionLogService supervisionLogService;

	@Resource
	private ISysClient sysClient;

	private IUnifyMessageService unifyMessageService;

	@Resource
	private IUserClient userClient;

	@Resource
	private IUserSearchClient iUserSearchClient;

	@Resource
	private IUnifyMessageService messageService;

	@Resource
	private IDictBizClient dictBizClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<SupervisionUpPlanVO>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Object status = entity.get("status");
		if(status!=null){
			entity.remove("status");
		}
		QueryWrapper<SupervisionUpPlan> queryWrapper = Condition.getQueryWrapper(entity, SupervisionUpPlan.class);
		if (status!=null){
			queryWrapper.in("status", Func.toIntList(status.toString()));
		}
		IPage<SupervisionUpPlan> pages = supervisionUpPlanService.page(Condition.getPage(query), queryWrapper);
		return R.data(SupervisionUpPlanWrapper.build().pageVO(pages));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("/supervisionUpPlan/listApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Map<String, Object> entity = new HashMap<>(jsonParams);
			Object status = entity.get("status");
			if(status!=null){
				entity.remove("status");
			}
			QueryWrapper<SupervisionUpPlan> queryWrapper = Condition.getQueryWrapper(entity, SupervisionUpPlan.class);
			if (status!=null){
				queryWrapper.in("status", Func.toIntList(status.toString()));
			}
			Query query = new Query();
			query.setSize(jsonParams.getInteger("size"));
			query.setCurrent(jsonParams.getInteger("current"));
			IPage<SupervisionUpPlan> pages = supervisionUpPlanService.page(Condition.getPage(query), queryWrapper);
			IPage<SupervisionUpPlanVO> pageVo = SupervisionUpPlanWrapper.build().pageVO(pages);
			JSONObject pageJson = objectMapper.convertValue(pageVo, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param vo
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@RequestBody SupervisionUpPlanVO vo) {
		return R.status(supervisionUpPlanService.saveAll(vo));
	}

	/**
	 * 修改
	 * @param vo
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@RequestBody SupervisionUpPlanVO vo) {
		return R.status(supervisionUpPlanService.updateAll(vo));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<SupervisionUpPlanVO> detail(@RequestParam Long id) {
		SupervisionUpPlanVO vo = Objects.requireNonNull(BeanUtil.copy(supervisionUpPlanService.getById(id),SupervisionUpPlanVO.class));
		SupervisionInfo info = supervisionInfoService.getById(vo.getServId());

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(info.getServCode());
		fileDel.setPhaseId(vo.getId());
		fileDel.setFileFrom("2");
		fileDel.setCreateDept(vo.getUpDept());
		List<SupervisionFiles> filesList = filesService.list(Condition.getQueryWrapper(fileDel));

		vo.setSupervisionFilesList(filesList);

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent("【"+userNameDecrypt+"】查看【"+info.getServName()+"】阶段计划");
		supervisionLogService.save(log);
		return R.data(vo);
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(supervisionUpPlanService.removeById(ids));
	}

	/**
	 * 计划确认
	 * @param id		计划主键
	 * @param status	计划状态	1待确认；2通过；3不通过
	 * @param opinion	确认意见
	 * @return
	 */
	@PostMapping("/affirm")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "计划确认", notes = "传入id、状态和意见")
	public R affirm(@RequestParam String id,@RequestParam String status,@RequestParam String opinion){
		SupervisionUpPlan entity = supervisionUpPlanService.getById(id);
		entity.setAuditOpinion(opinion);
		entity.setStatus(Integer.parseInt(status));

		boolean flag = this.supervisionUpPlanService.updateById(entity);

		SupervisionInfo info = supervisionInfoService.getById(entity.getServId());
		if(status.equals(StatusConstant.UP_PLAN_STATUS_2)){
			info.setFlowStatus(StatusConstant.FLOW_STATUS_3);
			flag = supervisionInfoService.updateById(info);
		}
		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		if("2".equals(status)){
			content = "【"+userNameDecrypt+"】审核通过【"+info.getServName()+"】阶段计划";
			msgType = "26";
			appType = "39";
			String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位
			String dutyUnitUser = this.getUserIds(info.getDutyUnit());//责任单位
			receiveUser = leadUnitUser+dutyUnitUser;

		}else if("3".equals(status)){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段计划";
			msgType = "25";
			appType = "11";
			receiveUser = entity.getCreateUser().toString();
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段计划上报");
		message.setMsgType(msgType);
		message.setMsgStatus(0);
		message.setMsgPlatform("web");
		message.setMsgIntro(content);
		message.setCreateTime(new Date());
		message.setReceiveUser(receiveUser);
		messageService.sendMessageInfo(message);

		String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
		message.setMsgSubitem(value);
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);
		return R.status(flag);
	}

	public String getUserIds(String deptIds){
		String receiveUser = "";

		String glyId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String[] ids = deptIds.split(",");
		for (String id : ids) {
			List<User> users= iUserSearchClient.listByPostAndDept(glyId,id).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
		}
		return receiveUser;
	}

	/**
	 * 送审
	 * @param id		事项id主键
	 * @param title		送审标题
	 * @param userIds	用户主键，多个逗号隔开
	 * @param sync		同步还是异步 1同步；0异步
	 * @return
	 */
	@PostMapping("/submitAudit")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "上报计划-送审", notes = "上报计划-送审")
	@Transactional
	public R submitAudit(@RequestParam String id,@RequestParam String title,@RequestParam String userIds,@RequestParam String sync){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync, StatusConstant.OPERATION_TYPE_PLAN);

			//修改状态
			SupervisionUpPlan supervisionUpPlan = new SupervisionUpPlan();
			supervisionUpPlan.setId(Long.parseLong(id));
			supervisionUpPlan.setStatus(Integer.parseInt(StatusConstant.UP_PLAN_STATUS_4));//送审状态
			this.supervisionUpPlanService.updateById(supervisionUpPlan);

			//发消息
			supervisionUpPlan = this.supervisionUpPlanService.getById(id);
			SupervisionInfo supervisionInfo = this.supervisionInfoService.getById(supervisionUpPlan.getServId());
			String value = DictBizCache.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo());

			String users = userIds;
			if("0".equals(sync)){
				users = userIds.split(",")[0];
			}
			this.unifyMessageService.sendSbjhSsMsg(id,users,value,supervisionInfo.getServName());

			//任务日志
			User user = UserCache.getUser(AuthUtil.getUserId());
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			SupervisionLog log = new SupervisionLog();
			log.setServCode(supervisionInfo.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("7");
			log.setOperationTime(new Date());
			log.setContent("【"+supervisionInfo.getServName()+"】已送审");
			supervisionLogService.save(log);
			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}
}
