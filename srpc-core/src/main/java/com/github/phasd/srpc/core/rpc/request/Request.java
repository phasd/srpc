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
 * 请求
 *
 * @author phz
 * @date 2020-07-23 14:39:33
 * @since V1.0
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

	/**
	 * @param url        请求URL
	 * @param httpMethod 请求方法
	 */
	private Request(String url, HttpMethod httpMethod) {
		this.url = url;
		this.httpMethod = httpMethod;
		this.uri = URI.create(url);
	}


	/**
	 * @param url        请求URL
	 * @param httpMethod 请求方法
	 * @param body       请求体
	 */
	private Request(String url, HttpMethod httpMethod, T body) {
		this(url, httpMethod);
		this.body = body;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param formParam  form参数
	 */
	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam) {
		this(url, httpMethod);
		this.formParam = formParam;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param formParam  form参数
	 * @param headers    头部参数
	 */
	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, MultiValueMap<String, String> headers) {
		this(url, httpMethod, formParam);
		this.headers = headers;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param formParam  form参数
	 * @param uriParams  路径参数
	 */
	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, Map<String, String> uriParams) {
		this(url, httpMethod, formParam);
		this.uriParams = uriParams;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param formParam  form参数
	 * @param headers    头部参数
	 * @param uriParams  路径参数
	 */
	private Request(String url, HttpMethod httpMethod, MultiValueMap<String, Object> formParam, MultiValueMap<String, String> headers, Map<String, String> uriParams) {
		this(url, httpMethod, formParam);
		this.headers = headers;
		this.uriParams = uriParams;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param body       请求体
	 * @param uriParams  路径参数
	 */
	private Request(String url, HttpMethod httpMethod, T body, Map<String, String> uriParams) {
		this(url, httpMethod, body);
		this.uriParams = uriParams;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param body       请求体
	 * @param headers    头部参数
	 */
	private Request(String url, HttpMethod httpMethod, T body, MultiValueMap<String, String> headers) {
		this(url, httpMethod, body);
		this.headers = headers;
	}

	/**
	 * @param url        请求url
	 * @param httpMethod 请求方法
	 * @param body       请求体
	 * @param uriParams  路径参数
	 * @param headers    头部参数
	 */
	private Request(String url, HttpMethod httpMethod, T body, Map<String, String> uriParams, MultiValueMap<String, String> headers) {
		this(url, httpMethod, body);
		this.headers = headers;
		this.uriParams = uriParams;
	}

	/**
	 * post 请求
	 *
	 * @param url 请求URL
	 * @return RequestBuilder
	 */
	public static RequestBuilder post(String url) {
		return new RequestBuilder(HttpMethod.POST, url);
	}

	/**
	 * get 请求
	 *
	 * @param url 请求URL
	 * @return RequestBuilder
	 */
	public static RequestBuilder get(String url) {
		return new RequestBuilder(HttpMethod.GET, url);
	}

	/**
	 * put 请求
	 *
	 * @param url 请求URL
	 * @return RequestBuilder
	 */
	public static RequestBuilder put(String url) {
		return new RequestBuilder(HttpMethod.PUT, url);
	}

	/**
	 * delete 请求
	 *
	 * @param url 请求URL
	 * @return RequestBuilder
	 */
	public static RequestBuilder delete(String url) {
		return new RequestBuilder(HttpMethod.DELETE, url);
	}

	/**
	 * patch 请求
	 *
	 * @param url 请求URL
	 * @return RequestBuilder
	 */
	public static RequestBuilder patch(String url) {
		return new RequestBuilder(HttpMethod.PATCH, url);
	}

	/**
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url 请求URL
	 */
	private void setUrl(String url) {
		this.url = url;
	}


	/**
	 * @return 参数体
	 */
	public T getBody() {
		return body;
	}

	/**
	 * @param body 参数体
	 */
	private void setBody(T body) {
		this.body = body;
	}


	/**
	 * @return 路径参数
	 */
	public Map<String, String> getUriParams() {
		return uriParams;
	}


	/**
	 * @param uriParams 路径参数
	 */
	private void setUriParams(Map<String, String> uriParams) {
		this.uriParams = uriParams;
	}


	/**
	 * @return 请求方法
	 */
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}


	/**
	 * @param httpMethod 设置请求方法
	 */
	private void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}


	/**
	 * @return 请求头
	 */
	public MultiValueMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers 请求头
	 */
	private void setHeaders(MultiValueMap<String, String> headers) {
		this.headers = headers;
	}


	/**
	 * @return form参数
	 */
	public MultiValueMap<String, Object> getFormParam() {
		return formParam;
	}


	/**
	 * @param formParams form参数
	 */
	private void setFormParam(MultiValueMap<String, Object> formParams) {
		this.formParam = formParams;
	}


	/**
	 * @return 获取uri
	 */
	public URI getUri() {
		return uri;
	}


	/**
	 * @param uri 请求uri
	 */
	private void setUri(URI uri) {
		this.uri = uri;
	}


	/**
	 * @return 是否包含文件
	 */
	public boolean isMultiPart() {
		return multiPart;
	}


	/**
	 * @param multiPart 设置文件标识
	 */
	private void setMultiPart(boolean multiPart) {
		this.multiPart = multiPart;
	}

	/**
	 * 请求构造
	 *
	 * @author phz
	 * @date 2020-07-23 14:39:33
	 * @since V1.0
	 */
	public static class RequestBuilder {
		private String url;
		private Map<String, String> uriParams;
		private HttpMethod httpMethod;
		private MultiValueMap<String, String> headers;
		private MultiValueMap<String, Object> formParam;
		private boolean multiPart = false;
		private Object body;

		/**
		 * @param method 请求方法
		 * @param url    url
		 */
		public RequestBuilder(HttpMethod method, String url) {
			this.httpMethod = method;
			this.url = url;
		}

		/**
		 * 设置单个路径参数
		 *
		 * @param key   参数key
		 * @param value 参数value
		 * @return this
		 */
		public RequestBuilder uriParam(String key, String value) {
			if (CollectionUtil.isEmpty(this.uriParams)) {
				this.uriParams = new LinkedHashMap<>();
				this.uriParams.put(key, value);
			} else {
				this.uriParams.put(key, value);
			}
			return this;
		}

		/**
		 * 批量设置路径参数
		 *
		 * @param uriParams 路径参数
		 * @return this
		 */
		public RequestBuilder uriParams(Map<String, String> uriParams) {
			if (CollectionUtil.isEmpty(this.uriParams)) {
				this.uriParams = uriParams;
			} else {
				this.uriParams.putAll(uriParams);
			}
			return this;
		}

		/**
		 * 设置请求头
		 *
		 * @param key   参数key
		 * @param value 参数value
		 * @return this
		 */
		public RequestBuilder header(String key, String value) {
			if (CollectionUtil.isEmpty(this.headers)) {
				this.headers = new LinkedMultiValueMap<>();
				this.headers.add(key, value);
			} else {
				this.headers.add(key, value);
			}
			return this;
		}

		/**
		 * 批量设置请求头
		 *
		 * @param valueMap 头部参数
		 * @return this
		 */
		public RequestBuilder headers(MultiValueMap<String, String> valueMap) {
			if (CollectionUtil.isEmpty(this.headers)) {
				this.headers = valueMap;
			} else {
				this.headers.addAll(valueMap);
			}
			return this;
		}

		/**
		 * 设置参数体
		 *
		 * @param body 参数体
		 * @return this
		 */
		public RequestBuilder setBody(Object body) {
			this.body = body;
			return this;
		}

		/**
		 * 设置form参数
		 *
		 * @param key   参数key
		 * @param value 参数value
		 * @return this
		 */
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

		/**
		 * 批量设置form参数
		 *
		 * @param valueMap form 参数
		 * @return this
		 */
		public RequestBuilder formParams(MultiValueMap<String, Object> valueMap) {
			if (CollectionUtil.isEmpty(this.formParam)) {
				this.formParam = valueMap;
			} else {
				this.formParam.addAll(valueMap);
			}
			this.multiPart = hasMultipart(this.formParam);
			return this;
		}


		/**
		 * 构造body request
		 *
		 * @param body 参数体
		 * @param <T>  泛型参数类型
		 * @return Request
		 */
		public <T> Request<T> body(T body) {
			assertBase();
			Assert.isFalse(CollectionUtil.isNotEmpty(this.formParam) && Objects.nonNull(body), "body和formParam不能同时设置");
			processUrl();
			return new Request<>(this.url, this.httpMethod, body, this.uriParams, this.headers);
		}

		/**
		 * build
		 *
		 * @param <T> 泛型参数类型
		 * @return Request
		 */
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

		/**
		 * 处理路径参数
		 */
		private void processUrl() {
			if (CollectionUtil.isNotEmpty(this.uriParams)) {
				URI uri = new UriTemplate(this.url).expand(this.uriParams);
				this.url = uri.toString();
			}
		}

		/**
		 * 校验url和method
		 */
		private void assertBase() {
			Assert.notNull(this.url, "url不能为空");
			Assert.notNull(this.httpMethod, "httpMethod不能为空");
		}


		/**
		 * 判断form参数是否有文件
		 *
		 * @param valueMap form参数
		 * @return true|false
		 */
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
