package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * QuarterlyEvaluation 分表 工作实绩
 * @Author JG🧸
 * @Create 2022/4/9 17:23
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class QuarterlyEvaluationExcel2 implements Serializable {
	private static final long serialVersionUID = 1L;



	/**
	 * 考核分组名称(字典编码：kh_group)
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分类")
	private String checkClassifyName;


	/**
	 * 一级指标*/
	@ColumnWidth(20)
	@ExcelProperty("一级指标")
	private String firstTarget;

	/**
	 * 二级指标*/
	@ColumnWidth(20)
	@ExcelProperty("二级指标")
	private String twoTarget;


	/**
	 * 考核对象
	 */
	@ColumnWidth(20)
	@ExcelProperty( "考核对象")
	private String checkObject;
	/**
	 * 评价单位
	 */
	@ColumnWidth(20)
	@ExcelProperty( "评价单位")
	private String appraiseDeptname;

	/**
	 * 权重
	 */
	@ColumnWidth(20)
	@ExcelProperty("权重")
	private String weight;

	/**
	 * 完成时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("完成时间")
	private Date finishDate;



	/*指标状态：0暂存 1推进中 2已完成*/
	@ColumnWidth(20)
	@ExcelProperty( "指标状态")
	private String targetStatus;

}
