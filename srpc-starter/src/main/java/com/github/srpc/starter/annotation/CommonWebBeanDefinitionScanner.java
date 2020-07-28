package com.github.srpc.starter.annotation;

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
 * @description:
 * @author: phz
 * @create: 2020-07-28 09:16:31
 */
public class CommonWebBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
	private Class<?> factoryBeanClass;
	private AdviceMode mode;

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

