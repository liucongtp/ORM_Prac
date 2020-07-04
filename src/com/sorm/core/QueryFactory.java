package com.sorm.core;
/**
 * Query�ӿڵĹ�����:�������������Ϣ����query����
 * @author liucong
 *����Query����Ĺ�����--������ĵ���ģʽ
 */
public class QueryFactory {
	
	private static QueryFactory factory = new QueryFactory();
	private static Query prototypeObj;//ԭ�Ͷ���
	static {
		try {
			Class c = Class.forName(DBManager.getConf().getQueryClass());
			prototypeObj = (Query) c.newInstance();
		}catch(Exception e) {
		}
	}
	
	private QueryFactory() {//����ģʽ������˽��
	}
	

	//�����ļ�����˭����߾ͻ�ȡ˭
	/**
	 * ���ÿ�¡ģʽ��ȡQuery�������Ч��
	 * @return Query����
	 */
	public static Query createQuery() {
		try {
			return (Query) prototypeObj.clone();//ע��Ҫ��Query����ʵ��Cloneable�ӿڵ�clone����
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
