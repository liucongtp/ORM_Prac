package com.sorm.utils;
/**
 * ��װ�����ַ�������
 * @author liucong
 *
 */
public class StringUtils {
	/**
	 * ��Ŀ���ַ�������ĸ��Ϊ��д
	 * @param ԭ�ַ���
	 * @return ����ĸ��д�ַ���
	 */
	public static String firstChar2UpperCase(String str) {
		//abcd-->Abcd
		return str.toUpperCase().substring(0,1)+str.substring(1);
	}
}
