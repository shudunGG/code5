package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于导出 QuarterlyEvaluation Excel 总表
 * @Author zrj
 * @Create 2022/4/9 17:23
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class QuarterlyAssessmentExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 考核分组字典值(字典编码：kh_group) 值是1，2，3
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分组字典值")
	private String checkClassify;

	/**
	 * 考核分组名称(字典编码：kh_group)
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分组")
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
	 * 评价单位id
	 */
	@ColumnWidth(20)
	@ExcelProperty( "评价单位id")
	private String appraiseDeptid;
	/**
	 * 权重
	 */
	@ColumnWidth(20)
	@ExcelProperty("权重")
	private String weight;



	/*
	 *季度考评分类：1、2、3
	 *季度指标分类字典值（字典代码：jdpj-type）
	 */
	@ColumnWidth(20)
	@ExcelProperty("季度考评分类字典值")
	private String jdzbType;

	/*
	 * 所属季度
	 */
	@ColumnWidth(20)
	@ExcelProperty("所属季度")
	private String toQuarter;

	/*
	 *季度指标分类字典名称（字典代码：jdpj-type）党建工作季度评价、工作实绩季度评价、党风廉政季度评价
	 *
	 */
	@ColumnWidth(20)
	@ExcelProperty("季度考评分类")
	private String jdzbName;


	/*季度考评状态*/
	@ColumnWidth(20)
	@ExcelProperty("季度考评状态")
	private String checkStatus;

	/*指标状态：0暂存 1推进中 2已完成*/
	@ColumnWidth(20)
	@ExcelProperty("指标状态")
	private String targetStatus;

	/**
	 * 办理状态：0正常1催办*/
	@ColumnWidth(20)
	@ExcelProperty( "办理状态")
	private String handleStatus;

	/**
	 * 指标*/
	@ColumnWidth(20)
	@ExcelProperty( "指标")
	private String target;



	/**
	 * 重点工作*/
	@ColumnWidth(20)
	@ExcelProperty("重点工作")
	private String importWork;


	/**
	 * 评分细则*/
	@ColumnWidth(20)
	@ExcelProperty("评分细则")
	private String scoringRubric;

	/**
	 * 阶段
	 */
	@ColumnWidth(20)
	@ExcelProperty("阶段")
	private String stage;

	/**
	 * 阶段要求
	 */
	@ColumnWidth(20)
	@ExcelProperty("阶段要求")
	private String stageRequirement;

	/**
	 * 开始时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("开始时间")
	private Date startDate;

	/**
	 * 截止时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("截止时间")
	private Date endDate;

}
