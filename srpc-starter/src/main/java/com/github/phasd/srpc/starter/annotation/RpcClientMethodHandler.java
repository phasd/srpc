package com.github.phasd.srpc.starter.annotation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.github.phasd.srpc.core.rpc.RpcUtils;
import com.github.phasd.srpc.core.rpc.SimpleRpc;
import com.github.phasd.srpc.core.rpc.SimpleRpcException;
import com.github.phasd.srpc.core.rpc.request.Request;
import com.github.phasd.srpc.starter.SpringContextUtils;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 代理方法执行
 *
 * @author phz
 * @date 2020-07-28 10:17:17
 * @since V1.0
 */
public class RpcClientMethodHandler {
	/**
	 * 方法注解
	 */
	private final Rpc rpc;

	/**
	 * 代理的方法
	 */
	private final Method method;

	/**
	 * 参数的元数据
	 */
	private final List<ParameterMetaData> parameterMetaDataList;

	/**
	 * @param rpc    方法注解
	 * @param method 代理的方法
	 */
	public RpcClientMethodHandler(Rpc rpc, Method method) {
		this.rpc = rpc;
		parameterMetaDataList = init(method);
		this.method = method;
	}


	/**
	 * 方法执行
	 *
	 * @param target target
	 * @param args   参数
	 * @return 执行结果
	 */
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


	/**
	 * 同步执行
	 *
	 * @param request 请求Request
	 * @return 返回结果
	 */
	private Object doInvoke(Request<?> request) {
		Type returnType = method.getGenericReturnType();
		SimpleRpc bean = SpringContextUtils.getBean(SimpleRpc.class);
		if (returnType != null && List.class.isAssignableFrom(TypeUtil.getClass(returnType))) {
			Type retType = getResClass(method.getGenericReturnType());
			return bean.getForList(request, retType);
		} else {
			return bean.getForObject(request, returnType);
		}
	}

	/**
	 * 如果是ParameterizedType 则返回泛型参数的第一个否则返回原type
	 *
	 * @param type 返回type
	 * @return 返回结果
	 */
	private Type getResClass(Type type) {
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments()[0];
		}
		return type;
	}

	/**
	 * 异步执行
	 *
	 * @param request 请求Request
	 * @return 返回结果
	 */
	private Object doInvokeAsync(Request<?> request) {
		Type returnType = method.getGenericReturnType();
		if (returnType != null && !CompletableFuture.class.isAssignableFrom((TypeUtil.getClass(returnType)))) {
			throw new SimpleRpcException("异步服务调用返回必须是CompleteFuture");
		}

		SimpleRpc bean = SpringContextUtils.getBean(SimpleRpc.class);
		if (returnType == null) {
			return bean.getForObjectAsync(request, returnType);
		}
		Type resType = getResClass(method.getGenericReturnType());
		if (List.class.isAssignableFrom(TypeUtil.getClass(resType))) {
			resType = getResClassAsync(method.getGenericReturnType());
			return bean.getForListAsync(request, resType);
		} else {
			return bean.getForObjectAsync(request, resType);
		}
	}

	/**
	 * 如果是ParameterizedType 则返回泛型参数的第一个的泛型参数列表的第一个
	 * CompletableFuture<String> => String
	 * CompletableFuture<List<String => 返回String
	 *
	 * @param type 返回type
	 * @return 返回结果
	 */
	private Type getResClassAsync(Type type) {
		if (type instanceof ParameterizedType) {
			Type first = ((ParameterizedType) type).getActualTypeArguments()[0];
			if (first instanceof ParameterizedType) {
				return ((ParameterizedType) first).getActualTypeArguments()[0];
			}
			return first;
		}
		return type;
	}

	/**
	 * 路径参数处理
	 *
	 * @param builder           RequestBuilder
	 * @param parameterMetaData 参数元数据
	 * @param arg               参数
	 */
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

	/**
	 * 头部参数处理
	 *
	 * @param builder           RequestBuilder
	 * @param parameterMetaData 参数元数据
	 * @param arg               参数
	 */
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

	/**
	 * List处理
	 *
	 * @param arg 参数
	 */
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

	/**
	 * form参数处理
	 *
	 * @param builder           RequestBuilder
	 * @param parameterMetaData 参数元数据
	 * @param arg               参数
	 */
	private void processParam(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		if (arg == null) {
			return;
		}
		List<Object> list = getList(arg);

		MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.put(parameterMetaData.getName(), list);
		builder.formParams(multiValueMap);
	}

	/**
	 * body参数处理
	 *
	 * @param builder           RequestBuilder
	 * @param parameterMetaData 参数元数据
	 * @param arg               参数
	 */
	private void processBody(Request.RequestBuilder builder, ParameterMetaData parameterMetaData, Object arg) {
		builder.setBody(arg);
	}

	/**
	 * multipart 参数处理
	 *
	 * @param builder           RequestBuilder
	 * @param parameterMetaData 参数元数据
	 * @param arg               参数
	 */
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

	/**
	 * 构建RequestBuilder
	 *
	 * @param target target
	 * @param rpc    方法注解
	 * @return RequestBuilder
	 */
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

	/**
	 * 初始化方法元数据
	 *
	 * @param method 代理方法
	 * @return 方法参数元数据
	 */
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