package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.AppriseFiles;
import com.vingsoft.entity.MessageInformation;
import com.vingsoft.entity.ReportsBaseinfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-20 15:25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "阶段单位汇报信息对象", description = "阶段单位汇报信息对象")
public class ReportsBaseinfoVo extends ReportsBaseinfo {
	private String processStatus;
	private String process;
	private String reportMessage;
	private String reportStatus;
	private String reportsId;//reports表主键
	private String hasFiles;//是否有附件 Y/N
	private String rYear;//年份

	private Integer isApprise;//是否评价
	private String isHb;//是否汇报

	/**
	 * 考核评价文件信息
	 */
	List<AppriseFiles> appriseFilesList;

	/**
	 * 留言信息
	 */
	List<MessageInformation> messageInformationList;

}
