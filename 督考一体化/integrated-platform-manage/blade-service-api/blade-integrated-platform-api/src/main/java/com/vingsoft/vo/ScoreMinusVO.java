package com.vingsoft.vo;

import com.vingsoft.entity.ScoreMinus;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视图实体类
 *
 * @Author JG🧸
 * @Create 2022/4/9 12:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ScoreMinusVO对象", description = "ScoreMinusVO对象")
public class ScoreMinusVO extends ScoreMinus {
	private static final long serialVersionUID = 1L;

	/** 当前记录起始索引 */
	private Integer pageNum=1;

	/** 每页显示记录数 */
	private Integer pageSize=10;

	/** 排序列 */
	private String orderByColumn;

	/** 排序的方向desc或者asc */
	private String isAsc = "asc";

}
