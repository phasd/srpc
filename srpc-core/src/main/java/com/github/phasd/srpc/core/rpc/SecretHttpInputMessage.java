package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.io.IoUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @description: secret HttpInputMessage
 * @author: phz
 * @create: 2020-07-22 09:45:17
 */
public class SecretHttpInputMessage implements HttpInputMessage {
	private HttpHeaders headers;
	private InputStream body;
	private String appid;

	public SecretHttpInputMessage(InputStream body, HttpHeaders headers, String appid) {
		this.body = body;
		this.headers = headers;
		this.appid = appid;
	}

	@Override
	public InputStream getBody() throws IOException {
		String dbcSecret = headers.getFirst(CommonWebConstants.SIMPLE_RPC_SECRET);
		if (!CommonWebConstants.SIMPLE_RPC_SECRET.equals(dbcSecret)) {
			return body;
		}
		String input = IoUtil.read(body, StandardCharsets.UTF_8);
		String content = CryptContent.getDecryptContent(input, appid);
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public HttpHeaders getHeaders() {
		return headers;
	}
}