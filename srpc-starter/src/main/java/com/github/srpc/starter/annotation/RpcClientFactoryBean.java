package com.github.srpc.starter.annotation;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-27 18:41:32
 */
public class RpcClientFactoryBean<T> implements FactoryBean<T>, InitializingBean {
	private Class<T> proxyInterface;
	private AdviceMode mode;

	public RpcClientFactoryBean() {
	}

	public RpcClientFactoryBean(Class<T> proxyInterface, AdviceMode mode) {
		this.proxyInterface = proxyInterface;
		this.mode = mode;
	}

	@Override
	public T getObject() throws Exception {
		RpcClient rpcClient = AnnotationUtils.findAnnotation(proxyInterface, RpcClient.class);
		if (rpcClient == null) {
			throw new IllegalArgumentException("RpcClientFactoryBean 代理接口必须被RpcClient标注");
		}
		Map<Method, RpcClientMethodHandler> methodHandlerMap = new LinkedHashMap<>();
		Method[] methods = proxyInterface.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Rpc.class)) {
				Rpc rpc = AnnotationUtils.findAnnotation(method, Rpc.class);
				RpcClientMethodHandler rpcClientMethodHandler = new RpcClientMethodHandler(rpc, method);
				methodHandlerMap.put(method, rpcClientMethodHandler);
			}
		}
		RpcClientTarget<T> rpcClientTarget = new RpcClientTarget<>(proxyInterface, rpcClient.baseUrl(), methodHandlerMap);
		RpcClientProxy<T> rpcClientProxy = new RpcClientProxy<>(rpcClientTarget);
		return rpcClientProxy.getProxy(mode);
	}

	@Override
	public Class<T> getObjectType() {
		return this.proxyInterface;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(proxyInterface, "需要代理的接口不能为空");
	}
}
