package com.github.phasd.srpc.core.rpc.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimpleRpcConfigRegister
 *
 * @author phz
 * @date 2020-07-31 14:50:07
 * @since V1.0
 */
public class SimpleRpcConfigRegister {
	/**
	 * 前置拦截
	 */
	private List<RpcPreInterceptor> rpcPreInterceptorList;

	/**
	 * 后置拦截
	 */
	private List<RpcPostInterceptor> rpcPostInterceptorList;

	/**
	 * 代理
	 */
	private Map<String, String> proxy;

	public SimpleRpcConfigRegister() {
		this.rpcPreInterceptorList = new ArrayList<>();
		this.rpcPostInterceptorList = new ArrayList<>();
		proxy = new HashMap<>();
	}

	/**
	 * @param rpcPreInterceptor 增加前置拦截
	 */
	public void addPreInterceptor(RpcPreInterceptor rpcPreInterceptor) {
		rpcPreInterceptorList.add(rpcPreInterceptor);
	}


	/**
	 * @return 前置拦截
	 */
	public List<RpcPreInterceptor> getAllPreInterceptorList() {
		return rpcPreInterceptorList;
	}


	/**
	 * @param rpcPostInterceptor 增加后置拦截
	 */
	public void addPostInterceptor(RpcPostInterceptor rpcPostInterceptor) {
		rpcPostInterceptorList.add(rpcPostInterceptor);
	}

	/**
	 * @return 后置拦截
	 */
	public List<RpcPostInterceptor> getAllPostInterceptorList() {
		return rpcPostInterceptorList;
	}


	/**
	 * 增加代理
	 *
	 * @param key 代理的key
	 * @param url 转发的url
	 */
	public void addProxy(String key, String url) {
		Assert.isTrue(StrUtil.isNotBlank(key), "代理key不能为空");
		Assert.isTrue(StrUtil.isNotBlank(url), "代理url不能为空");
		this.proxy.put(key, url);
	}

	/**
	 * 批量增加代理
	 *
	 * @param map Proxy
	 */
	public void addMoreProxy(Map<String, String> map) {
		if (CollectionUtil.isEmpty(map)) {
			return;
		}
		map.forEach(this::addProxy);
	}

	/**
	 * @return 获取代理
	 */
	public Map<String, String> getProxy() {
		return proxy;
	}
}
