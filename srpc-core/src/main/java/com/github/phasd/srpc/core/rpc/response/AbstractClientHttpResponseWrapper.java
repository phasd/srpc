package com.github.phasd.srpc.core.rpc.response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * AbstractClientHttpResponseWrapper
 *
 * @author phz
 * @date 2020-07-22 10:55:50
 * @since V1.0
 */
public abstract class AbstractClientHttpResponseWrapper implements ClientHttpResponseWrapper {
	/**
	 * http返回
	 */
	protected final ClientHttpResponse response;

	/**
	 * PushbackInputStream
	 */
	private PushbackInputStream pushbackInputStream;

	/**
	 * @param response http返回
	 * @throws IOException IO异常
	 */
	AbstractClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
		this.response = response;
		initInputStream(response);
	}

	@Override
	public boolean hasMessageBody() throws IOException {
		HttpStatus status = HttpStatus.resolve(getRawStatusCode());
		if (status != null && (status.is1xxInformational() || status == HttpStatus.NO_CONTENT ||
				status == HttpStatus.NOT_MODIFIED)) {
			return false;
		}
		if (getHeaders().getContentLength() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean hasEmptyMessageBody() throws IOException {
		int b = this.pushbackInputStream.read();
		if (b == -1) {
			return true;
		} else {
			this.pushbackInputStream.unread(b);
			return false;
		}
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.response.getHeaders();
	}

	@Override
	public InputStream getBody() throws IOException {
		return this.pushbackInputStream;
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return this.response.getStatusCode();
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return this.response.getRawStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return this.response.getStatusText();
	}

	@Override
	public void close() {
		this.response.close();
	}


	/**
	 * 初始化流
	 *
	 * @param response 客户端返回
	 * @throws IOException IO异常
	 */
	private void initInputStream(ClientHttpResponse response) throws IOException {
		InputStream body = response.getBody();
		this.pushbackInputStream = new PushbackInputStream(body);
	}
}
