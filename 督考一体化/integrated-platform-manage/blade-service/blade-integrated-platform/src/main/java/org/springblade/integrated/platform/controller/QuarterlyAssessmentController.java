package org.springblade.integrated.platform.controller;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.common.utils.file.FileUploadUtils;
import org.springblade.integrated.platform.excel.*;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;


/**
 * 考核体系 控制层
 *
 * @Author zrj
 * @Create
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("QuarterlyAssessment")
@Api(value = "季度评价", tags = "季度评价控制层代码")
public class QuarterlyAssessmentController extends BladeController {

	@Resource
	private IQuarterlyAssessmentService iQuarterlyAssessmentService;
	private final IFollowInformationService followInformationService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private ISysClient sysClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 公共方法--文件下载（传入文件名，例如：党风廉政季度评价.xls）
	 */
	@GetMapping("downFiles")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "公共方法--文件下载", notes = "公共方法--文件下载")
	public void downRegion(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String fileName = request.getParameter("fileName").toString();
		//文件父路径 /khpjExcel/annualAPPrise
		String filePath = request.getParameter("filePath").toString();
		if (fileName.length() == 0) {
			System.out.println("文件名称不能为空！！");
		}

		String parentPathName = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
		System.out.println("-------=========-------========"+parentPathName);
		try {
			FileUploadUtils.downPrintFile(parentPathName + filePath,fileName,request,response);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}


	/**
	 * 导入-季度评价数据
	 */
	@PostMapping("import-QuarterlyAssessment")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "导入季度评价", notes = "传入excel")
	public R importRegion(MultipartFile file) throws IOException {
		String type = "";
		String name = "";
		Long businessId=0L;
		String businessTable="";
		if (Objects.requireNonNull(file.getOriginalFilename()).contains("党建工作")) {
			type = "1";
			name = "党建工作";
		} else if (file.getOriginalFilename().contains("工作实绩")) {
			type = "2";
			name = "工作实绩";
		} else if (file.getOriginalFilename().contains("党风廉政")) {
			type = "3";
			name = "党风廉政";
		}
		try {
			QuarterlyAssessmentImporter regionImporter = new QuarterlyAssessmentImporter(iQuarterlyAssessmentService,type,name);
			ExcelUtil.save(file, regionImporter, QuarterlyAssessmentExcel.class);
		} catch (Exception ex) {
			throw ex;
		}

		return R.success("操作成功！");
	}



	/**
     * 导出
	 * @param entity
     * @param response
	 *
	 */
	@GetMapping("export-quarterlyAssessment")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "季度评价-导出", notes = "传入quarterlyAssessment")
	public void exportUser(@ApiIgnore @RequestParam Map<String, Object> entity, HttpServletResponse response) {
		Object quarterlyType = entity.get("quarterlyType");
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		//sql查询条件
		Object jdzbType = entity.get("jdzbType");//季度指标类型
		Object checkClassify = entity.get("checkClassify");//考核分类
		Object checkObjectId = entity.get("checkObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份

		LambdaQueryWrapper<QuarterlyAssessment> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper
			.eq(!StringUtil.isEmpty(checkClassify),QuarterlyAssessment::getCheckClassify,checkClassify)
			.eq(!StringUtil.isEmpty(checkObjectId),QuarterlyAssessment::getCheckObjectId,checkObjectId)
			.eq(!StringUtil.isEmpty(appraiseDeptid),QuarterlyAssessment::getAppraiseDeptid,appraiseDeptid)
			.like(!StringUtil.isEmpty(quarterlyYear.toString()),QuarterlyAssessment::getCreateTime,quarterlyYear.toString());
		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = currentUser.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		//如果用户不是四大班子领导，那就查看自己部门相关的信息
		if (isok) {
			queryWrapper.and(Wrappers -> Wrappers
				.eq(StringUtils.isNotNull(currentUser.getId()),QuarterlyAssessment::getCreateUser,currentUser.getId().toString())
				.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCreateDept,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCheckObjectId,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getAppraiseDeptid,currentUser.getDeptId())
			);
		}
		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (quarterlyType!=null && quarterlyType.equals("1")) {//如果是领导关注

			if (currentUser!=null) {// && !"1123598817738675202".equals(currentUser.getPostId())
				if (StringUtil.isBlank(currentUser.getDeptId())) {
					throw new RuntimeException("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
				List<String> leaderIds1 = new ArrayList<>();
				for (User leader : leaders) {
					leaderIds1.add(leader.getId().toString());
				}
				String[] leaderIds = leaderIds1.toString()
					.replace("[","")
					.replace("]","")
					.replace(" ","")
					.split(",");
				followInformationQueryWrapper.in("follow_user_id", leaderIds);

			}
		}else if(quarterlyType!=null && quarterlyType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","4");

		String [] tabId = null;
		if (quarterlyType!=null) {
			if ("1".equals(quarterlyType) || "2".equals(quarterlyType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() > 0) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in(QuarterlyAssessment::getId, tabId);
				} else {
					queryWrapper.in(QuarterlyAssessment::getId, "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) checkObjectId)){
			queryWrapper.apply("locate('"+checkObjectId+",',concat(check_object_id,',')) >0");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("locate('"+appraiseDeptid+",',concat(appraise_deptid,',')) >0");
		}

		queryWrapper.eq(!StringUtil.isBlank((CharSequence) jdzbType),QuarterlyAssessment::getJdzbType,jdzbType);
		queryWrapper.orderByDesc(QuarterlyAssessment::getCreateTime);
		List<QuarterlyAssessment> quarterlyAssessmentList = iQuarterlyAssessmentService.list(queryWrapper);

		if ("1".equals(jdzbType)) {
			List<QuarterlyAssessmentExcel1> list = new ArrayList<>();
			if (quarterlyAssessmentList.size() > 0) {
				for (int i = 0; i < quarterlyAssessmentList.size(); i++) {
					QuarterlyAssessmentExcel1 qqq = new QuarterlyAssessmentExcel1();
					qqq.setCheckClassifyName(quarterlyAssessmentList.get(i).getCheckClassifyName());
					qqq.setToQuarter(quarterlyAssessmentList.get(i).getToQuarter());
					qqq.setMajorTarget(quarterlyAssessmentList.get(i).getMajorTarget());
					qqq.setCheckObject(quarterlyAssessmentList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyAssessmentList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyAssessmentList.get(i).getWeight());

					list.add(qqq);
				}
			}

			ExcelUtil.export(response, "党建工作-" + DateUtil.time(), "党建工作", list, QuarterlyAssessmentExcel1.class);
		}else if ("2".equals(jdzbType)) {
			List<QuarterlyAssessmentExcel2> list = new ArrayList<>();
			if (quarterlyAssessmentList.size() > 0) {
				for (int i = 0; i < quarterlyAssessmentList.size(); i++) {
					QuarterlyAssessmentExcel2 qqq = new QuarterlyAssessmentExcel2();
					qqq.setToQuarter(quarterlyAssessmentList.get(i).getToQuarter());
					qqq.setCheckClassifyName(quarterlyAssessmentList.get(i).getCheckClassifyName());
					qqq.setTarget(quarterlyAssessmentList.get(i).getTarget());
					qqq.setCheckObject(quarterlyAssessmentList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyAssessmentList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyAssessmentList.get(i).getWeight());

					list.add(qqq);
				}
			}
			ExcelUtil.export(response, "工作实绩-" + DateUtil.time(), "工作实绩", list, QuarterlyAssessmentExcel2.class);
		}else if ("3".equals(jdzbType)) {
			List<QuarterlyAssessmentExcel3> list = new ArrayList<>();

			if (quarterlyAssessmentList.size() > 0) {
				for (int i = 0; i < quarterlyAssessmentList.size(); i++) {
					QuarterlyAssessmentExcel3 qqq = new QuarterlyAssessmentExcel3();
					qqq.setToQuarter(quarterlyAssessmentList.get(i).getToQuarter());
					qqq.setCheckClassifyName(quarterlyAssessmentList.get(i).getCheckClassifyName());
					qqq.setMajorTarget(quarterlyAssessmentList.get(i).getMajorTarget());
					qqq.setScoringRubric(quarterlyAssessmentList.get(i).getScoringRubric());
					qqq.setCheckObject(quarterlyAssessmentList.get(i).getCheckObject());
					qqq.setAppraiseDeptname(quarterlyAssessmentList.get(i).getAppraiseDeptname());
					qqq.setWeight(quarterlyAssessmentList.get(i).getWeight());


					list.add(qqq);
				}
			}
			ExcelUtil.export(response, "党建工作-" + DateUtil.time(), "党建工作", list, QuarterlyAssessmentExcel3.class);
		}





	}

	/**
	 * 季度评价新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@RequestBody QuarterlyAssessment qe) throws ParseException {
	    boolean isok =	iQuarterlyAssessmentService.saveAssessment(qe);

		String title1 = "新增季度评价数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyAssessment";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 季度评价修改接口
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody QuarterlyAssessment qe) throws ParseException {
		boolean isok =iQuarterlyAssessmentService.uptAssessment(qe);
		String title1 = "修改季度评价数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyAssessment";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 删除
	 * @param ids
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam("ids") String ids) {
		boolean isok =iQuarterlyAssessmentService.removeByIds(Func.toLongList(ids));
		String title1 = "逻辑删除季度评价数据";
		String businessId = String.valueOf(ids);
		String businessTable = "QuarterlyAssessment";
		int businessType = BusinessType.DELETE.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessId, businessTable, businessType);
		}


		return R.status(isok);
	}

	/**
	 * 分页查询
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核列表查询", notes = "")
	public R<IPage<QuarterlyAssessment>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Object quarterlyType = entity.get("quarterlyType");
		Object checkObjectId = entity.get("checkObjectId");//评价对象id
		Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

			//sql查询条件
		Object jdzbType = entity.get("jdzbType");//季度指标类型
		Object checkClassify = entity.get("checkClassify");//考核分类
		Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份
		Object quarter = entity.get("quarter");//季度 1，2，3，4
		if ("1".equals(quarter)) {//第一季度
			quarter = "第一季度";
		}else if ("2".equals(quarter)) {//第二季度
			quarter = "第二季度";
		}else if ("3".equals(quarter)) {//第三季度
			quarter = "第三季度";
		}else if ("4".equals(quarter)) {//第四季度
			quarter = "第四季度";
		}else if ("5".equals(quarter)) {//上半年
			quarter = "上半年";
		}else if ("6".equals(quarter)) {//下半年
			quarter = "下半年";
		}
		LambdaQueryWrapper<QuarterlyAssessment> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper
			.eq(StringUtils.isNotBlank((CharSequence) checkClassify),QuarterlyAssessment::getCheckClassify,checkClassify)
			.like(StringUtils.isNotBlank((CharSequence) checkObjectId),QuarterlyAssessment::getCheckObjectId,checkObjectId)
			.like(StringUtils.isNotBlank((CharSequence) appraiseDeptid),QuarterlyAssessment::getAppraiseDeptid,appraiseDeptid)
			.eq(StringUtils.isNotBlank((CharSequence) quarter),QuarterlyAssessment::getToQuarter,quarter)
			.like(!StringUtil.isBlank(quarterlyYear.toString()),QuarterlyAssessment::getCreateTime,quarterlyYear.toString());

		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = currentUser.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		//如果用户不是四大班子领导，那就查看自己部门相关的信息
		if (isok) {
			queryWrapper.and(Wrappers -> Wrappers
				.eq(StringUtils.isNotNull(currentUser.getId()),QuarterlyAssessment::getCreateUser,currentUser.getId().toString())
				.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCreateDept,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCheckObjectId,currentUser.getDeptId())
				.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getAppraiseDeptid,currentUser.getDeptId())
			);
		}


		//领导关注
		List<String> deptIds1 = new ArrayList<>();
		deptIds1.add(PropConstant.getSwldDeptId());//市委领导
		deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
		String deptIds = deptIds1.toString().replace("[","").replace("]","");
		QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

		if (quarterlyType!=null && quarterlyType.equals("1")) {//如果是领导关注

			if (currentUser!=null) {// && !"1123598817738675202".equals(currentUser.getPostId())
				if (StringUtil.isBlank(currentUser.getDeptId())) {
					return R.fail("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
				List<String> leaderIds1 = new ArrayList<>();
				for (User leader : leaders) {
					leaderIds1.add(leader.getId().toString());
				}
				String[] leaderIds = leaderIds1.toString()
					.replace("[","")
					.replace("]","")
					.replace(" ","")
					.split(",");
				followInformationQueryWrapper.in("follow_user_id", leaderIds);

			}
		}
		else if(quarterlyType!=null && quarterlyType.equals("2")){//如果是我的关注
			followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
		}
		followInformationQueryWrapper.eq("business_type","4");//2是年度指标，4是季度指标

		String [] tabId = null;
		if (quarterlyType!=null) {
			if ("1".equals(quarterlyType) || "2".equals(quarterlyType)) {
				List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
				tabId = new String[followInformations.size()];
				if (followInformations.size() > 0) {
					for (int i = 0; i < followInformations.size(); i++) {
						String id = followInformations.get(i).getBusinessId().toString();
						tabId[i] = id;
					}
					queryWrapper.in(QuarterlyAssessment::getId, tabId);
				} else {
					queryWrapper.in(QuarterlyAssessment::getId, "XXXXXXXX");
				}
			}
		}

		if(StringUtils.isNotBlank((CharSequence) checkObjectId)){
			queryWrapper.apply("locate('"+checkObjectId+",',concat(check_object_id,',')) >0");
		}
		if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
			queryWrapper.apply("locate('"+appraiseDeptid+",',concat(appraise_deptid,',')) >0");
		}
		queryWrapper.eq(!StringUtil.isBlank((CharSequence) jdzbType),QuarterlyAssessment::getJdzbType,jdzbType);
		queryWrapper.orderByDesc(QuarterlyAssessment::getCreateTime);
		IPage<QuarterlyAssessment> pages = iQuarterlyAssessmentService.page(Condition.getPage(query), queryWrapper);


		return R.data(pages);
	}

	/**
	 * 分页查询
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核列表查询", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("考核列表查询-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			Map<String, Object> entity = new HashMap<>(jsonParams);
			Object quarterlyType = entity.get("quarterlyType");
			Object checkObjectId = entity.get("checkObjectId");//评价对象id
			Object appraiseDeptid = entity.get("appraiseDeptid");//评价单位id
			User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

			//sql查询条件
			Object jdzbType = entity.get("jdzbType");//季度指标类型
			Object checkClassify = entity.get("checkClassify");//考核分类
			Object quarterlyYear = entity.get("createTime") == null ? DateTime.now().year() : entity.get("createTime");//年份
			Object quarter = entity.get("quarter");//季度 1，2，3，4
			if ("1".equals(quarter)) {//第一季度
				quarter = "第一季度";
			}else if ("2".equals(quarter)) {//第二季度
				quarter = "第二季度";
			}else if ("3".equals(quarter)) {//第三季度
				quarter = "第三季度";
			}else if ("4".equals(quarter)) {//第四季度
				quarter = "第四季度";
			}else if ("5".equals(quarter)) {//上半年
				quarter = "上半年";
			}else if ("6".equals(quarter)) {//下半年
				quarter = "下半年";
			}
			LambdaQueryWrapper<QuarterlyAssessment> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper
				.eq(ObjectUtils.isNotEmpty(checkClassify),QuarterlyAssessment::getCheckClassify,checkClassify)
				.like(ObjectUtils.isNotEmpty(checkObjectId),QuarterlyAssessment::getCheckObjectId,checkObjectId)
				.like(ObjectUtils.isNotEmpty(appraiseDeptid),QuarterlyAssessment::getAppraiseDeptid,appraiseDeptid)
				.eq(ObjectUtils.isNotEmpty(quarter),QuarterlyAssessment::getToQuarter,quarter)
				.like(ObjectUtils.isNotEmpty(quarterlyYear),QuarterlyAssessment::getCreateTime,quarterlyYear.toString());

			//appriseLeader
			boolean isok = true;
			String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
			String[] roleIds = currentUser.getRoleId().split(",");//判断该用户是不是市级四大班子领导
			for (String id : roleIds) {
				if (id.equals(roleId)) {
					isok = false;
					break;
				}
			}
			//如果用户不是四大班子领导，那就查看自己部门相关的信息
			if (isok) {
				queryWrapper.and(Wrappers -> Wrappers
					.eq(StringUtils.isNotNull(currentUser.getId()),QuarterlyAssessment::getCreateUser,currentUser.getId().toString())
					.or().eq(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCreateDept,currentUser.getDeptId())
					.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getCheckObjectId,currentUser.getDeptId())
					.or().like(StringUtils.isNotNull(currentUser.getDeptId()),QuarterlyAssessment::getAppraiseDeptid,currentUser.getDeptId())
				);
			}


			//领导关注
			List<String> deptIds1 = new ArrayList<>();
			deptIds1.add(PropConstant.getSwldDeptId());//市委领导
			deptIds1.add(PropConstant.getSzfldDeptId());//市政务领导
			String deptIds = deptIds1.toString().replace("[","").replace("]","");
			QueryWrapper<FollowInformation>  followInformationQueryWrapper = new QueryWrapper<>();

			if (quarterlyType!=null && quarterlyType.equals("1")) {//如果是领导关注

				if (currentUser!=null) {// && !"1123598817738675202".equals(currentUser.getPostId())
					if (StringUtil.isBlank(currentUser.getDeptId())) {
						return R.fail("用户找不到部门");
					}
					List<User> leaders = userClient.getUserLeader(currentUser.getDeptId(), currentUser.getPostId()).getData();
					List<String> leaderIds1 = new ArrayList<>();
					for (User leader : leaders) {
						leaderIds1.add(leader.getId().toString());
					}
					String[] leaderIds = leaderIds1.toString()
						.replace("[","")
						.replace("]","")
						.replace(" ","")
						.split(",");
					followInformationQueryWrapper.in("follow_user_id", leaderIds);

				}
			}
			else if(quarterlyType!=null && quarterlyType.equals("2")){//如果是我的关注
				followInformationQueryWrapper.eq("follow_user_id",currentUser.getId());
			}
			followInformationQueryWrapper.eq("business_type","4");//2是年度指标，4是季度指标

			String [] tabId = null;
			if (quarterlyType!=null) {
				if ("1".equals(quarterlyType) || "2".equals(quarterlyType)) {
					List<FollowInformation> followInformations = followInformationService.list(followInformationQueryWrapper);
					tabId = new String[followInformations.size()];
					if (followInformations.size() > 0) {
						for (int i = 0; i < followInformations.size(); i++) {
							String id = followInformations.get(i).getBusinessId().toString();
							tabId[i] = id;
						}
						queryWrapper.in(QuarterlyAssessment::getId, tabId);
					} else {
						queryWrapper.in(QuarterlyAssessment::getId, "XXXXXXXX");
					}
				}
			}

			if(StringUtils.isNotBlank((CharSequence) checkObjectId)){
				queryWrapper.apply("locate('"+checkObjectId+",',concat(check_object_id,',')) >0");
			}
			if(StringUtils.isNotBlank((CharSequence) appraiseDeptid)){
				queryWrapper.apply("locate('"+appraiseDeptid+",',concat(appraise_deptid,',')) >0");
			}
			queryWrapper.eq(!StringUtil.isBlank((CharSequence) jdzbType),QuarterlyAssessment::getJdzbType,jdzbType);
			queryWrapper.orderByDesc(QuarterlyAssessment::getCreateTime);
			IPage<QuarterlyAssessment> pages = iQuarterlyAssessmentService.page(Condition.getPage(query), queryWrapper);

			JSONObject pageJson = objectMapper.convertValue(pages, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}



	/**
	 * 详情
	 * @param id
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<QuarterlyAssessment> details(@Valid @RequestParam Long id) {

		String title1 = "查看了考核评价-季度指标详情";
		String businessId = String.valueOf(id);
		String businessTable = "QuarterlyAssessment";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.data(iQuarterlyAssessmentService.details(id));
	}

	/**
	 * 分页查询
	 */
	@GetMapping("/listForApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "APP年度评价列表查询", notes = "")
	public R<IPage<QuarterlyAssessment>> listForApp(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String checkObjectId = (String) entity.get("checkObjectId");
		String targetStatus= (String) entity.get("targetStatus");
		String createTime =(String) entity.get("createTime");
		String majorTarget =(String) entity.get("majorTarget");
		//sql查询条件
		QueryWrapper<QuarterlyAssessment> queryWrapper = new QueryWrapper<QuarterlyAssessment>();
		queryWrapper.apply("FIND_IN_SET('"+checkObjectId+"',check_object_id)"  );
		if(targetStatus.equals("1")){
			queryWrapper.apply("target_Status in ('1','3','4')");
		}else if(targetStatus.equals("2")){
			queryWrapper.apply("target_Status in ('2','5')");
		}else if(targetStatus.equals("0")){
			queryWrapper.eq("target_Status",targetStatus);
		}
		queryWrapper.eq("jdzb_type",entity.get("jdzbType"));
		if(StringUtils.isNotBlank(createTime)){
			queryWrapper.apply("date_format(create_time,'%Y')='"+createTime+"'");
		}
		if(StringUtils.isNotBlank(majorTarget)){
			queryWrapper.like("major_target",majorTarget);
		}
		String quarter = "第一季度";
		int ii = DateTime.now().month();
		int year = DateTime.now().year();
		/*if (ii==1 || ii==2 || ii==3) {//第一季度
			quarter = "第一季度";
		}else if (ii==4 || ii==5 || ii==6) {//第二季度
			quarter = "第二季度";
		}else if (ii==7 || ii==8 || ii==9) {//第三季度
			quarter = "第三季度";
		}else if (ii==10 || ii==11 || ii==12) {//第四季度
			quarter = "第四季度";
		}*/
		if (year < 2024) {
			if (ii==1 || ii==2 || ii==3) {//第一季度
				quarter = "第一季度";
			}else if (ii==4 || ii==5 || ii==6) {//第二季度
				quarter = "第二季度";
			}else if (ii==7 || ii==8 || ii==9) {//第三季度
				quarter = "第三季度";
			}else if (ii==10 || ii==11 || ii==12) {//第四季度
				quarter = "第四季度";
			}
		} else if (year == 2024) {
			if (ii==1 || ii==2 || ii==3) {//第一季度
				quarter = "第一季度";
			}else if (ii==4 || ii==5 || ii==6) {//第二季度
				quarter = "第二季度";
			}else {//下半年
				quarter = "下半年";
			}
		} else {
			if (ii <= 6) {//上半年
				quarter = "上半年";
			} else {//下半年
				quarter = "下半年";
			}
		}
		queryWrapper.eq("to_quarter",quarter);
		IPage<QuarterlyAssessment> pages = iQuarterlyAssessmentService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}


	/**
	 * 修改阶段
	 */
	@PostMapping("/updateStage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateStage(@Valid @RequestBody QuarterlyAssessment qe) throws ParseException {
		boolean isok = iQuarterlyAssessmentService.uptStage(qe);
		String title1 = "修改季度评价阶段数据";
		String businessId = String.valueOf(qe.getId());
		String businessTable = "QuarterlyAssessment";
		int businessType = BusinessType.OTHER.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}








}
