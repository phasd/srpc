package com.github.phasd.srpc.starter.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

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
@Target(ElementType.TYPE)
@Import(ProxySimpleRpcClientConfiguration.class)
public @interface EnableSimpleRpc {

	@AliasFor("value")
	String[] basePackages() default {};

	@AliasFor("basePackages")
	String[] value() default {};

	Class<?>[] basePackageClasses() default {};

	AdviceMode mode() default AdviceMode.PROXY;
}
