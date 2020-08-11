package com.github.phasd.srpc.starter.annotation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * BeanDefinitionScanner
 *
 * @author phz
 * @date 2020-07-28 19:08:24
 * @since V1.0
 */
public class CommonWebBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
	/**
	 * factoryBeanClass
	 */
	private Class<?> factoryBeanClass;

	/**
	 * 代理模式
	 */
	private AdviceMode mode;


	/**
	 * @param registry         BeanDefinitionRegistry
	 * @param type             扫描的type
	 * @param factoryBeanClass factoryBeanClass
	 * @param mode             代理模式
	 */
	public CommonWebBeanDefinitionScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> type, Class<?> factoryBeanClass, AdviceMode mode) {
		super(registry, false);
		super.addIncludeFilter(new AnnotationTypeFilter(type));
		Assert.notNull(factoryBeanClass, "factoryBeanClass不能为null");
		this.factoryBeanClass = factoryBeanClass;
		this.mode = mode;
	}

	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
		if (CollectionUtil.isNotEmpty(beanDefinitions)) {
			this.processBeanDefinitions(beanDefinitions);
		}
		return beanDefinitions;
	}


	/**
	 * 处理扫描结果
	 *
	 * @param beanDefinitions 扫描获取的BeanDefinitionHolder
	 */
	private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
		for (BeanDefinitionHolder holder : beanDefinitions) {
			GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
			String beanClassName = definition.getBeanClassName();
			definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
			definition.getConstructorArgumentValues().addGenericArgumentValue(this.mode);
			definition.setBeanClass(this.factoryBeanClass);
			definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		}
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
	}
}

