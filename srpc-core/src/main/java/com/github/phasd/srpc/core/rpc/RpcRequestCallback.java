package com.github.phasd.srpc.core.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 请求参数处理回调
 *
 * @author phz
 * @date 2020-07-22 09:45:17
 * @since V1.0
 */
public class RpcRequestCallback implements RequestCallback {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcRequestCallback.class);


	/**
	 * 返回参数类型，用于设置请求头中 accept信息
	 */
	private final Type responseType;

	/**
	 * 请求参数，用于RequestBody的处理
	 */
	private final RequestEntity<?> requestEntity;

	/**
	 * 请求参数，用于RequestParam的处理
	 */
	private final HttpEntity<?> httpEntity;

	/**
	 * 消息转换器
	 */
	private final List<HttpMessageConverter<?>> messageConverters;

	/**
	 * @param requestEntity     请求参数，用于RequestBody的处理
	 * @param httpEntity        请求参数，用于RequestParam的处理
	 * @param responseType      返回参数类型，用于设置请求头中 accept信息
	 * @param messageConverters 消息转换器
	 */
	RpcRequestCallback(RequestEntity<?> requestEntity, HttpEntity<?> httpEntity, Type responseType, List<HttpMessageConverter<?>> messageConverters) {
		this.requestEntity = requestEntity;
		this.messageConverters = messageConverters;
		this.responseType = responseType;
		this.httpEntity = httpEntity;
	}

	/**
	 * @param httpRequest http请求
	 * @throws IOException io 异常
	 * @see RequestCallback
	 */
	@Override
	public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
		headerRequest(httpRequest);
		if (this.httpEntity != null) {
			processHttpEntity(httpRequest);
		} else if (this.requestEntity != null) {
			processRequestBody(httpRequest);
		}
	}

	/**
	 * 处理HttpEntity
	 *
	 * @param httpRequest http请求
	 * @throws IOException io 异常
	 */
	private void processHttpEntity(ClientHttpRequest httpRequest) throws IOException {
		Object requestBody = this.httpEntity.getBody();
		if (requestBody == null) {
			HttpHeaders httpHeaders = httpRequest.getHeaders();
			HttpHeaders requestHeaders = this.httpEntity.getHeaders();
			if (!requestHeaders.isEmpty()) {
				requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
			}
			if (httpHeaders.getContentLength() < 0) {
				httpHeaders.setContentLength(0L);
			}
		} else {
			Class<?> requestBodyClass = requestBody.getClass();
			Type requestBodyType = (this.httpEntity instanceof RequestEntity ?
					((RequestEntity<?>) this.httpEntity).getType() : requestBodyClass);
			HttpHeaders httpHeaders = httpRequest.getHeaders();
			HttpHeaders requestHeaders = this.httpEntity.getHeaders();
			processByMessageConvert(httpRequest, requestBody, requestBodyClass, requestBodyType, httpHeaders, requestHeaders);
		}
	}

	/**
	 * 设置头部 accept
	 *
	 * @param httpRequest http请求
	 */
	private void headerRequest(ClientHttpRequest httpRequest) {
		if (this.responseType != null && !Void.TYPE.equals(this.responseType)) {
			Class<?> responseClass = null;
			if (this.responseType instanceof Class) {
				responseClass = (Class<?>) this.responseType;
			}
			List<MediaType> allSupportedMediaTypes = new ArrayList<>();
			for (HttpMessageConverter<?> converter : messageConverters) {
				if (responseClass != null) {
					if (converter.canRead(responseClass, null)) {
						allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
					}
				} else if (converter instanceof GenericHttpMessageConverter) {
					GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
					if (genericConverter.canRead(this.responseType, null, null)) {
						allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
					}
				}
			}
			if (!allSupportedMediaTypes.isEmpty()) {
				MediaType.sortBySpecificity(allSupportedMediaTypes);
				httpRequest.getHeaders().setAccept(allSupportedMediaTypes);
			}
		}
	}

	/**
	 * 设置头部 accept
	 *
	 * @param httpRequest http请求
	 */
	private void processRequestBody(ClientHttpRequest httpRequest) throws IOException {
		httpRequest.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
		Object requestBody = this.requestEntity.getBody();
		if (requestBody == null) {
			HttpHeaders httpHeaders = httpRequest.getHeaders();
			HttpHeaders requestHeaders = this.requestEntity.getHeaders();
			if (!requestHeaders.isEmpty()) {
				requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
			}
			if (httpHeaders.getContentLength() < 0) {
				httpHeaders.setContentLength(0L);
			}
		} else {
			Class<?> requestBodyClass = requestBody.getClass();
			Type requestBodyType = this.requestEntity.getType();
			HttpHeaders httpHeaders = httpRequest.getHeaders();
			HttpHeaders requestHeaders = this.requestEntity.getHeaders();
			processByMessageConvert(httpRequest, requestBody, requestBodyClass, requestBodyType, httpHeaders, requestHeaders);
		}
	}

	/**
	 * 获取支持的没替类型
	 *
	 * @param messageConverter 消息转换器
	 * @return List<MediaType>
	 */
	private List<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
		List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
		List<MediaType> result = new ArrayList<>(supportedMediaTypes.size());
		for (MediaType supportedMediaType : supportedMediaTypes) {
			if (supportedMediaType.getCharset() != null) {
				supportedMediaType = new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
			}
			result.add(supportedMediaType);
		}
		return result;
	}


	/**
	 * 处理请求参数
	 *
	 * @param httpRequest      http请求
	 * @param requestBody      请求体
	 * @param requestBodyClass 请求数据类型
	 * @param requestBodyType  请求数据type
	 * @param httpHeaders      参数中的 请求头
	 * @param requestHeaders   http 请求头
	 * @throws IOException IO异常
	 */
	@SuppressWarnings("unchecked")
	private void processByMessageConvert(ClientHttpRequest httpRequest, Object requestBody, Class<?> requestBodyClass, Type requestBodyType, HttpHeaders httpHeaders, HttpHeaders requestHeaders) throws IOException {
		MediaType requestContentType = requestHeaders.getContentType();
		for (HttpMessageConverter<?> messageConverter : messageConverters) {
			if (messageConverter instanceof GenericHttpMessageConverter) {
				GenericHttpMessageConverter<Object> genericConverter =
						(GenericHttpMessageConverter<Object>) messageConverter;
				if (genericConverter.canWrite(requestBodyType, requestBodyClass, requestContentType)) {
					if (!requestHeaders.isEmpty()) {
						requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
					}
					genericConverter.write(requestBody, requestBodyType, requestContentType, httpRequest);
					return;
				}
			} else if (messageConverter.canWrite(requestBodyClass, requestContentType)) {
				if (!requestHeaders.isEmpty()) {
					requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
				}
				((HttpMessageConverter<Object>) messageConverter).write(requestBody, requestContentType, httpRequest);
				return;
			}
		}
		LOGGER.error("不可以处理Request: 没有合适的MessageConvert 去处理 {}", requestBodyClass.getName());
		throw new RestClientException("不可以处理Request: 没有合适的MessageConvert 去处理 " + requestBodyClass.getName());
	}
}
