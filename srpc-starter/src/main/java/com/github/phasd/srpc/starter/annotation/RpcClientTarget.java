package com.github.phasd.srpc.starter.annotation;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Client的参数信息
 *
 * @author phz
 * @date 2020-07-28 10:24:11
 * @since V1.0
 */
public class RpcClientTarget<T> {
	private static final String EQUALS = "equals";
	private static final String HASH_CODE = "hashCode";
	private static final String TO_STRING = "toString";

	/**
	 * 代理接口
	 */
	private final Class<T> proxyInterface;

	/**
	 * 代理类注解
	 */
	private final RpcClient rpcClient;

	/**
	 * 方法对应的处理器
	 */
	private final Map<Method, RpcClientMethodHandler> dispatch;

	public RpcClientTarget(Class<T> proxyInterface, RpcClient rpcClient, Map<Method, RpcClientMethodHandler> dispatch) {
		this.proxyInterface = proxyInterface;
		this.dispatch = dispatch;
		this.rpcClient = rpcClient;
	}

	public Class getProxyInterface() {
		return proxyInterface;
	}

	public String getBaseUrl() {
		return rpcClient.baseUrl();
	}

	public RpcClient getRpcClient() {
		return rpcClient;
	}

	public Map<Method, RpcClientMethodHandler> getDispatch() {
		return dispatch;
	}

	/**
	 * 代理方法的执行
	 *
	 * @param proxy  被代理的对象
	 * @param method 方法
	 * @param args   参数
	 * @return 执行结果
	 */
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
