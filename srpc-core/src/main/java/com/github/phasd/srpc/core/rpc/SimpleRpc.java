package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.github.phasd.srpc.core.rpc.executor.RpcThreadPoolExecutor;
import com.github.phasd.srpc.core.rpc.interceptor.SimpleRpcConfigurer;
import com.github.phasd.srpc.core.rpc.request.Request;
import com.github.phasd.srpc.core.rpc.request.SimpleRpcHttpRequestInterceptor;
import com.github.phasd.srpc.core.rpc.request.SkipLoadBalancedRequestInterceptor;
import com.github.phasd.srpc.core.rpc.response.SimpleRpcArrayResponseExtractor;
import com.github.phasd.srpc.core.rpc.response.SimpleRpcResponseExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author phz
 * @date 2020-07-28 11:48:55
 * @since V1.0
 */
@Slf4j
public class SimpleRpc extends AbstractRestRpc implements InitializingBean, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, DisposableBean {
	private ExecutorService executor;
	private ApplicationContext applicationContext;
	private final RestTemplate restTemplate;

	public SimpleRpc(SimpleRpcConfigurationProperties rpcConfig, RestTemplate restTemplate) {
		super(rpcConfig);
		this.restTemplate = restTemplate;
	}

	public void getForObject(Request<?> request) {
		doExecute(request, Void.TYPE);
	}

	public CompletableFuture<Void> getForObjectAsync(Request<?> request) {
		return doExecuteAsync(request, Void.TYPE);
	}

	public <T> T getForObject(Request<?> request, Type responseType) {
		return doExecute(request, responseType);
	}

	public <T> List<T> getForList(Request<?> request, Type responseType) {
		return doExecuteArray(request, responseType);
	}


	public <T> CompletableFuture<T> getForObjectAsync(Request<?> request, Type responseType) {
		return doExecuteAsync(request, responseType);
	}

	public <T> CompletableFuture<List<T>> getForListAsync(Request<?> request, Type responseType) {
		return doExecuteArrayAsync(request, responseType);
	}


	@Override
	public <T> T doExecute(Request<?> request, Type responseType) {
		try {
			RpcContext.initContext(rpcConfig, request);
			SimpleRpcResponseExtractor<T> responseExtractor = new SimpleRpcResponseExtractor<>(request, simpleRpcConfigRegister.getAllPostInterceptorList(), responseType);
			return getResponse(request, responseExtractor, responseType);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> List<T> doExecuteArray(Request<?> request, Type responseType) {
		try {
			RpcContext.initContext(rpcConfig, request);
			SimpleRpcArrayResponseExtractor<T> responseExtractor = new SimpleRpcArrayResponseExtractor<>(request, simpleRpcConfigRegister.getAllPostInterceptorList(), responseType);
			return getResponse(request, responseExtractor, responseType);

		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> CompletableFuture<T> doExecuteAsync(Request<?> request, Type responseType) {
		try {
			RpcContext.initContext(rpcConfig, request);
			return CompletableFuture.supplyAsync(() -> {
				try {
					return doExecute(request, responseType);
				} catch (Exception e) {
					throw new SimpleRpcException(e);
				}
			}, executor);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public <T> CompletableFuture<List<T>> doExecuteArrayAsync(Request<?> request, Type responseType) {
		try {
			RpcContext.initContext(rpcConfig, request);
			return CompletableFuture.supplyAsync(() -> {
				try {
					return doExecuteArray(request, responseType);
				} catch (Exception e) {
					throw new SimpleRpcException(e);
				}
			}, executor);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public Resource doExecuteForResource(Request<?> request) {
		try {
			RpcContext.initContext(rpcConfig, request);
			return getInputStreamResponse(request);
		} finally {
			RpcContext.clear();
		}
	}

	@Override
	public CompletableFuture<Resource> doExecuteForResourceAsync(Request<?> request) {
		try {
			RpcContext.initContext(rpcConfig, request);
			return CompletableFuture.supplyAsync(() -> {
				try {
					return getInputStreamResponse(request);
				} catch (Exception e) {
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
			log.info("simple executor关闭");
			executor.shutdown();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext() == this.applicationContext) {
			Map<String, SimpleRpcConfigurer> configMap = this.applicationContext.getBeansOfType(SimpleRpcConfigurer.class);
			if (CollectionUtil.isNotEmpty(configMap)) {
				configMap.forEach((k, v) -> v.configure(simpleRpcConfigRegister));
			}
			AnnotationAwareOrderComparator.sort(simpleRpcConfigRegister.getAllPreInterceptorList());
			AnnotationAwareOrderComparator.sort(simpleRpcConfigRegister.getAllPostInterceptorList());
			Map<String, String> proxy = this.rpcConfig.getProxy();
			simpleRpcConfigRegister.addMoreProxy(proxy);
			this.rpcConfig.setProxy(simpleRpcConfigRegister.getProxy());
			initRestTemplate();
		}
	}

	private void initRestTemplate() {
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		interceptors.add(0, new SkipLoadBalancedRequestInterceptor(rpcConfig));
		interceptors.add(0, new SimpleRpcHttpRequestInterceptor(rpcConfig, simpleRpcConfigRegister.getAllPreInterceptorList()));
		restTemplate.setInterceptors(interceptors);
	}

	private void init() {
		if (rpcConfig == null) {
			throw new NullPointerException("Rpc 服务调用配置 rpcConfig 不能为空");
		}
		executor = RpcThreadPoolExecutor.getInstance(rpcConfig);
	}

	private <T> T getResponse(Request<?> request, ResponseExtractor<T> responseExtractor, Type responseType) {
		checkRequest(request);
		RequestEntity<?> requestEntity = getRequestEntity(request);
		HttpEntity<?> httpEntity = getHttpEntity(request);
		RpcRequestCallback rpcRequestCallback = new RpcRequestCallback(requestEntity, httpEntity, responseType, restTemplate.getMessageConverters());
		return restTemplate.execute(getUrl(request.getUrl()), request.getHttpMethod(), rpcRequestCallback, responseExtractor);
	}

	private Resource getInputStreamResponse(Request<?> request) {
		checkRequest(request);
		String url = getUrl(request.getUrl());
		HttpEntity<?> httpEntity = getBodyOrFormEntity(request);
		final ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, request.getHttpMethod(), httpEntity, Resource.class);
		if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
			return responseEntity.getBody();
		}
		throw new HttpClientErrorException(responseEntity.getStatusCode());
	}

	private HttpEntity<?> getBodyEntity(Request<?> request) {
		if (formParam(request)) {
			return null;
		}
		MultiValueMap<String, String> headers = request.getHeaders();
		if (headers == null) {
			headers = new LinkedMultiValueMap<>();
		}
		return new HttpEntity<>(request.getBody(), headers);
	}

	private HttpEntity<?> getBodyOrFormEntity(Request<?> request) {
		HttpEntity<?> httpEntity = getHttpEntity(request);
		if (httpEntity != null) {
			return httpEntity;
		}
		httpEntity = getBodyEntity(request);
		if (httpEntity != null) {
			return httpEntity;
		}
		return new HttpEntity<>(request.getHeaders());
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

	@Override
	protected LoadBalancerClient getLoadBalancerClient() {
		return applicationContext.getBean(LoadBalancerClient.class);
	}
}
