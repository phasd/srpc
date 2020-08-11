package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文
 *
 * @author phz
 * @date 2020-07-22 17:57:12
 * @since V1.0
 */
public class RpcContext {

	/**
	 * 请求头部信息
	 */
	private static final ThreadLocal<Map<String, String>> HEADERS = new ThreadLocal<>();

	/**
	 * 当前请求参数
	 */
	private static final ThreadLocal<Request> CURRENT_REQUEST = new ThreadLocal<>();

	private RpcContext() {}


	/**
	 * @return 请求头部参数
	 */
	public static Map<String, String> getHeaders() {
		return HEADERS.get();
	}

	/**
	 * 设置请亲头
	 */
	public static void setHeaders(Map<String, String> headers) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			HEADERS.set(headers);
		} else {
			map.putAll(headers);
		}
	}


	/**
	 * @return 获取当前请求的Request
	 */
	public static Request getRequest() {
		return CURRENT_REQUEST.get();
	}

	/**
	 * @param request 设置当前请求的Request
	 */
	public static void setRequest(Request request) {
		CURRENT_REQUEST.set(request);
	}

	/**
	 * 设置请求头部
	 *
	 * @param key   参数key
	 * @param value 参数value
	 */
	public static void setHeader(String key, String value) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			map = new HashMap<>();
			HEADERS.set(map);
		}
		map.put(key, value);
	}


	/**
	 * 根据key获取参数
	 *
	 * @param key 参数key
	 * @return 参数value
	 */
	public static String getHeader(String key) {
		Map<String, String> map = HEADERS.get();
		if (CollectionUtil.isEmpty(map)) {
			return null;
		}
		return map.get(key);
	}

	/**
	 * 上下文请求
	 */
	public static void clear() {
		HEADERS.remove();
		CURRENT_REQUEST.remove();
	}

	/**
	 * 初始化上下文
	 *
	 * @param rpcConfig      配置参数
	 * @param currentRequest 当前请求Request
	 */
	static void initContext(SimpleRpcConfigurationProperties rpcConfig, Request currentRequest) {
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
		RpcContext.setRequest(currentRequest);
	}
}
