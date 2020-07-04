package com.sorm.core;
/**
 * Query接口的工厂类:负责根据配置信息创建query对象
 * @author liucong
 *创建Query对象的工厂类--工厂类的单利模式
 */
public class QueryFactory {
	
	private static QueryFactory factory = new QueryFactory();
	private static Query prototypeObj;//原型对象
	static {
		try {
			Class c = Class.forName(DBManager.getConf().getQueryClass());
			prototypeObj = (Query) c.newInstance();
		}catch(Exception e) {
		}
	}
	
	private QueryFactory() {//单例模式，构造私有
	}
	

	//配置文件配置谁，这边就获取谁
	/**
	 * 采用克隆模式获取Query对象，提高效率
	 * @return Query对象
	 */
	public static Query createQuery() {
		try {
			return (Query) prototypeObj.clone();//注意要在Query类中实现Cloneable接口的clone方法
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
