package com.vingsoft.vo;

import com.vingsoft.entity.AppTaskChat;
import com.vingsoft.entity.SupervisionFiles;
import lombok.Data;

import java.util.List;

/**
* @Description:    交办回复视图类
* @Author:         WangRJ
* @CreateDate:     2022/5/11 16:45
* @Version:        1.0
*/
@Data
public class AppTaskChatVO extends AppTaskChat {

	/**
	 * 回复附件
	 */
	private List<SupervisionFiles> filesList;
}
