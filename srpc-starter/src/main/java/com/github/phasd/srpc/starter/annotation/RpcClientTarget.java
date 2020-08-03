package com.github.phasd.srpc.starter.annotation;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 10:24:11
 */
public class RpcClientTarget<T> {
	private static final String EQUALS = "equals";
	private static final String HASH_CODE = "hashCode";
	private static final String TO_STRING = "toString";

	private final Class<T> proxyInterface;
	private final String baseUrl;
	private final Map<Method, RpcClientMethodHandler> dispatch;

	public RpcClientTarget(Class<T> proxyInterface, String baseUrl, Map<Method, RpcClientMethodHandler> dispatch) {
		this.proxyInterface = proxyInterface;
		this.baseUrl = baseUrl;
		this.dispatch = dispatch;
	}

	public Class getProxyInterface() {
		return proxyInterface;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Map<Method, RpcClientMethodHandler> getDispatch() {
		return dispatch;
	}

	public Object invoke(Object proxy, Method method, Object[] args) {
		String methodName = method.getName();
		if (EQUALS.equals(methodName)) {
			return method.equals(args[0]);
		}

		if (HASH_CODE.equals(methodName)) {
			return method.hashCode();
		}
		if (TO_STRING.equals(method.getName())) {
			return method.toString();
		}
		return this.dispatch.get(method).invoke(this, args);
	}
}
