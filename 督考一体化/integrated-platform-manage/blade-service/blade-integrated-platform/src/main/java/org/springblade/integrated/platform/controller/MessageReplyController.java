package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.PropConstant;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 批示/留言回复 控制层
 *
 * @Author JG🧸
 * @Create 2022/5/14 14:55
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("MessageReply")
@Api(value = "批示/留言回复", tags = "批示/留言回复控制层代码")
public class MessageReplyController extends BladeController {

	private final IMessageReplyService iMessageReplyService;
	private final IUserClient userClient;
	private final IAppriseFilesService iAppriseFilesService;
	private final IMessageInformationService iMessageInformationService;
	private final IUnifyMessageService unifyMessageService;

	/**
	 * 回复保存接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@Valid @RequestBody MessageReply messageReply){
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		messageReply.setAppriseUser(userNameDecrypt);
		messageReply.setAppriseUserId(user.getId());
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
		messageReply.setAppriseuserDeptname(deptName);
		boolean isok  = iMessageReplyService.save(messageReply);

		if (messageReply.getAppriseFilesList() != null) {
			List<AppriseFiles> appriseFilesList = messageReply.getAppriseFilesList();
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setFileFrom("PC端");
				appriseFiles.setBusinessId(messageReply.getId());
				appriseFiles.setBusinessTable("MessageReply");
				appriseFiles.setUploadUserName(userNameDecrypt);
				iAppriseFilesService.save(appriseFiles);
			}
		}
		String title1 = "新增批示留言回复";
		String businessId = String.valueOf(messageReply.getId());
		String businessTable = "MessageReply";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);
		//通过id关联批示留言表
		MessageInformation messageInformation = iMessageInformationService.getById(messageReply.getMessageInformationId());
		String content = messageInformation.getMessageContent();
		if (content.length() > 10) {
			content = "【" + userNameDecrypt + "】对批示【" + messageInformation.getMessageContent().substring(0, 10) + "...】进行回复";
		} else {
			content = "【" + userNameDecrypt + "】对批示【" + messageInformation.getMessageContent()+ "】进行回复";
		}

		//绩效考核 【消息发送】
		if ("2".equals(messageInformation.getBusinessType())) {
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(messageInformation.getBusinessId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("批示留言-回复");//消息标题
			if ("1".equals(messageInformation.getEvaluationType())) {//1 年度
				unifyMessage.setMsgType("43");//消息类型，字典编码：web_message_type
			} else {// 2季度
				unifyMessage.setMsgType("44");//消息类型，字典编码：web_message_type
			}
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(String.valueOf(messageInformation.getAppriseUserId()));
			unifyMessage.setMsgIntro(content);//消息简介
			unifyMessage.setMsgSubitem("留言回复");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
			if ("1".equals(messageInformation.getEvaluationType())) {//1 年度
				unifyMessage.setTwoLevelType("46");//消息二级分类，字典编码：message_type_low
			} else {// 2季度
				unifyMessage.setTwoLevelType("47");//消息二级分类，字典编码：message_type_low
			}
			unifyMessageService.sendMessageInfo(unifyMessage);
		} else if("3".equals(messageInformation.getBusinessType())){//项目管理
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(messageInformation.getBusinessId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("批示留言-回复");//消息标题
			unifyMessage.setMsgType("34");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(String.valueOf(messageInformation.getAppriseUserId()));
			unifyMessage.setMsgIntro(content);//消息简介
			unifyMessage.setMsgSubitem("留言回复");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
			unifyMessage.setTwoLevelType("34");//消息类型，字典编码：message_type_low
			unifyMessageService.sendMessageInfo(unifyMessage);
		}

		return R.status(isok);
	}

	/**
	 * 回复列表查询
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "列表查询", notes = "vo")
	public R<List<MessageReply>> list(MessageReply messageReply){
		LambdaQueryWrapper<MessageReply> lambdaQueryWrapper = Wrappers.<MessageReply>query().lambda()
			.eq(StringUtils.isNotBlank(messageReply.getMessageInformationId()),MessageReply::getMessageInformationId,messageReply.getMessageInformationId())
			.eq(messageReply.getAppriseUserId() != null,MessageReply::getAppriseUserId,messageReply.getAppriseUserId());
		List<MessageReply> messageReplies = iMessageReplyService.list(lambdaQueryWrapper);

		for (MessageReply messageReply1 : messageReplies) {
			MessageInformation messageInformation = iMessageInformationService.getById(messageReply1.getMessageInformationId());
			//获取业务类型
			messageReply1.setBusinessType(messageInformation.getBusinessType());

			//当业务类型为【2】的时候获取绩效考核类型
			if ("2".equals(messageInformation.getBusinessType())) {
				messageReply1.setEvaluationType(messageInformation.getEvaluationType());
			}
			QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
			filesQueryWrapper.eq("business_id",Long.valueOf(messageReply1.getId()));
			List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
			messageReply1.setAppriseFilesList(appriseFilesList);
		}

		return R.data(messageReplies);
	}


}
