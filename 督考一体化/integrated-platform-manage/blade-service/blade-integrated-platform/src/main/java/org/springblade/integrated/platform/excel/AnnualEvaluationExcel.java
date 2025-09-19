package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.StageInformation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:政治思想建设年度导入
 * @date 2022-04-16 12:01
 */

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class AnnualEvaluationExcel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目名称字典值
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目名称字典值")
	private String projectId;

	/**
	 * 项目名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目")
	private String projectName;

	/**
	 * 考核分类
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分类id")
	private String appraiseClassify;

	/**
	 * 考核分类名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分类")
	private String appraiseClassifyName;

	/**
	 * 主要指标及评价要点
	 */
	@ColumnWidth(20)
	@ExcelProperty("主要指标及评价要点")
	private String majorTarget;

	/**
	 * 评价对象id
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价对象id")
	private String appraiseObjectId;

	/**
	 * 评价对象
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价对象")
	private String appraiseObject;

	/**
	 * 评价单位名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价单位")
	private String appraiseDeptname;

	/**
	 * 评价单位id
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价单位id")
	private String appraiseDeptid;

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


	/**
	 * 年度考评分类：政治思想建设、领导能力、党的建设、高质量发展（市直高质量发展、区县高质量发展）
	 */
	@ColumnWidth(20)
	@ExcelProperty("年度考评分类")
	private String type;

	/**
	 * 年度考评状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("年度考评状态")
	private String checkStatus;

	/**
	 * 指标状态：0暂存 1推进中 2已完成
	 */
	@ColumnWidth(20)
	@ExcelProperty("指标状态")
	private String targetStatus;

	/**
	 * 办理状态：0正常1催办
	 */
	@ColumnWidth(20)
	@ExcelProperty("办理状态")
	private String handleStatus;

	/**
	 * 阶段信息
	 */
	@TableField(exist = false)
	private List<StageInformation> stageInformationList;

	/**
	 * 是否删除
	 */
	@TableField(exist = false)
	private Integer isDeleted;

	/**
	 * 操作类型 0 新增 1下发
	 */
	@TableField(exist = false)
	private String operateType;


	/**
	 * 甘州区
	 */
	@ColumnWidth(20)
	@ExcelProperty("甘州区")
	private String ganzhouqu;

	/**
	 * 临泽县
	 */
	@ColumnWidth(20)
	@ExcelProperty("临泽县")
	private String linzexian;

	/**
	 * 高台县
	 */
	@ColumnWidth(20)
	@ExcelProperty("高台县")
	private String gaotaixian;

	/**
	 * 山丹县
	 */
	@ColumnWidth(20)
	@ExcelProperty("山丹县")
	private String shandanxian;

	/**
	 * 民乐县
	 */
	@ColumnWidth(20)
	@ExcelProperty("民乐县")
	private String minlexian;

	/**
	 * 肃南县
	 */
	@ColumnWidth(20)
	@ExcelProperty("肃南县")
	private String sunanxian;

	/**
	 * 阶段
	 */
	@ColumnWidth(15)
	@ExcelProperty("阶段")
	private String stage;

	/**
	 * 阶段要求
	 */
	@ColumnWidth(40)
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
