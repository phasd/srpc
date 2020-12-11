package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.lang.Singleton;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author phz
 * @date 2020-09-27 13:48:22
 * @since V1.0
 */
public class HttpClientUtils {

	private HttpClientUtils() {}

	private static HttpClientHolder httpClientHolder;

	public static CloseableHttpClient getHttpClient(SimpleRpcConfigurationProperties simpleRpcConfigurationProperties) {
		if (httpClientHolder == null) {
			httpClientHolder = Singleton.get(HttpClientHolder.class, simpleRpcConfigurationProperties);
		}
		return httpClientHolder.getHttpClient();
	}

	public static void destroy() {
		if (httpClientHolder != null) {
			httpClientHolder.destroy();
		}
	}


	private static class HttpClientHolder {
		private static final Log LOG = LogFactory.get(HttpClientHolder.class);

		/**
		 * HttpClient
		 */
		private CloseableHttpClient httpClient;

		public HttpClientHolder(SimpleRpcConfigurationProperties simpleRpcConfigurationProperties) {
			init(simpleRpcConfigurationProperties);
		}

		private void init(final SimpleRpcConfigurationProperties simpleRpcConfigurationProperties) {
			LOG.info("http client初始化, {}", simpleRpcConfigurationProperties.toString());

			// socket工厂协议注册
			Registry<ConnectionSocketFactory> socketFactoryRegistry = registerSocketFactory();
			// 连接池创建
			PoolingHttpClientConnectionManager connectionManager = initConnectionManager(socketFactoryRegistry);
			// httpClient初始化
			httpClient = initHttpClient(simpleRpcConfigurationProperties, connectionManager);
			//JVM 关闭钩子，最后一层保障，以防用户程序终止不调用destroy
			Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
		}

		public CloseableHttpClient getHttpClient() {
			return this.httpClient;
		}

		private void destroy() {
			if (httpClient == null) {
				return;
			}
			try {
				LOG.info("应用关闭，HttpClient连接释放开始");
				httpClient.close();
				httpClient = null;
				LOG.info("应用关闭，HttpClient连接释放结束");
			} catch (IOException e) {
				LOG.error("应用关闭，HttpClient连接释放异常", e);
			}
		}

		private PoolingHttpClientConnectionManager initConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
			//HttpConnectionFactory:配置写请求/解析响应处理器
			HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connectionFactory = new ManagedHttpClientConnectionFactory(
					DefaultHttpRequestWriterFactory.INSTANCE,
					DefaultHttpResponseParserFactory.INSTANCE
			);

			//DNS解析器
			DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connectionFactory,
					dnsResolver);
			cm.setMaxTotal(1000);
			cm.setDefaultMaxPerRoute(1000);
			cm.setDefaultSocketConfig(SocketConfig.custom().setTcpNoDelay(true).build());
			cm.setValidateAfterInactivity(10 * 1000);
			return cm;
		}

		private CloseableHttpClient initHttpClient(final SimpleRpcConfigurationProperties rpcConfigurationProperties, HttpClientConnectionManager cm) {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(rpcConfigurationProperties.getConnectionRequestTimeout())
					.setConnectTimeout(rpcConfigurationProperties.getConnectTimeout())
					.setSocketTimeout(rpcConfigurationProperties.getSocketTimeout())
					.setMaxRedirects(20).build();
			HttpClientBuilder customClientBuilder = HttpClients.custom();

			customClientBuilder.setConnectionManager(cm)
					//连接池不是共享模式，这个共享是指与其它httpClient是否共享
					.setConnectionManagerShared(false)
					//回收过期连接
					.evictExpiredConnections()
					//定期回收空闲连接
					.evictIdleConnections(180, TimeUnit.SECONDS)
					//连接存活时间，如果不设置，则根据长连接信息决定
					.setConnectionTimeToLive(300, TimeUnit.SECONDS)
					//设置默认的请求参数
					.setDefaultRequestConfig(requestConfig)
					//连接重用策略，即是否能keepAlive
					.setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE);
			//长连接配置，即获取长连接生存多长时间
			customClientBuilder.setKeepAliveStrategy((response, context) -> {
				HeaderElementIterator it = new BasicHeaderElementIterator
						(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && "timeout".equalsIgnoreCase(param)) {
						return Long.parseLong(value) * 1000;
					}
				}
				return 50 * 1000;
			});
			//设置重试次数，默认为3次；当前是禁用掉, 后续可根据场景来自定义重试策略选择是否重试
			customClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			return customClientBuilder.build();
		}

		private Registry<ConnectionSocketFactory> registerSocketFactory() {
			// https 配置
			SSLContext sslContext;
			try {
				//信任所有
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, (xcs, string) -> true).build();
			} catch (Exception e) {
				LOG.error(e);
				throw new SimpleRpcException(e);
			}

			SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);
			return RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", sslFactory)
					.build();
		}
	}
}
