package org.springblade.integrated.platform.service;

import com.vingsoft.entity.AppriseBaseinfo;
import com.vingsoft.vo.AnnualBaseInfoVO;
import com.vingsoft.vo.QuarterBaseInfoVO;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-18 10:28
 */
public interface IAppriseBaseinfoService extends BaseService<AppriseBaseinfo> {

	/**
	 * 新增
	 * @param appriseBaseinfo
	 * @return
	 */
	boolean saveApprise(AppriseBaseinfo appriseBaseinfo);


	/**
	 * 首页季度评价详细信息分页
	 */
	List<QuarterBaseInfoVO> QuarterBaseInfoList(QuarterBaseInfoVO quarterBaseInfoVO);

	/**
	 * 首页年度评价详细信息分页
	 */
	List<AnnualBaseInfoVO> AnnualBaseInfoList(AnnualBaseInfoVO appriseBaseInfoVO);

	/**
	 * 修改
	 * @param appriseBaseinfo
	 * @return
	 */
	boolean updateApprise(AppriseBaseinfo appriseBaseinfo);
}
