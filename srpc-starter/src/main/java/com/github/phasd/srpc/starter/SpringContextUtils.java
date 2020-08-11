package com.github.phasd.srpc.starter;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 用于从非Spring管理的对象中获取Spring容器中的Bean
 *
 * @author phz
 * @date 2020-07-14 12:34:06
 * @since V1.0
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

	/**
	 * 根据名称获取bean
	 * @param name bean 名称
	 * @return bean的实例化对象
	 */
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	/**
	 * 根据类别获取bean
	 * @param clazz bean 类型
	 * @return bean的实例化对象
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 根据类别和名称获取bean
	 * @param name bean 名称
	 * @param clazz bean 类型
	 * @return bean的实例化对象
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

	private static class ApplicationContextHolder {
		static ApplicationContext applicationContext = null;
	}
}