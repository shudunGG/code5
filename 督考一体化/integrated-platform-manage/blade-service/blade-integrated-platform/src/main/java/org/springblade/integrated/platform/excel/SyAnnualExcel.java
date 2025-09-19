package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于导出 绩效考核首页年度详情
 * @author JG🧸
 * @version 2022/8/17 14:32
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SyAnnualExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 责任单位
	 */
	@ColumnWidth(40)
	@ExcelProperty("责任单位")
	private String zrdw;

	/**
	 * 高质量发展
	 */
	@ColumnWidth(40)
	@ExcelProperty("高质量发展")
	private String gzlfz;


	/**
	 * 政治思想建设
	 */
	@ColumnWidth(40)
	@ExcelProperty("政治思想建设")
	private String zzsxjs;

	/**
	 * 领导能力
	 */
	@ColumnWidth(40)
	@ExcelProperty("领导能力")
	private String ldnl;

	/**
	 * 党的建设
	 */
	@ColumnWidth(40)
	@ExcelProperty("党的建设")
	private String ddjs;

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
	 * 季度评价得分
	 */
	@ColumnWidth(40)
	@ExcelProperty("季度评价得分")
	private String jdpjdf;

	/**
	 * 年度总分
	 */
	@ColumnWidth(20)
	@ExcelProperty("年度总分")
	private String zf;

	/**
	 * 排名
	 */
	@ColumnWidth(20)
	@ExcelProperty("排名")
	private String pm;
}
