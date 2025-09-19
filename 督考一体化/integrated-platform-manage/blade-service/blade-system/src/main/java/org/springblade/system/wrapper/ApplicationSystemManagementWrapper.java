package org.springblade.system.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DesUtil;
import org.springblade.system.entity.ApplicationSystemManagement;
import org.springblade.system.vo.ApplicationSystemManagementVO;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 1:45 2021/10/25 0025
 * @ Description：
 */
public class ApplicationSystemManagementWrapper extends BaseEntityWrapper<ApplicationSystemManagement, ApplicationSystemManagementVO> {
	public static ApplicationSystemManagementWrapper build() {
		return new ApplicationSystemManagementWrapper();
	}

	//加解密秘钥，不可更改，否则数据库密码解不开
	private final static String DES_KEY = "tvUp0Pzs90ClqhGO";

	@Override
	public ApplicationSystemManagementVO entityVO(ApplicationSystemManagement entity) {
		ApplicationSystemManagementVO managementVO = BeanUtil.copy(entity, ApplicationSystemManagementVO.class);
		managementVO.setAppSecret(DesUtil.decryptFormBase64(managementVO.getAppSecret(), DES_KEY));
		return managementVO;
	}
}
