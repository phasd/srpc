package com.github.srpc.core.rpc.response;

import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-22 10:53:15
 */
public class NoSecretClientResponseWrapper extends AbstractClientHttpResponseWrapper {
	public NoSecretClientResponseWrapper(ClientHttpResponse response) throws IOException {
		super(response);
	}
}
