package com.github.phasd.srpc.core.rpc;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.GlobalBouncyCastleProvider;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;

/**
 * 内容加密处理
 *
 * @author phz
 * @date 2020-07-21 15:34:10
 * @since V1.0
 */
public class CryptContent {

	private CryptContent() {}

	/**
	 * 参数解密
	 *
	 * @param encrypt   加密数据
	 * @param appid     appid
	 * @param secretKey secretKey
	 * @return 解密后的内容
	 */
	public static String getDecryptContent(String encrypt, String appid, String secretKey) {
		GlobalBouncyCastleProvider.setUseBouncyCastle(false);
		if (StrUtil.isBlank(encrypt)) {
			return StrUtil.EMPTY_JSON;
		}
		try {
			return decrypt(encrypt, appid, secretKey);
		} catch (Exception e) {
			throw new SimpleRpcException("服务调用的链路上下文解密失败", e);
		}
	}

	/**
	 * sha1 散列
	 *
	 * @param data 需要签名的数据
	 * @return sha1签名
	 */
	public static String getSign(String data) {
		if (StrUtil.isBlank(data)) {
			return data;
		}
		return SecureUtil.sha1(data);
	}

	/**
	 * 参数加密
	 *
	 * @param ret       要加密的数据
	 * @param appid     appid
	 * @param secretKey 密钥
	 * @return 加密后的结果
	 */
	public static String getEncryptContent(String ret, String appid, String secretKey) {
		try {
			GlobalBouncyCastleProvider.setUseBouncyCastle(false);
			return encrypt(ret, appid, secretKey);
		} catch (Exception e) {
			throw new SimpleRpcException("服务调用的链路上下文加密失败", e);
		}
	}

	/**
	 * 参数加密
	 *
	 * @param content   要加密的数据
	 * @param appid     appid
	 * @param secretKey 密钥
	 * @return 加密后的结果
	 * @throws Exception 异常
	 */
	public static String encrypt(String content, String appid, String secretKey) throws Exception {
		AES aes = new AES(Mode.ECB, Padding.PKCS5Padding, SecureUtil.md5(secretKey + appid).getBytes(StandardCharsets.UTF_8));
		return aes.encryptBase64(content);
	}

	/**
	 * 参数解密
	 *
	 * @param content   要解密的数据
	 * @param appid     appid
	 * @param secretKey 密钥
	 * @return 加密后的结果
	 * @throws Exception 异常
	 */
	public static String decrypt(String content, String appid, String secretKey) throws Exception {
		AES aes = new AES(Mode.ECB, Padding.PKCS5Padding, SecureUtil.md5(secretKey + appid).getBytes(StandardCharsets.UTF_8));
		return aes.decryptStr(content);
	}
}
