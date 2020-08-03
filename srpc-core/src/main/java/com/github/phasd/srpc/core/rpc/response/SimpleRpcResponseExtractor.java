package com.github.phasd.srpc.core.rpc.response;

import cn.hutool.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import com.github.phasd.srpc.core.rpc.request.Request;
import com.github.phasd.srpc.core.rpc.interceptor.RpcPostInterceptor;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @description: 单对象返回值
 * @author: phz
 * @create: 2020-07-21 13:27:55
 */
public class SimpleRpcResponseExtractor<T> extends AbstractResponseExtractor<T> {
	private final Type responseClass;

	public SimpleRpcResponseExtractor(Request request, List<RpcPostInterceptor> postInterceptorList, Type responseClass) {
		super(request, postInterceptorList);
		this.responseClass = responseClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T getRes(String content) {
		if (String.class.isAssignableFrom(TypeUtil.getClass(responseClass))) {
			return (T)content;
		}
		return JSON.parseObject(content, getOuterType(responseClass));
	}
}