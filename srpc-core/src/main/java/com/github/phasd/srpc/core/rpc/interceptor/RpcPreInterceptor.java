package com.github.phasd.srpc.core.rpc.interceptor;

import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;

/**
 * 请求拦截器
 *
 * @description:
 * @author: phz
 * @create: 2020-07-23 14:39:33
 */
public interface RpcPreInterceptor extends Ordered {

	/**
	 * 前置处理器
	 *
	 * @param request request
	 */
	void preInterceptor(HttpRequest request);

	/**
	 * 是否支持前置拦截
	 *
	 * @param request request
	 * @return boolean 是否支持前置拦截
	 */
	boolean preSupports(Request request);
}
