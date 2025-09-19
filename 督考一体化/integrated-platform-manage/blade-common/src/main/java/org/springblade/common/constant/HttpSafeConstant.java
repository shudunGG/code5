package org.springblade.common.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName: HttpSafeConstant
 * @Description: 类作用描述
 * @Author: Waston.FR
 * @Date: 2024/7/9 13:29 星期二
 * @Version 1.0
 **/
@Data
@Component
public class HttpSafeConstant {
	@Value("${http.safe.privateKey}")
	private String privateKey;
}
