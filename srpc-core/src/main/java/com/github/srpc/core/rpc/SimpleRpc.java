package com.github.srpc.core.rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.github.srpc.core.rpc.executor.RpcThreadPoolExecutor;
import com.github.srpc.core.rpc.request.Request;
import com.github.srpc.core.rpc.response.SimpleRpcArrayResponseExtractor;
import com.github.srpc.core.rpc.response.SimpleRpcResponseExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-20 11:53:20
 */
@Slf4j
public class SimpleRpc extends AbstractRestRpc implements InitializingBean, DisposableBean {

	private ExecutorService executor;

	public SimpleRpc(RestTemplate restTemplate, SimpleRpcConfigurationProperties rpcConfig) {
		super(restTemplate, rpcConfig);
	}

	public <T> T getForObject(Request<?> request, Class<T> responseType) {
		return doExecute(request, responseType);
	}

	public <T> List<T> getForList(Request<?> request, Class<T> responseType) {
		return doExecuteArray(request, responseType);
	}


	public <T> CompletableFuture<T> getForObjectAsync(Request<?> request, Class<T> responseType) {
		return doExecuteAsync(request, responseType);
	}

	public <T> CompletableFuture<List<T>> getForListAsync(Request<?> request, Class<T> responseType) {
		return doExecuteArrayAsync(request, responseType);
	}


	@Override
	public <T> T doExecute(Request<?> request, Class<T> responseType) {
		try {
			RpcContext.initContext(rpcConfig);
			SimpleRpcResponseExtractor<T> responseExtractor = new SimpleRpcResponseExtractor<>(responseType);
			return getResponse(request, responseExtractor, responseType);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> List<T> doExecuteArray(Request<?> request, Class<T> responseType) {
		try {
			RpcContext.initContext(rpcConfig);
			SimpleRpcArrayResponseExtractor<T> responseExtractor = new SimpleRpcArrayResponseExtractor<>(responseType);
			return getResponse(request, responseExtractor, responseType);

		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> CompletableFuture<T> doExecuteAsync(Request<?> request, Class<T> responseType) {
		try {
			RpcContext.initContext(rpcConfig);
			return CompletableFuture.supplyAsync(() -> {
				try {
					return doExecute(request, responseType);
				} catch (Exception e) {
					log.error("SimpleRpc 服务调用异常:", e);
					throw new SimpleRpcException(e);
				}
			}, executor);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> CompletableFuture<List<T>> doExecuteArrayAsync(Request<?> request, Class<T> responseType) {
		try {
			RpcContext.initContext(rpcConfig);
			return CompletableFuture.supplyAsync(() -> {
				try {
					return doExecuteArray(request, responseType);
				} catch (Exception e) {
					log.error("SimpleRpc 服务调用异常:", e);
					throw new SimpleRpcException(e);
				}
			}, executor);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public void destroy() throws Exception {
		if (executor != null) {
			executor.shutdown();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	private void init() {
		if (restTemplate == null) {
			throw new NullPointerException("Rpc 服务调用初始化 restTemplate 不能为空");
		}
		if (rpcConfig == null) {
			throw new NullPointerException("Rpc 服务调用配置 rpcConfig 不能为空");
		}
		executor = RpcThreadPoolExecutor.getInstance(rpcConfig);
	}

	private <T> T getResponse(Request<?> request, ResponseExtractor<T> responseExtractor, Class<?> responseType) {
		checkRequest(request);
		RequestEntity<?> requestEntity = getRequestEntity(request);
		HttpEntity<?> httpEntity = getHttpEntity(request);
		RpcRequestCallback rpcRequestCallback = new RpcRequestCallback(requestEntity, httpEntity, responseType, restTemplate.getMessageConverters());
		return restTemplate.execute(getUrl(request.getUrl()), request.getHttpMethod(), rpcRequestCallback,
				responseExtractor);
	}

	private void checkRequest(Request<?> request) {
		Assert.notNull(request, "Rpc 请求 request不能为null");
		Assert.notNull(request.getUrl(), "url不能为空");
		Assert.notNull(request.getHttpMethod(), "httpMethod不能为空");
		Assert.isFalse(CollectionUtil.isNotEmpty(request.getFormParam()) && Objects.nonNull(request.getBody()), "body和formParam不能同时设置");
	}

	private RequestEntity<?> getRequestEntity(Request<?> request) {
		if (formParam(request)) {
			return null;
		}
		String url = getUrl(request.getUrl());
		return new RequestEntity<>(request.getBody(), request.getHeaders(), request.getHttpMethod(), URI.create(url));
	}

	private HttpEntity<?> getHttpEntity(Request<?> request) {
		if (!formParam(request)) {
			return null;
		}
		MultiValueMap<String, String> headers = request.getHeaders();
		if (headers == null) {
			headers = new LinkedMultiValueMap<>();
		}

		if (request.isMultiPart()) {
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA.toString());
		} else {
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		return new HttpEntity<>(request.getFormParam(), headers);
	}

	private boolean formParam(Request<?> request) {
		return Objects.nonNull(request.getFormParam());
	}
}
