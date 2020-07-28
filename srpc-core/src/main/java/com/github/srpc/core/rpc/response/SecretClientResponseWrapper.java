package com.github.srpc.core.rpc.response;

import com.github.srpc.core.rpc.SecretHttpInputMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-22 10:39:01
 */
public class SecretClientResponseWrapper extends AbstractClientHttpResponseWrapper implements ClientHttpResponseWrapper {
	private HttpInputMessage httpInputMessage;

	public SecretClientResponseWrapper(ClientHttpResponse response, String appId) throws IOException {
		super(response);
		this.httpInputMessage = new SecretHttpInputMessage(super.getBody(), response.getHeaders(), appId);
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.httpInputMessage.getHeaders();
	}

	@Override
	public InputStream getBody() throws IOException {
		return this.httpInputMessage.getBody();
	}
}
