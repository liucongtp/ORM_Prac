package com.sorm.utils;

import java.lang.reflect.Method;

/**
 * 封装反射基本操作
 * @author liucong
 *
 */
public class ReflectUtils {
	/**
	 * 调用obj对象对应属性fieldName的get()方法
	 * @param fieldName 属性名
	 * @param obj 对象名
	 * @return 对象
	 */
	public static Object invokeGet(String fieldName,Object obj) {
		//获得get()
		try {
			Class c = obj.getClass();
			Method m = c.getDeclaredMethod("get"+StringUtils.firstChar2UpperCase(fieldName), null);
			return m.invoke(obj, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public static void invokeSet(Object obj,String columnName,Object columnValue) {
		try {
			Method m = obj.getClass().getDeclaredMethod("set"+StringUtils.firstChar2UpperCase(columnName), columnValue.getClass());
			m.invoke(obj, columnValue);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
