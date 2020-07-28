package com.github.srpc.core.rpc.response;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.srpc.core.rpc.CommonWebConstants;
import com.github.srpc.core.rpc.RpcContext;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-22 16:27:17
 */
public abstract class AbstractResponseExtractor<T> implements ResponseExtractor<T> {
	@Override
	public T extractData(ClientHttpResponse response) throws IOException {
		String dbcSecret = response.getHeaders().getFirst(CommonWebConstants.SIMPLE_RPC_SECRET);
		if (!CommonWebConstants.SIMPLE_RPC_SECRET.equals(dbcSecret)) {
			return extractData(response, false);
		}
		return extractData(response, true);
	}

	protected abstract TypeReference<T> buildType();

	private T extractData(ClientHttpResponse response, boolean secret) throws IOException {
		ClientHttpResponseWrapper responseWrapper;
		if (secret) {
			String appId = RpcContext.getHeader(CommonWebConstants.APPID);
			responseWrapper = new SecretClientResponseWrapper(response, appId);
		} else {
			responseWrapper = new NoSecretClientResponseWrapper(response);
		}

		if (!responseWrapper.hasMessageBody() || responseWrapper.hasEmptyMessageBody()) {
			return null;
		}
		InputStream body = responseWrapper.getBody();
		String readContent = IoUtil.read(body, StandardCharsets.UTF_8);
		if (readContent == null) {
			return null;
		}
		return JSON.parseObject(readContent, buildType());
	}
}
