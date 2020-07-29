package com.github.srpc.core.rpc.response;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * @description: 数组返回值
 * @author: phz
 * @create: 2020-07-21 13:27:55
 */
public class SimpleRpcArrayResponseExtractor<T> extends AbstractResponseExtractor<List<T>> {
	private final Class<T> responseClass;

	public SimpleRpcArrayResponseExtractor(Class<T> responseClass) {
		this.responseClass = responseClass;
	}
	
	@Override
	protected List<T> getRes(String content) {
		return JSON.parseArray(content, responseClass);
	}
}