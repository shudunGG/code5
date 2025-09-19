package org.springblade.common.utils;

import java.util.ConcurrentModificationException;

public class OrderNoUtils {

	private OrderNoUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 生成订单号
	 *
	 * @param productNo
	 * @return
	 */
	public static String createOrderNo(String productNo) {
		return DateTimeUtils.getTodayChar14() + productNo
			+ CommonUtil.createRandomNumber(6);
	}


	/**
	 * @param productNo
	 * @return
	 */
	public static String createVipBizNo(String productNo) {
		return "VIP" + DateTimeUtils.getTodayChar14() + productNo
			+ CommonUtil.createRandomNumber(4);
	}

	/**
	 * 生成流水号
	 * @param identifier
	 * @param randomLength
	 * @return
	 */
	public static String createRequestNo(String identifier,int randomLength) {
		return DateTimeUtils.getTodayChar17() + identifier
			+ CommonUtil.createRandomNumber(randomLength);
	}
}


