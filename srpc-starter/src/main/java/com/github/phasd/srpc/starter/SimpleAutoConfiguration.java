package com.github.phasd.srpc.starter;

import com.github.phasd.srpc.core.rpc.HttpClientUtils;
import com.github.phasd.srpc.core.rpc.SimpleRpc;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * SimpleAutoConfiguration
 *
 * @author phz
 * @date 2020-07-14 12:34:06
 * @since V1.0
 */
@Configuration
@EnableConfigurationProperties({SimpleRpcConfigurationProperties.class})
@ComponentScan(basePackageClasses = {SimpleAutoConfiguration.class})
public class SimpleAutoConfiguration {
	@Autowired
	private SimpleRpcConfigurationProperties rpcConfig;

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory =
				new HttpComponentsClientHttpRequestFactory(HttpClientUtils.getHttpClient(rpcConfig));
		return new RestTemplate(httpRequestFactory);
	}

	@Bean
	public SimpleRpc simpleRpc(RestTemplate restTemplate) {
		return new SimpleRpc(rpcConfig, restTemplate);
	}
}
