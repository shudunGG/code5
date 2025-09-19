package org.springblade.integrated.platform.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于导出 ProjectSummary  Excel
 *
 * @Author zrj
 * @Create 2022/4/9 13:23
 */

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ProjectSummaryExcel1 implements Serializable {

	private static final long serialVersionUID = 1L;


	/**
	 * 项目名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目名称")
	private String title;


	/**
	 * 建设内容和规模
	 */
	@ColumnWidth(20)
	@ExcelProperty("建设内容和规模")
	private String projContent;


	/**
	 * 总投资（万元）
	 */
	@ColumnWidth(20)
	@ExcelProperty("总投资（万元）")
	private Float totalInvestment;



	/**
	 * 年度计划投资（万元)
	 */
	@ColumnWidth(20)
	@ExcelProperty("年度计划投资（万元)")
	private Float yearEconomic;



	/**
	 * 建设性质
	 */
	@ColumnWidth(20)
	@ExcelProperty("建设性质")
	private String projNature;



	/**
	 * 所属区域
	 */
	@ColumnWidth(20)
	@ExcelProperty("所属区域")
	private String xmAddress;


	/**
	 * 县级领导
	 */
	@ColumnWidth(20)
	@ExcelProperty("县级领导")
	private String xjldName;






	/**
	 * 市级包抓领导
	 */
	@ColumnWidth(20)
	@ExcelProperty("市级包抓领导")
	private String sjbzldName;





	/**
	 * 1月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1月计划投资")
	private String jhtz01;




	/**
	 * 1月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1月完成投资")
	private String wctz01;



	/**
	 * 1-2月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-2月计划投资")
	private String jhtz02;




	/**
	 * 1-2月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-2月完成投资")
	private String wctz02;



	/**
	 * 1-3月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-3月计划投资")
	private String jhtz03;




	/**
	 * 1-3月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-3月完成投资")
	private String wctz03;



	/**
	 * 1-4月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-4月计划投资")
	private String jhtz04;




	/**
	 * 1-4月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-4月完成投资")
	private String wctz04;



	/**
	 * 1-5月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-5月计划投资")
	private String jhtz05;




	/**
	 * 1-5月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-5月完成投资")
	private String wctz05;



	/**
	 * 1-6月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-6月计划投资")
	private String jhtz06;




	/**
	 * 1-6月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-6月完成投资")
	private String wctz06;



	/**
	 * 1-7月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-7月计划投资")
	private String jhtz07;




	/**
	 * 1-7月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-7月完成投资")
	private String wctz07;



	/**
	 * 1-8月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-8月计划投资")
	private String jhtz08;




	/**
	 * 1-8月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-8月完成投资")
	private String wctz08;



	/**
	 * 1-9月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-9月计划投资")
	private String jhtz09;




	/**
	 * 1-9月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-9月完成投资")
	private String wctz09;



	/**
	 * 1-10月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-10月计划投资")
	private String jhtz10;




	/**
	 * 1-10月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-10月完成投资")
	private String wctz10;



	/**
	 * 1-11月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-11月计划投资")
	private String jhtz11;




	/**
	 * 1-11月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-11月完成投资")
	private String wctz11;



	/**
	 * 1-12月计划投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-12月计划投资")
	private String jhtz12;




	/**
	 * 1-12月完成投资
	 */
	@ColumnWidth(20)
	@ExcelProperty("1-12月完成投资")
	private String wctz12;





	/**
	 * 项目进展情况
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目进展情况")
	private String wcqk;


	/**
	 * 纳入情况
	 */
	@ColumnWidth(20)
	@ExcelProperty("纳入情况")
	private String porjStatus;



	/**
	 * 调度情况
	 */
	@ColumnWidth(20)
	@ExcelProperty("调度情况")
	private String reportStatus;


	/**
	 * 项目类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目类型")
	private String xmType;

	/**
	 * 县区行业主管部门
	 */
	@ColumnWidth(20)
	@ExcelProperty("县区行业主管部门")
	private String xqhyzgbmName;


	/**
	 * 是否纳入统计
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否纳入统计")
	private String sfnrtj;


	/**
	 * 项目标签
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目标签")
	private String projLabel;


	/**
	 * 责任单位
	 */
	@ColumnWidth(20)
	@ExcelProperty("责任单位")
	private String bzzrdwName;



	/**
	 * 投资类别
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资类别")
	private String projMain;

	/**
	 * 年份
	 */
	@ColumnWidth(20)
	@ExcelProperty("年份")
	private String xmnf;


	/**
	 * 市直行业主管部门
	 */
	@ColumnWidth(20)
	@ExcelProperty("市直行业主管部门")
	private String szhyzgbmName;


	/**
	 * 是否开复工
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否开复工")
	private String sfkfg;


















}
