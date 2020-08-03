package com.github.phasd.srpc.core.rpc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("simple.rpc")
@Data
public class SimpleRpcConfigurationProperties {
	// 网关URL
	private String gatewayUrl;

	// 是否启用本地代理
	private boolean enableProxy = false;

	// 是否启用本地代理 默认APP_ID
	private String appid = "simple-rpc";

	// 本地代理
	private Map<String, String> proxy = new HashMap<>();

	//创建连接的最长时间
	private int connectTimeout = 10000;

	//从连接池中获取到连接的最长时间
	private int connectionRequestTimeout = 10000;

	// 数据传输的最长时间
	private int socketTimeout = 30000;

	//提交请求前测试连接是否可用
	private boolean staleConnectionCheckEnabled = true;

	// 参数是否加密处理
	private boolean secret;

	private int corePoolSize = 5;

	private int maxPoolSize = 30;

	private int workQueueSize = -1;

	private long aliveTime = 5;
}