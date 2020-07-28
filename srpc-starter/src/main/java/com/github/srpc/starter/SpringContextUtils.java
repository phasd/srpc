package com.github.srpc.starter;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 用于从非Spring管理的对象中获取Spring容器中的Bean
 *
 * @author phz
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

	public static ApplicationContext getApplicationContext() {
		return ApplicationContextHolder.applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (ApplicationContextHolder.applicationContext == null) {
			ApplicationContextHolder.applicationContext = applicationContext;
		}
	}

	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

	private static class ApplicationContextHolder {
		static ApplicationContext applicationContext = null;
	}
}