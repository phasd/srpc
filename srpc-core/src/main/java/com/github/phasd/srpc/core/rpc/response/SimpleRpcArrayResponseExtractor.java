package com.github.phasd.srpc.core.rpc.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.github.phasd.srpc.core.rpc.interceptor.RpcPostInterceptor;
import com.github.phasd.srpc.core.rpc.request.Request;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 数组返回值
 *
 * @author phz
 * @date 2020-07-21 13:27:55
 * @since V1.0
 */
public class SimpleRpcArrayResponseExtractor<T> extends AbstractResponseExtractor<List<T>> {
	/**
	 * 返回值类别
	 */
	private final Type responseClass;

	/**
	 * @param request             请求参数
	 * @param postInterceptorList 后置拦截器
	 * @param responseClass       返回值type
	 * @param secretKey           密钥
	 */
	public SimpleRpcArrayResponseExtractor(Request request, List<RpcPostInterceptor> postInterceptorList, Type responseClass, String secretKey) {
		super(request, postInterceptorList, secretKey);
		this.responseClass = responseClass;
	}

	@Override
	protected List<T> getRes(String content) {
		return JSON.parseObject(content, new ParameterizedTypeImpl(new Type[]{getOuterType(responseClass)}, null, List.class));
	}
}