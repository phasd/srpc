package com.github.phasd.srpc.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RpcClient
 *
 * @author phz
 * @date 2020-07-27 16:10:15
 * @since V1.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcClient {

	/**
	 * url前缀
	 */
	String baseUrl();

}
