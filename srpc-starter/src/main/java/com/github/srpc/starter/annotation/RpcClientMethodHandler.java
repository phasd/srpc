package com.github.srpc.starter.annotation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.github.srpc.core.rpc.RpcUtils;
import com.github.srpc.core.rpc.SimpleRpc;
import com.github.srpc.core.rpc.request.Request;
import com.github.srpc.starter.SpringContextUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 10:17:17
 */
public class RpcClientMethodHandler {
	private final Rpc rpc;
	private final Method method;
	private final List<ParameterMetaData> parameterMetaDataList;

	public RpcClientMethodHandler(Rpc rpc, Method method) {
		this.rpc = rpc;
		parameterMetaDataList = init(method);
		this.method = method;
	}

	public Object invoke(RpcClientTarget target, Object[] args) {
		Request.RequestBuilder builder = getBuilder(target, rpc);
		Request<?> request;
		if (ArrayUtil.isEmpty(args)) {
			request = builder.build();
		} else {
			for (int i = 0; i < args.length; i++) {
				ParameterMetaData parameterMetaData = parameterMetaDataList.get(i);
				Object arg = args[i];
				switch (parameterMetaData.getParamterType()) {
					case PART:
						processPart(builder, parameterMetaData, arg);
						break;
					case BODY:
						processBody(builder, parameterMetaData, arg);
						break;
					case PARAM:
						processParam(builder, parameterMetaData, arg);
						break;
					case HEADER:
						processHeader(builder, parameterMetaData, arg);
						break;
					case PATH:
						processPath(builder, parameterMetaData, arg);
						break;
					default:
						break;
				}
			}
			request = builder.build();
		}

		if (rpc.async()) {
			return doInvokeAsync(request);
		}
		return doInvoke(request);
	}

	private Object doInvoke(Request<?> request) {
		Class<?> returnType = method.getReturnType();
		SimpleRpc bean = SpringContextUtils.getBean(SimpleRpc.class);
		if (List.class.isAssignableFrom(returnType)) {
			return bean.getForList(request, returnType);
		} else {
			return bean.getForObject(request, returnType);
		}
	}

	private Object doInvokeAsync(Request<?> request) {
		Class<?> returnType = method.getReturnType();
		SimpleRpc bean = SpringContextUtils.getBean(SimpleRpc.class);
		if (List.class.isAssignableFrom(returnType)) {
			return bean.getForListAsync(request, returnType);
		} else {
			return bean.getForObjectAsync(request, returnType);
		}
	}

	private void processPath(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		if (arg == null) {
			return;
		}
		Class<?> argClass = arg.getClass();
		if (!ClassUtil.isBasicType(argClass) && !(arg instanceof String)) {
			throw new IllegalArgumentException("@PathVariable 标注的参数必须是基本类型或者字符串和基本类型的包装类");
		}

		builder.uriParam(parameterMetaData.getName(), String.valueOf(arg));
	}

	private void processHeader(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		if (arg == null) {
			return;
		}
		List<Object> list = getList(arg);

		List<String> paramList = new ArrayList<>();
		for (Object obj : list) {
			if (obj == null) {
				continue;
			}
			paramList.add(String.valueOf(list));
		}

		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.put(parameterMetaData.getName(), paramList);
		builder.headers(headerMap);
	}

	@SuppressWarnings("unchecked")
	private List<Object> getList(Object arg) {
		Class<?> argClass = arg.getClass();
		List<Object> list;
		if (argClass.isArray()) {
			Object[] arr = (Object[]) arg;
			list = CollectionUtil.toList(arr);
		} else if (List.class.isAssignableFrom(argClass)) {
			list = (List) arg;
		} else {
			list = Collections.singletonList(arg);
		}
		return list;
	}

	private void processParam(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		if (arg == null) {
			return;
		}
		List<Object> list = getList(arg);

		MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.put(parameterMetaData.getName(), list);
		builder.formParams(multiValueMap);
	}

	private void processBody(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		builder.setBody(arg);
	}

	private void processPart(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		if (arg == null) {
			return;
		}

		List<Object> list = getList(arg);

		for (Object o : list) {
			if (!Resource.class.isAssignableFrom(o.getClass())) {
				throw new IllegalArgumentException("@RequestPart 标注的参数必须是Resource类型");
			}
		}

		MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.put(parameterMetaData.getName(), list);
		builder.formParams(multiValueMap);
	}

	private Request.RequestBuilder getBuilder(RpcClientTarget target, Rpc rpc) {
		String baseUrl = target.getBaseUrl();
		String url = rpc.url();
		HttpMethod httpMethod = rpc.method();
		Assert.isFalse(StrUtil.isBlank(baseUrl), "%s 的baseUrl不能为空", target.getProxyInterface().getName());
		Assert.isFalse(StrUtil.isBlank(url), "%s-%s 的url不能为空", target.getProxyInterface().getName(), method.getName());

		StringBuilder sb = new StringBuilder();
		sb.append(RpcUtils.URL_DELIMITER);
		sb.append(RpcUtils.trimUrlDelimiter(baseUrl));
		sb.append(RpcUtils.URL_DELIMITER);
		sb.append(url);
		return new Request.RequestBuilder(httpMethod, sb.toString());
	}

	private List<ParameterMetaData> init(Method method) {
		Parameter[] parameters = method.getParameters();
		if (ArrayUtil.isEmpty(parameters)) {
			return Collections.emptyList();
		}

		List<ParameterMetaData> list = new ArrayList<>();
		for (Parameter parameter : parameters) {
			ParameterMetaData parameterMetaData = new ParameterMetaData();
			if (parameter.isAnnotationPresent(RequestHeader.class)) {
				parameterMetaData.setParamterType(ParameterType.HEADER);
				RequestHeader header = AnnotationUtils.findAnnotation(parameter, RequestHeader.class);
				parameterMetaData.setName(header.name());
				list.add(parameterMetaData);
				continue;
			}

			if (parameter.isAnnotationPresent(RequestParam.class)) {
				parameterMetaData.setParamterType(ParameterType.PARAM);
				RequestParam param = AnnotationUtils.findAnnotation(parameter, RequestParam.class);
				parameterMetaData.setName(param.name());
				list.add(parameterMetaData);
				continue;
			}


			if (parameter.isAnnotationPresent(RequestPart.class)) {
				parameterMetaData.setParamterType(ParameterType.PART);
				RequestPart part = AnnotationUtils.findAnnotation(parameter, RequestPart.class);
				parameterMetaData.setName(part.name());
				list.add(parameterMetaData);
				continue;
			}

			if (parameter.isAnnotationPresent(RequestBody.class)) {
				parameterMetaData.setParamterType(ParameterType.BODY);
				String name = parameter.getName();
				parameterMetaData.setName(name);
				list.add(parameterMetaData);
				continue;
			}

			if (parameter.isAnnotationPresent(PathVariable.class)) {
				parameterMetaData.setParamterType(ParameterType.PATH);
				PathVariable path = AnnotationUtils.findAnnotation(parameter, PathVariable.class);
				parameterMetaData.setName(path.name());
				list.add(parameterMetaData);
			}
		}
		return list;
	}
}
