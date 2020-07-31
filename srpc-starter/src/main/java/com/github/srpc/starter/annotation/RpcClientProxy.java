package com.github.srpc.starter.annotation;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.annotation.AdviceMode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description: Client 代理
 * @author: phz
 * @create: 2020-07-28 09:44:27
 */
public class RpcClientProxy<T> implements InvocationHandler {
	private final RpcClientTarget<T> target;

	public RpcClientProxy(RpcClientTarget<T> target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return target.invoke(proxy, method, args);
	}

	@SuppressWarnings("unchecked")
	public T getProxy(AdviceMode mode) {
		if (AdviceMode.PROXY.equals(mode)) {
			return (T) Proxy.newProxyInstance(RpcClientFactoryBean.class.getClassLoader(), new Class[]{target.getProxyInterface()}, this);
		} else {
			// 通过CGLIB动态代理获取代理对象的过程
			Enhancer enhancer = new Enhancer();
			// 设置enhancer对象的父类
			enhancer.setSuperclass(target.getProxyInterface());
			// 设置enhancer的回调对象
			enhancer.setCallback(new RpcClientInterceptor<>(this.target));
			// 创建代理对象
			return (T) enhancer.create();
		}
	}
}
