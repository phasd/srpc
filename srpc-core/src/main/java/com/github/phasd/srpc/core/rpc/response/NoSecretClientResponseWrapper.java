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
public class NoSecretClientResponseWrapper extends AbstractClientHttpResponseWrapper {
	public NoSecretClientResponseWrapper(ClientHttpResponse response) throws IOException {
		super(response);
	}
}
