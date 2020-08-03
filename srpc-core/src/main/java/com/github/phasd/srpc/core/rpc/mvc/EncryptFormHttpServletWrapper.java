package com.github.phasd.srpc.core.rpc.mvc;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.URLUtil;
import com.github.phasd.srpc.core.rpc.CommonWebConstants;
import com.github.phasd.srpc.core.rpc.CryptContent;
import com.github.phasd.srpc.core.rpc.SimpleRpcException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-24 13:44:18
 */
public class EncryptFormHttpServletWrapper extends HttpServletRequestWrapper {
	private static final String DELIMITERS = "&";

	/***
	 *定义参数集合
	 */
	private MultiValueMap<String, String> map;

	public EncryptFormHttpServletWrapper(HttpServletRequest request) {
		super(request);
		this.map = init(request);
	}

	private MultiValueMap<String, String> init(HttpServletRequest request) {
		String data = request.getParameter(CommonWebConstants.DATA);
		String sign = request.getParameter(CommonWebConstants.SIGN);
		long timestamp = Long.parseLong(request.getParameter(CommonWebConstants.TIMESTAMP));
		String completeSign = CryptContent.getSign(String.format("data=%s&timestamp=%d", data, timestamp) + CommonWebConstants.SIMPLE_RPC_SECRET_FROM);
		if (!Objects.equals(sign, completeSign)) {
			throw new SimpleRpcException("签名验证错误");
		}
		String appId = request.getHeader(CommonWebConstants.APPID);
		data = Base64.decodeStr(data, StandardCharsets.UTF_8);
		String decryptContent = CryptContent.getDecryptContent(data, appId);
		String[] pairs = StringUtils.tokenizeToStringArray(decryptContent, DELIMITERS);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>(pairs.length);
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			if (idx == -1) {
				map.add(URLUtil.decode(pair, StandardCharsets.UTF_8), null);
			} else {
				String name = URLUtil.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
				String value = URLUtil.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
				map.add(name, value);
			}
		}
		return map;
	}

	@Override
	public String getParameter(String name) {
		return map.getFirst(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		if (CollectionUtil.isEmpty(map)) {
			return Collections.emptyMap();
		}
		Map<String, String[]> retMap = new HashMap<>();
		map.forEach((key, valueList) -> {
			if (CollectionUtil.isEmpty(valueList)) {
				retMap.put(key, null);
			} else {
				retMap.put(key, ArrayUtil.toArray(valueList, String.class));
			}
		});
		return retMap;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new Vector<>(map.keySet()).elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		List<String> retList = map.get(name);
		if (CollectionUtil.isEmpty(retList)) {
			retList = Collections.emptyList();
		}
		return ArrayUtil.toArray(retList, String.class);
	}
}
