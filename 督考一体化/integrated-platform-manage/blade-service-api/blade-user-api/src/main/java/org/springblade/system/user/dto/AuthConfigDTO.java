package org.springblade.system.user.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 2:13 2021/9/10 0010
 * @ Description：由于使用nacos配置的地方取值困难，所以放在这里取值
 */
@Component
@Data
public class AuthConfigDTO {

	/**
	 * @Author MaQY
	 * @Description 解密盐值
	 * @Date 下午 2:16 2021/9/10 0010
	 **/
	@Value("${blade.api.crypto.des-key}")
	private String desKey;

	/**
	 * @Author MaQY
	 * @Description 加密盐值
	 * @Date 下午 2:16 2021/9/10 0010
	 **/
	@Value("${blade.api.crypto.aes-key}")
	private String aesKey;
}
