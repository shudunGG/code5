package org.springblade.integrated.platform.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取地址类
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip)
    {
        // 内网不查询
        if (IpUtils.internalIp(ip))
        {
            return "内网IP";
        }
		try
		{
			String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
			if (StringUtils.isEmpty(rspStr))
			{
				log.error("获取地理位置异常 {}", ip);
				return UNKNOWN;
			}
			JSONObject obj = JSONObject.parseObject(rspStr);
			String region = obj.getString("pro");
			String city = obj.getString("city");
			return String.format("%s %s", region, city);
		}
		catch (Exception e)
		{
			log.error("获取地理位置异常 {}", e);
		}

        return UNKNOWN;
    }
}
