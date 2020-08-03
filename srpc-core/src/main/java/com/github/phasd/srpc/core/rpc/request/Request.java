package com.github.phasd.srpc.core.rpc.request;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-23 14:39:33
 */
public class Request<T> {
	private String url;
	private URI uri;
	private T body;
	private Map<String, String> uriParams;
	private HttpMethod httpMethod;
	private MultiValueMap<String, String> headers;
	private MultiValueMap<String, Object> formParam;
	private boolean multiPart = false;

	private Request(String url, HttpMethod httpMethod) {
		this.url = url;
		this.httpMethod = httpMethod;
		this.uri = URI.create(url);
	}


	private Request(String url, HttpMethod httpMethod, T body) {
		this(url, httpMethod);
		this.body = body;
	}

	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam) {
		this(url, httpMethod);
		this.formParam = formParam;
	}

	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, MultiValueMap<String, String> headers) {
		this(url, httpMethod, formParam);
		this.headers = headers;
	}

	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, Map<String, String> uriParams) {
		this(url, httpMethod, formParam);
		this.uriParams = uriParams;
	}

	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, MultiValueMap<String, String> headers, Map<String, String> uriParams) {
		this(url, httpMethod, formParam);
		this.headers = headers;
		this.uriParams = uriParams;
	}

	private Request(String url, HttpMethod httpMethod, T body, Map<String, String> uriParams) {
		this(url, httpMethod, body);
		this.uriParams = uriParams;
	}

	private Request(String url, HttpMethod httpMethod, T body, MultiValueMap<String, String> headers) {
		this(url, httpMethod, body);
		this.headers = headers;
	}

	private Request(String url, HttpMethod httpMethod, T body, Map<String, String> uriParams, MultiValueMap<String, String> headers) {
		this(url, httpMethod, body);
		this.headers = headers;
		this.uriParams = uriParams;
	}

	public static RequestBuilder post(String url) {
		return new RequestBuilder(HttpMethod.POST, url);
	}

	public static RequestBuilder get(String url) {
		return new RequestBuilder(HttpMethod.GET, url);
	}

	public static RequestBuilder put(String url) {
		return new RequestBuilder(HttpMethod.PUT, url);
	}

	public static RequestBuilder delete(String url) {
		return new RequestBuilder(HttpMethod.DELETE, url);
	}

	public static RequestBuilder patch(String url) {
		return new RequestBuilder(HttpMethod.PATCH, url);
	}

	public String getUrl() {
		return url;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	public T getBody() {
		return body;
	}

	private void setBody(T body) {
		this.body = body;
	}

	public Map<String, String> getUriParams() {
		return uriParams;
	}

	private void setUriParams(Map<String, String> uriParams) {
		this.uriParams = uriParams;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	private void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public MultiValueMap<String, String> getHeaders() {
		return headers;
	}

	private void setHeaders(MultiValueMap<String, String> headers) {
		this.headers = headers;
	}

	public MultiValueMap<String, Object> getFormParam() {
		return formParam;
	}

	private void setFormParam(MultiValueMap<String, Object> formParams) {
		this.formParam = formParams;
	}

	public URI getUri() {
		return uri;
	}

	private void setUri(URI uri) {
		this.uri = uri;
	}

	public boolean isMultiPart() {
		return multiPart;
	}

	public void setMultiPart(boolean multiPart) {
		this.multiPart = multiPart;
	}

	public static class RequestBuilder {
		private String url;
		private Map<String, String> uriParams;
		private HttpMethod httpMethod;
		private MultiValueMap<String, String> headers;
		private MultiValueMap<String, Object> formParam;
		private boolean multiPart = false;
		private Object body;

		public RequestBuilder(HttpMethod method, String url) {
			this.httpMethod = method;
			this.url = url;
		}

		public RequestBuilder uriParam(String key, String value) {
			if (CollectionUtil.isEmpty(this.uriParams)) {
				this.uriParams = new LinkedHashMap<>();
				this.uriParams.put(key, value);
			} else {
				this.uriParams.put(key, value);
			}
			return this;
		}

		public RequestBuilder uriParams(Map<String, String> uriParams) {
			if (CollectionUtil.isEmpty(this.uriParams)) {
				this.uriParams = uriParams;
			} else {
				this.uriParams.putAll(uriParams);
			}
			return this;
		}

		public RequestBuilder header(String key, String value) {
			if (CollectionUtil.isEmpty(this.headers)) {
				this.headers = new LinkedMultiValueMap<>();
				this.headers.add(key, value);
			} else {
				this.headers.add(key, value);
			}
			return this;
		}

		public RequestBuilder headers(MultiValueMap<String, String> valueMap) {
			if (CollectionUtil.isEmpty(this.headers)) {
				this.headers = valueMap;
			} else {
				this.headers.addAll(valueMap);
			}
			return this;
		}

		public RequestBuilder setBody(Object body) {
			this.body = body;
			return this;
		}

		public RequestBuilder formParam(String key, Object value) {
			if (CollectionUtil.isEmpty(this.formParam)) {
				this.formParam = new LinkedMultiValueMap<>();
				this.formParam.add(key, value);
			} else {
				this.formParam.add(key, value);
			}
			if (value instanceof Resource) {
				this.multiPart = true;
			}
			return this;
		}

		public RequestBuilder formParams(MultiValueMap<String, Object> valueMap) {
			if (CollectionUtil.isEmpty(this.formParam)) {
				this.formParam = valueMap;
			} else {
				this.formParam.addAll(valueMap);
			}
			this.multiPart = hasMultipart(this.formParam);
			return this;
		}

		public <T> Request<T> body(T body) {
			assertBase();
			Assert.isFalse(CollectionUtil.isNotEmpty(this.formParam) && Objects.nonNull(body), "body和formParam不能同时设置");
			processUrl();
			return new Request<>(this.url, this.httpMethod, body, this.uriParams, this.headers);
		}

		@SuppressWarnings("unchecked")
		public <T> Request<T> build() {
			assertBase();
			processUrl();
			Request<T> request = new Request<>(this.url, this.httpMethod, this.formParam, this.headers, this.uriParams);
			request.setBody((T) this.body);
			Assert.isFalse(CollectionUtil.isNotEmpty(this.formParam) && Objects.nonNull(body), "body和formParam不能同时设置");
			request.setMultiPart(this.multiPart);
			return request;
		}

		private void processUrl() {
			if (CollectionUtil.isNotEmpty(this.uriParams)) {
				URI uri = new UriTemplate(this.url).expand(this.uriParams);
				this.url = uri.toString();
			}
		}

		private void assertBase() {
			Assert.notNull(this.url, "url不能为空");
			Assert.notNull(this.httpMethod, "httpMethod不能为空");
		}

		private boolean hasMultipart(MultiValueMap<String, Object> valueMap) {
			if (CollectionUtil.isEmpty(valueMap)) {
				return false;
			}
			for (Map.Entry<String, List<Object>> entry : valueMap.entrySet()) {
				List<Object> value = entry.getValue();
				if (CollectionUtil.isNotEmpty(value)) {
					boolean flag = value.stream().allMatch(item -> item instanceof Resource);
					if (flag) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
