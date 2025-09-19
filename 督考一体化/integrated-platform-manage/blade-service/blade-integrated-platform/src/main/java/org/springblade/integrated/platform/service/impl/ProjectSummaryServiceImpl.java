package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.MapPorjectVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel;
import org.springblade.integrated.platform.excel.ProjectSummaryExcel1;
import org.springblade.integrated.platform.mapper.ProjectSummaryMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectSummaryServiceImpl extends BaseServiceImpl<ProjectSummaryMapper, ProjectSummary> implements IProjectSummaryService {

	@Autowired
	private IProjectPhasePlanService projectPhasePlanService;
	@Autowired
	private IProjectFilesService projectFilesService;
	@Autowired
	private IProjectLogService projectLogService;
	@Autowired
	private IUserClient userClient;
	@Autowired
	private ISysClient sysClient;
	@Autowired
	private IProjectPhaseRemindService projectPhaseRemindService;
	@Autowired
	private IUserSearchClient iUserSearchClient;

	@Override
	public int getTotalCount(String year) {
		return baseMapper.getTotalCount(year);
	}

	@Override
	public int getStartedCount(String year) {
		return baseMapper.getStartedCount(year);
	}
	@Override
	public float getTotalInvestment(String year) {
		return baseMapper.getTotalInvestment(year);
	}
	@Override
	public float getYearInvestment(String year) {
		return baseMapper.getYearInvestment(year);
	}
	@Override
	public float getDoneInvestment(String year) {
		return baseMapper.getDoneInvestment(year);
	}

	@Override
	public int getTZTotalCount(String projLabel,String year) {
		return baseMapper.getTZTotalCount(projLabel,year);
	}

	@Override
	public int getTZStartedCount(String projLabel,String year) {
		return baseMapper.getTZStartedCount(projLabel,year);
	}
	@Override
	public float getTZTotalInvestment(String projLabel,String year) {
		return baseMapper.getTZTotalInvestment(projLabel,year);
	}
	@Override
	public float getTZYearInvestment(String projLabel,String year) {
		return baseMapper.getTZYearInvestment(projLabel,year);
	}
	@Override
	public float getTZDoneInvestment(String projLabel,String year) {
		return baseMapper.getTZDoneInvestment(projLabel,year);
	}

	@Override
	public IPage<MapPorjectVO> queryProjectMap(IPage<MapPorjectVO> page, Map<String, Object> entity) {
		return baseMapper.queryProjectMap(page, entity);
	}
	@Override
	public List<MapPorjectVO> queryProjectMapNoPage(Map<String, Object> entity) {
		return baseMapper.queryProjectMapNoPage(entity);
	}

	@Override
	public List<ProjectSummaryExcel1> exportProjectSummary(ProjectSummaryExcel1 projectSummaryExcel1) {
		List<ProjectSummaryExcel1> scoreList = baseMapper.exportProjectSummary(projectSummaryExcel1);
		return scoreList;
	}




	@Override
	public ProjectSummary selectDetail(ProjectSummary projectSummary) {
		QueryWrapper<ProjectSummary> queryWrapper = new QueryWrapper<ProjectSummary>();
		queryWrapper.select(" * ");
		queryWrapper.eq(projectSummary.getId()!=0,"id",projectSummary.getId());
		return baseMapper.selectOne(queryWrapper);
	}

	/**
	 * 保存申报项目基础信息
	 * @param projectSummary
	 * @return
	 */
	@Override
	public boolean saveProjectSummary(ProjectSummary projectSummary){
		int zytzjh = 0;
		int sjtzjh = 0;
		int dftzjh = 0;
		if(ObjectUtil.isNotEmpty(projectSummary.getZytzjh())){
			zytzjh = Integer.parseInt(projectSummary.getZytzjh());
		}
		if(ObjectUtil.isNotEmpty(projectSummary.getSjtzjh())){
			sjtzjh = Integer.parseInt(projectSummary.getSjtzjh());
		}
		if(ObjectUtil.isNotEmpty(projectSummary.getDftzjh())){
			dftzjh = Integer.parseInt(projectSummary.getDftzjh());
		}
		projectSummary.setTzjhhj(String.valueOf(zytzjh+sjtzjh+dftzjh));
		boolean result = this.save(projectSummary);
		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(projectSummary.getId());
		projectLog.setHandleType("项目新增");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			projectLog.setHandleUser(userNameDecrypt);
		}
		String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
		projectLog.setHandleContent("【"+handleUserDecrypt+"】新增【"+projectSummary.getTitle()+"】");
		projectLogService.save(projectLog);
		String monthStr = "";
		if(result){
			List<ProjectPhasePlan> projectPhasePlanList = projectSummary.getProjectPhasePlanList();
			if(ObjectUtil.isNotEmpty(projectPhasePlanList)){
				for(ProjectPhasePlan projectPhasePlan:projectPhasePlanList){
					if(!monthStr.contains(String.valueOf(projectPhasePlan.getPlanMonth()))){
						monthStr += projectPhasePlan.getPlanMonth()+",";
						projectPhasePlan.setProjId(projectSummary.getId());
					}else{
						projectPhasePlanList.remove(projectPhasePlan);
					}
				}
				result = projectPhasePlanService.saveList(projectPhasePlanList);
			}
			List<ProjectFiles> projectFilesList = projectSummary.getProjectFilesList();
			if(ObjectUtil.isNotEmpty(projectFilesList)){
				for(ProjectFiles projectFiles:projectFilesList){
					projectFiles.setProjId(projectSummary.getId());
					String fileType = projectFiles.getFileType();
					if(fileType.equals(".xbm")||fileType.equals(".tif")||fileType.equals(".pjp")||fileType.equals(".svgz")||fileType.equals(".jpg")||fileType.equals(".jpeg")
						||fileType.equals(".ico")||fileType.equals(".tiff")||fileType.equals(".gif")||fileType.equals(".svg")||fileType.equals(".jfif")||fileType.equals(".webp")
						||fileType.equals(".png")||fileType.equals(".bmp")||fileType.equals(".pjpeg")||fileType.equals(".avif")){
						projectFiles.setFileFormat(1);
					}else if(fileType.equals(".avi")||fileType.equals(".wmv")||fileType.equals(".mpg")||fileType.equals(".mpeg")||fileType.equals(".mov")||fileType.equals(".rm")
						||fileType.equals(".ram")||fileType.equals(".swf")||fileType.equals(".flv")||fileType.equals(".mp4")){
						projectFiles.setFileFormat(2);
					}else{
						projectFiles.setFileFormat(3);
					}
				}
				result = projectFilesService.saveOrUpdateBatch(projectFilesList);
			}
		}
		return result;
	}

	/**
	 * 投资项目修改
	 * @param projectSummary
	 * @return
	 */
	@Override
	public boolean updateProjectSummary(ProjectSummary projectSummary){
		boolean result = this.updateById(projectSummary);
		ProjectLog projectLog = new ProjectLog();//项目日志
		projectLog.setProjId(projectSummary.getId());
		projectLog.setHandleType("项目修改");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		if(user==null){
			projectLog.setHandleUser(AuthUtil.getUserName());
		}else{
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			projectLog.setHandleUser(userNameDecrypt);
		}
		projectLog.setHandleDept(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getDeptName());
		String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
		projectLog.setHandleContent("【"+handleUserDecrypt+"】修改【"+projectSummary.getTitle()+"】");
		projectLogService.save(projectLog);
		if(result){
			List<ProjectPhasePlan> projectPhasePlanList = projectSummary.getProjectPhasePlanList();
			if(ObjectUtil.isNotEmpty(projectPhasePlanList)){
				result = projectPhasePlanService.saveList(projectPhasePlanList);
			}
			projectFilesService.deleteProjectFilesByProjId(projectSummary.getId().toString());
			List<ProjectFiles> projectFilesList = projectSummary.getProjectFilesList();
			if(ObjectUtil.isNotEmpty(projectFilesList)){
				for(ProjectFiles projectFiles:projectFilesList){
					projectFiles.setProjId(projectSummary.getId());
					String fileType = projectFiles.getFileType();
					if(fileType.equals(".xbm")||fileType.equals(".tif")||fileType.equals(".pjp")||fileType.equals(".svgz")||fileType.equals(".jpg")||fileType.equals(".jpeg")
						||fileType.equals(".ico")||fileType.equals(".tiff")||fileType.equals(".gif")||fileType.equals(".svg")||fileType.equals(".jfif")||fileType.equals(".webp")
						||fileType.equals(".png")||fileType.equals(".bmp")||fileType.equals(".pjpeg")||fileType.equals(".avif")){
						projectFiles.setFileFormat(1);
					}else if(fileType.equals(".avi")||fileType.equals(".wmv")||fileType.equals(".mpg")||fileType.equals(".mpeg")||fileType.equals(".mov")||fileType.equals(".rm")
						||fileType.equals(".ram")||fileType.equals(".swf")||fileType.equals(".flv")||fileType.equals(".mp4")){
						projectFiles.setFileFormat(2);
					}else{
						projectFiles.setFileFormat(3);
					}
				}
				result = projectFilesService.saveOrUpdateBatch(projectFilesList);
			}
		}
		return result;
	}

	/**
	 * 项目入库
	 * @param id
	 * @return
	 */
	@Override
	public boolean projectSummaryRk(String id,String reportStatus){
		return baseMapper.projectSummaryRk(id,reportStatus);
	}

	/**
	 * 项目送审
	 * @param id
	 * @return
	 */
	@Override
	public boolean projectSummaryWare(String id,String reportStatus){
		return baseMapper.projectSummaryWare(id,reportStatus);
	}

	/**
	 * 更新项目状态
	 * @param id
	 * @param projStatus
	 * @return
	 */
	@Override
	public boolean updateProjStatus(String id,String projStatus){
		return baseMapper.updateProjStatus(id, projStatus);
	}

	/**
	 * 项目退回
	 * @param id
	 * @return
	 */
	@Override
	public boolean projectSummaryRebake(String id,String reportStatus,String projStatus){
		return baseMapper.projectSummaryRebake(id,reportStatus,projStatus);
	}

	/**
	 * 项目挂牌
	 * @param id
	 * @param autoState
	 * @return
	 */
	@Override
	public boolean projectSummaryListing(String id,String autoState){
		return baseMapper.projectSummaryListing(id,autoState);
	}

	/**
	 * 项目移库
	 * @param id
	 * @param projLabel
	 * @return
	 */
	@Override
	public boolean projectSummaryYk(String id,String projLabel){
		return baseMapper.projectSummaryYk(id, projLabel);
	}

	@Override
	public void imimportProjectSummary(List<ProjectSummaryExcel> data){
		List<ProjectSummary> list = new ArrayList<>();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		data.forEach(projectSummaryExcel ->{
			ProjectSummary projectSummary = BeanUtil.copy(projectSummaryExcel,ProjectSummary.class);
			this.save(projectSummary);
			try {
				for (int i=0;i<12;i++){
					ProjectPhasePlan projectPhasePlan = new ProjectPhasePlan();
					if(i==0){
						projectPhasePlan.setStartTime(ft.parse(year+"-01-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-01-25 18:00:00"));
						projectPhasePlan.setPhaseName("1月进展情况");
						projectPhasePlan.setPlanMonth(1);
					}else if(i==1){
						projectPhasePlan.setStartTime(ft.parse(year+"-02-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-02-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-2月进展情况");
						projectPhasePlan.setPlanMonth(2);
					}else if(i==2){
						projectPhasePlan.setStartTime(ft.parse(year+"-03-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-03-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-3月进展情况");
						projectPhasePlan.setPlanMonth(3);
					}else if(i==3){
						projectPhasePlan.setStartTime(ft.parse(year+"-04-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-04-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-4月进展情况");
						projectPhasePlan.setPlanMonth(4);
					}else if(i==4){
						projectPhasePlan.setStartTime(ft.parse(year+"-05-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-05-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-5月进展情况");
						projectPhasePlan.setPlanMonth(5);
					}else if(i==5){
						projectPhasePlan.setStartTime(ft.parse(year+"-06-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-06-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-6月进展情况");
						projectPhasePlan.setPlanMonth(6);
					}else if(i==6){
						projectPhasePlan.setStartTime(ft.parse(year+"-07-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-07-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-7月进展情况");
						projectPhasePlan.setPlanMonth(7);
					}else if(i==7){
						projectPhasePlan.setStartTime(ft.parse(year+"-08-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-08-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-8月进展情况");
						projectPhasePlan.setPlanMonth(8);
					}else if(i==8){
						projectPhasePlan.setStartTime(ft.parse(year+"-09-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-09-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-9月进展情况");
						projectPhasePlan.setPlanMonth(9);
					}else if(i==9){
						projectPhasePlan.setStartTime(ft.parse(year+"-10-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-10-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-10月进展情况");
						projectPhasePlan.setPlanMonth(10);
					}else if(i==10){
						projectPhasePlan.setStartTime(ft.parse(year+"-11-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-11-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-11月进展情况");
						projectPhasePlan.setPlanMonth(11);
					}else if(i==11){
						projectPhasePlan.setStartTime(ft.parse(year+"-12-01 08:00:00"));
						projectPhasePlan.setEndTime(ft.parse(year+"-12-25 18:00:00"));
						projectPhasePlan.setPhaseName("1-12月进展情况");
						projectPhasePlan.setPlanMonth(12);
					}
					projectPhasePlan.setProjId(projectSummary.getId());
					projectPhasePlanService.save(projectPhasePlan);
					Calendar calendar1 = Calendar.getInstance();
					calendar1.setTime(projectPhasePlan.getEndTime());
					calendar1.add(Calendar.HOUR_OF_DAY, -1);
					ProjectPhaseRemind projectPhaseRemind = new ProjectPhaseRemind();
					projectPhaseRemind.setProjId(projectSummary.getId());
					projectPhaseRemind.setPhaseId(projectPhasePlan.getId());
					projectPhaseRemind.setPhaseName(projectPhasePlan.getPhaseName());
					projectPhaseRemind.setReportTime(calendar1.getTime());
					projectPhaseRemind.setReminderTime(1L);
					projectPhaseRemindService.save(projectPhaseRemind);
				}
			}catch (ParseException ex){
				ex.printStackTrace();
			}
		});
	}

	@Override
	public List<Map<String,Object>> getXmnfList(){
		return  baseMapper.getXmnfList();
	}



	/**
	 * 获取汇报列表信息
	 * @param id
	 * @return
	 */
	@Override
	public ProjectSummary getProjectPhaseReportByHbjdId(Long id){
		return baseMapper.getProjectPhaseReportByHbjdId(id);
	}

	/**
	 * 查询项目人员
	 * @param id
	 * @return
	 */
	@Override
	public List<User> getUserListByProjId(String id){
//		ProjectSummary projectSummary = this.getById(id);
//		List<User> list = new ArrayList<>();
//		String userId = "";
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getSjld())){
//				userId += projectSummary.getSjld()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getSjld())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getSjld())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getXjld())&&!userId.contains(projectSummary.getXjld())){
//				userId += projectSummary.getXjld()+",";
//				if (ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getXjld())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getXjld())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getSzhyzgbmZrr())&&!userId.contains(projectSummary.getSzhyzgbmZrr())){
//				userId += projectSummary.getSzhyzgbmZrr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getSzhyzgbmZrr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getSzhyzgbmZrr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getXqhyzgbmZrr())&&!userId.contains(projectSummary.getXqhyzgbmZrr())){
//				userId += projectSummary.getXqhyzgbmZrr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getXqhyzgbmZrr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getXqhyzgbmZrr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getBzzgdwZrr())&&!userId.contains(projectSummary.getBzzgdwZrr())){
//				userId += projectSummary.getBzzgdwZrr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getBzzgdwZrr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getBzzgdwZrr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getBzzrdwZrr())&&!userId.contains(projectSummary.getBzzrdwZrr())){
//				userId += projectSummary.getBzzrdwZrr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getBzzrdwZrr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getBzzrdwZrr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getSgfzr())&&!userId.contains(projectSummary.getSgfzr())){
//				userId += projectSummary.getSgfzr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getSgfzr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getSgfzr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getZrr())&&!userId.contains(projectSummary.getZrr())){
//				userId += projectSummary.getZrr()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getZrr())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getZrr())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		try {
//			if(StringUtil.isNotBlank(projectSummary.getSjbzld())&&!userId.contains(projectSummary.getSjbzld())){
//				userId += projectSummary.getSjbzld()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getSjbzld())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getSjbzld())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}try {
//			if(StringUtil.isNotBlank(projectSummary.getXjbzld())&&!userId.contains(projectSummary.getXjbzld())){
//				userId += projectSummary.getSjbzld()+",";
//				if(ObjectUtil.isNotEmpty(userClient.userInfoById(Long.parseLong(projectSummary.getXjbzld())).getData())){
//					list.add(userClient.userInfoById(Long.parseLong(projectSummary.getXjbzld())).getData());
//				}
//			}
//		}catch (Exception e){
//
//		}
//		return list;
		List<User> users = new ArrayList<>();
		ProjectSummary projectSummary = this.getById(id);
		if(projectSummary==null)  return users;
		String userIds = "";
		if(StringUtil.isNotBlank(projectSummary.getSjld()) && StringUtil.isNumeric(projectSummary.getSjld())){
			userIds += projectSummary.getSjld()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getXjld()) && StringUtil.isNumeric(projectSummary.getXjld())){
			userIds += projectSummary.getXjld()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSzhyzgbmZrr()) && StringUtil.isNumeric(projectSummary.getSzhyzgbmZrr())){
			userIds += projectSummary.getSzhyzgbmZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getXqhyzgbmZrr()) && StringUtil.isNumeric(projectSummary.getXqhyzgbmZrr())){
			userIds += projectSummary.getXqhyzgbmZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getBzzgdwZrr()) && StringUtil.isNumeric(projectSummary.getBzzgdwZrr())){
			userIds += projectSummary.getBzzgdwZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getBzzrdwZrr()) && StringUtil.isNumeric(projectSummary.getBzzrdwZrr())){
			userIds += projectSummary.getBzzrdwZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSgfzr()) && StringUtil.isNumeric(projectSummary.getSgfzr())){
			userIds += projectSummary.getSgfzr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getZrr())&& StringUtil.isNumeric(projectSummary.getZrr())){
			userIds += projectSummary.getZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSjbzld()) && StringUtil.isNumeric(projectSummary.getSjbzld())){
			userIds += projectSummary.getSjbzld()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getXjbzld()) && StringUtil.isNumeric(projectSummary.getXjbzld())){
			userIds += projectSummary.getSjbzld()+",";
		}
		if(StringUtils.isNotBlank(userIds))
     	   users = iUserSearchClient.sortListByIds(userIds).getData();
		return users;
	}

	/**
	 * 查询项目人员id(出掉loginId)
	 * @param id
	 * @return
	 */
	@Override
	public String getUserIdListByProjId(Long id,Long loginId){
		ProjectSummary projectSummary = this.getById(id);
		if(projectSummary==null)  return "";
		String userIds = "";
		if(StringUtil.isNotBlank(projectSummary.getSjld()) && StringUtil.isNumeric(projectSummary.getSjld()) && !projectSummary.getSjld().equals(String.valueOf(loginId))){
			userIds += projectSummary.getSjld()+",";
		}

		if(StringUtil.isNotBlank(projectSummary.getXjld()) && StringUtil.isNumeric(projectSummary.getXjld()) && !projectSummary.getXjld().equals(String.valueOf(loginId))){
			userIds += projectSummary.getXjld()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSzhyzgbmZrr()) && StringUtil.isNumeric(projectSummary.getSzhyzgbmZrr()) && !projectSummary.getSzhyzgbmZrr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getSzhyzgbmZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getXqhyzgbmZrr()) && StringUtil.isNumeric(projectSummary.getXqhyzgbmZrr()) && !projectSummary.getXqhyzgbmZrr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getXqhyzgbmZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getBzzgdwZrr()) && StringUtil.isNumeric(projectSummary.getBzzgdwZrr()) && !projectSummary.getBzzgdwZrr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getBzzgdwZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getBzzrdwZrr()) && StringUtil.isNumeric(projectSummary.getBzzrdwZrr()) && !projectSummary.getBzzrdwZrr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getBzzrdwZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSgfzr()) && StringUtil.isNumeric(projectSummary.getSgfzr()) && !projectSummary.getSgfzr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getSgfzr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getZrr())&& StringUtil.isNumeric(projectSummary.getZrr()) && !projectSummary.getZrr().equals(String.valueOf(loginId))){
			userIds += projectSummary.getZrr()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getSjbzld()) && StringUtil.isNumeric(projectSummary.getSjbzld()) && !projectSummary.getSjbzld().equals(String.valueOf(loginId))){
			userIds += projectSummary.getSjbzld()+",";
		}
		if(StringUtil.isNotBlank(projectSummary.getXjbzld()) && StringUtil.isNumeric(projectSummary.getXjbzld()) && !projectSummary.getXjbzld().equals(String.valueOf(loginId))){
			userIds += projectSummary.getSjbzld()+",";
		}

		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id

		List<User> users = new ArrayList<>();
		//市直行业主管部门
		if(StringUtil.isNotBlank(projectSummary.getSzhyzgbm())){
			users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, projectSummary.getSzhyzgbm()).getData());
			users.addAll(iUserSearchClient.listByPostAndDept(authPostId, projectSummary.getSzhyzgbm()).getData());
		}
		//县区行业主管部门
		if(StringUtil.isNotBlank(projectSummary.getXqhyzgbm())){
			users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, projectSummary.getXqhyzgbm()).getData());
			users.addAll(iUserSearchClient.listByPostAndDept(authPostId, projectSummary.getXqhyzgbm()).getData());
		}
		//调度单位
		if(StringUtil.isNotBlank(projectSummary.getDwmc())){
			users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, projectSummary.getDwmc()).getData());
			users.addAll(iUserSearchClient.listByPostAndDept(authPostId, projectSummary.getDwmc()).getData());
		}
		for (User user:users) {
			if(!String.valueOf(loginId).equals(String.valueOf(user.getId()))){
				userIds += user.getId()+",";
			}
		}
		return userIds;
	}

	/**
	 * 根据单位id获取用户列表(除掉loginId)
	 * @return
	 */
	@Override
	public String getUserIdListByDeptId(String deptId,String loginId){
		if(StringUtil.isBlank(deptId))
			return "";
		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
//		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
		String userIds = "";
		List<User> users = new ArrayList<>();
		//市直行业主管部门
		if(StringUtil.isNotBlank(deptId)){
//			users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId).getData());
			users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId).getData());
		}

		for (User user:users) {
			if(!loginId.equals(String.valueOf(user.getId()))){
				userIds += user.getId()+",";
			}
		}
		return userIds;
	}


	@Override
	public IPage<ProjectSummary> selectPage(IPage<ProjectSummary> page, Map<String, Object> entity) {
		return baseMapper.selectPage(page, entity);
	}

	@Override
	public List<ProjectSummary> selectListByYear(String searchYear) {
		return baseMapper.selectListByYear(searchYear);
	}

	@Override
	public IPage<ProjectSummary> selectXMGLPage(IPage<ProjectSummary> page, Map<String, Object> entity) {
		return baseMapper.selectXMGLPage(page, entity);
	}


}
