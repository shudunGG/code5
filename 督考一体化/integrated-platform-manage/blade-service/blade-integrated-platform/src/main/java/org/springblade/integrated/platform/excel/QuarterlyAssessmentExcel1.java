package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * QuarterlyEvaluation 分表 党建工作
 * @Author zrj
 * @Create 2022/4/9 17:23
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class QuarterlyAssessmentExcel1 implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 所属季度
	 */
	@ColumnWidth(20)
	@ExcelProperty("所属季度")
	private String toQuarter;



	/**
	 * 考核分组名称(字典编码：kh_group)
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分类")
	private String checkClassifyName;

	/**
	 * 评价要点
	 */
	@ColumnWidth(20)
	@ExcelProperty( "评价要点")
	private String majorTarget;
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







}
