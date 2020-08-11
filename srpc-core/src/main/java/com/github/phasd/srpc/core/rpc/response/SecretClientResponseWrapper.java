package com.github.phasd.srpc.core.rpc.response;

import com.github.phasd.srpc.core.rpc.SecretHttpInputMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * SecretClientResponseWrapper
 *
 * @author phz
 * @date 2020-07-22 10:39:01
 * @since V1.0
 */
public class SecretClientResponseWrapper extends AbstractClientHttpResponseWrapper implements ClientHttpResponseWrapper {

	/**
	 * httpInputMessage
	 */
	private HttpInputMessage httpInputMessage;

	/**
	 * @param response  http 返回
	 * @param appId     appId
	 * @param secretKey 密钥
	 * @throws IOException IO异常
	 */
	public SecretClientResponseWrapper(ClientHttpResponse response, String appId, String secretKey) throws IOException {
		super(response);
		this.httpInputMessage = new SecretHttpInputMessage(super.getBody(), response.getHeaders(), appId, secretKey);
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
