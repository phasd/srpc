package com.github.phasd.srpc.core.rpc.response;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.github.phasd.srpc.core.rpc.interceptor.RpcPostInterceptor;
import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AbstractResponseExtractor
 *
 * @author phz
 * @date 2020-07-22 16:27:17
 * @since V1.0
 */
public abstract class AbstractResponseExtractor<T> implements ResponseExtractor<T> {

	/**
	 * type 缓存
	 */
	private static final SimpleCache<Type, Type> TYPE_CACHE = new SimpleCache<>();

	/**
	 * 后置拦截器
	 */
	private final List<RpcPostInterceptor> postInterceptorList;

	/**
	 * 原始请求
	 */
	private final Request request;

	/**
	 * @param request             原始Request
	 * @param postInterceptorList 后置拦截器
	 */
	AbstractResponseExtractor(Request request, List<RpcPostInterceptor> postInterceptorList) {
		this.request = request;
		this.postInterceptorList = postInterceptorList;
	}

	@Override
	public T extractData(ClientHttpResponse response) throws IOException {
		ClientHttpResponseWrapper responseWrapper = new DefaultClientResponseWrapper(response);
		if (!responseWrapper.hasMessageBody() || responseWrapper.hasEmptyMessageBody()) {
			return null;
		}
		InputStream body = responseWrapper.getBody();
		String readContent = IoUtil.read(body, StandardCharsets.UTF_8);

		readContent = handlePostInterceptors(response.getHeaders(), readContent);
		if (readContent == null) {
			return null;
		}
		return getRes(readContent);
	}


	/**
	 * 获取返回值
	 *
	 * @param content 请求返回
	 * @return 返回结果
	 */
	protected abstract T getRes(String content);

	/**
	 * 获取返回值的type
	 *
	 * @param type 返回值类型
	 * @return 返回值的type
	 */
	Type getOuterType(Type type) {
		Type cachedType = TYPE_CACHE.get(type);
		if (cachedType == null) {
			return TYPE_CACHE.put(type, completeRealType(type));
		}
		return cachedType;
	}

	/**
	 * 根据是否是ParameterizedType 递归处理
	 *
	 * @param type 返回值类型
	 * @return 返回值的type
	 */
	private Type completeRealType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = ((ParameterizedType) type);
			Type[] argumentTypeArr = parameterizedType.getActualTypeArguments();
			if (ArrayUtil.isNotEmpty(argumentTypeArr)) {
				for (int i = 0; i < argumentTypeArr.length; i++) {
					Type argument = argumentTypeArr[i];
					argumentTypeArr[i] = completeRealType(argument);
				}
			}
			return new ParameterizedTypeImpl(argumentTypeArr, null, parameterizedType.getRawType());
		}
		return type;
	}

	/**
	 * 后置处理
	 *
	 * @param headers 返回头部
	 * @param content 返回内容
	 * @return 返回内容
	 */
	private String handlePostInterceptors(HttpHeaders headers, String content) {
		if (CollectionUtil.isEmpty(this.postInterceptorList)) {
			return content;
		}

		for (RpcPostInterceptor rpcPostInterceptor : this.postInterceptorList) {
			if (!rpcPostInterceptor.postSupports(request, headers)) {
				continue;
			}
			content = rpcPostInterceptor.postInterceptor(request, headers, content);
		}
		return content;
	}
}
