package com.github.srpc.starter;

import com.github.srpc.core.rpc.CommonWebConstants;
import com.github.srpc.core.rpc.SecretHttpInputMessage;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-21 13:32:59
 */
@ControllerAdvice
@RestControllerAdvice
public class RequestSecretData extends RequestBodyAdviceAdapter {

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		String appId = inputMessage.getHeaders().getFirst(CommonWebConstants.APPID);
		return new SecretHttpInputMessage(inputMessage.getBody(), inputMessage.getHeaders(), appId);
	}
}
