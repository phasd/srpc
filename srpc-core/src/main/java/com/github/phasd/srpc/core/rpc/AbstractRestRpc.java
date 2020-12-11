package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.util.StrUtil;
import com.github.phasd.srpc.core.rpc.interceptor.SimpleRpcConfigRegister;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.net.URI;
import java.util.Map;

/**
 * AbstractRestRpc
 *
 * @author phz
 * @date 2020-07-22 08:44:52
 * @since V1.0
 */
public abstract class AbstractRestRpc implements RpcInterface {
	private static final String HTTPS = "https://";
	private static final String HTTP = "http://";
	private static final String CONTEXT_PATH = "contextPath";

	private SimpleCache<String, String> contextPathCache = new SimpleCache<>();

	/**
	 * rpcConfig
	 */
	protected SimpleRpcConfigurationProperties rpcConfig;

	/**
	 * simpleRpcRegister
	 */
	protected SimpleRpcConfigRegister simpleRpcConfigRegister;


	/**
	 * @param rpcConfig 配置参数
	 */
	public AbstractRestRpc(SimpleRpcConfigurationProperties rpcConfig) {
		this.rpcConfig = rpcConfig;
		this.simpleRpcConfigRegister = new SimpleRpcConfigRegister();
	}


	/**
	 * uri 预处理
	 *
	 * @param uri uri
	 * @return 处理后的url
	 */
	String getUrl(URI uri) {
		if (uri == null || StrUtil.isBlank(uri.toString())) {
			throw new NullPointerException("SimpleRpc 调用 url不能为空");
		}
		return getUrl(uri.toString());
	}

	/**
	 * url 预处理
	 *
	 * @param url uri
	 * @return 处理后的url
	 */
	String getUrl(String url) {
		if (StrUtil.isBlank(url)) {
			throw new NullPointerException("SimpleRpc 调用 url不能为空");
		}

		if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
			// 绝对路径直接返回入参的url地址
			return url;
		}

		url = RpcUtils.trimUrlDelimiter(url);
		int i = url.indexOf(RpcUtils.URL_DELIMITER);
		String key;
		String surplus = StrUtil.EMPTY;
		if (i > 0) {
			key = url.substring(0, i);
			surplus = url.substring(i + 1);
		} else {
			key = url;
		}
		if (rpcConfig.isEnableProxy()) {
			Map<String, String> proxy = rpcConfig.getProxy();
			if (proxy.containsKey(key)) {
				String value = proxy.get(key);
				if (StrUtil.isNotBlank(value)) {
					value = RpcUtils.trimSymbol(value, RpcUtils.URL_DELIMITER);
					if (rpcConfig.isWithServiceId()) {
						return value + RpcUtils.URL_DELIMITER + url;
					}
					return value + RpcUtils.URL_DELIMITER + surplus;
				}
			}
		}

		if (Boolean.TRUE.equals(rpcConfig.isEnableRegister())) {
			String contextPath = contextPathCache.get(key, () -> {
				LoadBalancerClient loadBalancerClient = getLoadBalancerClient();
				if (loadBalancerClient == null) {
					throw new SimpleRpcException("Register 模式需要 LoadBalancerClient");
				}
				ServiceInstance chooseInstance = loadBalancerClient.choose(key);
				if (chooseInstance == null) {
					throw new SimpleRpcException(String.format("对于服务Id:[%s]未找到可用的服务实例", key));
				}
				Map<String, String> metadata = chooseInstance.getMetadata();
				String path = metadata.get(CONTEXT_PATH);
				if (StrUtil.isBlank(path)) {
					path = StrUtil.EMPTY;
				}
				return RpcUtils.trimUrlDelimiter(path);
			});
			return String.format("http://%s/%s/%s", key, contextPath, surplus);
		}

		String gatewayUrl = rpcConfig.getGatewayUrl();
		if (gatewayUrl.endsWith(RpcUtils.URL_DELIMITER)) {
			url = gatewayUrl + url;
		} else {
			url = gatewayUrl + RpcUtils.URL_DELIMITER + url;
		}
		return url;
	}

	protected abstract LoadBalancerClient getLoadBalancerClient();
}
