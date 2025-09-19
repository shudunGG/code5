package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于导出 绩效考核首页季度详情
 * @author JG🧸
 * @version 2022/8/17 14:32
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SyQuarterExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 责任单位
	 */
	@ColumnWidth(40)
	@ExcelProperty("责任单位")
	private String zrdw;

	/**
	 * 党建工作
	 */
	@ColumnWidth(40)
	@ExcelProperty("党建工作")
	private String djgz;


	/**
	 * 工作实绩
	 */
	@ColumnWidth(40)
	@ExcelProperty("工作实绩")
	private String gzsj;

	/**
	 * 党风廉政
	 */
	@ColumnWidth(40)
	@ExcelProperty("党风廉政")
	private String dflz;

	/**
	 * 加分
	 */
	@ColumnWidth(20)
	@ExcelProperty("加分")
	private String jiaFen;

	/**
	 * 减分
	 */
	@ColumnWidth(20)
	@ExcelProperty("减分")
	private String jianFen;

	/**
	 * 领导评价
	 */
	@ColumnWidth(40)
	@ExcelProperty("领导评价")
	private String ldpj;

	/**
	 * 季度总分
	 */
	@ColumnWidth(20)
	@ExcelProperty("季度总分")
	private String zf;

	/**
	 * 排名
	 */
	@ColumnWidth(20)
	@ExcelProperty("排名")
	private String pm;
}
