package com.github.phasd.srpc.core.rpc;

/**
 * SimpleRpcException
 *
 * @author phz
 * @date 2020-07-28 19:08:24
 * @since V1.0
 */
public class SimpleRpcException extends RuntimeException {
	/**
	 * @param e pre exception
	 */
	public SimpleRpcException(Throwable e) {
		super(e);
	}

	/**
	 * @param message 异常提示信息
	 */
	public SimpleRpcException(String message) {
		super(message);
	}


	/**
	 * @param message 异常提示信息
	 * @param cause   cause exception
	 */
	public SimpleRpcException(String message, Throwable cause) {
		super(message, cause);
	}
}
