package com.github.phasd.srpc.core.rpc.interceptor;

/**
 * SimpleRpcConfigurer
 *
 * @author phz
 * @date 2020-07-31 14:50:07
 * @since V1.0
 */
@FunctionalInterface
public interface SimpleRpcConfigurer {

	/**
	 * 配置
	 *
	 * @param simpleRpcConfigRegister simpleRpcRegister
	 */
	void configure(SimpleRpcConfigRegister simpleRpcConfigRegister);
}
