package com.github.phasd.srpc.core.rpc;


import com.github.phasd.srpc.core.rpc.request.Request;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Rpc接口
 *
 * @author phz
 * @date 2020-07-20 11:53:20
 * @since V1.0
 */
public interface RpcInterface {


	/**
	 * 获取单个对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @param <T>           泛型参数
	 * @return Response
	 */
	<T> T doExecute(Request<?> requestEntity, Type responseType);

	/**
	 * 异步获取单个对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @param <T>           泛型参数
	 * @return Response
	 */
	<T> CompletableFuture<T> doExecuteAsync(Request<?> requestEntity, Type responseType);

	/**
	 * 获取集合对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @param <T>           泛型参数
	 * @return Response
	 */
	<T> List<T> doExecuteArray(Request<?> requestEntity, Type responseType);

	/**
	 * 异步获取集合对象
	 *
	 * @param requestEntity RequestEntity
	 * @param responseType  responseType
	 * @param <T>           泛型参数
	 * @return Response
	 */
	<T> CompletableFuture<List<T>> doExecuteArrayAsync(Request<?> requestEntity, Type responseType);
}
