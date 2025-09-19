package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-15 14:31
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class DdjsExcel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 考核分类
	 */
	@ColumnWidth(20)
	@ExcelProperty("考核分类")
	private String appraiseClassifyName;

	/**
	 * 主要指标及评价要点
	 */
	@ColumnWidth(40)
	@ExcelProperty("主要指标及评价要点")
	private String majorTarget;

	/**
	 * 评价对象
	 */
	@ColumnWidth(40)
	@ExcelProperty("评价对象")
	private String appraiseObject;

	/**
	 * 评价单位
	 */
	@ColumnWidth(40)
	@ExcelProperty("评价单位")
	private String appraiseDeptname;

	/**
	 * 权重
	 */
	@ColumnWidth(10)
	@ExcelProperty("权重")
	private String weight;

	/**
	 * 完成时间
	 */
	@ColumnWidth(15)
	@ExcelProperty("完成时间")
	private Date finishDate;

	/**
	 * 指标状态：0暂存 1推进中 2已完成
	 */
	@ColumnWidth(15)
	@ExcelProperty("指标状态")
	private String targetStatus;
}
