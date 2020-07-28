package com.github.srpc.starter.annotation;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-28 11:33:56
 */
public enum ParameterType {
	// 头部参数
	HEADER,
	// form 参数
	PARAM,
	// 文件参数
	PART,
	// 路径参数
	PATH,
	// 体参数
	BODY
}