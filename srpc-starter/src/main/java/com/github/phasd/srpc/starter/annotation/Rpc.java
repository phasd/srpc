package com.github.phasd.srpc.starter.annotation;

import org.springframework.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-27 16:10:15
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Rpc {
	String url();

	HttpMethod method();

	boolean async() default false;
}
