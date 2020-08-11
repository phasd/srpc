package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.util.StrUtil;
import com.github.phasd.srpc.core.rpc.interceptor.SimpleRpcConfigRegister;

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
					url = value + RpcUtils.URL_DELIMITER + surplus;
					return url;
				}
			}
		}
		String gatewayUrl = rpcConfig.getGatewayUrl();
		if (gatewayUrl.endsWith(RpcUtils.URL_DELIMITER)) {
			url = gatewayUrl + url;
		} else {
			url = gatewayUrl + RpcUtils.URL_DELIMITER + url;
		}
		return url;
	}
}
