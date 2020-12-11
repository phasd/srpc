package com.github.phasd.srpc.starter.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author phz
 * @date 2020-11-09 16:43:42
 * @since V1.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface BodyPart {
	@AliasFor("value")
	String name() default "";

	@AliasFor("name")
	String value() default "";
}
