package com.github.phasd.srpc.starter;

import com.github.phasd.srpc.core.rpc.SimpleRpc;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.github.phasd.srpc.core.rpc.mvc.EncryptFromFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
	public FilterRegistrationBean<EncryptFromFilter> encryptFormFilter() {
		FilterRegistrationBean<EncryptFromFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(new EncryptFromFilter(rpcConfig.getSecretKey()));
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setName("EncryptFromFilter");
		return filterRegistrationBean;
	}

	@Bean
	public SimpleRpc simpleRpc() {
		return new SimpleRpc(rpcConfig);
	}
}
