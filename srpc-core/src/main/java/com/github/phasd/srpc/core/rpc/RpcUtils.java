package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.util.StrUtil;

/**
 * 通用utils
 *
 * @author phz
 * @date 2020-07-28 11:48:55
 * @since V1.0
 */
public class RpcUtils {
	/**
	 * url分隔符
	 */
	public static final String URL_DELIMITER = "/";

	private RpcUtils() {}


	/**
	 * 字符串的头部和尾部移除url分隔符
	 *
	 * @param url 字符串
	 * @return 处理后的字符串
	 */
	public static String trimUrlDelimiter(String url) {
		return trimSymbol(url, URL_DELIMITER);
	}


	/**
	 * 字符串的头部和尾部移除标识符
	 *
	 * @param value  要出理的字符串
	 * @param symbol 标识符
	 * @return 处理后的字符串
	 */
	public static String trimSymbol(String value, String symbol) {
		if (StrUtil.hasBlank(value, symbol)) {
			return value;
		}
		value = StrUtil.removePrefix(value, symbol);
		value = StrUtil.removeSuffix(value, symbol);
		return value;
	}
}
