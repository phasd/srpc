package com.github.phasd.srpc.starter.annotation;

import lombok.Data;

/**
 * ParameterMetaData 参数元数据
 *
 * @author phz
 * @date 2020-07-28 11:33:56
 * @since V1.0
 */
@Data
public class ParameterMetaData {
	private String name;
	private ParameterType paramterType;
}
