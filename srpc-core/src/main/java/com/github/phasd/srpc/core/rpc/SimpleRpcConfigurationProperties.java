package com.github.phasd.srpc.core.rpc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置参数
 *
 * @author phz
 * @date 2020-07-28 19:08:24
 * @since V1.0
 */
@Component
@ConfigurationProperties("simple.rpc")
@Data
public class SimpleRpcConfigurationProperties {
	/**
	 * 网关URL
	 */
	private String gatewayUrl;

	/**
	 * 是否启用本地代理
	 */
	private boolean enableProxy = false;

	/**
	 * 默认APP_ID
	 */
	private String appId = "simple-rpc";

	/**
	 * 本地代理
	 */
	private Map<String, String> proxy = new HashMap<>();

	/**
	 * 创建连接的最长时间
	 */
	private int connectTimeout = 10000;

	/**
	 * 从连接池中获取到连接的最长时间
	 */
	private int connectionRequestTimeout = 10000;

	/**
	 * 数据传输的最长时间
	 */
	private int socketTimeout = 30000;

	/**
	 * 提交请求前测试连接是否可用
	 */
	private boolean staleConnectionCheckEnabled = true;

	/**
	 * 异步线程池 核心线程数
	 */
	private int corePoolSize = 5;

	/**
	 * 异步线程池 最大线程数
	 */
	private int maxPoolSize = 30;

	/**
	 * 异步线程池 阻塞队列
	 */
	private int workQueueSize = -1;

	/**
	 * 异步线程池 线程最大存活时间 单位秒
	 */
	private long aliveTime = 5;

	// 否启用服务注册中心模式
	private boolean enableRegister = false;

	// 转发是否带上包含serviceId
	private boolean withServiceId = false;
}