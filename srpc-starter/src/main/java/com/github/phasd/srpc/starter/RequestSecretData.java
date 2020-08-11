package com.github.phasd.srpc.starter;

import com.github.phasd.srpc.core.rpc.CommonWebConstants;
import com.github.phasd.srpc.core.rpc.SecretHttpInputMessage;
import com.github.phasd.srpc.core.rpc.SimpleRpcConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 请求body数据处理
 *
 * @author phz
 * @date 2020-07-21 13:32:59
 * @since V1.0
 */
@ControllerAdvice
@RestControllerAdvice
public class RequestSecretData extends RequestBodyAdviceAdapter {

	@Autowired
	private SimpleRpcConfigurationProperties rpcConfig;

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		String appId = inputMessage.getHeaders().getFirst(CommonWebConstants.APPID);
		return new SecretHttpInputMessage(inputMessage.getBody(), inputMessage.getHeaders(), appId, rpcConfig.getSecretKey());
	}
}
