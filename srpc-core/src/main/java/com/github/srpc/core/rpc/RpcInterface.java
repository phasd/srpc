package com.github.srpc.core.rpc;


import com.github.srpc.core.rpc.request.Request;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @description: Rpc接口
 * @author: phz
 * @create: 2020-07-20 11:53:20
 */
public interface RpcInterface {


	/**
	 * 获取单个对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @return Response
	 */
	<T> T doExecute(Request<?> requestEntity, Class<T> responseType);

	/**
	 * 异步获取单个对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @return Response
	 */
	<T> CompletableFuture<T> doExecuteAsync(Request<?> requestEntity, Class<T> responseType);

	/**
	 * 获取集合对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @return Response
	 */
	<T> List<T> doExecuteArray(Request<?> requestEntity, Class<T> responseType);

	/**
	 * 异步获取集合对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @return Response
	 */
	<T> CompletableFuture<List<T>> doExecuteArrayAsync(Request<?> requestEntity, Class<T> responseType);
}
