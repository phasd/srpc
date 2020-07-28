package com.github.srpc.core.rpc;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 19:08:24
 */
public class SimpleRpcException extends RuntimeException {
	public SimpleRpcException(Throwable e) {
		super(e);
	}

	public SimpleRpcException(String message) {
		super(message);
	}

	public SimpleRpcException(String message, Throwable cause) {
		super(message, cause);
	}
}
