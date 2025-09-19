package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionUrgeVo;
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
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionUrgeWrapper;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办催办
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionUrge")
@Api(value = "督察督办催办", tags = "督察督办催办")
public class SupervisionUrgeController extends BladeController {

	private final ISupervisionUrgeService supervisionUrgeService;

	private final ISupervisionLogService supervisionLogService;

	@Resource
	private ISysClient sysClient;

	@Resource
	private IUnifyMessageService messageService;

	@Resource
	private ISupervisionInfoService supervisionInfoService;

	@Resource
	private IUserSearchClient iUserSearchClient;

	@Resource
	private IDictBizClient dictBizClient;

	@Resource
	private IUserClient userClient;

	@Resource
	private ISupervisionFilesService filesService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * @param start
	 * @param limit
	 * @return
	 */
	@GetMapping("/list/{start}/{limit}")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办催办", notes = "")
	public R<PageInfo> list(@ApiIgnore @PathVariable Integer start, @PathVariable Integer limit, @RequestParam String columnCode ) {
		QueryWrapper<SupervisionUrge> ew =new QueryWrapper<>();
		PageHelper.startPage(start,limit).setOrderBy("update_time desc");
		Map<String,Object> param=new HashMap<>();
		param.put("columnCode",columnCode);
		List<SupervisionUrge> list = supervisionUrgeService.list(ew);
		PageInfo pageInfo = new PageInfo(list);
		return R.data(pageInfo);
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办信息List", notes = "")
	public R<List<SupervisionUrgeVo>> list(@RequestParam Map<String, Object> entity ) {
		List<SupervisionUrge> records = supervisionUrgeService.list(Condition.getQueryWrapper(entity,SupervisionUrge.class).orderByDesc("urge_Time"));
		return R.data(SupervisionUrgeWrapper.build().listVO(records));
	}

	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办信息List", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("supervisionUrge-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Map<String, Object> entity = new HashMap<>(jsonParams);
			List<SupervisionUrge> records = supervisionUrgeService.list(Condition.getQueryWrapper(entity,SupervisionUrge.class).orderByDesc("urge_Time"));
			List<SupervisionUrgeVo> supervisionUrgeVoList = SupervisionUrgeWrapper.build().listVO(records);
			JSONArray resultJson = objectMapper.convertValue(supervisionUrgeVoList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, resultJson.toJSONString(), VSTool.CHN));
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
	public R save(@Valid @RequestBody SupervisionUrge entity) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(entity.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

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
		log.setContent("【"+userNameDecrypt+"】对【"+info.getServName()+"】进行督查催办");
		supervisionLogService.save(log);

		String content = "【"+userNameDecrypt+"】对【"+info.getServName()+"】进行督查催办";
		String receiveUser = "";
		String msgType = "23";

		String glyId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		List<User> users= iUserSearchClient.listByPostAndDept(glyId,entity.getUrgedUnit().toString()).getData();//获取单位下面所有管理员用户
		if(users!=null){
			for(User u : users){
				receiveUser += u.getId()+",";
			}
		}
		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("事项催办");
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
		message.setTwoLevelType("9");
		messageService.sendMessageInfo(message);

		entity.setUrgeTime(new Date());

		boolean save = supervisionUrgeService.save(entity);

		List<SupervisionFiles> supervisionFilesList = entity.getSupervisionFilesList();
		for (SupervisionFiles supervisionFiles : supervisionFilesList) {
			supervisionFiles.setServCode(entity.getServCode());
			supervisionFiles.setFileFrom("4");
			supervisionFiles.setUploadUser(user.getId().toString());
			supervisionFiles.setUploadUserName(userNameDecrypt);
			supervisionFiles.setUploadTime(new Date());
			supervisionFiles.setPhaseId(entity.getId());
			filesService.save(supervisionFiles);
		}
		return R.status(save);
	}

	/**
	 * 修改
	 * @param entity
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionUrge entity) {
		return R.status(supervisionUrgeService.updateById(entity));
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
		return R.status(supervisionUrgeService.removeById(id));
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
		return R.status(supervisionUrgeService.removeByIds(Arrays.asList(id)));
	}

}
