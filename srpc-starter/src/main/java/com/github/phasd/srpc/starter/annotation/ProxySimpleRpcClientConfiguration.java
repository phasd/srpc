package com.github.phasd.srpc.starter.annotation;

import cn.hutool.core.util.ArrayUtil;
import com.github.phasd.srpc.core.rpc.SimpleRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ProxySimpleRpcClientConfiguration
 *
 * @author phz
 * @date 2020-07-27 19:02:48
 * @since V1.0
 */
@ConditionalOnClass({SimpleRpc.class})
@ConditionalOnBean(SimpleRpc.class)
public class ProxySimpleRpcClientConfiguration implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes enableRpc = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableSimpleRpc.class.getCanonicalName(), false));
		if (enableRpc == null) {
			throw new IllegalArgumentException(String.format("导入的类: %s ,不存在 EnableSimpleRpc 标注 ", importMetadata.getClassName()));
		}
		String[] basePackages = enableRpc.getStringArray("basePackages");
		Class<?>[] basePackageClasses = enableRpc.getClassArray("basePackageClasses");
		if (ArrayUtil.isEmpty(basePackages)) {
			if (ArrayUtil.isEmpty(basePackageClasses)) {
				return;
			}
			basePackages = new String[basePackageClasses.length];
			for (int i = 0; i < basePackageClasses.length; i++) {
				basePackages[i] = basePackageClasses[i].getPackage().getName();
			}
		}
		if (ArrayUtil.isEmpty(basePackages)) {
			return;
		}
		AdviceMode mode = enableRpc.getEnum("mode");
		CommonWebBeanDefinitionScanner scanner = new CommonWebBeanDefinitionScanner(registry, RpcClient.class, RpcClientFactoryBean.class, mode);
		scanner.doScan(basePackages);
	}
}
