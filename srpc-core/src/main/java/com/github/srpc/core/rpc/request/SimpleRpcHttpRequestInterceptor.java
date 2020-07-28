package com.github.srpc.core.rpc.request;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.srpc.core.rpc.CommonWebConstants;
import com.github.srpc.core.rpc.CryptContent;
import com.github.srpc.core.rpc.RpcContext;
import com.github.srpc.core.rpc.SimpleRpcException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @description: 用于RPC调用上下文参数传递
 * @author: phz
 * @create: 2020-07-20 14:16:16
 */
public class SimpleRpcHttpRequestInterceptor implements ClientHttpRequestInterceptor {
	private boolean secret;

	public SimpleRpcHttpRequestInterceptor(boolean secret) {
		this.secret = secret;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
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
		if (secret && !multipart) {
			String source = new String(bytes, StandardCharsets.UTF_8);
			String encryptContent;
			try {
				headers.add(CommonWebConstants.SIMPLE_RPC_SECRET, CommonWebConstants.SIMPLE_RPC_SECRET);
				encryptContent = CryptContent.getEncryptContent(source, appid);
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
}
