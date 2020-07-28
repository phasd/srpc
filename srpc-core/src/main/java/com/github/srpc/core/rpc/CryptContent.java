package com.github.srpc.core.rpc;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.GlobalBouncyCastleProvider;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-21 15:34:10
 */
public class CryptContent {
	private static String ENCRYPT_KEY = "1234567890abcdefghijklmnopqrstuvwxyz@";

	private CryptContent() {}

	public static String getDecryptContent(String encrypt, String appid) {
		GlobalBouncyCastleProvider.setUseBouncyCastle(false);
		if (StrUtil.isBlank(encrypt)) {
			return StrUtil.EMPTY_JSON;
		}
		try {
			return decrypt(encrypt, appid);
		} catch (Exception e) {
			throw new SimpleRpcException("服务调用的链路上下文解密失败", e);
		}
	}

	public static String getSign(String data) {
		if (StrUtil.isBlank(data)) {
			return data;
		}
		return SecureUtil.sha1(data);
	}

	public static String getEncryptContent(String ret, String appid) {
		try {
			GlobalBouncyCastleProvider.setUseBouncyCastle(false);
			return encrypt(ret, appid);
		} catch (Exception e) {
			throw new SimpleRpcException("服务调用的链路上下文加密失败", e);
		}
	}


	public static String encrypt(String content, String appid) throws Exception {
		AES aes = new AES(Mode.ECB, Padding.PKCS5Padding, SecureUtil.md5(ENCRYPT_KEY + appid).substring(0, 16).getBytes(StandardCharsets.UTF_8));
		return aes.encryptBase64(content);
	}

	public static String decrypt(String content, String appid) throws Exception {
		AES aes = new AES(Mode.ECB, Padding.PKCS5Padding, SecureUtil.md5(ENCRYPT_KEY + appid).substring(0, 16).getBytes(StandardCharsets.UTF_8));
		return aes.decryptStr(content);
	}
}
