package com.github.phasd.srpc.core.rpc.interceptor;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-31 14:50:07
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
