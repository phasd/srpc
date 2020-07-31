package com.github.srpc.example;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class ExampleTest {

	@Test
	public void test1() {
		String str = "sfsdf";
//		String s = JSON.parseObject(str, String.class);
//		System.out.println(s);
//
//		String s1 = JSONUtil.toBean(str, String.class);
//		System.out.println(s1);

		List<String> strList = Arrays.asList("sfsdf", "sdsddd");
		System.out.println(JSON.toJSONString(strList));
		System.out.println(JSONUtil.toJsonStr(strList));


		String strAdd = "[\"sdsdd\"]";
		List<String> stringList = JSON.parseArray(strAdd, String.class);
		System.out.println(stringList);
	}
}