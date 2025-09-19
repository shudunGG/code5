package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.LeaderApprise;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:领导评价(APP)
 * @date 2022-05-15 16:27
 */
@Data
@ApiModel(value = "LeaderAppriseScoreAppVO对象", description = "LeaderAppriseScoreAppVO对象")
public class LeaderAppriseScoreAppVO implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 年度
	 */
	private String appriseYear;

	/**
	 * 季度
	 */
	private String appriseQuarter;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList;
}
