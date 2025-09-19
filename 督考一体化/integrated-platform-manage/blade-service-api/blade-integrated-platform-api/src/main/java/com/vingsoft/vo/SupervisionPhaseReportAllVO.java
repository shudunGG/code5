package com.vingsoft.vo;

import com.vingsoft.entity.MessageInformation;
import com.vingsoft.entity.SupervisionPhaseReportAll;
import lombok.Data;

import java.util.List;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/4/21 22:13
* @Version:        1.0
*/
@Data
public class SupervisionPhaseReportAllVO extends SupervisionPhaseReportAll {

	private String servName;
	private List<MessageInformation> messageList;
}
