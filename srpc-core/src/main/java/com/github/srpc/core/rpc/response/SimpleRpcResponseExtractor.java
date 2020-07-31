package com.github.srpc.core.rpc.response;

import cn.hutool.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

/**
 * @description: 单对象返回值
 * @author: phz
 * @create: 2020-07-21 13:27:55
 */
public class SimpleRpcResponseExtractor<T> extends AbstractResponseExtractor<T> {
	private final Type responseClass;

	public SimpleRpcResponseExtractor(Type responseClass) {
		this.responseClass = responseClass;
	}

	@Override
	protected T getRes(String content) {
		if (String.class.isAssignableFrom(TypeUtil.getClass(responseClass))) {
			return (T) content;
		}
		return JSON.parseObject(content, getOuterType(responseClass));
	}
}