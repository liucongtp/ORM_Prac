package com.sorm.bean;
/**
 * �������ݿ���������Java��
 * ��װ��java���Ժ�get��set������Դ����
 * @author liucong
 *
 */
public class JavaFieldGetSet {
	/**
	 * ���Ե�Դ����Ϣ���磺private int userId;
	 */
	private String fieldInfo;
	/**
	 * get������Դ����Ϣ.�磺public int getUserId(){}
	 */
	private String getInfo;
	/**
	 * set������Դ����Ϣ.�磺public void setUserId(int id){this.id = id;}
	 */
	private String setInfo;
	@Override
	public String toString() {
		System.out.println(fieldInfo);
		System.out.println(getInfo);
		System.out.println(setInfo);
		return super.toString();
	}
	public String getFieldInfo() {
		return fieldInfo;
	}
	public void setFieldInfo(String fieldInfo) {
		this.fieldInfo = fieldInfo;
	}
	public String getGetInfo() {
		return getInfo;
	}
	public void setGetInfo(String getInfo) {
		this.getInfo = getInfo;
	}
	public String getSetInfo() {
		return setInfo;
	}
	public void setSetInfo(String setInfo) {
		this.setInfo = setInfo;
	}
	public JavaFieldGetSet(String fieldInfo, String getInfo, String setInfo) {
		super();
		this.fieldInfo = fieldInfo;
		this.getInfo = getInfo;
		this.setInfo = setInfo;
	}
	public JavaFieldGetSet() {
		
	}
}
