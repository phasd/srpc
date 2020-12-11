package com.github.phasd.srpc.core.rpc.response;

import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * NoSecretClientResponseWrapper
 *
 * @author phz
 * @date 2020-07-22 10:53:15
 * @since V1.0
 */
public class DefaultClientResponseWrapper extends AbstractClientHttpResponseWrapper {
	public DefaultClientResponseWrapper(ClientHttpResponse response) throws IOException {
		super(response);
	}
}
