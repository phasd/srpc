package com.github.phasd.srpc.core.rpc.response;

import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;


/**
 * @description: ClientHttpResponseWrapper
 * @author: phz
 * @create: 2020-07-22 08:44:52
 */
public interface ClientHttpResponseWrapper extends ClientHttpResponse {
	/**
	 * @return 是否有返回消息体
	 * @throws IOException
	 */
	boolean hasMessageBody() throws IOException;

	/**
	 * @return 是否是空的消息体
	 * @throws IOException
	 */
	boolean hasEmptyMessageBody() throws IOException;
}
