package com.github.phasd.srpc.core.rpc.executor;

import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @description:
 * @author: phz
 * @create: 2020-07-23 08:50:31
 */
public class RpcThreadPoolExecutor {

	public static ExecutorService getInstance(SimpleRpcConfigurationProperties rpcConfig) {
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("simple-rpc-thread-%d").build();
		int corePoolSize = rpcConfig.getCorePoolSize();
		int maxPoolSize = 30;
		if (rpcConfig.getMaxPoolSize() > 0) {
			maxPoolSize = rpcConfig.getMaxPoolSize();
		}
		int workQueueSize = rpcConfig.getWorkQueueSize();
		long aliveTime = rpcConfig.getAliveTime();
		if (aliveTime < 0) {
			aliveTime = 60;
		}
		final BlockingQueue<Runnable> workQueue;
		if (corePoolSize <= 0) {
			workQueue = new SynchronousQueue<>();
		} else if (workQueueSize <= 0) {
			workQueue = new LinkedBlockingDeque<>();
		} else {
			workQueue = new ArrayBlockingQueue<>(workQueueSize);
		}
		final RpcContextThreadDecorator threadDecorator = new RpcContextThreadDecorator();
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, aliveTime, TimeUnit.SECONDS, workQueue, threadFactory,
				new ThreadPoolExecutor.AbortPolicy()) {
			@Override
			public void execute(Runnable command) {
				Runnable decorated = threadDecorator.decorate(command);
				super.execute(decorated);
			}
		};
	}
}
