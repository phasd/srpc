package com.github.phasd.srpc.core.rpc.executor;

import com.github.phasd.srpc.core.rpc.RpcContext;
import com.github.phasd.srpc.core.rpc.request.Request;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-23 10:54:39
 */
public class RpcContextThreadDecorator implements TaskDecorator {
	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> headers = RpcContext.getHeaders();
		Request request = RpcContext.getRequest();
		return () -> {
			try {
				RpcContext.setHeaders(headers);
				RpcContext.setRequest(request);
				runnable.run();
			} finally {
				RpcContext.clear();
			}
		};
	}
}
