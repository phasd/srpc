package com.github.phasd.srpc.starter.annotation;

import org.springframework.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rpc
 *
 * @author phz
 * @date 2020-07-27 16:10:15
 * @since V1.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Rpc {
	/**
	 * 请求url
	 */
	String url();

	/**
	 * 请求方法
	 */
	HttpMethod method();

	/**
	 * 是否是异步请求
	 */
	boolean async() default false;
}
