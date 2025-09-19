package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionPhaseReportChiVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionPhaseReportAllWrapper;
import org.springblade.integrated.platform.wrapper.SupervisionPhaseReportChiWrapper;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/14 21:50
* @Version:        1.0
*/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionPhaseReportChi")
@Api(value = "督察督办阶段汇报", tags = "督察督办阶段汇报")
public class SupervisionPhaseReportChiController extends BladeController {

	@Resource
	private ISupervisionPhaseReportChiService supervisionPhaseReportChiService;
	@Resource
	private IUserClient userClient;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Resource
	private ISupervisionInfoService supervisionInfoService;
	@Resource
	private ISysClient sysClient;
	private final ISupervisionLogService supervisionLogService;
	private final ISupervisionFilesService filesService;
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
	@GetMapping("/page")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<SupervisionPhaseReportChiVO>> page(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<SupervisionPhaseReportChi> pages = supervisionPhaseReportChiService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SupervisionPhaseReportChi.class));
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		for(SupervisionPhaseReportChi re:pages.getRecords()){
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

		return R.data(SupervisionPhaseReportChiWrapper.build().pageVO(pages));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/pageApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R page(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("supervisionPhaseReportChi-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			Map<String, Object> entity = new HashMap<>(jsonParams);
			Query query = new Query();
			query.setSize(jsonParams.getInteger("size"));
			query.setCurrent(jsonParams.getInteger("current"));
			IPage<SupervisionPhaseReportChi> pages = supervisionPhaseReportChiService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SupervisionPhaseReportChi.class));
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			for(SupervisionPhaseReportChi re:pages.getRecords()){
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
			JSONObject pageJson = objectMapper.convertValue(SupervisionPhaseReportChiWrapper.build().pageVO(pages), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 分派
	 * @param entity
	 * @return
	 */
	@PostMapping("/updateAll")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateAll(@Valid @RequestBody SupervisionPhaseReportChi entity) {
		return R.status(supervisionPhaseReportChiService.updateAll(entity));
	}

	/**
	 * 撤销
	 * @param entity
	 * @return
	 */
	@PostMapping("/deleteAll")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R deleteAll(@Valid @RequestBody SupervisionPhaseReportChi entity) {
		return R.status(supervisionPhaseReportChiService.deleteAll(entity));
	}

	/**
	 * 撤销
	 * @param entity
	 * @return
	 */
	@PostMapping("/delete")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R delete(@Valid @RequestBody SupervisionPhaseReportChi entity) {
		return R.status(supervisionPhaseReportChiService.delete(entity));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入entity")
	public R<SupervisionPhaseReportChi> detail(SupervisionPhaseReportChi entity) {
		SupervisionPhaseReportChi detail = supervisionPhaseReportChiService.getOne(Condition.getQueryWrapper(entity));

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(detail.getServCode());
		fileDel.setPhaseId(detail.getPhaseId());
		fileDel.setFileFrom("chi");
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
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionLog log = new SupervisionLog();
		log.setServCode(detail.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("5");
		log.setOperationTime(new Date());
		log.setContent("【"+userNameDecrypt+"】查看【"+info.getServName()+"】阶段分派汇报详情");
		supervisionLogService.save(log);
		return R.data(detail);
	}
}
