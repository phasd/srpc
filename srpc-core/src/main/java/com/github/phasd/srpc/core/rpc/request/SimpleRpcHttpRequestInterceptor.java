package com.github.phasd.srpc.core.rpc.request;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.phasd.srpc.core.rpc.CommonWebConstants;
import com.github.phasd.srpc.core.rpc.CryptContent;
import com.github.phasd.srpc.core.rpc.RpcContext;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.github.phasd.srpc.core.rpc.SimpleRpcException;
import com.github.phasd.srpc.core.rpc.interceptor.RpcPreInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		String appid = StrUtil.EMPTY;
		if (CollectionUtil.isNotEmpty(rpcParams)) {
			appid = rpcParams.get(CommonWebConstants.APPID);
			rpcParams.forEach(headers::add);
		}
		headers.add(CommonWebConstants.APPID, appid);

		MediaType contentType = headers.getContentType();
		boolean multipart = MediaType.MULTIPART_FORM_DATA.includes(contentType);
		if (rpcConfig.isSecret() && !multipart) {
			String source = new String(bytes, StandardCharsets.UTF_8);
			String encryptContent;
			try {
				headers.add(CommonWebConstants.SIMPLE_RPC_SECRET, CommonWebConstants.SIMPLE_RPC_SECRET);
				encryptContent = CryptContent.getEncryptContent(source, appid, rpcConfig.getSecretKey());
			} catch (Exception e) {
				throw new SimpleRpcException("服务调用的链路上下文加密失败", e);
			}

			if (MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)) {
				long timestamp = System.currentTimeMillis();
				encryptContent = Base64.encodeUrlSafe(encryptContent, StandardCharsets.UTF_8);
				String inRawSign = String.format("data=%s&timestamp=%d", encryptContent, timestamp);
				String sign = CryptContent.getSign(inRawSign + CommonWebConstants.SIMPLE_RPC_SECRET_FROM);
				encryptContent = String.format("%s&sign=%s", inRawSign, sign);
				headers.add(CommonWebConstants.SIMPLE_RPC_SECRET_FROM, CommonWebConstants.SIMPLE_RPC_SECRET_FROM);
			}
			bytes = encryptContent.getBytes(StandardCharsets.UTF_8);
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
