package org.springblade.common.utils;

import io.jsonwebtoken.Claims;
import org.springblade.core.jwt.JwtUtil;
import org.springblade.core.launch.constant.TokenConstant;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.StringUtil;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
/**
 * @author : luye
 * @date : 2021/8/11
 */
public class TokenUtil{

	public static Map<String,Object> getTokenInfo(HttpServletRequest request) {
		Map<String,Object> tokenInfo = new HashMap<>();
		String auth = request.getHeader(TokenConstant.HEADER);
		Claims claims = null;
		String token;
		// 获取 Token 参数
		if (StringUtil.isNotBlank(auth)) {
			token = JwtUtil.getToken(auth);
		} else {
			String parameter = request.getParameter(TokenConstant.HEADER);
			token = JwtUtil.getToken(parameter);
		}
		// 获取 Token 值
		if (StringUtil.isNotBlank(token)) {
			claims = AuthUtil.parseJWT(token);
		}
		tokenInfo.put("token",token);
		tokenInfo.put("exp",claims.getExpiration().getTime());
		return tokenInfo;
	}

}
