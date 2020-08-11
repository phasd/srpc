package com.github.phasd.srpc.starter.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableSimpleRpc
 *
 * @author phz
 * @date 2020-07-28 19:08:24
 * @since V1.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ProxySimpleRpcClientConfiguration.class)
public @interface EnableSimpleRpc {

	/**
	 * basePackages
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * basePackages
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * basePackageClasses
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * AdviceMode
	 */
	AdviceMode mode() default AdviceMode.PROXY;
}
