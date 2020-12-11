package com.github.phasd.srpc.core.rpc.request;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.github.phasd.srpc.core.rpc.HttpClientUtils;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.github.phasd.srpc.core.rpc.SimpleRpcException;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author phz
 * @date 2020-08-27 15:05:34
 * @since V1.0
 */
public class SkipLoadBalancedRequestInterceptor implements ClientHttpRequestInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SkipLoadBalancedRequestInterceptor.class);
	private static final String DELIMITERS = "&";
	private SimpleRpcConfigurationProperties rpcConfig;
	private SimpleCache<String, Boolean> hostCache;

	public SkipLoadBalancedRequestInterceptor(SimpleRpcConfigurationProperties rpcConfig) {
		this.rpcConfig = rpcConfig;
		hostCache = new SimpleCache<>();
	}


	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		URI uri = request.getURI();
		String host = uri.getHost();
		if (checkHost(host)) {
			return getSkipHttpResponse(request, body);
		}
		return execution.execute(request, body);
	}

	private boolean checkHost(String host) {
		if (StrUtil.isBlank(host)) {
			throw new SimpleRpcException("非法的host");
		}
		return hostCache.get(host, () -> {
			final String readIp = NetUtil.getIpByHost(host);
			Matcher matcher = PatternPool.IPV4.matcher(readIp);
			if (matcher.matches()) {
				return true;
			}
			return PatternPool.IPV6.matcher(readIp).matches();
		});
	}

	private SkipHttpResponse getSkipHttpResponse(HttpRequest request, byte[] body) throws IOException {
		HttpMethod method = request.getMethod();
		if (method == null) {
			throw new SimpleRpcException("非法的http请求方法");
		}
		final RequestBuilder requestBuilder = RequestBuilder.create(method.name());
		final HttpHeaders headers = request.getHeaders();
		if (headers.size() > 0) {
			processHeaders(requestBuilder, headers);
		}
		setHttpEntity(request, requestBuilder, headers, body);
		final HttpUriRequest httpUriRequest = requestBuilder.build();
		final CloseableHttpClient httpClient = HttpClientUtils.getHttpClient(rpcConfig);
		CloseableHttpResponse res = httpClient.execute(httpUriRequest);
		return new SkipHttpResponse(res);
	}

	private void setHttpEntity(HttpRequest request, RequestBuilder requestBuilder, HttpHeaders headers, byte[] body) throws IOException {
		requestBuilder.setCharset(StandardCharsets.UTF_8);
		if (CollectionUtil.isEmpty(headers)) {
			requestBuilder.setUri(request.getURI());
			requestBuilder.setEntity(new ByteArrayEntity(body, ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8)));
			return;
		}
		final MediaType springContentType = headers.getContentType();
		if (MediaType.APPLICATION_FORM_URLENCODED.includes(springContentType) && HttpMethod.GET.equals(request.getMethod())) {
			final String formParam = new String(body, StandardCharsets.UTF_8);
			final List<NameValuePair> parametersList = getParameters(formParam);
			String getParams = EntityUtils.toString(new UrlEncodedFormEntity(parametersList, Consts.UTF_8));
			requestBuilder.setUri(request.getURI().toString() + "?" + getParams);
			return;
		}
		requestBuilder.setUri(request.getURI());
		requestBuilder.setEntity(new ByteArrayEntity(body, ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8)));
	}


	private void processHeaders(RequestBuilder requestBuilder, HttpHeaders headers) {
		headers.forEach((headerName, headerValues) -> {
			if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {
				String headerValue = StringUtils.collectionToDelimitedString(headerValues, "; ");
				requestBuilder.addHeader(headerName, headerValue);
			} else if (!HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) && !HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
				for (String headerValue : headerValues) {
					requestBuilder.addHeader(headerName, headerValue);
				}
			}
		});
	}

	private List<NameValuePair> getParameters(String formParam) {
		String[] pairs = StringUtils.tokenizeToStringArray(formParam, DELIMITERS);
		List<NameValuePair> nameValuePairList = new ArrayList<>();
		for (String pair : pairs) {
			BasicNameValuePair basicNameValuePair;
			int idx = pair.indexOf('=');
			if (idx == -1) {
				basicNameValuePair = new BasicNameValuePair(URLUtil.decode(pair, StandardCharsets.UTF_8), null);
			} else {
				String name = URLUtil.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
				String value = URLUtil.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
				basicNameValuePair = new BasicNameValuePair(name, value);
			}
			nameValuePairList.add(basicNameValuePair);
		}
		return nameValuePairList;
	}

	private static class SkipHttpResponse extends AbstractClientHttpResponse {
		private final HttpResponse httpResponse;
		private HttpHeaders headers;

		SkipHttpResponse(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		@Override
		public int getRawStatusCode() throws IOException {
			return this.httpResponse.getStatusLine().getStatusCode();
		}

		@Override
		public String getStatusText() throws IOException {
			return this.httpResponse.getStatusLine().getReasonPhrase();
		}

		@Override
		public HttpHeaders getHeaders() {
			if (this.headers == null) {
				this.headers = new HttpHeaders();
				for (Header header : this.httpResponse.getAllHeaders()) {
					this.headers.add(header.getName(), header.getValue());
				}
			}
			return this.headers;
		}

		@Override
		public InputStream getBody() throws IOException {
			HttpEntity entity = this.httpResponse.getEntity();
			return (entity != null ? entity.getContent() : StreamUtils.emptyInput());
		}

		@Override
		public void close() {
			try {
				try {
					EntityUtils.consume(this.httpResponse.getEntity());
				} finally {
					if (this.httpResponse instanceof Closeable) {
						((Closeable) this.httpResponse).close();
					}
				}
			} catch (IOException ex) {
				LOGGER.warn("SkipHttpResponse close ", ex);
			}
		}
	}

}
