package org.springblade.gateway.filter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.CommonConstant;
import org.springblade.gateway.props.AuthProperties;
import org.springblade.gateway.utils.EncryptAesUtil;
import org.springblade.gateway.utils.ResponseMessageUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * <p>
 * 全局拦截器，作用所有的微服务
 * <p>
 * 1. 对请求头中参数进行处理 from 参数进行清洗
 * 2. 重写StripPrefix = 1,支持全局
 *
 * @author lengleng
 */
@Slf4j
@Component
@AllArgsConstructor
public class RequestFilter implements GlobalFilter, Ordered {
	private final ObjectMapper objectMapper;

	private AuthProperties authProperties;

	private RedisTemplate redisTemplate;

	/**
	 * Process the Web request and (optionally) delegate to the next
	 * {@code WebFilter} through the given {@link GatewayFilterChain}.
	 *
	 * @param exchange the current server exchange
	 * @param chain    provides a way to delegate to the next filter
	 * @return {@code Mono<Void>} to indicate when request processing is complete
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpResponse resp = exchange.getResponse();

		//校验有效期
		if(!validDate()){
			return ResponseMessageUtil.forbiddenMethod(resp,objectMapper,HttpStatus.FORBIDDEN,"系统已过有效期，请联系管理员。");
		}

		List<String> forbiddens = new ArrayList<>();
		forbiddens.add("OPTIONS");
		forbiddens.add("PUT");
		forbiddens.add("DELETE");
		if (forbiddens.contains(exchange.getRequest().getMethodValue())) {
			return ResponseMessageUtil.forbiddenMethod(resp,objectMapper,HttpStatus.FORBIDDEN,"方法已被禁用");
		}
		// 1. 清洗请求头中from 参数
		ServerHttpRequest request = exchange.getRequest().mutate()
			.headers(httpHeaders -> {
				httpHeaders.remove("X");
				// 处理Authorization弱密码漏洞
				try {
					String authorization = httpHeaders.getFirst("Authorization");
					if(!StringUtils.isEmpty(authorization)){
						authorization = EncryptAesUtil.decryptFormBase64ToString(authorization, CommonConstant.AES_KEY);
						httpHeaders.set("Authorization",authorization);
					}
				} catch (Exception e){
					log.warn(e.getMessage());
				}
			})
			.build();

		// 2. 重写StripPrefix
		addOriginalRequestUrl(exchange, request.getURI());
		String rawPath = request.getURI().getRawPath();
		String newPath = "/" + Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/"))
			.skip(1L).collect(Collectors.joining("/"));
		ServerHttpRequest newRequest = request.mutate()
			.path(newPath)
			.build();
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

		return chain.filter(exchange.mutate().request(newRequest.mutate().build()).build());
	}

	@Override
	public int getOrder() {
		return -1000;
	}

	/**
	 * 校验有效期
	 * @return	false 无效  true 有效
	 */
	private boolean validDate(){
		try {
			//私钥
			String privateKey = authProperties.getPrivateKey();
			//注册码
			String registerCode = authProperties.getRegisterCode();
			if(StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(registerCode)){
				log.warn("私钥或注册码未配置");
				return false;
			}else{

				//第一重验证：验证有效标记
				String flag = this.redisTemplate.opsForValue().get(EncryptAesUtil.SYS_VALID_KEY)+"";
				if(!StringUtils.isEmpty(flag) && !"null".equals(flag)){
					String[] flagArr = flag.split(":");
					if(!registerCode.equals(flagArr[0])){
						log.warn("使用新的key，启用有效状态");
						this.valid(registerCode);
					}
					if(EncryptAesUtil.INVALID_KEY.equals(flagArr[1])){
						log.warn("系统已禁用");
						return false;
					}
				}else{
					this.valid(registerCode);
				}

				String validDateStr = EncryptAesUtil.aesDecrypt(privateKey,registerCode);
				if(StringUtils.isEmpty(validDateStr)){
					log.warn("请检查秘钥和注册码配置");
					return false;
				}

				//第二重验证：判断当前时间是否过了有效期
				Date validDate = DateUtil.parse(validDateStr, DatePattern.NORM_DATE_FORMAT);
				Date nowDate = new Date();
				boolean isValid = nowDate.after(validDate);
				if(isValid){
					//过期了，生成标记，防止修改系统日期
					this.invalid(registerCode);
					log.warn("已过有效期，系统已禁用");
					return false;
				}
			}
		} catch (Exception e){
			log.error("异常...");
			log.error(e.getMessage(),e);
			return false;
		}
		return true;
	}

	/**
	 * 标记有效key
	 * @param key
	 */
	private void valid(String key){
		key = key + ":" + EncryptAesUtil.VALID_KEY;
		redisTemplate.opsForValue().set(EncryptAesUtil.SYS_VALID_KEY,key);
	}
	/**
	 * 标记无效key
	 * @param key
	 */
	private void invalid(String key){
		key = key + ":" + EncryptAesUtil.INVALID_KEY;
		redisTemplate.opsForValue().set(EncryptAesUtil.SYS_VALID_KEY,key);
	}
}
