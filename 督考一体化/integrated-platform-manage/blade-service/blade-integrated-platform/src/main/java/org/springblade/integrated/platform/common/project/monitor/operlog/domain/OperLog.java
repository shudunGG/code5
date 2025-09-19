package org.springblade.integrated.platform.common.project.monitor.operlog.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springblade.core.mp.base.BaseEntity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import java.util.Date;

/**
 * 操作日志记录表 oper_log
 *
 * @Author JG🧸
 * @Create 2022/4/13 11:40
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
@TableName("sys_oper_log")
public class OperLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 操作模块 */
    @ExcelProperty(value = "操作模块")
    private String title;

	/** 业务表ID */
	@ExcelProperty(value = "业务表ID")
	private String businessId;

	/** 业务表名 */
	@ExcelProperty(value = "业务表名")
	private String businessTable;

    /** 业务类型 */
    @ExcelProperty(value = "业务类型")//"0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据,10=下载,11查看"
    private Integer businessType;

    /** 业务类型数组 */
    private Integer[] businessTypes;

    /** 请求方法 */
    @ExcelProperty(value = "请求方法")
    private String method;

    /** 请求方式 */
    @ExcelProperty(value = "请求方式")
    private String requestMethod;

    /** 操作人类别 */
    @ExcelProperty(value = "操作类别")//"0=其它,1=后台用户,2=手机端用户"
    private Integer operatorType;

    /** 操作人员 */
    @ExcelProperty(value = "操作人员")
    private String operName;

    /** 部门名称 */
    @ExcelProperty(value = "部门名称")
    private String deptName;

    /** 请求url */
    @ExcelProperty(value = "请求地址")
    private String operUrl;

    /** 操作地址 */
    @ExcelProperty(value = "操作地址")
    private String operIp;

    /** 操作地点 */
    @ExcelProperty(value = "操作地点")
    private String operLocation;

    /** 请求参数 */
    @ExcelProperty(value = "请求参数")
    private String operParam;

    /** 返回参数 */
    @ExcelProperty(value = "返回参数")
    private String jsonResult;

    /** 状态0正常 1异常 */
    @ExcelProperty(value = "状态")//"0=正常,1=异常"
    private Integer status;

    /** 错误消息 */
    @ExcelProperty(value = "错误消息")
    private String errorMsg;

    /** 操作时间 */
    @ExcelProperty(value = "操作时间")
    private Date operTime;

	/*不带时分秒的操作时间*/
	@TableField(exist = false)
	private String operDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("title", getTitle())
            .append("businessType", getBusinessType())
            .append("businessTypes", getBusinessTypes())
            .append("method", getMethod())
            .append("requestMethod", getRequestMethod())
            .append("operatorType", getOperatorType())
            .append("operName", getOperName())
            .append("deptName", getDeptName())
            .append("operUrl", getOperUrl())
            .append("operIp", getOperIp())
            .append("operLocation", getOperLocation())
            .append("operParam", getOperParam())
            .append("status", getStatus())
            .append("errorMsg", getErrorMsg())
            .append("operTime", getOperTime())
            .toString();
    }





}
