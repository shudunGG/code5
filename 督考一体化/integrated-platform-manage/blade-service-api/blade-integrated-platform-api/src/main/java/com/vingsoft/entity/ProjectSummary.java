package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.vingsoft.support.mybatis.SM4EncryptHandler;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目基本信息实体类
 * @author AdamJin 2022-4-9 17:33:28
 */
@Data
@TableName("project_summary")
public class ProjectSummary extends BaseEntity{

    /**
     * 项目状态
     */
    private String porjStatus;

    /**
     * 调度状态
     */
    private String reportStatus;

    /**
     * 项目类型
     */
    private String xmType;

	/**
	 * 行业类型
	 */
	private String hyType;

    /**
     * 项目大类（8大类投资项目清单）
     */
    private String xmdl;

    /**
     * 项目挂牌
     */
    private String autoState;

    /**
     * 前期产业类型
     */
    private String qqcylx;

    /**
     * 项目名称
     */
    private String title;

    /**
     * 投资主体
     */
    private String projMain;

    /**
     * 建设内容
     */
    private String projContent;

    /**
     * 项目地址
     */
    private String xmAddress;

    /**
     * 项目地点(json存储的经纬)
     */
    private String ddAddress;

    /**
     * 总投资（万元）
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Float totalInvestment;

    /**
     * 年度计划投资（万元）
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Float yearEconomic;

    /**
     * 当月完成投资（万元）
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Float monthEconomic;

    /**
     * 项目代码
     */
    private String projCode;

    /**
     * 项目标签
     */
    private String projLabel;

    /**
     * 计划开工年月/拟开工日期
     */
    private Date startDatePlan;

    /**
     * 计划竣工年月/拟竣工日期
     */
    private Date completeDate;

    /**
     * 项目单位
     */
    private String sgdw;

	/**
	 * 项目单位名称
	 */
	private String sgdwName;

    /**
     * 1月完成投资
     */
    private String wctz01;

    /**
     * 1-2月完成投资
     */
    private String wctz02;

    /**
     * 1-3月完成投资
     */
    private String wctz03;

    /**
     * 1-4月完成投资
     */
    private String wctz04;

    /**
     * 1-5月完成投资
     */
    private String wctz05;

    /**
     * 1-6月完成投资
     */
    private String wctz06;

    /**
     * 1-7月完成投资
     */
    private String wctz07;

    /**
     * 1-8月完成投资
     */
    private String wctz08;

    /**
     * 1-9月完成投资
     */
    private String wctz09;

    /**
     * 1-10月完成投资
     */
    private String wctz10;

    /**
     * 1-11月完成投资
     */
    private String wctz11;

    /**
     * 1-12月完成投资
     */
    private String wctz12;

    /**
     * 1月计划投资
     */
    private String jhtz01;

    /**
     * 1-2月计划投资
     */
    private String jhtz02;

    /**
     * 1-3月计划投资
     */
    private String jhtz03;

    /**
     * 1-4月计划投资
     */
    private String jhtz04;

    /**
     * 1-5月计划投资
     */
    private String jhtz05;

    /**
     * 1-6月计划投资
     */
    private String jhtz06;

    /**
     * 1-7月计划投资
     */
    private String jhtz07;

    /**
     * 1-8月计划投资
     */
    private String jhtz08;

    /**
     * 1-9月计划投资
     */
    private String jhtz09;

    /**
     * 1-10月计划投资
     */
    private String jhtz10;

    /**
     * 1-11月计划投资
     */
    private String jhtz11;

    /**
     * 1-12月计划投资
     */
    private String jhtz12;

    /**
     * 市级领导
     */
    private String sjld;

	/**
	 * 市级领导名称
	 */
	private String sjldName;

    /**
     * 县级领导
     */
    private String xjld;

	/**
	 * 县级领导名称
	 */
	private String xjldName;

    /**
     * 市直行业主管部门
     */
    private String szhyzgbm;

	/**
	 * 市直行业主管部门名称
	 */
	private String szhyzgbmName;

    /**
     * 市直行业主管部门责任人
     */
    private String szhyzgbmZrr;

	/**
	 * 市直行业主管部门责任人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String szhyzgbmZrrName;

    /**
     * 县区行业主管部门
     */
    private String xqhyzgbm;

	/**
	 * 县区行业主管部门名称
	 */
	private String xqhyzgbmName;

    /**
     * 县区行业主管部门责任人
     */
    private String xqhyzgbmZrr;

	/**
	 * 县区行业主管部门责任人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String xqhyzgbmZrrName;

    /**
     * 手机号码
     */
	@TableField(typeHandler = SM4EncryptHandler.class)
    private String dhhm;

    /**
     * 固定电话
     */
	@TableField(typeHandler = SM4EncryptHandler.class)
    private String gddh;

    /**
     * 业主单位联系人
     */
	@TableField(typeHandler = SM4EncryptHandler.class)
    private String managerContact;

    /**
     * 调度单位
     */
    private String dwmc;

	/**
	 * 调度单位名称
	 */
	private String dwmcName;

    /**
     * 文件
     */
    private String file;

    /**
     * 前期任务目标
     */
    private String yearGoal;

    /**
     * 前期工作计划完成时间
     */
    private Date qqgzjhwcsj;

    /**
     * 包抓主管单位
     */
    private String bzzgdw;

	/**
	 * 包抓主管单位名称
	 */
	private String bzzgdwName;

    /**
     * 包抓主管单位责任人
     */
    private String bzzgdwZrr;

	/**
	 * 包抓主管单位责任人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String bzzgdwZrrName;

    /**
     * 包抓责任单位
     */
    private String bzzrdw;

	/**
	 * 包抓责任单位名称
	 */
	private String bzzrdwName;

    /**
     * 包抓责任单位责任人
     */
    private String bzzrdwZrr;

	/**
	 * 包抓责任单位责任人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String bzzrdwZrrName;

    /**
     * 项目年份
     */
    private String xmnf;

    /**
     * 是否开复工
     */
    private String sfkfg;

    /**
     * 是否纳入统计
     */
    private String sfnrtj;

	/**
	 * 创建人
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Long createUser;

	/**
	 * 创建部门
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Long createDept;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改人
	 */
	private Long updateUser;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 状态
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Integer status;

	/**
	 * 专项名称
	 */
	private String zxmc;

	/**
	 * 建设规模
	 */
	private String projScale;

	/**
	 * 项目负责人
	 */
	private String sgfzr;

	/**
	 * 项目负责人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String sgfzrName;

	/**
	 * 隶属关系
	 */
	private String subordination;

	/**
	 * 建设性质
	 */
	private String projNature;

	/**
	 * 下达资金文号
	 */
	private String xdzjwh;

	/**
	 * 下达资金日期
	 */
	private Date xdzjTime;
	/**
	 * 下达资金文号
	 */
	private String sjxdzjwh;

	/**
	 * 下达资金日期
	 */
	private Date sjxdzjTime;

	/**
	 * 投资计划(合计)
	 */
	private String tzjhhj;

	/**
	 * 投资计划(中央)
	 */
	private String zytzjh;

	/**
	 * 投资计划(省级)
	 */
	private String sjtzjh;

	/**
	 * 投资计划(地方)
	 */
	private String dftzjh;

	/**
	 * 发行债券日期
	 */
	private Date fxzqTime;

	/**
	 * 发行债券额度（万元）
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float fxzqje;

	/**
	 * 是否入库
	 */
	private Integer sfrk;

	/**
	 * 产业类型
	 */
	private String cyType;

	/**
	 * 完成情况
	 */
	private String wcqk;

	/**
	 * 调度人
	 */
	private String zrr;

	/**
	 * 调度人名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String zrrName;

	/**
	 * 区划
	 */
	private String areaCode;

	/**
	 * 完成成总投资
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float totalEconomic;

	/**
	 * 市级抓包领导
	 */
	private String sjbzld;

	/**
	 * 市级抓包领导名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String sjbzldName;

	/**
	 * 县级抓包领导
	 */
	private String xjbzld;

	/**
	 * 县级抓包领导名称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String xjbzldName;

	/**
	 * 未纳入统计库原因
	 */
	private String wnrtjkyy;

	/**
	 * 未开复工原因
	 */
	private String wkfgyy;

	/**
	 * 开工时间
	 */
	private Date kfgsj;

	/**
	 * 纳入统计库时间
	 */
	private Date snrtjksj;

	/**
	 * 百度经纬度
	 */
	@TableField(exist = false)
	private Map baiDu;

	/**
	 * 高德经纬度
	 */
	@TableField(exist = false)
	private Map gaoDe;

	/**
	 * 项目阶段信息
	 */
	@TableField(exist = false)
	private List<ProjectPhasePlan> projectPhasePlanList;

	/**
	 * 项目附件信息
	 */
	@TableField(exist = false)
	private List<ProjectFiles> projectFilesList;

	/**
	 * 项目主题背景图名称
	 */
	private String bgFileName;

	/**
	 * 项目主题背景图地址
	 */
	private String bgFileUrl;

	/**
	 * 项目专员列表
	 */
	@TableField(exist = false)
	private List<ProjectSpecialist> projectSpecialistList;

}
