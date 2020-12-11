package com.github.phasd.srpc.core.rpc.request;

import cn.hutool.core.collection.CollectionUtil;
import com.github.phasd.srpc.core.rpc.RpcContext;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.github.phasd.srpc.core.rpc.interceptor.RpcPreInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用于RPC调用上下文参数传递
 *
 * @author phz
 * @date 2020-07-20 14:16:16
 * @since V1.0
 */
public class SimpleRpcHttpRequestInterceptor implements ClientHttpRequestInterceptor {
	/**
	 * 参数配置
	 */
	private final SimpleRpcConfigurationProperties rpcConfig;

	/**
	 * 前置拦截
	 */
	private final List<RpcPreInterceptor> preInterceptors;

	/**
	 * @param rpcConfig       参数配置
	 * @param preInterceptors 前置拦截
	 */
	public SimpleRpcHttpRequestInterceptor(SimpleRpcConfigurationProperties rpcConfig, List<RpcPreInterceptor> preInterceptors) {
		this.rpcConfig = rpcConfig;
		this.preInterceptors = preInterceptors;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		handlePreInterceptors(httpRequest, bytes);
		HttpHeaders headers = httpRequest.getHeaders();
		Map<String, String> rpcParams = RpcContext.getHeaders();
		if (CollectionUtil.isNotEmpty(rpcParams)) {
			rpcParams.forEach(headers::add);
		}
		return clientHttpRequestExecution.execute(httpRequest, bytes);
	}


	/**
	 * @param httpRequest http 请求
	 * @param bytes       请求体
	 */
	private void handlePreInterceptors(HttpRequest httpRequest, byte[] bytes) {
		if (CollectionUtil.isEmpty(this.preInterceptors)) {
			return;
		}
		Request request = RpcContext.getRequest();
		for (RpcPreInterceptor preInterceptor : this.preInterceptors) {
			if (!preInterceptor.preSupports(request)) {
				continue;
			}
			preInterceptor.preInterceptor(httpRequest);
		}
	}
}
