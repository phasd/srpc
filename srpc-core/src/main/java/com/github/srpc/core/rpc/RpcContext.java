package com.github.srpc.core.rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-22 17:57:12
 */
public class RpcContext {
	private static final ThreadLocal<Map<String, String>> HEADERS = new ThreadLocal<>();

	private RpcContext() {}


	public static Map<String, String> getHeaders() {
		return HEADERS.get();
	}

	public static void setHeaders(Map<String, String> headers) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			HEADERS.set(headers);
		} else {
			map.putAll(headers);
		}
	}

	public static void setHeader(String key, String value) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			map = new HashMap<>();
			HEADERS.set(map);
		}
		map.put(key, value);
	}


	public static String getHeader(String key) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			return null;
		}
		return map.get(key);
	}

	public static void clear() {
		HEADERS.remove();
	}

	static void initContext(SimpleRpcConfigurationProperties rpcConfig) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = requestAttributes.getRequest();
			Map<String, String> headerMap = ServletUtil.getHeaderMap(request);
			String jsonStr = JSON.toJSONString(headerMap);
			Map<String, String> copyHeadMap = JSON.parseObject(jsonStr, new TypeReference<Map<String, String>>() {});
			copyHeadMap.putIfAbsent(CommonWebConstants.APPID, rpcConfig.getAppid());
			RpcContext.setHeaders(copyHeadMap);
		} else {
			RpcContext.setHeaders(Collections.singletonMap(CommonWebConstants.APPID, rpcConfig.getAppid()));
		}
	}
}
