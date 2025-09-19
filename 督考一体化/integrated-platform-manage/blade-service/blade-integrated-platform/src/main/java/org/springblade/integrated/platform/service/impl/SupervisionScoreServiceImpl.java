package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionDeptScoretVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.mapper.SupervisionScoreMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionScoreServiceImpl extends BaseServiceImpl<SupervisionScoreMapper, SupervisionScore> implements ISupervisionScoreService {
	@Autowired
	private ISupervisionSignService supervisionSignService;

	@Autowired
	private ISupervisionInfoService supervisionInfoService;

	@Autowired
	private ISupervisionEvaluateService supervisionEvaluateService;

	@Resource
	private ISysClient sysClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean calculationScore(SupervisionEvaluate supervisionEvaluate) {
		String evaluateType = supervisionEvaluate.getEvaluateType();
		SupervisionInfo info = supervisionInfoService.getOne(new QueryWrapper<SupervisionInfo>().eq("serv_code", supervisionEvaluate.getServCode()));
		Long servId=Long.valueOf(0);
		if(ObjectUtil.isNotEmpty(info)){
			servId=info.getId();
		}
		Long leaderScore=null;
		Long dcdbScore=null;
		Long leadUnitScore=null;
		//需更新的得分情况

		List<SupervisionScore> listScore=new ArrayList<>();
		if(evaluateType.equals(Constants.DCDB_EVALUATE_TYPE_LEADER)){
			//获取需要更新得分单位
			QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
			wrapper.eq("serv_id",servId);
			List<SupervisionSign> list = supervisionSignService.list(wrapper);

			//当前评价为责任领导评价
			//更新所有部门得分
			if(ObjectUtil.isEmpty(supervisionEvaluate.getId())){
				leaderScore=Long.valueOf(supervisionEvaluate.getResult());
			}
			for(SupervisionSign sign:list){
				//获取该事项该部门督查督办评价得分
				QueryWrapper<SupervisionEvaluate> wrapperE=new QueryWrapper<>();
				wrapperE.eq(" cast(evaluated_Dept as signed )",sign.getSignDept());
				wrapperE.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_DCDB);
				wrapperE.eq("serv_code",supervisionEvaluate.getServCode());
				wrapperE.eq("is_deleted",0);
				SupervisionEvaluate supervisionEvaluateD = supervisionEvaluateService.getOne(wrapperE);
				if(ObjectUtil.isNotEmpty(supervisionEvaluateD)){
					dcdbScore=Long.valueOf(supervisionEvaluateD.getResult());
				}
				//获取该事项该部门牵头单位评价得分
				QueryWrapper<SupervisionEvaluate> wrapperU=new QueryWrapper<>();
				wrapperU.eq(" cast(evaluated_Dept as signed )",sign.getSignDept());
				wrapperU.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_LEADUNIT);
				wrapperU.eq("serv_code",supervisionEvaluate.getServCode());
				wrapperU.eq("is_deleted",0);
				SupervisionEvaluate supervisionEvaluateU = supervisionEvaluateService.getOne(wrapperU);
				if(ObjectUtil.isNotEmpty(supervisionEvaluateU)){
					leadUnitScore=Long.valueOf(supervisionEvaluateU.getResult());
				}
				Map<String,Object> map  = this.score(leaderScore, dcdbScore, leadUnitScore,sign.getDeptType());
				//获取部门是否存在事项得分数据
				listScore.add(this.getSupervisionScore(supervisionEvaluate,map,sign.getSignDept()));
			}

		}else if(evaluateType.equals(Constants.DCDB_EVALUATE_TYPE_DCDB)){
			//只更新评价部门得分
			if(ObjectUtil.isEmpty(supervisionEvaluate.getId())){
				dcdbScore=Long.valueOf(supervisionEvaluate.getResult());
			}
			//获取该事项该部门责任领导评价得分
			QueryWrapper<SupervisionEvaluate> wrapperZ=new QueryWrapper<>();
			wrapperZ.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_LEADER);
			wrapperZ.eq("serv_code",supervisionEvaluate.getServCode());
			wrapperZ.eq("is_deleted",0);
			SupervisionEvaluate supervisionEvaluateZ = supervisionEvaluateService.getOne(wrapperZ);
			if(ObjectUtil.isNotEmpty(supervisionEvaluateZ)){
				leaderScore=Long.valueOf(supervisionEvaluateZ.getResult());
			}
			//获取该事项该部门牵头单位评价得分
			QueryWrapper<SupervisionEvaluate> wrapperU=new QueryWrapper<>();
			wrapperU.eq(" cast(evaluated_Dept as signed )",Long.valueOf(supervisionEvaluate.getEvaluatedDept()));
			wrapperU.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_LEADUNIT);
			wrapperU.eq("serv_code",supervisionEvaluate.getServCode());
			wrapperU.eq("is_deleted",0);
			SupervisionEvaluate supervisionEvaluateU = supervisionEvaluateService.getOne(wrapperU);
			if(ObjectUtil.isNotEmpty(supervisionEvaluateU)){
				leadUnitScore=Long.valueOf(supervisionEvaluateU.getResult());
			}
			//获取需要更新得分单位
			QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
			wrapper.eq("serv_id",servId);
			wrapper.eq("sign_Dept",supervisionEvaluate.getEvaluatedDept());
			SupervisionSign supervisionSign = supervisionSignService.getOne(wrapper);
			//计算得分
			Map<String,Object> map = this.score(leaderScore, dcdbScore, leadUnitScore,supervisionSign.getDeptType());
			//获取部门是否存在事项得分数据
			listScore.add(this.getSupervisionScore(supervisionEvaluate,map,supervisionSign.getSignDept()));

		}else  if(evaluateType.equals(Constants.DCDB_EVALUATE_TYPE_LEADUNIT)){
			//只更新评价部门得分
			if(ObjectUtil.isEmpty(supervisionEvaluate.getId())) {
				leadUnitScore = Long.valueOf(supervisionEvaluate.getResult());
			}
			//获取该事项该部门责任领导评价得分
			QueryWrapper<SupervisionEvaluate> wrapperZ=new QueryWrapper<>();
			wrapperZ.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_LEADER);
			wrapperZ.eq("serv_code",supervisionEvaluate.getServCode());
			wrapperZ.eq("is_deleted",0);
			SupervisionEvaluate supervisionEvaluateZ = supervisionEvaluateService.getOne(wrapperZ);
			if(ObjectUtil.isNotEmpty(supervisionEvaluateZ)){
				leaderScore=Long.valueOf(supervisionEvaluateZ.getResult());
			}
			//获取该事项该部门督查督办评价得分
			QueryWrapper<SupervisionEvaluate> wrapperE=new QueryWrapper<>();
			wrapperE.eq(" cast(evaluated_Dept as signed )",Long.valueOf(supervisionEvaluate.getEvaluatedDept()));
			wrapperE.eq("evaluate_Type",Constants.DCDB_EVALUATE_TYPE_DCDB);
			wrapperE.eq("serv_code",supervisionEvaluate.getServCode());
			wrapperE.eq("is_deleted",0);
			SupervisionEvaluate supervisionEvaluateD = supervisionEvaluateService.getOne(wrapperE);
			if(ObjectUtil.isNotEmpty(supervisionEvaluateD)){
				dcdbScore=Long.valueOf(supervisionEvaluateD.getResult());
			}
			//获取需要更新得分单位
			QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
			wrapper.eq("serv_id",servId);
			wrapper.eq("sign_Dept",supervisionEvaluate.getEvaluatedDept());
			SupervisionSign supervisionSign = supervisionSignService.getOne(wrapper);
			//计算得分
			Map<String,Object> map = this.score(leaderScore, dcdbScore, leadUnitScore,supervisionSign.getDeptType());
			//获取保存部门得分List
			listScore.add(this.getSupervisionScore(supervisionEvaluate,map,supervisionSign.getSignDept()));
		}
		boolean flag = this.saveOrUpdateBatch(listScore);
		return flag;
	}

	@Override
	public List<SupervisionDeptScoretVO> deptScoreStatistics(String deptGroup,String supYear, String startTime,String endTime) {
		//根据部门分组获取部门
		R<List<Dept>> deptR = sysClient.getDeptByGroup("000000",deptGroup, supYear);
		List<Dept> deptList=deptR.getData();
		List<SupervisionDeptScoretVO> deptscoreList= new ArrayList<>();
		if(deptGroup.equals("1")){
			for(Dept dept:deptList){
				SupervisionDeptScoretVO  deptscore=new SupervisionDeptScoretVO();
				R<List<Dept>> deptChildR = sysClient.getDeptChild(dept.getId());
				List<Dept> deptChildRData = deptChildR.getData();
				List<Long> deptIds = deptChildRData.stream().map(Dept::getId).collect(toList());
				deptIds.add(dept.getId());
				deptscore.setDeptId(dept.getId());
				deptscore.setDeptName(dept.getDeptName());
				QueryWrapper<SupervisionScore>  wrapper=new QueryWrapper<>();
				wrapper.in("dept_Id",deptIds);
//				wrapper.between("create_time",startTime,endTime);
				BigDecimal score = this.baseMapper.deptScoreStatistics(wrapper,startTime,endTime);
				if(score.compareTo(new BigDecimal(100))==1){
					deptscore.setScore(String.valueOf(new BigDecimal(100.00).setScale(2,BigDecimal.ROUND_HALF_UP)));
					deptscore.setSort(score);
				}else if(score.compareTo(new BigDecimal(0))<0){
					deptscore.setScore(String.valueOf(new BigDecimal(0)));
					deptscore.setSort(new BigDecimal(0));
				}else {
					deptscore.setScore(String.valueOf(score));
					deptscore.setSort(score);
				}
				deptscore.setDeptId(dept.getId());
				deptscore.setDeptName(dept.getDeptName());
				deptscoreList.add(deptscore);
			}
		}else {
			List<Long> deptIds = deptList.stream().map(Dept::getId).collect(toList());
			QueryWrapper<SupervisionScore>  wrapper=new QueryWrapper<>();
			wrapper.in("dept_Id",deptIds);
//			wrapper.between("create_time",startTime,endTime);
			List<SupervisionDeptScoretVO> deptscores = this.baseMapper.deptScoreStatisticsGroupby(wrapper,startTime,endTime);
			for(Dept dept:deptList){
				SupervisionDeptScoretVO  deptscore=new SupervisionDeptScoretVO();
				deptscore.setDeptId(dept.getId());
				deptscore.setDeptName(dept.getDeptName());
				boolean flag=true;
				for(SupervisionDeptScoretVO score:deptscores){
					if(score.getDeptId().longValue()==dept.getId().longValue()){
						if(score.getScore().compareTo("100")==0){
							deptscore.setScore(String.valueOf(new BigDecimal(100.00).setScale(2,BigDecimal.ROUND_HALF_UP)));
							deptscore.setSort(new BigDecimal(deptscore.getScore()));
						}else if(score.getScore().compareTo("0")<0){
							deptscore.setScore("0");
							deptscore.setSort(new BigDecimal(0));
						}else{
							deptscore.setScore(String.valueOf(new BigDecimal(score.getScore()).setScale(2,BigDecimal.ROUND_HALF_UP)));
							deptscore.setSort(new BigDecimal(deptscore.getScore()));
						}
						flag=false;
						break;
					}else{
						flag=true;
					}
				}
				if(flag){
					deptscore.setSort(new BigDecimal(-1));
					deptscore.setScore("/");
				}
				deptscoreList.add(deptscore);
			}
			for(SupervisionDeptScoretVO score:deptscoreList){
				score.setDeptName(sysClient.getDeptName(score.getDeptId()).getData());
			}

		}

		return deptscoreList.stream().sorted(Comparator.comparing(SupervisionDeptScoretVO::getSort)
			.reversed())
			.collect(Collectors.toList());
	}


	@Override
	public List<SupervisionDeptScoretVO> deptScoreDetails(String deptGroup,Long deptId,String startTime,String endTime) {
		List<SupervisionDeptScoretVO> list=new ArrayList<>();
		QueryWrapper<SupervisionScore> wrapper=new QueryWrapper<>();
		List<Long> deptIds=new ArrayList<>();
		if(deptGroup.equals("1")){
			R<List<Dept>> deptChildR = sysClient.getDeptChild(deptId);
			List<Dept> deptChildRData = deptChildR.getData();
			deptIds = deptChildRData.stream().map(Dept::getId).collect(toList());
			deptIds.add(deptId);
		}else{
			wrapper.eq("s.dept_Id",deptId);
		}
		if(ObjectUtil.isNotEmpty(deptIds)){
			wrapper.in("s.dept_Id",deptIds);
		}
		wrapper.between("s.create_time",startTime,endTime);
		List<SupervisionDeptScoretVO> voList = this.baseMapper.deptScoreServ(wrapper);

		for(SupervisionDeptScoretVO vo:voList){
			Long id = vo.getDeptId();
			R<String> deptNameR = sysClient.getDeptName(id);
			String deptName = deptNameR.getData();
			vo.setDeptName(deptName);
			String servCode = vo.getServCode();
			QueryWrapper<SupervisionInfo> wrapperInfo=new QueryWrapper<>();
			wrapperInfo.eq("serv_code",servCode);
			SupervisionInfo supervisionInfo = supervisionInfoService.getOne(wrapperInfo);
			if(ObjectUtil.isNotEmpty(supervisionInfo)){
				vo.setServName(supervisionInfo.getServName());
			}
		}
		return voList;
	}


	/**
	 *
	 * @param leaderScore 领导评价结果
	 * @param dcdbScore   督查督办评价结果
	 * @param leadUnitScore 牵头单位评价结果
	 * @param deptType   需计算分值的事项部门类型:牵头单位或责任单位
	 * @return
	 */
	private Map<String,Object> score(Long leaderScore, Long dcdbScore, Long leadUnitScore, String deptType){
		BigDecimal leaderScoreB = new BigDecimal(0);
		BigDecimal dcdbScoreB = new BigDecimal(0);
		BigDecimal leadUnitScoreB = new BigDecimal(0);
		if (ObjectUtil.isNotEmpty(dcdbScore)) {
			 dcdbScoreB = new BigDecimal(dcdbScore);
		}
		if (ObjectUtil.isNotEmpty(leadUnitScore)) {
			leadUnitScoreB = new BigDecimal(leadUnitScore);
		}
		BigDecimal score=new BigDecimal(0);
		String scoreDetails="";
		if(ObjectUtil.isNotEmpty(leaderScore)){
			leaderScoreB=new BigDecimal(leaderScore);
			//当责任领导评价了事项
			if(deptType.equals(Constants.DCDB_SERV_DEPT_TYPE_LEAD)){
				//牵头单位得分计算
				//领导评价得分
				BigDecimal scoreLD= leaderScoreB.multiply(Constants.LEADER_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				//督察督办得分
				BigDecimal scoreLDCDB= dcdbScoreB.multiply(Constants.DCDB_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				score=scoreLD.add(scoreLDCDB);
				scoreDetails="牵头单位得分=领导评价得分("+leaderScoreB+")*"+Constants.LEADER_SCORE_COEFFICIENT+"+督办单位评价得分("+dcdbScoreB+")*"+Constants.DCDB_SCORE_COEFFICIENT;
			}else if(deptType.equals(Constants.DCDB_SERV_DEPT_TYPE_DUTY)){
				//责任单位得分计算
				//领导评价得分
				BigDecimal scoreLD= leaderScoreB.multiply(Constants.LEADUNIT_LEADER_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				//督察督办得分
				BigDecimal scoreLDCDB= dcdbScoreB.multiply(Constants.LEADUNIT_DCDB_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				//牵头单位得分
				BigDecimal scoreLEADUNIT= leadUnitScoreB.multiply(Constants.LEADUNIT_LEAD_UNIT_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				score=scoreLD.add(scoreLDCDB).add(scoreLEADUNIT);
				scoreDetails="责任单位得分=领导评价得分("+leaderScoreB+")*"+Constants.LEADUNIT_LEADER_SCORE_COEFFICIENT+"+督办单位评价得分("+dcdbScoreB+")" +
					"*"+Constants.LEADUNIT_DCDB_SCORE_COEFFICIENT+"+牵头单位评价得分("+leadUnitScoreB+")*"+Constants.LEADUNIT_LEAD_UNIT_SCORE_COEFFICIENT+"";
			}
		}else {
			if(deptType.equals(Constants.DCDB_SERV_DEPT_TYPE_LEAD)) {
				//牵头单位得分计算
				//督察督办得分
				score= dcdbScoreB.multiply(Constants.NOLEADER_LEAD_UNIT_DCDB_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				scoreDetails="牵头单位得分=督办单位评价得分("+dcdbScoreB+")*"+Constants.NOLEADER_LEAD_UNIT_DCDB_SCORE_COEFFICIENT;
			}else if(deptType.equals(Constants.DCDB_SERV_DEPT_TYPE_DUTY)) {
				//责任单位得分计算
				//督察督办得分
				BigDecimal scoreLDCDB= dcdbScoreB.multiply(Constants.NOLEADER_DUTY_DCDB_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				//牵头单位得分
				BigDecimal scoreLEADUNIT= leadUnitScoreB.multiply(Constants.NOLEADUNIT_DUTY_LEAD_UNIT_SCORE_COEFFICIENT).setScale(2,BigDecimal.ROUND_HALF_UP);
				score=scoreLDCDB.add(scoreLEADUNIT);
				scoreDetails="责任单位得分=督办单位评价得分("+dcdbScoreB+")*"+Constants.NOLEADER_DUTY_DCDB_SCORE_COEFFICIENT+"+牵头单位评价得分("+leadUnitScoreB+")*"+Constants.NOLEADUNIT_DUTY_LEAD_UNIT_SCORE_COEFFICIENT;
			}
		}
		Map<String,Object> map=new HashMap<>();
//		if(score.compareTo(new BigDecimal(100))==1){
//			score=new BigDecimal(100.00);
//		}
		map.put("score",score);
		map.put("details",scoreDetails);

		return map;
	}

	private SupervisionScore getSupervisionScore(SupervisionEvaluate supervisionEvaluate,Map<String,Object> map,Long detpId){

		//获取部门是否存在事项得分数据
		QueryWrapper<SupervisionScore> wrapperS=new QueryWrapper<>();
		wrapperS.eq("serv_code",supervisionEvaluate.getServCode());
		wrapperS.eq("dept_Id",detpId);
		wrapperS.eq("score_Type",Constants.DCDB_SCORE_TYPE_PJ);
		SupervisionScore supervisionScore = this.getOne(wrapperS);
		if(ObjectUtil.isEmpty(supervisionScore)){
			supervisionScore=new SupervisionScore();
			supervisionScore.setScore((BigDecimal) map.get("score"));
			supervisionScore.setDeptId(detpId);
			supervisionScore.setServCode(supervisionEvaluate.getServCode());
			supervisionScore.setScoreType(Constants.DCDB_SCORE_TYPE_PJ);
			supervisionScore.setDetails((String) map.get("details"));
		}else {
			supervisionScore.setScore((BigDecimal) map.get("score"));
			supervisionScore.setDetails((String) map.get("details"));
		}

	return supervisionScore;
	}
}
