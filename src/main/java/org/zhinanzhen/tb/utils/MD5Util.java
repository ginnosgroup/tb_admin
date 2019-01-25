package org.zhinanzhen.tb.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * MD5加密工具类
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public class MD5Util {

	public static String getMD5(String str) throws Exception {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}