package com.github.phasd.srpc.core.rpc.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-31 15:06:55
 */
public class SimpleRpcConfigRegister {
	private List<RpcPreInterceptor> rpcPreInterceptorList;
	private List<RpcPostInterceptor> rpcPostInterceptorList;
	private Map<String, String> proxy;

	public SimpleRpcConfigRegister() {
		this.rpcPreInterceptorList = new ArrayList<>();
		this.rpcPostInterceptorList = new ArrayList<>();
		proxy = new HashMap<>();
	}

	public void addPreInterceptor(RpcPreInterceptor rpcPreInterceptor) {
		rpcPreInterceptorList.add(rpcPreInterceptor);
	}

	public List<RpcPreInterceptor> getAllPreInterceptorList() {
		return rpcPreInterceptorList;
	}


	public void addPostInterceptor(RpcPostInterceptor rpcPostInterceptor) {
		rpcPostInterceptorList.add(rpcPostInterceptor);
	}

	public List<RpcPostInterceptor> getAllPostInterceptorList() {
		return rpcPostInterceptorList;
	}

	public void addProxy(String key, String url) {
		Assert.isTrue(StrUtil.isNotBlank(key), "代理key不能为空");
		Assert.isTrue(StrUtil.isNotBlank(url), "代理url不能为空");
		this.proxy.put(key, url);
	}

	public void addMoreProxy(Map<String, String> map) {
		if (CollectionUtil.isEmpty(map)) {
			return;
		}
		map.forEach(this::addProxy);
	}

	public Map<String, String> getProxy() {
		return proxy;
	}
}
