package com.github.phasd.srpc.starter.annotation;

/**
 * ParameterType
 *
 * @author phz
 * @date 2020-07-28 11:33:56
 * @since V1.0
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