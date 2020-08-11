package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.io.IoUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 加密参数流的处理
 *
 * @author phz
 * @date 2020-07-22 09:45:17
 * @since V1.0
 */
public class SecretHttpInputMessage implements HttpInputMessage {

	/**
	 * http 头部参数
	 */
	private HttpHeaders headers;

	/**
	 * 输入流
	 */
	private InputStream body;

	/**
	 * appid
	 */
	private String appid;

	/**
	 * 密钥
	 */
	private String secretKey;


	/**
	 * @param body    输入流
	 * @param headers http 头部参数
	 * @param appid   appid
	 */
	public SecretHttpInputMessage(InputStream body, HttpHeaders headers, String appid, String secretKey) {
		this.body = body;
		this.headers = headers;
		this.appid = appid;
		this.secretKey = secretKey;
	}

	/**
	 * @return 处理后的参数流
	 * @throws IOException io异常
	 */
	@Override
	public InputStream getBody() throws IOException {
		String dbcSecret = headers.getFirst(CommonWebConstants.SIMPLE_RPC_SECRET);
		if (!CommonWebConstants.SIMPLE_RPC_SECRET.equals(dbcSecret)) {
			return body;
		}
		String input = IoUtil.read(body, StandardCharsets.UTF_8);
		String content = CryptContent.getDecryptContent(input, appid, secretKey);
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * @return 返回http 头部信息
	 */
	@Override
	public HttpHeaders getHeaders() {
		return headers;
	}
}