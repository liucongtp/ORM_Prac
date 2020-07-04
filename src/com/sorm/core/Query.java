/**
 * 所有数据库处理问题相同的方法提到抽象类中实现
 */
package com.sorm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sorm.bean.ColumnInfo;
import com.sorm.bean.TableInfo;
import com.sorm.utils.JDBCUtils;
import com.sorm.utils.ReflectUtils;

/**
 * 我们在写一个框架的时候尽量把注释写全一点,方便其他人使用
 * @author liucong
 *
 *此接口负责查询(对外提供服务的核心类)--增删改(传进去对象出来SQL)查(传进去SQL出来对象)
 *注意:在设计接口时,要
 */
public abstract class Query implements Cloneable{
	/**
	 * 执行查询的模板--采用模板方法模式将JDBC操作封装成模板，便于重用
	 * @param sql sql语句
	 * @param params sql参数
	 * @param clazz 记录要封装的java类
	 * @param back CallBack的实现类，实现回调  匿名
	 * @return 查询对象
	 */
	public Object executeQueryTemplate(String sql,Object[] params,Class clazz,CallBack back) {

		Connection conn = DBManager.getConn();
		List list = null;//存储查询结果的容器
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			//给sql设参
			JDBCUtils.handleParams(ps, params);
			System.out.println(ps);
			rs = ps.executeQuery();
			
			return back.doExecute(conn, ps, rs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBManager.close(ps,conn);
		}
	}
	
	
	/**
	 * 直接执行一个DML语句
	 * @param sql sql语句
	 * @param params 参数
	 * @return 执行SQL语句后影响记录的行数
	 */
	public int executeDML(String sql,Object[] params) {
		Connection conn = DBManager.getConn();
		int count = 0;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			//给sql设参
			JDBCUtils.handleParams(ps, params);
			System.out.println(ps);
			count = ps.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			DBManager.close(ps,conn);
		}
		return count;
	}
	/**
	 * 将一个对象存储到数据库中
	 * @param obj 要存储的对象
	 */
	public void insert(Object obj) {

		Class c = obj.getClass();
		//存储sql的参数对象
		List<Object> params = new ArrayList<Object>();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		StringBuilder sql = new StringBuilder("insert into "+tableInfo.getTname()+" (");
		int countNotNullField = 0;//计算不为null的属性值
		Field[] fs = c.getDeclaredFields();
		for(Field f:fs) {
			String fieldName = f.getName();
			Object fieldValue = ReflectUtils.invokeGet(fieldName, obj);
			
			if(fieldValue != null) {
				countNotNullField++;
				sql.append(fieldName+",");
				params.add(fieldValue);
			}
		}
		
		sql.setCharAt(sql.length()-1, ')');
		sql.append(" values (");
		for(int i = 0; i < countNotNullField; i++) {
			sql.append("?,");
		}
		sql.setCharAt(sql.length()-1, ')');
		
		executeDML(sql.toString(), params.toArray());
	}
	/**
	 * 删除clazz表示类对应的表中的记录(指定主键值id的记录)
	 * @param clazz 跟表对应的类的Class对象
	 * @param id 主键的值
	 */
	public void delete(Class clazz,Object id) {
		//delete from User where id=2;

		//通过Class对象找TableInfo
		TableInfo tableInfo = TableContext.poClassTableMap.get(clazz);
		//获得主键
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
		
		String sql = "delete from "+tableInfo.getTname()+" where "+onlyPriKey.getName()+"=? ";
		executeDML(sql, new Object[] {id});
	}
	/**
	 * 删除对象在数据库中对应的记录(对象所在的类对应到表,对象的主键的值对应到记录)
	 * @param obj 记录
	 */
	public void delete(Object obj) {

		Class c = obj.getClass();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
		
		//通过反射机制，调用属性对应的get,set方法
		Object priKeyValue = ReflectUtils.invokeGet(onlyPriKey.getName(), obj);
		
		delete(c,priKeyValue);
	}
	/**
	 * 更新对象对应的记录,并且只更新指定的字段的值
	 * @param obj 所要更新的对象
	 * @param fieldNames 更新的属性列表
	 * @return 执行SQL语句后影响记录的行数
	 */
	public int update(Object obj,String[] fieldNames) {
		//update user set uname=?,pwd=?;

		Class c = obj.getClass();
		List<Object> params = new ArrayList<Object>();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c); 
		ColumnInfo priKey = tableInfo.getOnlyPriKey();
		StringBuilder sql = new StringBuilder("update "+tableInfo.getTname()+" set ");
		
		for(String fname:fieldNames) {
			Object fvalue = ReflectUtils.invokeGet(fname, obj);
			params.add(fvalue);
			sql.append(fname+"=?,");
		}
		sql.setCharAt(sql.length()-1, ' ');
		sql.append(" where ");
		sql.append(priKey.getName()+"=? ");
		
		params.add(ReflectUtils.invokeGet(priKey.getName(), obj));
		
		return executeDML(sql.toString(), params.toArray());
	}
	/**
	 * 查询返回多行记录,并将该记录封装到clazz指定的类的对象中  查询多行多列
	 * @param sql 查询语句
	 * @param clazz 封装数据的javabean类的Class对象
	 * @param params 查询到的结果
	 * @return 查询记录
	 */
	public List queryRows(final String sql,final Class clazz,final Object[] params) {

		final List list = new ArrayList();//存储查询结果的容器
		//使用模板，在回调里定义实现--匿名内部类
		return (List)executeQueryTemplate(sql, params, clazz, new CallBack() {
			@Override
			public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
				//每次查询都不一样的地方
				ResultSetMetaData metaData;
				try {
					List list = null;
					metaData = rs.getMetaData();
					//多行
					while(rs.next()) {
						if(list == null) {
							list = new ArrayList();
						}
						Object rowObj = clazz.newInstance();
						//多列
						for(int i = 0; i < metaData.getColumnCount(); i++) {
							String columnName = metaData.getColumnLabel(i+1);
							Object columnValue = rs.getObject(i+1);
							
							//调用rowObj对象的setUsername(String uname)方法，将columnValue的值设置进去
							ReflectUtils.invokeSet(rowObj, columnName, columnValue);
						}
						list.add(rowObj);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return list;
			}
		});
	}
	
	/**
	 * 查询返回一行记录,并将该记录封装到clazz指定的类的对象中  查询一行多列
	 * @param sql 查询语句
	 * @param clazz 封装数据的javabean类的Class对象
	 * @param params 查询到的结果
	 * @return 查询的一行多列记录
	 */
	public Object queryUniqueRow(String sql,Class clazz,Object[] params) {
		List list = queryRows(sql, clazz, params);
		return (list == null && list.size()>0) ? null : list.get(0);
	}
	
	/**
	 * 查询返回一个值(一行一列),并将该值返回
	 * @param sql 查询语句
	 * @param params SQL的参数
	 * @return 查询到的结果
	 */
	public Object queryValue(String sql,Object[] params) {
		return executeQueryTemplate(sql,params,null,new CallBack() {
			@Override
			public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
				Object value = null;
				try {
					while(rs.next()) {
						value = rs.getObject(1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return value;
			}
		});
	}
	
	/**
	 * 查询返回一个数字(一行一列),并将该值返回
	 * @param sql 查询语句
	 * @param params SQL的参数
	 * @return 查询到的数字
	 */
	public Number queryNumber(String sql,Object[] params) {
		return (Number)queryValue(sql,params);
	}
	
	
	/**
	 * 分页查询--各个实现类不一样
	 * @param pageNum 第几页数据
	 * @param size 每页显示多少记录
	 * @return 
	 */
	public abstract Object queryPagenate(int pageNum,int size);
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}