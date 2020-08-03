package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.util.StrUtil;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 11:48:55
 */
public class RpcUtils {
	public static final String URL_DELIMITER = "/";

	private RpcUtils() {}

	public static String trimUrlDelimiter(String url) {
		return trimSymbol(url, URL_DELIMITER);
	}

	public static String trimSymbol(String value, String symbol) {
		if (StrUtil.hasBlank(value, symbol)) {
			return value;
		}
		value = StrUtil.removePrefix(value, symbol);
		value = StrUtil.removeSuffix(value, symbol);
		return value;
	}
}
