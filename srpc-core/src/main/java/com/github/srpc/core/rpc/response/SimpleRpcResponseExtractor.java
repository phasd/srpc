package com.github.srpc.core.rpc.response;

import com.alibaba.fastjson.TypeReference;

/**
 * @description: 单对象返回值
 * @author: phz
 * @create: 2020-07-21 13:27:55
 */
public class SimpleRpcResponseExtractor<T> extends AbstractResponseExtractor<T> {
	private final Class<T> responseClass;

	public SimpleRpcResponseExtractor(Class<T> responseClass) {
		this.responseClass = responseClass;
	}

	@Override
	protected TypeReference<T> buildType() {
		return new TypeReference<T>(responseClass) {};
	}
}