package com.github.srpc.core.rpc;

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
 * @description:
 * @author: phz
 * @create: 2020-07-22 09:45:17
 */
public class RpcRequestCallback implements RequestCallback {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcRequestCallback.class);
	private final Type responseType;
	private final RequestEntity<?> requestEntity;
	private final HttpEntity<?> httpEntity;
	private final List<HttpMessageConverter<?>> messageConverters;

	RpcRequestCallback(RequestEntity<?> requestEntity, HttpEntity<?> httpEntity, Type responseType, List<HttpMessageConverter<?>> messageConverters) {
		this.requestEntity = requestEntity;
		this.messageConverters = messageConverters;
		this.responseType = responseType;
		this.httpEntity = httpEntity;
	}

	@Override
	public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
		headerRequest(httpRequest);
		if (this.httpEntity != null) {
			processHttpEntity(httpRequest);
		} else if (this.requestEntity != null) {
			processRequestBody(httpRequest);
		}
	}

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
