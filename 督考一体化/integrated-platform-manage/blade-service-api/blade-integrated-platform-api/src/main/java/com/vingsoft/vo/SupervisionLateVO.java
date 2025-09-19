package com.vingsoft.vo;

import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionLate;
import lombok.Data;

import java.util.List;

/**
* @Description:    事项申请延期视图
* @Author:         WangRJ
* @CreateDate:     2022/5/20 23:47
* @Version:        1.0
*/
@Data
public class SupervisionLateVO extends SupervisionLate {

	/**
	 * 申请附件
	 */
	private List<SupervisionFiles> filesList;
}
