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
public class ProjectSummaryExcel implements Serializable {

	private static final long serialVersionUID = 1L;


	/**
	 * 项目状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目状态")
	private String porjStatus;

	/**
	 * 状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("状态")
	private int status;



	/**
	 * 调度状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("调度状态")
	private String reportStatus;

	/**
	 * 项目类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目类型")
	private String xmType;

	/**
	 * 项目大类（8大类投资项目清单）
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目大类（8大类投资项目清单）")
	private String xmdl;

	/**
	 * 项目挂牌
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目挂牌")
	private String autoState;

	/**
	 * 前期产业类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("前期产业类型")
	private String qqcylx;

	/**
	 * 产业类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("产业类型")
	private String cyType;



	/**
	 * 项目名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目名称")
	private String title;

	/**
	 * 投资主体
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资主体")
	private String projMain;

	/**
	 * 建设内容
	 */
	@ColumnWidth(2000)
	@ExcelProperty("建设内容")
	private String projContent;

	/**
	 * 项目地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目地址")
	private String xmAddress;

	/**
	 * 项目地点(json存储的经纬)
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目地点(json存储的经纬)")
	private String ddAddress;

	/**
	 * 总投资（万元）
	 */
	@ColumnWidth(20)
	@ExcelProperty("总投资（万元）")
	private Float totalInvestment;

	/**
	 * 年度计划投资（万元）
	 */
	@ColumnWidth(20)
	@ExcelProperty("年度计划投资（万元）")
	private Float yearEconomic;

	/**
	 * 当月调度投资（万元）
	 */
	@ColumnWidth(20)
	@ExcelProperty("当月调度投资（万元））")
	private Float monthEconomic;

	/**
	 * 项目代码
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目代码")
	private String projCode;

	/**
	 * 项目标签
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目标签")
	private String projLabel;

	/**
	 * 计划开工年月/拟开工日期
	 */
	@ColumnWidth(20)
	@ExcelProperty("计划开工年月/拟开工日期")
	private Date startDatePlan;
	/**
	 * 计划竣工年月/拟竣工日期
	 */
	@ColumnWidth(20)
	@ExcelProperty("计划竣工年月/拟竣工日期")
	private Date completeDate;

	/**
	 * 项目单位
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目单位")
	private String sgdw;

	/**
	 * 责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("责任人")
	private String zrr;

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
	 * 市级领导
	 */
	@ColumnWidth(20)
	@ExcelProperty("市级领导")
	private String sjld;

	/**
	 * 县级领导
	 */
	@ColumnWidth(20)
	@ExcelProperty("县级领导")
	private String xjld;

	/**
	 * 市直行业主管部门
	 */
	@ColumnWidth(20)
	@ExcelProperty("市直行业主管部门")
	private String szhyzgbm;

	/**
	 * 市直行业主管部门责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("市直行业主管部门责任人")
	private String szhyzgbmZrr;

	/**
	 * 县区行业主管部门
	 */
	@ColumnWidth(20)
	@ExcelProperty("县区行业主管部门")
	private String xqhyzgbm;

	/**
	 * 县区行业主管部门责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("县区行业主管部门责任人")
	private String xqhyzgbmZrr;

	/**
	 * 手机号码
	 */
	@ColumnWidth(20)
	@ExcelProperty("手机号码")
	private String dhhm;

	/**
	 * 固定电话
	 */
	@ColumnWidth(20)
	@ExcelProperty("固定电话")
	private String gddh;

	/**
	 * 业主单位联系人
	 */
	@ColumnWidth(20)
	@ExcelProperty("业主单位联系人")
	private String managerContact;

	/**
	 * 调度单位名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("调度单位名称")
	private String dwmc;

	/**
	 * 文件
	 */
	@ColumnWidth(20)
	@ExcelProperty("文件")
	private String file;

	/**
	 * 前期任务目标
	 */
	@ColumnWidth(20)
	@ExcelProperty("前期任务目标")
	private String yearGoal;

	/**
	 * 前期工作计划完成时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("前期工作计划完成时间")
	private String qqgzjhwcsj;

	/**
	 * 包抓主管单位
	 */
	@ColumnWidth(20)
	@ExcelProperty("包抓主管单位")
	private String bzzgdw;

	/**
	 * 包抓主管单位责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("包抓主管单位责任人")
	private String bzzgdwZrr;

	/**
	 * 包抓责任单位
	 */
	@ColumnWidth(20)
	@ExcelProperty("包抓责任单位")
	private String bzzrdw;

	/**
	 * 包抓责任单位责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("包抓责任单位责任人")
	private String bzzrdwZrr;

	/**
	 * 项目年份
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目年份")
	private String xmnf;

	/**
	 * 是否开复工
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否开复工")
	private String sfkfg;

	/**
	 * 是否纳入统计
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否纳入统计")
	private String sfnrtj;

	/**
	 * 创建人
	 */
	@ColumnWidth(20)
	@ExcelProperty("创建人")
	private Long createUser;

	/**
	 * 创建部门
	 */
	@ColumnWidth(20)
	@ExcelProperty("创建部门")
	private Long createDept;


	/**
	 * 创建时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("创建时间")
	private Date createTime;


	/**
	 * 修改人
	 */
	@ColumnWidth(20)
	@ExcelProperty("修改人")
	private Long updateUser;


	/**
	 * 修改时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("修改时间")
	private Date updateTime;

	/**
	 * 专项名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("专项名称")
	private String zxmc;


	/**
	 * 建设规模
	 */
	@ColumnWidth(255)
	@ExcelProperty("建设规模")
	private String projScale;


	/**
	 * 项目负责人
	 */
	@ColumnWidth(20)
	@ExcelProperty("项目负责人")
	private String sgfzr;


	/**
	 * 隶属关系
	 */
	@ColumnWidth(20)
	@ExcelProperty("隶属关系")
	private String subordination;


	/**
	 * 建设性质
	 */
	@ColumnWidth(20)
	@ExcelProperty("建设性质")
	private String projNature;


	/**
	 * 下达资金文号
	 */
	@ColumnWidth(20)
	@ExcelProperty("下达资金文号")
	private String xdzjwh;


	/**
	 * 下达资金日期
	 */
	@ColumnWidth(20)
	@ExcelProperty("下达资金日期")
	private Date xdzjTime;


	/**
	 * 投资计划(合计)
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资计划(合计)")
	private String tzjhhj;


	/**
	 * 投资计划(中央)
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资计划(中央)")
	private String zytzjh;


	/**
	 * 投资计划(省级)
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资计划(省级)")
	private String sjtzjh;


	/**
	 * 投资计划(地方)
	 */
	@ColumnWidth(20)
	@ExcelProperty("投资计划(地方)")
	private String dftzjh;


	/**
	 * 发行债券日期
	 */
	@ColumnWidth(20)
	@ExcelProperty("发行债券日期")
	private Date fxzqTime;


	/**
	 * 发行债券额度（万元）
	 */
	@ColumnWidth(20)
	@ExcelProperty("发行债券额度（万元）")
	private Float fxzqje;


	/**
	 * 是否入库
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否入库")
	private Integer sfrk;

	/**
	 * 完成情况
	 */
	@ColumnWidth(20)
	@ExcelProperty("完成情况")
	private String wcqk;

	/**
	 * 责任人
	 */
	@ColumnWidth(255)
	@ExcelProperty("项目单位及负责人")
	private String sgdwName;

	/**
	 * 所属地区
	 */
	@ColumnWidth(20)
	@ExcelProperty("所属地区")
	private String areaCode;



}
