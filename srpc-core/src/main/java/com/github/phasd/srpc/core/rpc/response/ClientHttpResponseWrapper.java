package com.github.phasd.srpc.core.rpc.response;

import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * ClientHttpResponseWrapper
 *
 * @author phz
 * @date 2020-07-22 08:44:52
 * @since V1.0
 */
public interface ClientHttpResponseWrapper extends ClientHttpResponse {
	/**
	 * @return 是否有返回消息体
	 * @throws IOException IO异常
	 */
	boolean hasMessageBody() throws IOException;

	/**
	 * @return 是否是空的消息体
	 * @throws IOException IO异常
	 */
	boolean hasEmptyMessageBody() throws IOException;
}
