package com.github.srpc.starter;

import com.github.srpc.core.rpc.SimpleRpc;
import com.github.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.github.srpc.core.rpc.mvc.EncryptFromFilter;
import com.github.srpc.core.rpc.request.SimpleRpcHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-14 12:34:06
 */
@Configuration
@EnableConfigurationProperties({SimpleRpcConfigurationProperties.class})
@ComponentScan(basePackageClasses = {SimpleAutoConfiguration.class})
public class SimpleAutoConfiguration {
	@Autowired
	private SimpleRpcConfigurationProperties rpcConfig;

	@Bean
	@ConditionalOnMissingBean(ClientHttpRequestFactory.class)
	public ClientHttpRequestFactory requestFactory() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(rpcConfig.getConnectTimeout());
		requestFactory.setConnectionRequestTimeout(rpcConfig.getConnectionRequestTimeout());
		requestFactory.setReadTimeout(rpcConfig.getSocketTimeout());
		return requestFactory;
	}

	@Bean
	public FilterRegistrationBean<EncryptFromFilter> encryptFormFilter() {
		FilterRegistrationBean<EncryptFromFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(new EncryptFromFilter());
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setName("EncryptFromFilter");
		return filterRegistrationBean;
	}

	@Bean
	public ClientHttpRequestInterceptor rpcHttpRequestInterceptor() {
		return new SimpleRpcHttpRequestInterceptor(rpcConfig.isSecret());
	}

	@Bean("simpleRestTemplate")
	@DependsOn({"requestFactory", "rpcHttpRequestInterceptor"})
	public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory, List<ClientHttpRequestInterceptor> rpcHttpRequestInterceptorList) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);
		restTemplate.setInterceptors(rpcHttpRequestInterceptorList);
		return restTemplate;
	}

	@Bean
	@DependsOn("simpleRestTemplate")
	public SimpleRpc simpleRpc(RestTemplate restTemplate) {
		return new SimpleRpc(restTemplate, rpcConfig);
	}
}
