package org.springblade.integrated.platform.service;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.vingsoft.entity.Submit;
import org.apache.commons.codec.binary.Base64;
import org.springblade.core.http.HttpRequest;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @className: SmsDockingService
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/5/16 10:16 星期二
 * @Version 1.0
 **/
public interface SmsDockingService {

	R send(String mobiles,String content);
}
