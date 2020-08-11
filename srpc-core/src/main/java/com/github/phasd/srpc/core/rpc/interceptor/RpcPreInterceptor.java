package com.github.phasd.srpc.core.rpc.interceptor;

import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;

/**
 * 请求前置拦截接口
 *
 * @author phz
 * @date 2020-07-23 14:39:33
 * @since V1.0
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
