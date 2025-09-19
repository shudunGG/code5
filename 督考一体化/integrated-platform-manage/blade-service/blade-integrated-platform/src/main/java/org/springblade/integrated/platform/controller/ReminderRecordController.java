package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import com.vingsoft.vo.ReminderRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.ReminderRecordWrapper;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 考核评价-季度评价-催办
 *
 * @Author JG🧸
 * @Create 2022/4/18 9:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("ReminderRecord")
@Api(value = "考核评价-季度评价-催办", tags = "季度评价-催办控制层代码")
public class ReminderRecordController extends BladeController {

	@Autowired
	private IReminderRecordService iReminderRecordService;
	private final IAppriseFilesService iAppriseFilesService;

	@Autowired
	private IUnifyMessageService unifyMessageService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserSearchClient iUserSearchClient;

	@Resource
	private IDictBizClient dictBizClient;

	@Autowired
	private IAnnualEvaluationService annualEvaluationService;

	@Autowired
	private IQuarterlyEvaluationService quarterlyEvaluationService;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "催办详细信息", notes = "传入 reminderRecord")
	public R<List<ReminderRecord>> detail(ReminderRecord reminderRecord) {
		//sql查询条件
		QueryWrapper<ReminderRecord> queryWrapper = new QueryWrapper<ReminderRecord>();
		queryWrapper.select(" * ");
		queryWrapper.eq(reminderRecord.getId()!=null,"id",reminderRecord.getId());
		List<ReminderRecord> detail = iReminderRecordService.list(queryWrapper);
		return R.data(detail);
	}


	/**
	 * 催办
	 * @param reminderRecord
	 * @return
	 */
	@PostMapping("/saveReminderRecord")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "催办信息和附件上传", notes = "传入 MessageInformation 对象")
	public R save(@Valid @RequestBody ReminderRecord reminderRecord) throws Exception {
		try {
			iReminderRecordService.save(reminderRecord);

			String title1 = "催办了季度评价指标";
			String businessId = String.valueOf(reminderRecord.getId());
			String businessTable = "ReminderRecord";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			//发送消息
			String receiver="";
			String evaluationId= String.valueOf(reminderRecord.getEvaluationId());//指标表id
			R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
			String glyId=rgly.getData();
			String reminderDeptids =reminderRecord.getReminderDeptid();//被催办单位ids
			if(StringUtils.isNotBlank(reminderDeptids)){
				//发送消息
				BladeUser user = AuthUtil.getUser();
				String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
				UnifyMessage unifyMessage = new UnifyMessage();
				String msgIntro = "";
				String msgSubmit = "";
				String twoLevelType="";
				if (reminderRecord != null && reminderRecord.getEvaluationType().equals("1")) {//1年度考核
					String[] deptStr = reminderDeptids.split(",");
					for(String deptId : deptStr){
						R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,deptId);//获取单位下面所有管理员用户
						if(ruser!=null){
							List<User> userList = ruser.getData();
							for(User userInfo : userList){
								receiver+=userInfo.getId()+",";
							}
						}
					}
					AnnualEvaluation ae =annualEvaluationService.getById(evaluationId);
					msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
					msgIntro = "【"+deptName+"】催办了年度评价指标："+reminderRecord.getReminderContent();
					unifyMessage.setMsgTitle("年度评价催办");//消息标题
					unifyMessage.setMsgType("11");//消息类型，字典编码：web_message_type
					twoLevelType="22";
				} else if (reminderRecord != null && reminderRecord.getEvaluationType().equals("2")) {//2季度考核
					QuarterlyEvaluation qe = quarterlyEvaluationService.getById(evaluationId);
					msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
					msgIntro = "【"+deptName+"】催办了季度评价指标："+reminderRecord.getReminderContent();
					unifyMessage.setMsgTitle("季度评价催办");//消息标题
					unifyMessage.setMsgType("4");//消息类型，字典编码：web_message_type
					twoLevelType="15";
				}

				unifyMessage.setMsgId(Long.valueOf(evaluationId));//消息主键（业务主键）
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(receiver);
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem(msgSubmit);//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);

				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
				unifyMessage.setTwoLevelType(twoLevelType);//评价催办
				unifyMessageService.sendMessageInfo(unifyMessage);
			}


			List<AppriseFiles> appriseFilesList = reminderRecord.getAppriseFilesList();
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setBusinessId(reminderRecord.getId());
				iAppriseFilesService.save(appriseFiles);
			}
		} catch (Exception exception) {
			return R.fail("操作失败："+exception.toString());
		}

		return R.success("操作成功!");
	}


	/**
	 * 分页查询
	 * @param reminderRecord
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "季度评价-催办分页查询", notes = "传入reminderRecord")
	public R<IPage<ReminderRecordVO>> list(@RequestParam Map<String, Object> reminderRecord, Query query) {
		//sql查询条件
		String reminderDeptid = (String) reminderRecord.get("reminderDeptid");
		String evaluationId = (String) reminderRecord.get("evaluationId");
		String evaluationType = (String) reminderRecord.get("evaluationType");
		QueryWrapper<ReminderRecord> queryWrapper = new QueryWrapper<ReminderRecord>();
		queryWrapper.select(" * ");
		queryWrapper.eq(StringUtils.isNotBlank(reminderDeptid),"reminder_deptid",reminderDeptid);
		queryWrapper.eq(StringUtils.isNotBlank(evaluationId),"evaluation_id",evaluationId);
		queryWrapper.eq(StringUtils.isNotBlank(evaluationType),"evaluation_type",evaluationType);

		//查询数据，封装分页参数
		IPage<ReminderRecord> pages = iReminderRecordService.page(Condition.getPage(query), queryWrapper);
		if (pages.getRecords().size() > 0) {
			for (ReminderRecord reminderRecord1 : pages.getRecords()) {
				QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
				filesQueryWrapper.eq("business_id", reminderRecord1.getId());
				List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
				reminderRecord1.setAppriseFilesList(appriseFilesList);
			}
		}

		return R.data(ReminderRecordWrapper.build().pageVO(pages));
	}



}
