package org.zhinanzhen.tb.util;

import com.ikasoa.core.utils.StringUtil;
import org.junit.Test;
import org.zhinanzhen.tb.utils.MD5Util;

import junit.framework.TestCase;

public class MD5UtilTest extends TestCase {
	
	@Test
	public void testToMD5() throws Exception {
		System.out.println(MD5Util.getMD5("1111"));
//		assertEquals(MD5Util.getMD5("hello1234"), "9a1996efc97181f0aee18321aa3b3b12");
	}

	public static void main(String[] args) {
		String path = StringUtil.merge("C:\\Users\\yjt\\Desktop\\pdf\\data\\uploads\\payment_voucher_image_url_s2\\", "newFileName", ".", "type");
		System.out.println(path);
	}
}
