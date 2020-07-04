package com.sorm.core;
/**
 * ����Java�������ͺ����ݿ����͵Ļ���ת��
 * @author liucong
 *
 */
public interface TypeConvertor {
	/**
	 * �����ݿ���������ת����Java����������
	 * @param columnType ���ݿ��ֶε���������
	 * @return Java��������
	 */
	public String databaseType2JavaType(String columnType);
	/**
	 * ��java��������ת�������ݿ����������
	 * @param columnType Java��������
	 * @return ���ݿ����������
	 */
	public String javaType2DatabaseType(String javaDataType);
}
