package com.github.srpc.core.rpc.mvc;

import com.github.srpc.core.rpc.CommonWebConstants;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-24 13:40:17
 */
public class EncryptFromFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String dbcSecret = request.getHeader(CommonWebConstants.SIMPLE_RPC_SECRET);
		String dbcFromSecret = request.getHeader(CommonWebConstants.SIMPLE_RPC_SECRET_FROM);
		if (CommonWebConstants.SIMPLE_RPC_SECRET.equals(dbcSecret) && CommonWebConstants.SIMPLE_RPC_SECRET_FROM.equals(dbcFromSecret)) {
			request = new EncryptFormHttpServletWrapper(request);
		}
		filterChain.doFilter(request, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
