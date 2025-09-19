package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionPhaseReportVO;
import com.vingsoft.vo.SupervisionSignVO;
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
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionPhaseReportWrapper;
import org.springblade.integrated.platform.wrapper.SupervisionSignWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.entity.Post;
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
import javax.validation.Valid;
import java.util.*;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办阶段汇报控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionPhaseReport")
@Api(value = "督察督办阶段汇报", tags = "督察督办阶段汇报")
public class SupervisionPhaseReportController extends BladeController {

	@Resource
	private ISupervisionPhaseReportService supervisionPhaseReportService;
	@Resource
	private ISupervisionPhasePlanService supervisionPhasePlanService;
	private final ISupervisionFilesService filesService;
	private final ISupervisionLogService supervisionLogService;
	@Resource
	private ISysClient sysClient;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Resource
	private ISupervisionInfoService supervisionInfoService;
	@Resource
	private IUserSearchClient iUserSearchClient;
	@Resource
	private IUnifyMessageService messageService;
	@Resource
	private IDictBizClient dictBizClient;
	@Resource
	private IUserClient userClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 分页查询1
	 * @param start
	 * @param limit
	 * @return
	 */
	@GetMapping("/list/{start}/{limit}")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办阶段汇报", notes = "")
	public R<PageInfo> list(@ApiIgnore @PathVariable Integer start, @PathVariable Integer limit, @RequestParam String columnCode ) {
		QueryWrapper<SupervisionPhaseReport> ew =new QueryWrapper<>();
		PageHelper.startPage(start,limit).setOrderBy("update_time desc");
		Map<String,Object> param=new HashMap<>();
		param.put("columnCode",columnCode);
		List<SupervisionPhaseReport> list = supervisionPhaseReportService.list(ew);
		PageInfo pageInfo = new PageInfo(list);
		return R.data(pageInfo);
	}


	/**
	 * 当前登录人未汇报列表
	 * @param entity
	 * @param user
	 * @return
	 */
	@GetMapping("/listNotReport")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "当前登录人未汇报列表", notes = "当前登录人未汇报列表")
	public R<List<SupervisionPhaseReportVO>> listNotReport(@RequestParam Map<String, Object> entity, BladeUser user){
		QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("reportStatus"))){
			wrapper.eq("report_Status",entity.get("reportStatus"));
		}
		if(ObjectUtil.isNotEmpty(user)){
			R<Post> postR = sysClient.getPost(Long.valueOf(user.getPostId()));
			Post data = postR.getData();
			//当前用户所在部门
			String deptId=user.getDeptId();
			//当前登录用户岗位
			String post=data.getPostCode();
			wrapper.eq("report_Dept",deptId);
			wrapper.apply("'"+ Constants.USER_POST_GLY+"'='"+post+"'");
		}
		wrapper.orderByDesc("create_Time");
		List<SupervisionPhaseReport> signs = this.supervisionPhaseReportService.list(wrapper);
		return R.data(SupervisionPhaseReportWrapper.build().listVO(signs));
	}

	/**
	 * 分页查询
	 * @param entity
	 * @param query
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<SupervisionPhaseReportVO>> page(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		QueryWrapper<SupervisionPhaseReport> wrapperReport=new QueryWrapper<>();
		wrapperReport=Condition.getQueryWrapper(entity, SupervisionPhaseReport.class);
		wrapperReport.and(i->{
			i.or().isNull("parent_Id");
			i.or().eq("parent_Id","");
		});
		IPage<SupervisionPhaseReport> pages = supervisionPhaseReportService.page(Condition.getPage(query), wrapperReport);
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		for(SupervisionPhaseReport re:pages.getRecords()){
			QueryWrapper wrapper=new QueryWrapper();
			if(ObjectUtil.isNotEmpty(user)){
				wrapper.eq("user_Id",user.getId());
			}
			wrapper.eq("status","0");
			wrapper.eq("serv_Id",re.getId());
			SupervisionSubmitAudit auditServiceOne = supervisionSubmitAuditService.getOne(wrapper);
			re.setSupervisionSubmitAudit(auditServiceOne);
			//20240411112528处理脱敏字段开始
			SM4Crypto sm4 = SM4Crypto.getInstance();
			if(StringUtils.isNotEmpty(re.getLinkedName()) && sm4.checkDataIsEncrypt(re.getLinkedName())){
				re.setLinkedName(sm4.decrypt(re.getLinkedName()));
			}
			if(StringUtils.isNotEmpty(re.getLinkedPhone()) && sm4.checkDataIsEncrypt(re.getLinkedPhone())){
				re.setLinkedPhone(sm4.decrypt(re.getLinkedPhone()));
			}
			//20240411112528处理脱敏字段结束
		}

		return R.data(SupervisionPhaseReportWrapper.build().pageVO(pages));
	}

	/**
	 * 分页查询-app
	 * @return
	 */
	@PostMapping("/pageApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R pageApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("supervisionPhaseReport-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Map<String, Object> entity = new HashMap<>(jsonParams);
			QueryWrapper<SupervisionPhaseReport> wrapperReport = Condition.getQueryWrapper(entity, SupervisionPhaseReport.class);
			wrapperReport.and(i->{
				i.or().isNull("parent_Id");
				i.or().eq("parent_Id","");
			});
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			IPage<SupervisionPhaseReport> pages = supervisionPhaseReportService.page(Condition.getPage(query), wrapperReport);
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			for(SupervisionPhaseReport re:pages.getRecords()){
				QueryWrapper wrapper=new QueryWrapper();
				if(ObjectUtil.isNotEmpty(user)){
					wrapper.eq("user_Id",user.getId());
				}
				wrapper.eq("status","0");
				wrapper.eq("serv_Id",re.getId());
				SupervisionSubmitAudit auditServiceOne = supervisionSubmitAuditService.getOne(wrapper);
				re.setSupervisionSubmitAudit(auditServiceOne);
				//20240411112528处理脱敏字段开始
				SM4Crypto sm4 = SM4Crypto.getInstance();
				if(StringUtils.isNotEmpty(re.getLinkedName()) && sm4.checkDataIsEncrypt(re.getLinkedName())){
					re.setLinkedName(sm4.decrypt(re.getLinkedName()));
				}
				if(StringUtils.isNotEmpty(re.getLinkedPhone()) && sm4.checkDataIsEncrypt(re.getLinkedPhone())){
					re.setLinkedPhone(sm4.decrypt(re.getLinkedPhone()));
				}
				//20240411112528处理脱敏字段结束
			}
			JSONObject pageJson = objectMapper.convertValue(SupervisionPhaseReportWrapper.build().pageVO(pages), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionPhaseReport entity) {
		return R.status(supervisionPhaseReportService.save(entity));
	}

	/**
	 * 修改
	 * @param entity
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionPhaseReport entity) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(entity.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));
		// 在这里先做一个判断，判断是牵头单位汇总还是责任单位上报
		// 如果只有牵头单位没有责任单位，也定义为汇报
		boolean isLeadUnit = info.getLeadUnit().equals(entity.getReportDept()) && StringUtils.isNotEmpty(info.getDutyUnit()) ? true : false;

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionLog log = new SupervisionLog();
		log.setServCode(entity.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("5");
		log.setOperationTime(new Date());
		log.setContent("【"+userNameDecrypt+"】对【"+info.getServName()+"】进行阶段汇报");
		supervisionLogService.save(log);
		return R.status(supervisionPhaseReportService.updateAll(entity, isLeadUnit));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<SupervisionPhaseReport> detail(SupervisionPhaseReport entity) {
		SupervisionPhaseReport detail = supervisionPhaseReportService.getOne(Condition.getQueryWrapper(entity));

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(detail.getServCode());
		fileDel.setPhaseId(detail.getPhaseId());
		fileDel.setFileFrom("5");
		fileDel.setCreateDept(Long.parseLong(detail.getReportDept()));
		List<SupervisionFiles> list = filesService.list(Condition.getQueryWrapper(fileDel));

		detail.setSupervisionFilesList(list);

		//20240411131234处理脱敏字段开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(detail.getLinkedName()) && sm4.checkDataIsEncrypt(detail.getLinkedName())){
			detail.setLinkedName(sm4.decrypt(detail.getLinkedName()));
		}
		if(StringUtils.isNotEmpty(detail.getLinkedPhone()) && sm4.checkDataIsEncrypt(detail.getLinkedPhone())){
			detail.setLinkedPhone(sm4.decrypt(detail.getLinkedPhone()));
		}
		//20240411131234处理脱敏字段结束

		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(detail.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		SupervisionLog log = new SupervisionLog();
		log.setServCode(detail.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("5");
		log.setOperationTime(new Date());
		log.setContent("【"+userNameDecrypt+"】查看【"+info.getServName()+"】阶段汇报详情");
		supervisionLogService.save(log);
		return R.data(detail);
	}

	/**
	 * 牵头单位汇总
	 * @param id
	 * @return
	 */
	@PostMapping("/updateAll")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateAll(@RequestParam String id) {
		boolean flag = true;
		SupervisionPhaseReport entity = new SupervisionPhaseReport();
		entity.setPhaseId(Long.parseLong(id));//阶段id
		entity.setReportStatus("1");
		QueryWrapper<SupervisionPhaseReport> queryWrapper = Condition.getQueryWrapper(entity);
		List<SupervisionPhaseReport> list = supervisionPhaseReportService.list(queryWrapper);
		if(list.size()>0){
			for (SupervisionPhaseReport report : list) {
				String status = "2";
				if("6".equals(report.getReportStatus())){//超期未汇报
					status = "7";//超期已汇报
				}
				report.setReportStatus(status);
			}
			flag = supervisionPhaseReportService.updateBatchById(list);
		}
		if(flag){
			//牵头单位汇总后修改阶段上报状态为已上报
			SupervisionPhasePlan plan = supervisionPhasePlanService.getById(Long.valueOf(id));
			plan.setReportStatus("1");
			flag=supervisionPhasePlanService.updateById(plan);

			SupervisionInfo info = new SupervisionInfo();
			info.setServCode(plan.getServCode());
			info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			SupervisionLog log = new SupervisionLog();
			log.setServCode(info.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("5");
			log.setOperationTime(new Date());
			log.setContent("【"+userNameDecrypt+"】对【"+info.getServName()+"】进行阶段汇总");
			supervisionLogService.save(log);

			String content = "【"+userNameDecrypt+"】对【"+info.getServName()+"】进行阶段汇总";
			String receiveUser = "";
			String msgType = "20";

			String glyId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
			List<User> users= iUserSearchClient.listByPostAndDept(glyId,info.getCreateDept().toString()).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(info.getId());
			message.setMsgTitle("阶段汇总");
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
			message.setTwoLevelType("6");
			messageService.sendMessageInfo(message);
		}
		return R.status(flag);
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String id) {
		return R.status(supervisionPhaseReportService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return`
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(supervisionPhaseReportService.removeByIds(Arrays.asList(id)));
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
	@ApiOperation(value = "汇报-送审", notes = "汇报-送审")
	@Transactional
	public R submitAudit(@RequestParam String id,@RequestParam String title,@RequestParam String userIds,@RequestParam String sync){

		try {
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(id,title,userIds,sync,StatusConstant.OPERATION_TYPE_REPORT);

			//修改状态
			SupervisionPhaseReport supervisionPhaseReport = new SupervisionPhaseReport();
			supervisionPhaseReport.setId(Long.parseLong(id));
			supervisionPhaseReport.setReportStatus(StatusConstant.DC_REPORT_STATUS_3);//送审状态
			this.supervisionPhaseReportService.updateById(supervisionPhaseReport);


			//发消息
			supervisionPhaseReport = this.supervisionPhaseReportService.getById(id);
			SupervisionInfo supervisionInfo = this.supervisionInfoService.getOne(new QueryWrapper<>(new SupervisionInfo()).eq("serv_code",supervisionPhaseReport.getServCode()));
//			String value = DictBizCache.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo());
//			String msg = String.format("事项【%s】已送审，请审核。",supervisionInfo.getServName());
//
//			//发消息
//			this.unifyMessageService.saveMessageInfo(id,title,StatusConstant.WEB_MSG_TYPE_21,StatusConstant.MSG_PLATFORM_WEB,userIds,null,msg,null);
//
//			this.unifyMessageService.saveMessageInfo(id,title,StatusConstant.APP_MSG_TYPE_1,StatusConstant.MSG_PLATFORM_APP,userIds,StatusConstant.APP_TWO_MSG_TYPE_7,msg,value);

			//任务日志
			User user = UserCache.getUser(AuthUtil.getUserId());
			SupervisionLog log = new SupervisionLog();
			log.setServCode(supervisionInfo.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
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

	/**
	 * 跨部门下发
	 * @param servCode
	 * @param user
	 * @return
	 */
	@PostMapping("/issueByCrossDept")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "跨部门下发", notes = "vo")
	public R issueByCrossDept(@RequestParam String servCode,@RequestParam String deptIds, BladeUser user) {
		boolean flag = supervisionPhaseReportService.issueByCrossDept(servCode, deptIds, user);
		return R.status(flag);
	}


	/**
	 * 获取所有跨部门汇报数据
	 * @param servCode
	 * @param planId
	 * @param issueDeptId
	 * @return
	 */
	@GetMapping("/issueByCrossDeptReport")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取所有跨部门汇报数据", notes = "获取所有跨部门汇报数据")
	public R<List<SupervisionPhaseReportVO>> issueByCrossDeptReport(@RequestParam String servCode,@RequestParam Long planId,@RequestParam Long issueDeptId,  BladeUser user){
		QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper<>();
		wrapper.eq("serv_Code",servCode);
		wrapper.eq("phase_Id",planId);
		wrapper.eq("issue_Dept",issueDeptId);
		List<SupervisionPhaseReport> reports = this.supervisionPhaseReportService.list(wrapper);
		return R.data(SupervisionPhaseReportWrapper.build().listVO(reports));
	}
}
