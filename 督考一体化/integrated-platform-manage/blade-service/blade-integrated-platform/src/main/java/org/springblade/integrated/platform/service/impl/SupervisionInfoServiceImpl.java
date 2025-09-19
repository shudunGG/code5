package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.*;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.NotNull;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.OrderNoUtils;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionInfoMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionPhaseReportWrapper;
import org.springblade.integrated.platform.wrapper.SupervisionSignWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.entity.Post;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionInfoServiceImpl extends BaseServiceImpl<SupervisionInfoMapper, SupervisionInfo> implements ISupervisionInfoService {


	@Autowired
	private ISupervisionPhasePlanService supervisionPhasePlanService;

	@Lazy
	@Autowired
	private  ISupervisionSignService supervisionSignService;

	@Autowired
	private ISupervisionFilesService supervisionFilesService;

	@Autowired
	private ISupervisionPhaseRemindService supervisionPhaseRemindService;

	@Resource
	private  ISysClient sysClient;

	@Resource
	private  IUserClient userClient;

	@Resource
	private IDictBizClient dictBizClient;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Autowired
	private ISupervisionPhaseReportService supervisionPhaseReportService;

	@Autowired
	private  IFollowInformationService followInformationService;

	@Autowired
	private  ISupervisionSubmitAuditService supervisionSubmitAuditService;

	@Autowired
	private  ISupervisionLogService supervisionLogService;

	//发送短信
	@Resource
	private SmsDockingService smsDockingService;

	@Override
	public boolean updateFlowStatus(Long id, String status) {
		SupervisionInfo supervisionInfo = new SupervisionInfo();
		supervisionInfo.setId(id);
		supervisionInfo.setFlowStatus(status);
		if(status.equals("1")){
			supervisionInfo.setServStatus("1");
		}
		return baseMapper.updateById(supervisionInfo)>0;
	}

	@Override
	public List<SupervisionInfo> queryList(Map<String, Object> entity) {
		QueryWrapper<SupervisionInfoVO> wrapper=new QueryWrapper<>();
		wrapper.like("info.is_deleted",0);
		if(ObjectUtil.isNotEmpty(entity.get("servName"))){
			wrapper.like("info.serv_Name",entity.get("servName"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeOne"))){
			wrapper.eq("info.serv_Type_One",entity.get("servTypeOne"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeTwo"))){
			wrapper.eq("info.serv_Type_Two",entity.get("servTypeTwo"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
			wrapper.eq("info.serv_Type_Three",entity.get("servTypeThree"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeFour"))){
			wrapper.eq("info.serv_Type_Four",entity.get("servTypeFour"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("flowStatus"))){
			wrapper.eq("info.flow_Status",entity.get("flowStatus"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("year"))){
			wrapper.eq("date_format(info.create_Time,'%Y')",entity.get("year"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
			wrapper.like("info.duty_Unit",entity.get("dutyUnit"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
			wrapper.like("info.lead_Unit",entity.get("leadUnit"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("swszf"))){
			wrapper.eq("info.swszf",entity.get("swszf"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
			String leadUnits= (String) entity.get("leadUnit");
			String [] leadUnit =leadUnits.split(",");
			wrapper.and(wpleadUnit->{
				for(int i=0;i<leadUnit.length;i++){
					String aa= leadUnit[i];
					wpleadUnit.or().apply("INSTR(lead_unit,"+aa+")>0");
				}
			});
		}
		if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
			String dutyUnits= (String) entity.get("dutyUnit");
			String [] dutyUnit =dutyUnits.split(",");
			wrapper.and(wpdutyUnit->{
				for(int i=0;i<dutyUnit.length;i++){
					String aa= dutyUnit[i];
					wpdutyUnit.or().apply("INSTR(duty_Unit,"+aa+")>0");
				}
			});
		}
		if(ObjectUtil.isNotEmpty(entity.get("startCreateTime"))&&ObjectUtil.isNotEmpty(entity.get("endCreateTime"))){
			wrapper.between("info.create_time",entity.get("startCreateTime"),entity.get("endCreateTime"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("createUser"))){
			wrapper.like("info.create_User",entity.get("createUser"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("id"))){
			wrapper.like("info.id",entity.get("id"));
		}

		String tbBus= (String) entity.get("tbBus");
		List<SupervisionInfo> infos=new ArrayList<>();
		BladeUser user = AuthUtil.getUser();
		String deptId="";
		String AutUserId="";
		String userPostIds="";
		if(ObjectUtil.isNotEmpty(user)){
			//当前用户所在部门
			deptId=user.getDeptId();
			AutUserId=user.getUserId().toString();
			userPostIds=user.getPostId();
		}

		if(tbBus.equals("1")){
			//查询督查督办
			if(ObjectUtil.isNotEmpty(user)){
				String roleNames = user.getRoleName();
				String [] roleName=roleNames.split(",");
				//允许查询数据的角色
//				String role="市级四大班子";
//				wrapper.and(wprole->{
//					for(int i=0;i<roleName.length;i++){
//						String aa= roleName[i];
//						wprole.or().apply("INSTR('"+role+"','"+aa+"')>0");
//					}
//				});
				boolean isRolePT=false;
				for(int i=0;i<roleName.length;i++){
					String aa= roleName[i];
					if(Constants.DCDB_SELECT_ROLE.indexOf(aa)>=0){
						isRolePT=false;
						break;
					}else {
						isRolePT=true;
					}
				}
				if(isRolePT){
					wrapper.eq("info.create_user",user.getUserId());
				}
			}
			infos = this.baseMapper.queryList(wrapper,deptId,AutUserId,tbBus);
		}else if(tbBus.equals("2")){
			//查询承办事项
			Boolean isGly=false;
			Boolean isDeptLg=false;
			Set<String> post=new HashSet<>();
			if(ObjectUtil.isNotEmpty(userPostIds)){
				String [] posts=userPostIds.split(",");
				for(int i=0;i<posts.length; i++){
					String postId= posts[i];
					if(postId.equals(Constants.USER_POST_GLY_id)){
						isGly=true;
						break;
					}else if(postId.equals(Constants.USER_POST_DEPT_LD_id)){
						isDeptLg=true;
						break;
					}else{
						isGly =false;
						isDeptLg=false;
					}
				}
			}


			//当前登录用户岗位
			String finalDeptId = deptId;
			Boolean finalIsGly = isGly;
//			if(finalIsGly|| isDeptLg){
//				wrapper.eq("sign.sign_dept", finalDeptId);
//				wrapper.eq("1",1);
//			}
			wrapper.and(i->{
				if(finalIsGly){
					i.or().and(j->{
						j.eq("sign.sign_dept", finalDeptId);
						j.eq("1", 1);
					});
				}

				if(ObjectUtil.isNotEmpty(user)){
					i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
					i.or().apply("INSTR(plan.down_User_Id,'"+user.getUserId()+"')>0");
//					i.or().eq("plan.down_User_Id",user.getUserId());
					i.or().eq("auditShow.user_id",user.getUserId());
					i.or().eq("auditHB.user_id",user.getUserId());
					i.or().eq("auditJH.user_id",user.getUserId());
					i.or().eq("reportAlt.report_user",user.getUserId());
					if(finalIsGly){
						i.or().eq("plan.report_dept", user.getDeptId());
					}
				}
			});
			infos = this.baseMapper.queryList(wrapper,deptId,AutUserId,tbBus);

		}else if(tbBus.equals("3")){
			//查询我的关注
			QueryWrapper<FollowInformation> wrapperF=new QueryWrapper<>();
			wrapperF.eq("business_Type","1");
			if(ObjectUtil.isNotEmpty(user)){
				wrapperF.eq("follow_User_Id",user.getUserId());
			}
			wrapperF.eq("business_Type","1");
			wrapperF.eq("status","1");
			wrapperF.select("distinct business_Id");
			List<Object> list = followInformationService.listObjs(wrapperF);
			if(ObjectUtil.isNotEmpty(list)){
				wrapper.in("info.id",list);
			}else {
				wrapper.ne("1",1);
			}
			infos = this.baseMapper.queryList(wrapper,deptId,AutUserId,tbBus);
		}
		else if(tbBus.equals("5")){
			//查询待办事项
			Boolean isGly=false;
			Boolean isDeptLg=false;
			Set<String> post=new HashSet<>();
			if(ObjectUtil.isNotEmpty(userPostIds)){
				String [] posts=userPostIds.split(",");
				for(int i=0;i<posts.length; i++){
					String postId= posts[i];
					if(postId.equals(Constants.USER_POST_GLY_id)){
						isGly=true;
						break;
					}else if(postId.equals(Constants.USER_POST_DEPT_LD_id)){
						isDeptLg=true;
						break;
					}else{
						isGly =false;
						isDeptLg=false;
					}
				}
			}


			//当前登录用户岗位
			String finalDeptId = deptId;
			Boolean finalIsGly = isGly;
//			if(finalIsGly|| isDeptLg){
//				wrapper.eq("sign.sign_dept", finalDeptId);
//				wrapper.eq("1",1);
//			}
			wrapper.and(i->{
				if(finalIsGly){
					i.or().and(j->{
						j.eq("sign.sign_dept", finalDeptId);
						j.eq("1", 1);
					});
				}

				if(ObjectUtil.isNotEmpty(user)){
					i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
					i.or().apply("INSTR(plan.down_User_Id,'"+user.getUserId()+"')>0");
//					i.or().eq("plan.down_User_Id",user.getUserId());
					i.or().eq("auditShow.user_id",user.getUserId());
					i.or().eq("auditHB.user_id",user.getUserId());
					i.or().eq("auditJH.user_id",user.getUserId());
					if(finalIsGly){
						i.or().eq("plan.report_dept", user.getDeptId());
					}
				}
			});
			wrapper.ne("info.flow_status", 4);
			infos = this.baseMapper.queryList(wrapper,deptId,AutUserId,tbBus);
		}
		for (SupervisionInfo info : infos){
			QueryWrapper<SupervisionSign> ee=new QueryWrapper();
			ee.eq("serv_id",info.getId());
			//获取单位签收信息
			List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
			Map<Long, Integer> dutyMap = new HashMap<>();
			Map<Long, Integer> leadMap = new HashMap<>();
			for (SupervisionSign sign: supervisionSignList)
			{
				int status = sign.getSignStatus();
				Long unit = sign.getSignDept();
				String type = sign.getDeptType();
				if (type.equals("duty")){
					dutyMap.put(unit, status);
				}
				if (type.equals("lead")){
					leadMap.put(unit, status);
				}
			}
			info.setDutyMap(dutyMap);
			info.setLeadMap(leadMap);
		}
		return infos;
	}

	@Override
	public PageInfo queryListPage(Query query, Map<String, Object> entity) {
		QueryWrapper<SupervisionInfoVO> wrapper=new QueryWrapper<>();
		wrapper.like("info.is_deleted",0);
		if(ObjectUtil.isNotEmpty(entity.get("servName"))){
			wrapper.like("info.serv_Name",entity.get("servName"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeOne"))){
			wrapper.eq("info.serv_Type_One",entity.get("servTypeOne"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeTwo"))){
			wrapper.eq("info.serv_Type_Two",entity.get("servTypeTwo"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
			wrapper.eq("info.serv_Type_Three",entity.get("servTypeThree"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("servTypeFour"))){
			wrapper.eq("info.serv_Type_Four",entity.get("servTypeFour"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("flowStatus"))){
			wrapper.eq("info.flow_Status",entity.get("flowStatus"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("year"))){
			wrapper.eq("date_format(info.create_Time,'%Y')",entity.get("year"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
			wrapper.like("info.duty_Unit",entity.get("dutyUnit"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
			wrapper.like("info.lead_Unit",entity.get("leadUnit"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("swszf"))){
			wrapper.eq("info.swszf",entity.get("swszf"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
			String leadUnits= (String) entity.get("leadUnit");
			String [] leadUnit =leadUnits.split(",");
			wrapper.and(wpleadUnit->{
				for(int i=0;i<leadUnit.length;i++){
					String aa= leadUnit[i];
					wpleadUnit.or().apply("INSTR(lead_unit,"+aa+")>0");
				}
			});
		}
		if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
			String dutyUnits= (String) entity.get("dutyUnit");
			String [] dutyUnit =dutyUnits.split(",");
			wrapper.and(wpdutyUnit->{
				for(int i=0;i<dutyUnit.length;i++){
					String aa= dutyUnit[i];
					wpdutyUnit.or().apply("INSTR(duty_Unit,"+aa+")>0");
				}
			});
		}
		if(ObjectUtil.isNotEmpty(entity.get("startCreateTime"))&&ObjectUtil.isNotEmpty(entity.get("endCreateTime"))){
			wrapper.between("info.create_time",entity.get("startCreateTime"),entity.get("endCreateTime"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("createUser"))){
			wrapper.like("info.create_User",entity.get("createUser"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("id"))){
			wrapper.like("info.id",entity.get("id"));
		}

		String tbBus= (String) entity.get("tbBus");
		List<SupervisionInfoVO> infos=new ArrayList<>();
		BladeUser user = AuthUtil.getUser();
		String deptId="";
		String AutUserId="";
		String userPostIds="";
		if(ObjectUtil.isNotEmpty(user)){
			//当前用户所在部门
			deptId=user.getDeptId();
			AutUserId=user.getUserId().toString();
			userPostIds=user.getPostId();
		}

		if(tbBus.equals("1")){
			//查询督查督办
			if(ObjectUtil.isNotEmpty(user)){
				String roleNames = user.getRoleName();
				String [] roleName=roleNames.split(",");
				//允许查询数据的角色
//				String role="市级四大班子";
//				wrapper.and(wprole->{
//					for(int i=0;i<roleName.length;i++){
//						String aa= roleName[i];
//						wprole.or().apply("INSTR('"+role+"','"+aa+"')>0");
//					}
//				});
				boolean isRolePT=false;
				for(int i=0;i<roleName.length;i++){
					String aa= roleName[i];
					if(Constants.DCDB_SELECT_ROLE.indexOf(aa)>=0){
						isRolePT=false;
						break;
					}else {
						isRolePT=true;
					}
				}
				if(isRolePT){
					wrapper.eq("info.create_user",user.getUserId());
				}
			}
			PageHelper.startPage(query.getCurrent(),query.getSize());
			PageHelper.orderBy("info.create_time desc");
			infos = this.baseMapper.queryListCB(wrapper,deptId,AutUserId,tbBus);
		}else if(tbBus.equals("2")){
			//查询承办事项
			Boolean isGly=false;
			Boolean isDeptLg=false;
			Set<String> post=new HashSet<>();
			if(ObjectUtil.isNotEmpty(userPostIds)){
				String [] posts=userPostIds.split(",");
				for(int i=0;i<posts.length; i++){
					String postId= posts[i];
					if(postId.equals(Constants.USER_POST_GLY_id)){
						isGly=true;
						break;
					}else if(postId.equals(Constants.USER_POST_DEPT_LD_id)){
						isDeptLg=true;
						break;
					}else{
						isGly =false;
						isDeptLg=false;
					}
				}
			}


			//当前登录用户岗位
			String finalDeptId = deptId;
			Boolean finalIsGly = isGly;
//			if(finalIsGly|| isDeptLg){
//				wrapper.eq("sign.sign_dept", finalDeptId);
//				wrapper.eq("1",1);
//			}
			wrapper.and(i->{
				if(finalIsGly){
					i.or().and(j->{
						j.eq("sign.sign_dept", finalDeptId);
						j.eq("1", 1);
					});
				}

				if(ObjectUtil.isNotEmpty(user)){
					i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
					i.or().apply("INSTR(plan.down_User_Id,'"+user.getUserId()+"')>0");
//					i.or().eq("plan.down_User_Id",user.getUserId());
					i.or().eq("auditShow.user_id",user.getUserId());
					i.or().eq("auditHB.user_id",user.getUserId());
					i.or().eq("auditJH.user_id",user.getUserId());
					i.or().eq("reportAlt.report_user",user.getUserId());
					if(finalIsGly){
						i.or().eq("plan.report_dept", user.getDeptId());
					}
				}
			});
			PageHelper.startPage(query.getCurrent(),query.getSize());
			PageHelper.orderBy("info.create_time desc");
			infos = this.baseMapper.queryListCB(wrapper,deptId,AutUserId,tbBus);

		}else if(tbBus.equals("3")){
			//查询我的关注
			QueryWrapper<FollowInformation> wrapperF=new QueryWrapper<>();
			wrapperF.eq("business_Type","1");
			if(ObjectUtil.isNotEmpty(user)){
				wrapperF.eq("follow_User_Id",user.getUserId());
			}
			wrapperF.eq("business_Type","1");
			wrapperF.eq("status","1");
			wrapperF.select("distinct business_Id");
			List<Object> list = followInformationService.listObjs(wrapperF);
			if(ObjectUtil.isNotEmpty(list)){
				wrapper.in("info.id",list);
			}else {
				wrapper.ne("1",1);
			}
			PageHelper.startPage(query.getCurrent(),query.getSize());
			PageHelper.orderBy("info.create_time desc");
			infos = this.baseMapper.queryListCB(wrapper,deptId,AutUserId,tbBus);
		}else if(tbBus.equals("4")){
			//查询领导关注
			//督查督办领导关注
			Set userId=new HashSet();
			Set leaderDeptId=new HashSet();
			leaderDeptId.add(PropConstant.getSwldDeptId());
			leaderDeptId.add(PropConstant.getSzfldDeptId());
			if(ObjectUtil.isNotEmpty(user)){
				boolean flag=true;
				if(leaderDeptId.contains(user.getDeptId())){
				}else {
					if(flag){
						if(ObjectUtil.isEmpty(user.getPostId())||!user.getPostId().equals(Constants.USER_POST_DEPT_LD_id)){
							R<List<User>> userLeader = userClient.getUserLeader(user.getDeptId(),Constants.USER_POST_DEPT_LD_id);
							List<User> users = userLeader.getData();
							for(User u:users){
								userId.add(u.getId());
							}
						}
					}
				}
			}
			//获取领导关注的事项id
			QueryWrapper<FollowInformation> wrapperF=new QueryWrapper<>();
			Set finalLeaderDeptId = leaderDeptId;
			if(ObjectUtil.isEmpty(finalLeaderDeptId)&&ObjectUtil.isEmpty(userId)){
				wrapper.ne("1",1);
			}
			if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
				wrapper.and(i->{
					if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
						i.or().in("follow.follow_Dept_Id",finalLeaderDeptId);
					}
					if(ObjectUtil.isNotEmpty(userId)){
						i.or().in("follow.follow_User_Id",userId);
					}
				});
			}
//			wrapperF.eq("business_Type","1");
//			wrapperF.eq("status","1");
//			wrapperF.select("distinct business_Id");
//			List<Object> list = followInformationService.listObjs(wrapperF);
//			if(ObjectUtil.isNotEmpty(list)){
//				wrapper.in("info.id",list);
//			}else {
//				wrapper.ne("1",1);
//			}

			if(ObjectUtil.isNotEmpty(user)){
				String[] deptIds = user.getDeptId().split(",");
				boolean isNoleader=false;
				for(String id:deptIds){
					if(leaderDeptId.contains(id)){
						isNoleader=false;
						break;
					}else{
						isNoleader=true;
					}
				}

				if(isNoleader){
					wrapper.and(i->{
						if(ObjectUtil.isNotEmpty(user.getPostId())&&user.getPostId().equals(Constants.USER_POST_DEPT_LD_id)){
							i.or().eq("info.create_dept",user.getDeptId());
						}
						i.or().apply("INSTR(info.lead_unit,"+user.getDeptId()+")>0");
						i.or().apply("INSTR(info.duty_Unit,"+user.getDeptId()+")>0");
						i.or().eq("info.create_user",user.getUserId());
					});
				}

			}
			PageHelper.startPage(query.getCurrent(),query.getSize());
			PageHelper.orderBy("info.create_time desc");
			infos = this.baseMapper.queryListfollow(wrapper,deptId,AutUserId);
		}
			else if(tbBus.equals("5")){
				//查询待办事项
				Boolean isGly=false;
				Boolean isDeptLg=false;
				Set<String> post=new HashSet<>();
				if(ObjectUtil.isNotEmpty(userPostIds)){
					String [] posts=userPostIds.split(",");
					for(int i=0;i<posts.length; i++){
						String postId= posts[i];
						if(postId.equals(Constants.USER_POST_GLY_id)){
							isGly=true;
							break;
						}else if(postId.equals(Constants.USER_POST_DEPT_LD_id)){
							isDeptLg=true;
							break;
						}else{
							isGly =false;
							isDeptLg=false;
						}
					}
				}


				//当前登录用户岗位
				String finalDeptId = deptId;
				Boolean finalIsGly = isGly;
//			if(finalIsGly|| isDeptLg){
//				wrapper.eq("sign.sign_dept", finalDeptId);
//				wrapper.eq("1",1);
//			}
				wrapper.and(i->{
					if(finalIsGly){
						i.or().and(j->{
							j.eq("sign.sign_dept", finalDeptId);
							j.eq("1", 1);
						});
					}

					if(ObjectUtil.isNotEmpty(user)){
						i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
						i.or().apply("INSTR(plan.down_User_Id,'"+user.getUserId()+"')>0");
//					i.or().eq("plan.down_User_Id",user.getUserId());
						i.or().eq("auditShow.user_id",user.getUserId());
						i.or().eq("auditHB.user_id",user.getUserId());
						i.or().eq("auditJH.user_id",user.getUserId());
						if(finalIsGly){
							i.or().eq("plan.report_dept", user.getDeptId());
						}
					}
				});
				PageHelper.startPage(query.getCurrent(),query.getSize());
				PageHelper.orderBy("info.create_time desc");
				wrapper.ne("info.flow_status", 4);
				infos = this.baseMapper.queryListCB(wrapper,deptId,AutUserId,tbBus);
		}
		for (SupervisionInfoVO info : infos){
			QueryWrapper<SupervisionSign> ee=new QueryWrapper();
			ee.eq("serv_id",info.getId());
			//获取单位签收信息
			List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
			Map<Long, Integer> dutyMap = new HashMap<>();
			Map<Long, Integer> leadMap = new HashMap<>();
			for (SupervisionSign sign: supervisionSignList)
			{
				int status = sign.getSignStatus();
				Long unit = sign.getSignDept();
				String type = sign.getDeptType();
				if (type.equals("duty")){
					dutyMap.put(unit, status);
				}
				if (type.equals("lead")){
					leadMap.put(unit, status);
				}
			}
			info.setDutyMap(dutyMap);
			info.setLeadMap(leadMap);
			if (info.getPlanId() == null) {
				String servCode = info.getServCode();
				SupervisionInfoVO infos1 = this.baseMapper.selectplanid(servCode,deptId);
				if (infos1 != null) {
					if (infos1.getReportStatus() != null) {
						info.setReportStatus(infos1.getReportStatus());
					}
					if (infos1.getPlanId() != null) {
						info.setPlanId(infos1.getPlanId());
					}
					if (infos1.getReporId() != null) {
						info.setReporId(infos1.getReporId());
					}
					if (infos1.getReportDownStatus() != null) {
						info.setReportDownStatus(infos1.getReportDownStatus());
					}
				}
			}
		}
		PageInfo pageInfo = new PageInfo(infos);
		return pageInfo;
	}

	@Override
	@Transactional
	public boolean savebus(SupervisionInfo supervisionInfo, String userIds,  String sync,String title) {

		String servCode= OrderNoUtils.createOrderNo("serv");
		supervisionInfo.setServCode(servCode);
		if(supervisionInfo.getFlowStatus().equals("1")){
			supervisionInfo.setServStatus("1");
		}
		boolean flag=this.save(supervisionInfo);

		if(flag){
			//保存阶段信息
			List<SupervisionPhasePlan> supervisionPhasePlanList = supervisionInfo.getSupervisionPhasePlanList();
			if(ObjectUtil.isNotEmpty(supervisionPhasePlanList)){
				for(SupervisionPhasePlan plana:supervisionPhasePlanList){
					plana.setServCode(servCode);
				}
				flag=supervisionPhasePlanService.saveList(supervisionPhasePlanList);
			}

			//保存项目附件
			if(flag){
				List<SupervisionFiles> supervisionFilesList = supervisionInfo.getSupervisionFilesList();
				BladeUser user = AuthUtil.getUser();
				if(ObjectUtil.isNotEmpty(supervisionFilesList)){
					for(SupervisionFiles file:supervisionFilesList){
						file.setServCode(servCode);
						file.setFileFrom("1");
						file.setUploadUser(user.getUserId().toString());
						file.setUploadUserName(user.getNickName());
						file.setUploadTime(DateUtils.getNowDate());
					}
					flag=supervisionFilesService.saveOrUpdateBatch(supervisionFilesList);
				}
			}
		}
//		获取发送短信的人员信息：
		List<User> magUsers=new ArrayList<>();
		User user=null;
		if(ObjectUtil.isNotEmpty(AuthUtil.getUserId())){
			user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		}

		//下发
		if(flag) {
			if (supervisionInfo.getFlowStatus().equals("1")) {
				//处理签收单位记录
				String[] leadUnits = supervisionInfo.getLeadUnit().split(",");
				for (String lead : leadUnits) {
					supervisionSignService.saveSignInfo("lead", lead, supervisionInfo.getId().toString());
					//发遂消息  岗位:"管理员ID"1516056792837869570
					R<List<User>> userLeader = userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
					if (ObjectUtil.isNotEmpty(userLeader.getData())) {
						magUsers.addAll(userLeader.getData());
					}
				}
				String dutyUnit = supervisionInfo.getDutyUnit();
				if (StringUtils.isNotEmpty(dutyUnit)) {
					String[] dutyUnits = dutyUnit.split(",");
					for (String duty : dutyUnits) {
						supervisionSignService.saveSignInfo("duty", duty, supervisionInfo.getId().toString());
						//获取需要发送消息的人员
						R<List<User>> userLeader = userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
						if (ObjectUtil.isNotEmpty(userLeader.getData())) {
							magUsers.addAll(userLeader.getData());
						}
					}
				}
				String userId = "";
				if(ObjectUtil.isNotEmpty(supervisionInfo.getDutyLeader())){
					userId=supervisionInfo.getDutyLeader()+",";
				}
				for (int i = 0; i < magUsers.size(); i++) {
					if (i == magUsers.size() - 1) {
						userId += magUsers.get(i).getId().toString();
					} else {
						userId += magUsers.get(i).getId().toString() + ",";
					}
				}

				if (ObjectUtil.isNotEmpty(userId)) {
					supervisionInfo = this.getById(supervisionInfo.getId());
					//督查督办人
					User userdb = userClient.userInfoById(supervisionInfo.getCreateUser()).getData();
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(userdb.getRealName());
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(supervisionInfo.getId());
					unifyMessage.setMsgTitle("事项下发消息");
					unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_PC_XF);
					unifyMessage.setMsgPlatform("web");
					unifyMessage.setReceiveUser(userId);
					unifyMessage.setMsgIntro("【" + userNameDecrypt + "】已下发【" + supervisionInfo.getServName() + "】。");
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					//sysClie
					updateAppMss(dictBizClient, supervisionInfo, unifyMessage, unifyMessageService);
//					sendMessage(unifyMessage);
				}

				SupervisionLog log = new SupervisionLog();
				log.setServCode(supervisionInfo.getServCode());
				log.setOperationDept(user.getDeptId());
				log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
				log.setOperationUser(user.getId().toString());
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				log.setOperationUserName(userNameDecrypt);
				log.setOperationType("1");
				log.setOperationTime(new Date());
				log.setContent("【" + supervisionInfo.getServName() + "】已下发");
				supervisionLogService.save(log);
			} else if (supervisionInfo.getFlowStatus().equals("5")) {
				try {
					//送审
					this.supervisionSubmitAuditService.saveSubmitAudit(supervisionInfo.getId().toString(), title, userIds, sync, StatusConstant.OPERATION_TYPE_INFO);


					//发消息
					String value = DictBizCache.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo());

					String users = userIds;
					if ("0".equals(sync)) {
						users = userIds.split(",")[0];
					}
					this.unifyMessageService.sendDcSsMsg(user.getRealName(),supervisionInfo.getId().toString(), users, value, supervisionInfo.getServName());

					SupervisionLog log = new SupervisionLog();
					log.setServCode(supervisionInfo.getServCode());
					log.setOperationDept(user.getDeptId());
					log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
					log.setOperationUser(user.getId().toString());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					log.setOperationUserName(userNameDecrypt);
					log.setOperationType("7");
					log.setOperationTime(new Date());
					log.setContent("【" + supervisionInfo.getServName() + "】已送审");
					supervisionLogService.save(log);
				} catch (Exception e) {
				}

			}
		}
		sendMessageByNames(supervisionInfo);
		return flag;
	}

	// 发送短信
	public void sendMessageByNames(SupervisionInfo supervisionInfo){
		String[] userName = getArray(supervisionInfo.getDutyLeaderName(), supervisionInfo.getLeadUnitName(), supervisionInfo.getDutyUnitName());
		String[] userNames = removeRepeat(userName);
		List<String> phones = getUsers(userNames);
		System.out.println("select result userList = " + phones.toString());
		if (Func.isNull(phones) || phones.size() == 0) return;
		String[] tels = phones.toArray(new String[phones.size()]);
		String[] newTels = removeRepeat(tels);
		if (!Func.isNull(newTels) && newTels.length > 0){
			String telStr = String.join(",", newTels);
			String smsContent = "【督考一体化平台提醒】您单位有一项新的督办事项，请登录督考一体化平台查看。";
			smsDockingService.send(telStr, smsContent);
		}
	}
	public List<String> getUsers(String[] userNames) {
		System.out.println("select condition userNames = " + Arrays.toString(userNames));
		if (Func.isNull(userNames) || userNames.length == 0) return null;
		List<String> names = Arrays.asList(userNames);
		List<String> phones = userClient.getPhones(names).getData();
		return phones;
	}

	//将字符传 转换为数组
	public String [] getArray(String ... args){
		if(Func.isNull(args) || args.length == 0) return null;
		ArrayList<String> list = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			if (Func.notNull(args[i]) && args[i].length() > 0) {
				String[] split = args[i].split(",");
				for (String s : split) {
					if (Func.notNull(s) && s.length() > 0) list.add(s);
//					if (Func.notNull(s) && s.length() > 0) list.add("'" + s + "'");
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	//发送短信
//	public void sendMessage(UnifyMessage unifyMessage){
//		if (Func.isNull(unifyMessage) || Func.isNull(unifyMessage.getReceiveUser())) return;
//		//对接收短信的用户id去重
//		String[] receivers = removeRepeat(unifyMessage.getReceiveUser().split(","));
//		String[] phones = new String[receivers.length];
//		for (int i = 0; i < receivers.length; i++) {
//			phones[i] = userClient.userInfoById(Long.parseLong(receivers[i])).getData().getPhone();
//		}
//		String[] tels = removeRepeat(phones);
//		if (!Func.isNull(tels) && tels.length > 0) {
//			String telstr = String.join(",", tels);
////			String smsContent = "【督考一体化平台】\n【" + unifyMessage.getMsgTitle() + "】 " + unifyMessage.getMsgSubitem() + "：" + unifyMessage.getMsgIntro();
//			String smsContent = "【督考一体化平台提醒】您单位有一项新的督办事项,请登录督考一体化平台查看。";
////			smsDockingService.send(telstr, smsContent);
//		}
//	}
	//对数组去重
	public String [] removeRepeat(String [] strs){
		if (Func.isNull(strs) || strs.length <= 0) return null;
		Set<Object> set = new TreeSet<>();
		for (String str : strs) {
			if (Func.notNull(str) && str.length() > 0) set.add(str);
		}
		return set.toArray(new String[set.size()]);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveAll(List<SupervisionInfo> list) {
		boolean flag = false;
		for (SupervisionInfo info : list) {
			String servCode= OrderNoUtils.createOrderNo("serv");
			info.setServCode(servCode);
			flag=this.save(info);

			if(flag){
				//保存阶段信息
				List<SupervisionPhasePlan> supervisionPhasePlanList = info.getSupervisionPhasePlanList();
				if(ObjectUtil.isNotEmpty(supervisionPhasePlanList)){
					for(SupervisionPhasePlan plana:supervisionPhasePlanList){
						plana.setServCode(servCode);

						List<SupervisionPhaseRemind> supervisionPhaseRemindList = plana.getSupervisionPhaseRemindList();
						for (SupervisionPhaseRemind phaseRemind : supervisionPhaseRemindList) {
							phaseRemind.setServCode(servCode);
						}
					}
					flag=supervisionPhasePlanService.saveList(supervisionPhasePlanList);
				}

				//保存项目附件
				if(flag){
					List<SupervisionFiles> supervisionFilesList = info.getSupervisionFilesList();
					if(ObjectUtil.isNotEmpty(supervisionFilesList)){
						for(SupervisionFiles file:supervisionFilesList){
							file.setServCode(servCode);
							file.setFileFrom("1");
						}
						flag=supervisionFilesService.saveOrUpdateBatch(supervisionFilesList);
					}
				}
			}
		}
		return flag;
	}

	@Override
	public SupervisionInfo details(Long id) {
		SupervisionInfo info=new SupervisionInfo();
		String servCode=null;
		info=this.getById(id);
		if(ObjectUtil.isNotEmpty(info))
			servCode=info.getServCode();

		//获取阶段信息
		List<SupervisionPhasePlan> supervisionPhasePlanList = supervisionPhasePlanService.listByservCode(servCode);
		if(!supervisionPhasePlanList.isEmpty()){
			//获取阶段汇报提醒信息
			for (SupervisionPhasePlan plan:supervisionPhasePlanList){
				String planCode=plan.getPhaseCode();
				QueryWrapper<SupervisionPhaseRemind> wrapper=new QueryWrapper<>();
				wrapper.eq("phase_code",planCode);
				List<SupervisionPhaseRemind> supervisionPhaseRemindList = supervisionPhaseRemindService.list(wrapper);
				if(!supervisionPhaseRemindList.isEmpty()){
					plan.setSupervisionPhaseRemindList(supervisionPhaseRemindList);
				}
			}
			info.setSupervisionPhasePlanList(supervisionPhasePlanList);
		}
		QueryWrapper<SupervisionSign> ee=new QueryWrapper();
		ee.eq("serv_id",info.getId());
		//获取单位签收信息
		List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
		Map<Long, Integer> dutyMap = new HashMap<>();
		Map<Long, Integer> leadMap = new HashMap<>();
		for (SupervisionSign sign: supervisionSignList)
		{
			int status = sign.getSignStatus();
			Long unit = sign.getSignDept();
			String type = sign.getDeptType();
			if (type.equals("duty")){
				dutyMap.put(unit, status);
			}
			if (type.equals("lead")){
				leadMap.put(unit, status);
			}
		}
		info.setDutyMap(dutyMap);
		info.setLeadMap(leadMap);
		//获取附件信息
		QueryWrapper<SupervisionFiles> wrapperFiles=new QueryWrapper<>();
		wrapperFiles.eq("serv_code",servCode);
		wrapperFiles.eq("file_From","1");
		List<SupervisionFiles> supervisionFilesList = supervisionFilesService.list(wrapperFiles);
		if(!supervisionFilesList.isEmpty()){
			info.setSupervisionFilesList(supervisionFilesList);
		}
		//获取当前登录人对该事项的关注信息
		QueryWrapper<FollowInformation>  wrapperMation=new QueryWrapper<>();
		wrapperMation.eq("business_Id",id);
		wrapperMation.eq("business_Type","1");
		Long userId = AuthUtil.getUserId();
		if(ObjectUtil.isNotEmpty(userId)){
			wrapperMation.eq("follow_User_Id",userId);
		}else{
			wrapperMation.ne("1",1);
		}
		FollowInformation followInformation = followInformationService.getOne(wrapperMation);
		if(ObjectUtil.isNotEmpty(followInformation)){
			info.setFollowInformation(followInformation);
		}
		return info;
	}

	@Override
	public SupervisionInfoVO detailsNew(Long servId,String servCode,String tbBus,BladeUser user) {
		String deptId="";
		String AutUserId="";
		String userPostIds="";
		if(ObjectUtil.isNotEmpty(user)){
			//当前用户所在部门
			deptId=user.getDeptId();
			AutUserId=user.getUserId().toString();
			userPostIds=user.getPostId();
		}
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper();
		if(ObjectUtil.isNotEmpty(servId)){
			wrapper.eq("info.id",servId);
		}else {
			wrapper.eq("info.serv_code",servCode);
		}
		SupervisionInfoVO detailsNew= this.baseMapper.detailsNew(wrapper,deptId,AutUserId);
		if (detailsNew.getPlanId() ==null){
			String servCode1 = detailsNew.getServCode();

			SupervisionInfoVO detailsNew1 = this.baseMapper.selectplanid(servCode1,deptId);
			if (detailsNew1 != null) {
				if (detailsNew1.getReportStatus() != null) {
					detailsNew.setReportStatus(detailsNew1.getReportStatus());
				}
				if (detailsNew1.getPlanId() != null) {
					detailsNew.setPlanId(detailsNew1.getPlanId());
				}
				if (detailsNew1.getReporId() != null) {
					detailsNew.setReporId(detailsNew1.getReporId());
				}
				if (detailsNew1.getReportDownStatus() != null) {
					detailsNew.setReportDownStatus(detailsNew1.getReportDownStatus());
				}
			}
		}
		return detailsNew;
	}

	@Override
	public boolean updatebus(SupervisionInfo supervisionInfo,String userIds, String sync,String title) {
		SupervisionInfo supervisionInfo1 = this.getById(supervisionInfo.getId());
		//更新督查督办主表数据
		if(supervisionInfo.getFlowStatus().equals("1")){
			supervisionInfo.setServStatus("1");
		}
		boolean flag = this.updateById(supervisionInfo);
		if(flag){
			//更新阶段信息
			List<SupervisionPhasePlan> supervisionPhasePlanList = supervisionInfo.getSupervisionPhasePlanList();
			flag=supervisionPhasePlanService.updateList(supervisionPhasePlanList,supervisionInfo.getServCode(),supervisionInfo);
		}
		if(flag){
			//更新附件
			List<SupervisionFiles> supervisionFilesList = supervisionInfo.getSupervisionFilesList();
			flag = supervisionFilesService.updateList(supervisionFilesList, supervisionInfo.getServCode());

		}

		List<User> magUsers=new ArrayList<>();
		//下发
		if(flag){
			/*if(supervisionInfo.getFlowStatus().equals("1")){
				//处理签收单位记录
				String[] leadUnits =supervisionInfo.getLeadUnit().split(",");
				for(String lead : leadUnits){
					supervisionSignService.saveSignInfo("lead",lead,supervisionInfo.getId().toString());
					//发遂消息  岗位:"管理员ID"1516056792837869570
					R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
					if(ObjectUtil.isNotEmpty(userLeader.getData())){
						magUsers.addAll(userLeader.getData());
					}
				}
				String dutyUnit=supervisionInfo.getDutyUnit();
				if(StringUtils.isNotEmpty(dutyUnit)){
					String[] dutyUnits = dutyUnit.split(",");
					for(String duty : dutyUnits){
						supervisionSignService.saveSignInfo("duty",duty,supervisionInfo.getId().toString());
						//获取需要发送消息的人员
						R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
						if(ObjectUtil.isNotEmpty(userLeader.getData())){
							magUsers.addAll(userLeader.getData());
						}
					}
				}

				String userId="";
				if(ObjectUtil.isNotEmpty(supervisionInfo.getDutyLeader())){
					userId=supervisionInfo.getDutyLeader()+",";
				}
				for(int i=0;i<magUsers.size();i++){
					if(i==magUsers.size()-1){
						userId+=magUsers.get(i).getId().toString();
					}else{
						userId+=magUsers.get(i).getId().toString()+",";
					}
				}
				if(ObjectUtil.isNotEmpty(userId)){
					User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
					//督查督办人
					User userdb = userClient.userInfoById(supervisionInfo.getCreateUser()).getData();
					supervisionInfo = this.getById(supervisionInfo.getId());
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(supervisionInfo.getId());
					unifyMessage.setMsgTitle("事项签收消息");
					unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_PC_XF);
					unifyMessage.setMsgPlatform("web");
					unifyMessage.setReceiveUser(userId);
					unifyMessage.setMsgIntro("【"+userdb.getRealName()+"】已下发事项【"+supervisionInfo.getServName()+"】请注意签收!");
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);
					//sysClie
					updateAppMss(dictBizClient, supervisionInfo, unifyMessage, unifyMessageService);

					SupervisionLog log = new SupervisionLog();
					log.setServCode(supervisionInfo.getServCode());
					log.setOperationDept(user.getDeptId());
					log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
					log.setOperationUser(user.getId().toString());
					log.setOperationUserName(user.getRealName());
					log.setOperationType("1");
					log.setOperationTime(new Date());
					log.setContent("【"+supervisionInfo.getServName()+"】已下发");
					supervisionLogService.save(log);
				}

			}else*/ if(supervisionInfo.getFlowStatus().equals("5")){
				try {
					//送审
					this.supervisionSubmitAuditService.saveSubmitAudit(supervisionInfo.getId().toString(), title, userIds, sync, StatusConstant.OPERATION_TYPE_INFO);
					//发消息
					String value = DictBizCache.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo());

					String users = userIds;
					if ("0".equals(sync)) {
						users = userIds.split(",")[0];
					}
					R<User> userR = userClient.userInfoById(AuthUtil.getUserId());
					User user= userR.getData();
					this.unifyMessageService.sendDcSsMsg(user.getRealName(),supervisionInfo.getId().toString(), users, value, supervisionInfo.getServName());
					SupervisionLog log = new SupervisionLog();
					log.setServCode(supervisionInfo.getServCode());
					log.setOperationDept(user.getDeptId());
					log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
					log.setOperationUser(user.getId().toString());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					log.setOperationUserName(userNameDecrypt);
					log.setOperationType("7");
					log.setOperationTime(new Date());
					log.setContent("【" + supervisionInfo.getServName() + "】已送审");
					supervisionLogService.save(log);
				} catch (Exception e) {
				}


			}
			else if (supervisionInfo.getFlowStatus().equals("1")||supervisionInfo.getFlowStatus().equals("2") || supervisionInfo.getFlowStatus().equals("3") || supervisionInfo.getFlowStatus().equals("4") )
			{
				supervisionInfo = this.getById(supervisionInfo.getId());
				List<String> unitArray1 = Arrays.asList(supervisionInfo.getDutyUnit().split(","));
				List<String> leadArray1 = Arrays.asList(supervisionInfo.getLeadUnit().split(","));
				/*flag = this.updateById(supervisionInfo);*/
				List<String> unitArray = Arrays.asList(supervisionInfo1.getDutyUnit().split(","));
				List<String> leadArray = Arrays.asList(supervisionInfo1.getLeadUnit().split(","));
				QueryWrapper<SupervisionSign> ee=new QueryWrapper();
				ee.eq("serv_id",supervisionInfo1.getId());
				ee.eq("sign_status", 0);
				List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
				QueryWrapper<SupervisionSign> ee1=new QueryWrapper();
				ee1.eq("sign_status", 1);
				ee1.eq("serv_id",supervisionInfo1.getId());
				List<SupervisionSign> supervisionSignList1 = supervisionSignService.list(ee1);
				String duty = "";
				int signStatus = 0;
				 deleteDuty(supervisionSignList,duty,signStatus);
					if(ObjectUtil.isNotEmpty(unitArray1)) {
						duty = "duty";
						updateduty(supervisionSignList1, supervisionInfo1.getId(), duty, unitArray1);
					}
					if (ObjectUtil.isNotEmpty(leadArray1)) {
						duty = "lead";
						updateduty(supervisionSignList1, supervisionInfo1.getId(), duty, leadArray1);
					}
				if (flag) {
					UnifyMessage unifyMessage = new UnifyMessage();
					if (flag) {
						List<User> userList = null;
						if (ObjectUtil.isNotEmpty(unitArray)) {
							userList = getUsers(unitArray);
						}
						List<User> userList1 = null;
						if (ObjectUtil.isNotEmpty(leadArray)) {
							userList1 = getUsers(leadArray);
						}
						userList.addAll(userList1);
						for (User user : userList) {
							QueryWrapper<UnifyMessage> uu = new QueryWrapper<>();
							uu.eq("msg_title", "事项下发消息");
							uu.eq("msg_id", supervisionInfo.getId());
							uu.eq("receive_user", user.getId());
							unifyMessageService.remove(uu);
						}
						 userList = null;
						if (ObjectUtil.isNotEmpty(unitArray1)) {
							userList = getUsers(unitArray1);
						}
						 userList1 = null;
						if (ObjectUtil.isNotEmpty(leadArray1)) {
							userList1 = getUsers(leadArray1);
						}
						userList.addAll(userList1);
						String receiveUser = getReceiveUser(userList);
						updataWebMss(supervisionInfo.getId(), supervisionInfo, unifyMessage, receiveUser);
						updateAppMss(dictBizClient, supervisionInfo, unifyMessage, unifyMessageService);
					}
					flag = updateLog(supervisionInfo);
				}

			}

		}

		return flag;
	}

	@Override
	public boolean deletebus(String ids,String servCodes) {
		boolean flag=true;
		String id[] =ids.split(",");
		Set<Long> idList=new HashSet<>();
		for(String idStr:id){
			idList.add(Long.valueOf(idStr));
		}
		String servCode[] =servCodes.split(",");
		//删除附件
		QueryWrapper<T> wrapperServCode=new QueryWrapper<>();
		wrapperServCode.in("serv_Code",servCode);
		QueryWrapper<T> wrapperServId=new QueryWrapper<>();
		wrapperServId.in("serv_id",idList);
		QueryWrapper<T> wrapperReport=new QueryWrapper<>();
		wrapperReport.in("report_id",idList);
		QueryWrapper<T> wrapperMsgId=new QueryWrapper<>();
		wrapperMsgId.in("msg_id",idList);

		this.baseMapper.deleteByQueryWrapper("supervision_files",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_evaluate",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_log",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_phase_plan",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_phase_remind",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_phase_report",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_urge",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("supervision_phase_report_all",wrapperServCode);

		this.baseMapper.deleteByQueryWrapper("supervision_sign",wrapperServId);
		this.baseMapper.deleteByQueryWrapper("supervision_submit_audit",wrapperServId);
		this.baseMapper.deleteByQueryWrapper("supervision_submit_audit",wrapperReport);
		this.baseMapper.deleteByQueryWrapper("supervision_up_plan",wrapperServId);

		//删除基本信息
		this.baseMapper.deleteByQueryWrapper("supervision_info",wrapperServCode);
		this.baseMapper.deleteByQueryWrapper("unify_message",wrapperMsgId);
		this.baseMapper.deleteByQueryWrapper("supervision_score",wrapperServCode);
		return flag;
	}

	@Override
	public boolean finish(Long id,BladeUser user) {
		SupervisionInfo info= this.getById(id);
		info.setFlowStatus("4");
		if(ObjectUtil.isNotEmpty(info.getServStatus())&&info.getServStatus().equals("3")){
			info.setServStatus("5");
		}
		boolean flag = this.updateById(info);
		if(flag){
			SupervisionSign sign=new SupervisionSign();
			sign.setOverStatus(1);
			sign.setOverTime(DateUtils.getNowDate());
			if (ObjectUtil.isNotEmpty(user)){
				sign.setOverUser(user.getUserId());
				sign.setOverDept(Long.valueOf(user.getDeptId()));
			}
			QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
			wrapper.eq("serv_id",id);
			flag = supervisionSignService.update(sign, wrapper);
			//阶段进行办结
			if(flag){
				UpdateWrapper<SupervisionPhaseReport> wrapperR=new UpdateWrapper<>();
				wrapperR.eq("serv_code",info.getServCode());
				SupervisionPhaseReport report=new SupervisionPhaseReport();
				report.setStatus(2);
				supervisionPhaseReportService.update(report,wrapperR);
			}
		}
		if(flag){
			final R<User> userR1 = userClient.userInfoById(user.getUserId());
			User userR1Data = userR1.getData();

			List<User> userList=new ArrayList<>();
			//责任领导
			String dutyLeader = info.getDutyLeader();
			if(ObjectUtil.isNotEmpty(dutyLeader)){
				String[] dutyLeadeID = dutyLeader.split(",");
				for(String dID:dutyLeadeID){
					R<User> userR = userClient.userInfoById(Long.valueOf(dID));
					User userLD=userR.getData();
					if(ObjectUtil.isNotEmpty(userLD)){
						userList.add(userLD);
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
			String receiveUser="";
			for (User user1 : userList) {
				receiveUser += user1.getId()+",";
			}

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(id);
			message.setMsgTitle("督查督办事项办结");
			message.setMsgType("52");
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro("【"+userR1Data.getRealName()+"】已办结【"+info.getServName()+"】");
			message.setCreateTime(new Date());
			message.setReceiveUser(receiveUser);
			unifyMessageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType("10");
			message.setMsgSubitem(value);
			message.setTwoLevelType("52");
			unifyMessageService.sendMessageInfo(message);
		}
		R<User> userR = userClient.userInfoById(user.getUserId());
		User u=userR.getData();
		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(u.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(u.getDeptId())).getData());
		log.setOperationUser(u.getId().toString());
		log.setOperationUserName(u.getRealName());
		log.setOperationType("9");
		log.setOperationTime(new Date());
		log.setContent("【"+info.getServName()+"】已办结");
		supervisionLogService.save(log);
		return flag;
	}

	@Override
	public boolean finishDept(String deptIds,Long servId, BladeUser user) {
		String []  dept= deptIds.split(",");
		SupervisionSign sign=new SupervisionSign();
		sign.setOverStatus(1);
		sign.setOverTime(DateUtils.getNowDate());
		if (ObjectUtil.isNotEmpty(user)){
			sign.setOverUser(user.getUserId());
			sign.setOverDept(Long.valueOf(user.getDeptId()));
		}
		QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
		wrapper.eq("serv_id",servId);
		wrapper.in("sign_Dept",dept);
		boolean flag = supervisionSignService.update(sign, wrapper);
		//判断是否所有部门都办结
		QueryWrapper<SupervisionSign> wrapperb=new QueryWrapper<>();
		wrapperb.eq("serv_id",servId);
		wrapperb.eq("over_Status","0");
		List<SupervisionSign> supervisionSigns = supervisionSignService.list(wrapperb);
		if (ObjectUtil.isEmpty(supervisionSigns)){
			SupervisionInfo info=new SupervisionInfo();
			info.setId(servId);
			info.setFlowStatus("4");
			 flag = this.updateById(info);
		}
		return flag;
	}

	@Override
	public Map<String,Object> listStatistics(String servTypeThree,String deptId,String year) {
		Map<String,Object> re=this.baseMapper.listStatistics(servTypeThree,deptId,year);
		return re;
	}

	@Override
	public List<SupervisionInfo> listStatisticsdhb(Map<String, Object> entity ) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		wrapper.apply("ifnull(s.num,'0')>0");
		wrapper.eq("flow_status","3");
		if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
			wrapper.eq("i.serv_Type_Three",entity.get("servTypeThree"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
			wrapper.eq("i.create_dept",entity.get("deptId"));
		}
		wrapper.orderByDesc("i.create_time");
		List<SupervisionInfo> supervisionInfos = this.baseMapper.listStatisticsdhb(wrapper);
		return supervisionInfos;
	}
	@Override
	public List<SupervisionInfo> listStatisticsycq(Map<String, Object> entity ) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		wrapper.notIn("ifnull(flow_status,'0')","0","4");
		wrapper.apply("date_format( wcsx , '%Y-%m-%d %H:%i:%s' )< date_format( now( ) , '%Y-%m-%d %H:%i:%s' )");
//		wrapper.in("serv_Status","3","5");
		if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
			wrapper.eq("serv_Type_Three",entity.get("servTypeThree"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
			wrapper.eq("create_dept",entity.get("deptId"));
		}
		if (ObjectUtil.isNotEmpty(entity.get("searchYear"))){
			wrapper.eq("year(create_time)",entity.get("searchYear"));
		}
		wrapper.orderByDesc("create_time");
		List<SupervisionInfo> supervisionInfos = this.list(wrapper);
		return supervisionInfos;
	}

	@Override
	public Map<String, Object> mySupervision(Map<String, Object> entity, BladeUser user) {
		//未签收数据
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
		List<SupervisionSignVO> supervisionSignVOS = SupervisionSignWrapper.build().listVO(signs);
		//未汇报
		QueryWrapper<SupervisionPhaseReport> wrapperRe=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("reportStatus"))){
			wrapperRe.eq("report_Status",entity.get("reportStatus"));
		}
		if(ObjectUtil.isNotEmpty(user)){
			R<Post> postR = sysClient.getPost(Long.valueOf(user.getPostId()));
			Post data = postR.getData();
			//当前用户所在部门
			String deptId=user.getDeptId();
			//当前登录用户岗位
			String post=data.getPostCode();

			wrapperRe.eq("report_Dept",deptId);
			wrapperRe.apply("'"+ Constants.USER_POST_GLY+"'='"+post+"'");
		}
		wrapperRe.orderByDesc("create_Time");
		List<SupervisionPhaseReport> report = supervisionPhaseReportService.list(wrapperRe);
		List<SupervisionPhaseReportVO> supervisionPhaseReportVOS = SupervisionPhaseReportWrapper.build().listVO(report);
		//超期
		QueryWrapper<SupervisionInfo> ew =new QueryWrapper<>();
		ew.and(i->i.ne("flow_Status","4").ne("flow_Status","0"));
		ew.apply("date_format( wcsx , '%Y-%m-%d' )< date_format( now( ) , '%Y-%m-%d' )");
		if(ObjectUtil.isNotEmpty(user)){
			ew.eq("create_user",user.getUserId());
		}
		List<SupervisionInfo> records = this.list(ew);
		int sum=supervisionSignVOS.size()+supervisionPhaseReportVOS.size()+records.size();
		Map <String,Object> reult=new HashMap<>();
		reult.put("sum",sum);
		reult.put("wqs",supervisionSignVOS.size() );
		reult.put("whb",supervisionPhaseReportVOS.size() );
		reult.put("ycq",records.size() );
		reult.put("wqsList",supervisionSignVOS );
		reult.put("whbList",supervisionPhaseReportVOS );
		reult.put("ycqList",records );
		return reult;
	}

	@Override
	public PageInfo supervisionFollow(Query query,Map<String, Object> entity, BladeUser user) {
		//督查督办领导关注
		Set userId=new HashSet();
		Set leaderDeptId=new HashSet();
		leaderDeptId.add(PropConstant.getSwldDeptId());
		leaderDeptId.add(PropConstant.getSzfldDeptId());
		if(ObjectUtil.isNotEmpty(user)){
			boolean flag=true;
			if(leaderDeptId.contains(user.getDeptId())){
			}else {
				if(flag){
					if(ObjectUtil.isEmpty(user.getPostId())||!user.getPostId().equals(Constants.USER_POST_DEPT_LD_id)){
						R<List<User>> userLeader = userClient.getUserLeader(user.getDeptId(), Constants.USER_POST_DEPT_LD_id);
						List<User> users = userLeader.getData();
						for(User u:users){
							userId.add(u.getId());
						}
					}
				}
			}
		}
		//获取领导关注的事项id
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();

		Set finalLeaderDeptId = leaderDeptId;
		if(ObjectUtil.isEmpty(finalLeaderDeptId)&&ObjectUtil.isEmpty(userId)){
			wrapper.ne("1",1);
		}
		if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
			wrapper.and(i->{
				if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
					i.or().in("follow_Dept_Id",finalLeaderDeptId);
				}
				if(ObjectUtil.isNotEmpty(userId)){
					i.or().in("follow_User_Id",userId);
				}
			});
		}



		wrapper.eq("business_Type","1");
		wrapper.eq("status","1");
		//与当前登录人有关的数据
		if(ObjectUtil.isNotEmpty(user)){
			if(!leaderDeptId.contains(user.getDeptId())){
				wrapper.and(i->{
					i.or().apply("INSTR(info.lead_unit,"+user.getDeptId()+")>0");
					i.or().apply("INSTR(info.duty_Unit,"+user.getDeptId()+")>0");
					i.or().eq("info.create_user",user.getUserId());
				});
			}
		}
		wrapper.isNotNull("info.id");
		wrapper.orderByDesc("follow.create_time");
		wrapper.eq("follow.is_deleted","0");
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionFollowVO> list = this.baseMapper.supervisionFollow(wrapper);
		//查询领导关注的事项
		PageInfo pageInfo = new PageInfo(list);
		return pageInfo;
	}

	@Override
	public PageInfo supervisionMyFollow(Query query,Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		wrapper.eq("business_Type","1");
		if(ObjectUtil.isNotEmpty(user)){
			wrapper.eq("follow_User_Id",user.getUserId());
		}
		wrapper.eq("follow.is_deleted","0");
		wrapper.isNotNull("info.id");
		wrapper.orderByDesc("follow.create_time");
		PageHelper.startPage(query.getCurrent(),query.getSize());
		List<SupervisionFollowVO> supervisionInfos = this.baseMapper.supervisionFollow(wrapper);
		PageInfo pageInfo = new PageInfo(supervisionInfos);
		return pageInfo;
	}

	@Override
	public List<SupervisionSign> supervisionNoSign(Map<String, Object> entity,BladeUser user) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("signStatus"))){
			wrapper.eq("sign.sign_Status",entity.get("signStatus"));
		}
		if(ObjectUtil.isNotEmpty(user)){
			wrapper.eq("info.create_user",user.getUserId());
		}
		wrapper.orderByDesc("sign.create_Time");
		List<SupervisionSign> supervisionInfos = this.baseMapper.supervisionNoSign(wrapper);
		return supervisionInfos;
	}

	@Override
	public List<SupervisionInfo> supervisionNoReport(Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("reportStatus"))){
			wrapper.eq("plan.report_Status",entity.get("reportStatus"));
		}
		if(ObjectUtil.isNotEmpty(user)){
			wrapper.eq("info.create_user",user.getUserId());
		}
		wrapper.ne("flow_Status",0);
		wrapper.orderByDesc("plan.create_Time");
		List<SupervisionInfo> supervisionInfos = this.baseMapper.supervisionNoReport(wrapper);
		return supervisionInfos;
	}

	@Override
	public List<SupervisionInfo> supervisionOverdue(Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionInfo> ew =new QueryWrapper<>();
		ew.and(i->i.ne("flow_Status","4").ne("flow_Status","0"));
		ew.apply("date_format( wcsx , '%Y-%m-%d' )< date_format( now( ) , '%Y-%m-%d' )");
		if(ObjectUtil.isNotEmpty(user)){
			ew.eq("create_user",user.getUserId());
		}
		List<SupervisionInfo> records = this.list(ew);
		return records;
	}

	@Override
	public List<SupervisionDeptPlanReportVO> servDeptPlanReport(Map<String, Object> entity) {
		QueryWrapper<SupervisionInfo> wrapper=new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("servId"))){
			wrapper.eq("sign.serv_id",entity.get("servId"));
		}
		String evaluateType="";
		if(ObjectUtil.isNotEmpty(entity.get("evaluateType"))){
			evaluateType= (String) entity.get("evaluateType");
		}
		wrapper.orderByDesc("sign.dept_type");
		return this.baseMapper.servDeptPlanReport(wrapper,evaluateType);
	}

	@Override
	public List<User> getMember(Long id) {
		SupervisionInfo supervisionInfo = this.getById(id);
		List<User> userList=new ArrayList<>();
		List<Long> userIds=new ArrayList<>();
		//责任领导
		String dutyLeader = supervisionInfo.getDutyLeader();
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
		String leadUnit = supervisionInfo.getLeadUnit();
		String[] leadUnits = leadUnit.split(",");
		for (String lead : leadUnits) {
			//发遂消息  岗位:"管理员ID"1516056792837869570
			R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
			if(ObjectUtil.isNotEmpty(userLeader.getData())){
				userList.addAll(userLeader.getData());
			}
		}
		//责任单位
		String dutyUnit = supervisionInfo.getDutyUnit();
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
		//评价人
		String evaluator = supervisionInfo.getEvaluator();
		if(ObjectUtil.isNotEmpty(evaluator)){
			R<User> userR = userClient.userInfoById(Long.valueOf(evaluator));
			User user=userR.getData();
			if(ObjectUtil.isNotEmpty(user)){
				userList.add(user);
			}
		}
		//督办人
		String supervisor = supervisionInfo.getSupervisor();
		if(ObjectUtil.isNotEmpty(supervisor)){
			R<User> userR = userClient.userInfoById(Long.valueOf(supervisor));
			User user=userR.getData();
			if(ObjectUtil.isNotEmpty(user)){
				userList.add(user);
			}
		}
		return userList;
	}

	@Override
	public String getMagUserTest(Long id,BladeUser user) {
		final R<User> userR1 = userClient.userInfoById(user.getUserId());
		User userR1Data = userR1.getData();
		String receiveUser = "";//接收人
		//事项成员
		List<User> member = this.getMember(id);
		//关注事项的人
		QueryWrapper<FollowInformation> wrapper=new QueryWrapper<>();
		wrapper.eq("business_Type","1");
		wrapper.eq("business_Id",id);
		List<FollowInformation> followInformations = followInformationService.list(wrapper);
		for(FollowInformation follow:followInformations ){
			R<User> userR = userClient.userInfoById(follow.getFollowUserId());
			User u=userR.getData();
			if(ObjectUtil.isNotEmpty(u)){
				member.add(u);
			}
		}
		//该事项部门领导
		String deptId = user.getDeptId();
		R<List<User>> userDeptLeader = userClient.getUserLeader(deptId, Constants.USER_POST_DEPT_LD_id);
		List<User> deptLeaderData = userDeptLeader.getData();
		if(ObjectUtil.isNotEmpty(deptLeaderData)){
			member.addAll(deptLeaderData);
		}

		//该事项的分管领导
		List<User> FGUser = userClient.getUserListByDeptId(deptId).getData();//获取该单位所有分管领导
		if(ObjectUtil.isNotEmpty(FGUser)){
			member.addAll(FGUser);
		}

		member =  member.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
		for (User user1 : member) {
			receiveUser += user1.getId()+",";
		}
		return receiveUser;
	}

	@Override
	public String getMagUserTest(Long id,Long userID) {
		final R<User> userR1 = userClient.userInfoById(userID);
		User userR1Data = userR1.getData();
		String receiveUser = "";//接收人
		//事项成员
		List<User> member = this.getMember(id);
		//关注事项的人
		QueryWrapper<FollowInformation> wrapper=new QueryWrapper<>();
		wrapper.eq("business_Type","1");
		wrapper.eq("business_Id",id);
		List<FollowInformation> followInformations = followInformationService.list(wrapper);
		for(FollowInformation follow:followInformations ){
			R<User> userR = userClient.userInfoById(follow.getFollowUserId());
			User u=userR.getData();
			if(ObjectUtil.isNotEmpty(u)){
				member.add(u);
			}
		}
		//该事项部门领导
		String deptId = userR1Data.getDeptId();
		R<List<User>> userDeptLeader = userClient.getUserLeader(deptId, Constants.USER_POST_DEPT_LD_id);
		List<User> deptLeaderData = userDeptLeader.getData();
		if(ObjectUtil.isNotEmpty(deptLeaderData)){
			member.addAll(deptLeaderData);
		}

		//该事项的分管领导
		List<User> FGUser = userClient.getUserListByDeptId(deptId).getData();//获取该单位所有分管领导
		if(ObjectUtil.isNotEmpty(FGUser)){
			member.addAll(FGUser);
		}

		member =  member.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
		for (User user1 : member) {
			receiveUser += user1.getId()+",";
		}
		return receiveUser;
	}

	@Override
	public boolean updateDuty(long id,String servCode,String dutyUnit,String dutyUnitName) {
		/*boolean flag = this.baseMapper.updateDuty(servCode,dutyUnit,dutyUnitName);*/
		SupervisionInfo supervisionInfo = new SupervisionInfo();
		supervisionInfo.setId(id);
		supervisionInfo.setServCode(servCode);
		supervisionInfo.setDutyUnit(dutyUnit);
		supervisionInfo.setDutyUnitName(dutyUnitName);
		UpdateWrapper<SupervisionInfo> qq=new UpdateWrapper();
		qq.eq("serv_code",servCode);
		boolean flag = this.update(supervisionInfo,qq);
		SupervisionInfo info = this.getById(id);
		List<String> array = Arrays.asList(dutyUnit.split(","));
		if (flag){
/*			supervisionSign.setServId(id);*/
			QueryWrapper<SupervisionSign> ee=new QueryWrapper();
			ee.eq("serv_id",id);
			ee.eq("sign_status", 0);
			List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
			QueryWrapper<SupervisionSign> ee1=new QueryWrapper();
			ee1.eq("sign_status", 1);
			ee1.eq("serv_id",id);
			ee1.eq("dept_type", "duty");
			List<SupervisionSign> supervisionSignList1 = supervisionSignService.list(ee1);
			String duty = "duty";
			int signStatus = 0;
			deleteDuty(supervisionSignList,duty,signStatus);
			updateduty(supervisionSignList1,id,duty, array);
		}
		if (flag) {
			UnifyMessage unifyMessage = new UnifyMessage();
			/*unifyMessage.setMsgTitle("事项下发消息");
			QueryWrapper<UnifyMessage> uu = new QueryWrapper<>();
			uu.eq("msg_id",id);
			uu.eq("msg_title",unifyMessage.getMsgTitle());*/
			List<User> userList = getUsers(array);

			for (User user:userList) {
				QueryWrapper<UnifyMessage> uu = new QueryWrapper<>();
				uu.eq("msg_id",id);
				unifyMessage.setMsgTitle("事项下发消息");
				uu.eq("receive_user",user.getId());
				 unifyMessageService.remove(uu);
			}
			if (flag) {
				String receiveUser = getReceiveUser(userList);
				supervisionInfo = this.getById(id);
				updataWebMss(id, supervisionInfo, unifyMessage, receiveUser);
				updateAppMss(dictBizClient, supervisionInfo, unifyMessage, unifyMessageService);
			}
			flag = updateLog(info);
		}
		return flag;
	}

	@Override
	public boolean deletedept(QueryWrapper<SupervisionSign> ew) {
		return this.baseMapper.deletedept(ew);
	}

	@Override
	public PageInfo cbqueryListPage(Query query, Map<String, Object> entity) {
			QueryWrapper<SupervisionInfoVO> wrapper=new QueryWrapper<>();
			wrapper.like("info.is_deleted",0);
			if(ObjectUtil.isNotEmpty(entity.get("servName"))){
				wrapper.like("info.serv_Name",entity.get("servName"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("servTypeOne"))){
				wrapper.eq("info.serv_Type_One",entity.get("servTypeOne"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("servTypeTwo"))){
				wrapper.eq("info.serv_Type_Two",entity.get("servTypeTwo"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("servTypeThree"))){
				wrapper.eq("info.serv_Type_Three",entity.get("servTypeThree"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("servTypeFour"))){
				wrapper.eq("info.serv_Type_Four",entity.get("servTypeFour"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("flowStatus"))){
				wrapper.eq("info.flow_Status",entity.get("flowStatus"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("year"))){
				wrapper.eq("date_format(info.create_Time,'%Y')",entity.get("year"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
				wrapper.like("info.duty_Unit",entity.get("dutyUnit"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
				wrapper.like("info.lead_Unit",entity.get("leadUnit"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("leadUnit"))){
				String leadUnits= (String) entity.get("leadUnit");
				String [] leadUnit =leadUnits.split(",");
				wrapper.and(wpleadUnit->{
					for(int i=0;i<leadUnit.length;i++){
						String aa= leadUnit[i];
						wpleadUnit.or().apply("INSTR(lead_unit,"+aa+")>0");
					}
				});
			}
			if(ObjectUtil.isNotEmpty(entity.get("dutyUnit"))){
				String dutyUnits= (String) entity.get("dutyUnit");
				String [] dutyUnit =dutyUnits.split(",");
				wrapper.and(wpdutyUnit->{
					for(int i=0;i<dutyUnit.length;i++){
						String aa= dutyUnit[i];
						wpdutyUnit.or().apply("INSTR(duty_Unit,"+aa+")>0");
					}
				});
			}
			if(ObjectUtil.isNotEmpty(entity.get("startCreateTime"))&&ObjectUtil.isNotEmpty(entity.get("endCreateTime"))){
				wrapper.between("info.create_time",entity.get("startCreateTime"),entity.get("endCreateTime"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("createUser"))){
				wrapper.like("info.create_User",entity.get("createUser"));
			}
			if(ObjectUtil.isNotEmpty(entity.get("id"))){
				wrapper.like("info.id",entity.get("id"));
			}

			String tbBus= (String) entity.get("tbBus");
			List<SupervisionInfoVO> infos=new ArrayList<>();
			BladeUser user = AuthUtil.getUser();
			String deptId="";
			String AutUserId="";
			String userPostIds="";
			if(ObjectUtil.isNotEmpty(user)){
				//当前用户所在部门
				deptId=user.getDeptId();
				AutUserId=user.getUserId().toString();
				userPostIds=user.getPostId();
			}
				//查询承办事项
				Boolean isGly=false;
				Boolean isDeptLg=false;
				Set<String> post=new HashSet<>();
				if(ObjectUtil.isNotEmpty(userPostIds)){
					String [] posts=userPostIds.split(",");
					for(int i=0;i<posts.length; i++){
						String postId= posts[i];
						if(postId.equals(Constants.USER_POST_GLY_id)){
							isGly=true;
							break;
						}else if(postId.equals(Constants.USER_POST_DEPT_LD_id)){
							isDeptLg=true;
							break;
						}else{
							isGly =false;
							isDeptLg=false;
						}
					}
				}
				//当前登录用户岗位
				String finalDeptId = deptId;
				Boolean finalIsGly = isGly;
//			if(finalIsGly|| isDeptLg){
//				wrapper.eq("sign.sign_dept", finalDeptId);
//				wrapper.eq("1",1);
//			}
				wrapper.and(i->{
					if(finalIsGly){
						i.or().and(j->{
							j.eq("sign.sign_dept", finalDeptId);
							j.eq("1", 1);
						});
					}

					if(ObjectUtil.isNotEmpty(user)){
						i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
						i.or().apply("INSTR(plan.down_User_Id,'"+user.getUserId()+"')>0");
//					i.or().eq("plan.down_User_Id",user.getUserId());
						i.or().eq("auditShow.user_id",user.getUserId());
						i.or().eq("auditHB.user_id",user.getUserId());
						i.or().eq("auditJH.user_id",user.getUserId());
						if(finalIsGly){
							i.or().eq("plan.report_dept", user.getDeptId());
						}
					}
				});
				PageHelper.startPage(query.getCurrent(),query.getSize());
				PageHelper.orderBy("info.create_time desc");
				infos = this.baseMapper.queryListCB(wrapper,deptId,AutUserId,tbBus);

			for (SupervisionInfoVO info : infos){
				QueryWrapper<SupervisionSign> ee=new QueryWrapper();
				ee.eq("serv_id",info.getId());
				//获取单位签收信息
				List<SupervisionSign> supervisionSignList = supervisionSignService.list(ee);
				Map<Long, Integer> dutyMap = new HashMap<>();
				Map<Long, Integer> leadMap = new HashMap<>();
				for (SupervisionSign sign: supervisionSignList)
				{
					int status = sign.getSignStatus();
					Long unit = sign.getSignDept();
					String type = sign.getDeptType();
					if (type.equals("duty")){
						dutyMap.put(unit, status);
					}
					if (type.equals("lead")){
						leadMap.put(unit, status);
					}
				}
				info.setDutyMap(dutyMap);
				info.setLeadMap(leadMap);
				if (info.getPlanId() == null) {
					String servCode = info.getServCode();
					SupervisionInfoVO infos1 = this.baseMapper.selectplanid(servCode,deptId);
					if (infos1 != null) {
						if (infos1.getReportStatus() != null) {
							info.setReportStatus(infos1.getReportStatus());
						}
						if (infos1.getPlanId() != null) {
							info.setPlanId(infos1.getPlanId());
						}
						if (infos1.getReporId() != null) {
							info.setReporId(infos1.getReporId());
						}
						if (infos1.getReportDownStatus() != null) {
							info.setReportDownStatus(infos1.getReportDownStatus());
						}
					}
				}
			}
			PageInfo pageInfo = new PageInfo(infos);
			return pageInfo;
		}

	private boolean updateLog(SupervisionInfo supervisionInfo) {
		boolean flag;
		User user=null;
		supervisionInfo = this.getById(supervisionInfo.getId());
		if(ObjectUtil.isNotEmpty(AuthUtil.getUserId())){
			user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		}
		SupervisionLog log = new SupervisionLog();
		log.setServCode(supervisionInfo.getServCode());
		log.setOperationDept(user.getDeptId());
		if (ObjectUtil.isNotEmpty(user.getDeptId())) {
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		}
		if (ObjectUtil.isNotEmpty(user.getId())) {
			log.setOperationUser(user.getId().toString());
		}
		if (ObjectUtil.isNotEmpty(user.getRealName())) {
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			log.setOperationUserName(userNameDecrypt);
		}
		log.setOperationType("1");
		log.setOperationTime(new Date());
		log.setContent("【" + supervisionInfo.getServName() + "】已下发");
		flag = supervisionLogService.save(log);
		return flag;
	}

	private void updateAppMss(IDictBizClient dictBizClient, SupervisionInfo supervisionInfo, UnifyMessage unifyMessage, IUnifyMessageService unifyMessageService) {
		R<String> value = dictBizClient.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo()); // 重要会议
		unifyMessage.setId(null);
		unifyMessage.setMsgPlatform("app");
		unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
		unifyMessage.setMsgSubitem(value.getData()); //重要会议
		unifyMessage.setTwoLevelType(Constants.DCDB_MAG_TYPE_APP_DB_tow_DCXF);
		unifyMessageService.sendMessageInfo(unifyMessage);
	}

	private void updataWebMss(long id, SupervisionInfo supervisionInfo, UnifyMessage unifyMessage, String receiveUser) {
		User userdb = userClient.userInfoById(supervisionInfo.getCreateUser()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(userdb.getRealName());
		unifyMessage.setMsgId(id);
		unifyMessage.setMsgTitle("事项下发消息");
		unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_PC_XF);
		unifyMessage.setMsgPlatform("web");
		unifyMessage.setReceiveUser(receiveUser);
		unifyMessage.setMsgIntro("【" + userNameDecrypt + "】已下发【" + supervisionInfo.getServName() + "】。");
		unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
		unifyMessageService.sendMessageInfo(unifyMessage);
	}

	@NotNull
	private String getReceiveUser(List<User> userList) {
		userList = userList.stream().filter(distinctByKey1(s -> s.getId())).collect(Collectors.toList());
		String receiveUser = "";
		for (User user1 : userList) {
			receiveUser += user1.getId() + ",";
		}
		return receiveUser;
	}

	@NotNull
	private List<User> getUsers(List<String> array) {
		List<User> userList = new ArrayList<>();
		for (String unit : array) {
			R<List<User>> userLeader = userClient.getUserLeader(unit, Constants.USER_POST_GLY_id);
			if (ObjectUtil.isNotEmpty(userLeader.getData())) {
				userList.addAll(userLeader.getData());
			}
		}

		return userList;
	}

	private void updateduty(List<SupervisionSign> supervisionSignList,long id,String duty, List<String> array) {
		for (String  unit: array) {
			boolean flag1 = false;
			if (unit != null && !unit.equals("")) {
				if (supervisionSignList != null) {
					/*for (SupervisionSign supervisionSign : supervisionSignList) {*/
						int i = 0;

						while (i < supervisionSignList.size()) {
							for (SupervisionSign supervisionSign1 : supervisionSignList) {
								if (unit.equals(String.valueOf(supervisionSign1.getSignDept()))) {
									flag1 = true;
								}
							}
							i++;
						}
						/*if (flag1) {
							break;
						}*/

					/*}*/
					if (!flag1) {
						SupervisionSign supervisionSign1 = new SupervisionSign();
						supervisionSign1.setServId(id);
						supervisionSign1.setSignDept(Long.valueOf(unit));
						supervisionSign1.setOverDept(Long.valueOf(unit));
						supervisionSign1.setSignStatus(0);
						supervisionSign1.setDeptType(duty);
						supervisionSignService.save(supervisionSign1);
					}
				} /*else {
					SupervisionSign supervisionSign1 = new SupervisionSign();
					supervisionSign1.setServId(id);
					supervisionSign1.setSignDept(Long.valueOf(unit));
					supervisionSign1.setOverDept(Long.valueOf(unit));
					supervisionSign1.setSignStatus(0);
					supervisionSign1.setDeptType(duty);
					supervisionSignService.save(supervisionSign1);
				}*/
			}
		}
	}

	private void deleteDuty(List<SupervisionSign> supervisionSignList, String duty, int signStatus ) {
		for (SupervisionSign sign: supervisionSignList) {
			QueryWrapper<SupervisionSign> ss = new QueryWrapper<>();
			ss.eq("serv_id",sign.getServId());
			if (ObjectUtil.isNotEmpty(duty)&&!duty.equals("")) {
				ss.eq("dept_type", duty);
			}
			ss.eq("sign_status",signStatus);
			 this.deletedept(ss);
		}
	}

	static <T> Predicate<T> distinctByKey1(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
