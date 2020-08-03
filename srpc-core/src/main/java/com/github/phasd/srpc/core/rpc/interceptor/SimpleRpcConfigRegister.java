package com.github.phasd.srpc.core.rpc.interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-31 15:06:55
 */
public class SimpleRpcConfigRegister {
	private List<RpcPreInterceptor> rpcPreInterceptorList;
	private List<RpcPostInterceptor> rpcPostInterceptorList;

	public SimpleRpcConfigRegister() {
		this.rpcPreInterceptorList = new ArrayList<>();
		this.rpcPostInterceptorList = new ArrayList<>();
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
}
