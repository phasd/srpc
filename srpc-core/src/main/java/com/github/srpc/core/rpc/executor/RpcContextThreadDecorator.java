package com.github.srpc.core.rpc.executor;

import com.github.srpc.core.rpc.RpcContext;
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
		return () -> {
			try {
				RpcContext.setHeaders(headers);
				runnable.run();
			} finally {
				RpcContext.clear();
			}
		};
	}
}
