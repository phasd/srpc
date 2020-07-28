package com.github.srpc.core.rpc.response;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;
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
	protected TypeReference<List<T>> buildType() {
		ParameterizedTypeImpl inner = new ParameterizedTypeImpl(new Type[]{responseClass}, null, List.class);
		return new TypeReference<List<T>>(inner) {};
	}
}