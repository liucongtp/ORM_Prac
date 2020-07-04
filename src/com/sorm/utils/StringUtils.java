package com.sorm.utils;
/**
 * 封装常用字符串操作
 * @author liucong
 *
 */
public class StringUtils {
	/**
	 * 将目标字符串首字母变为大写
	 * @param 原字符串
	 * @return 首字母大写字符串
	 */
	public static String firstChar2UpperCase(String str) {
		//abcd-->Abcd
		return str.toUpperCase().substring(0,1)+str.substring(1);
	}
}
