package com.sorm.core;
/**
 * 负责Java数据类型和数据库类型的互相转换
 * @author liucong
 *
 */
public interface TypeConvertor {
	/**
	 * 将数据库数据类型转化成Java的数据类型
	 * @param columnType 数据库字段的数据类型
	 * @return Java数据类型
	 */
	public String databaseType2JavaType(String columnType);
	/**
	 * 将java数据类型转化成数据库的数据类型
	 * @param columnType Java数据类型
	 * @return 数据库的数据类型
	 */
	public String javaType2DatabaseType(String javaDataType);
}
