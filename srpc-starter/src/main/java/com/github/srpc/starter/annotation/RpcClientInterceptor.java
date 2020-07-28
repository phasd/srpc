package com.github.srpc.starter.annotation;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 15:49:00
 */
public class RpcClientInterceptor<T> implements MethodInterceptor {
	private final RpcClientTarget<T> target;

	public RpcClientInterceptor(RpcClientTarget<T> target) {
		this.target = target;
	}

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		return this.target.invoke(proxy, method, args);
	}
}
