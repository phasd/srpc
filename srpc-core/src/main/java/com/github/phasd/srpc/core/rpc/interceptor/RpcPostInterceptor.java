package com.github.phasd.srpc.core.rpc.interceptor;

import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;

/**
 * 请求拦截器
 *
 * @description:
 * @author: phz
 * @create: 2020-07-23 14:39:33
 */
public interface RpcPostInterceptor extends Ordered {

	/**
	 * 后置处理器
	 *
	 * @param request request
	 * @param headers headers
	 * @param content content
	 * @return Object object
	 */
	String postInterceptor(Request request, HttpHeaders headers, String content);

	/**
	 * 是否支持后置拦截
	 *
	 * @param request request
	 * @param headers HttpHeaders
	 * @return boolean 是否支持后置拦截
	 */
	boolean postSupports(Request request, HttpHeaders headers);
}
