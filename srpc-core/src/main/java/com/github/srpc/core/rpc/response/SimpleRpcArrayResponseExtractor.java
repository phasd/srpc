package com.github.srpc.core.rpc.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @description: 数组返回值
 * @author: phz
 * @create: 2020-07-21 13:27:55
 */
public class SimpleRpcArrayResponseExtractor<T> extends AbstractResponseExtractor<List<T>> {
	private final Type responseClass;

	public SimpleRpcArrayResponseExtractor(Type responseClass) {
		this.responseClass = responseClass;
	}

	@Override
	protected List<T> getRes(String content) {
		return JSON.parseObject(content, new ParameterizedTypeImpl(new Type[]{getOuterType(responseClass)}, null, List.class));
	}
}