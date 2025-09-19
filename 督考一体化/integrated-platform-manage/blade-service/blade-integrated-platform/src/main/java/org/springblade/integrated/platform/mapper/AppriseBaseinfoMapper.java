package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.AppriseBaseinfo;
import com.vingsoft.vo.AnnualBaseInfoVO;
import com.vingsoft.vo.QuarterBaseInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-18 10:30
 */
@Repository
public interface AppriseBaseinfoMapper extends BaseMapper<AppriseBaseinfo> {

	List<QuarterBaseInfoVO> QuarterBaseInfoList(QuarterBaseInfoVO appriseBaseInfoVO);

	List<AnnualBaseInfoVO> AnnualBaseInfoList(AnnualBaseInfoVO appriseBaseInfoVO);
}
