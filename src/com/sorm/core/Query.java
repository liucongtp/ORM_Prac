/**
 * �������ݿ⴦��������ͬ�ķ����ᵽ��������ʵ��
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
 * ������дһ����ܵ�ʱ������ע��дȫһ��,����������ʹ��
 * @author liucong
 *
 *�˽ӿڸ����ѯ(�����ṩ����ĺ�����)--��ɾ��(����ȥ�������SQL)��(����ȥSQL��������)
 *ע��:����ƽӿ�ʱ,Ҫ
 */
public abstract class Query implements Cloneable{
	/**
	 * ִ�в�ѯ��ģ��--����ģ�巽��ģʽ��JDBC������װ��ģ�壬��������
	 * @param sql sql���
	 * @param params sql����
	 * @param clazz ��¼Ҫ��װ��java��
	 * @param back CallBack��ʵ���࣬ʵ�ֻص�  ����
	 * @return ��ѯ����
	 */
	public Object executeQueryTemplate(String sql,Object[] params,Class clazz,CallBack back) {

		Connection conn = DBManager.getConn();
		List list = null;//�洢��ѯ���������
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			//��sql���
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
	 * ֱ��ִ��һ��DML���
	 * @param sql sql���
	 * @param params ����
	 * @return ִ��SQL����Ӱ���¼������
	 */
	public int executeDML(String sql,Object[] params) {
		Connection conn = DBManager.getConn();
		int count = 0;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			//��sql���
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
	 * ��һ������洢�����ݿ���
	 * @param obj Ҫ�洢�Ķ���
	 */
	public void insert(Object obj) {

		Class c = obj.getClass();
		//�洢sql�Ĳ�������
		List<Object> params = new ArrayList<Object>();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		StringBuilder sql = new StringBuilder("insert into "+tableInfo.getTname()+" (");
		int countNotNullField = 0;//���㲻Ϊnull������ֵ
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
	 * ɾ��clazz��ʾ���Ӧ�ı��еļ�¼(ָ������ֵid�ļ�¼)
	 * @param clazz �����Ӧ�����Class����
	 * @param id ������ֵ
	 */
	public void delete(Class clazz,Object id) {
		//delete from User where id=2;

		//ͨ��Class������TableInfo
		TableInfo tableInfo = TableContext.poClassTableMap.get(clazz);
		//�������
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
		
		String sql = "delete from "+tableInfo.getTname()+" where "+onlyPriKey.getName()+"=? ";
		executeDML(sql, new Object[] {id});
	}
	/**
	 * ɾ�����������ݿ��ж�Ӧ�ļ�¼(�������ڵ����Ӧ����,�����������ֵ��Ӧ����¼)
	 * @param obj ��¼
	 */
	public void delete(Object obj) {

		Class c = obj.getClass();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
		
		//ͨ��������ƣ��������Զ�Ӧ��get,set����
		Object priKeyValue = ReflectUtils.invokeGet(onlyPriKey.getName(), obj);
		
		delete(c,priKeyValue);
	}
	/**
	 * ���¶����Ӧ�ļ�¼,����ֻ����ָ�����ֶε�ֵ
	 * @param obj ��Ҫ���µĶ���
	 * @param fieldNames ���µ������б�
	 * @return ִ��SQL����Ӱ���¼������
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
	 * ��ѯ���ض��м�¼,�����ü�¼��װ��clazzָ������Ķ�����  ��ѯ���ж���
	 * @param sql ��ѯ���
	 * @param clazz ��װ���ݵ�javabean���Class����
	 * @param params ��ѯ���Ľ��
	 * @return ��ѯ��¼
	 */
	public List queryRows(final String sql,final Class clazz,final Object[] params) {

		final List list = new ArrayList();//�洢��ѯ���������
		//ʹ��ģ�壬�ڻص��ﶨ��ʵ��--�����ڲ���
		return (List)executeQueryTemplate(sql, params, clazz, new CallBack() {
			@Override
			public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
				//ÿ�β�ѯ����һ���ĵط�
				ResultSetMetaData metaData;
				try {
					List list = null;
					metaData = rs.getMetaData();
					//����
					while(rs.next()) {
						if(list == null) {
							list = new ArrayList();
						}
						Object rowObj = clazz.newInstance();
						//����
						for(int i = 0; i < metaData.getColumnCount(); i++) {
							String columnName = metaData.getColumnLabel(i+1);
							Object columnValue = rs.getObject(i+1);
							
							//����rowObj�����setUsername(String uname)��������columnValue��ֵ���ý�ȥ
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
	 * ��ѯ����һ�м�¼,�����ü�¼��װ��clazzָ������Ķ�����  ��ѯһ�ж���
	 * @param sql ��ѯ���
	 * @param clazz ��װ���ݵ�javabean���Class����
	 * @param params ��ѯ���Ľ��
	 * @return ��ѯ��һ�ж��м�¼
	 */
	public Object queryUniqueRow(String sql,Class clazz,Object[] params) {
		List list = queryRows(sql, clazz, params);
		return (list == null && list.size()>0) ? null : list.get(0);
	}
	
	/**
	 * ��ѯ����һ��ֵ(һ��һ��),������ֵ����
	 * @param sql ��ѯ���
	 * @param params SQL�Ĳ���
	 * @return ��ѯ���Ľ��
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
	 * ��ѯ����һ������(һ��һ��),������ֵ����
	 * @param sql ��ѯ���
	 * @param params SQL�Ĳ���
	 * @return ��ѯ��������
	 */
	public Number queryNumber(String sql,Object[] params) {
		return (Number)queryValue(sql,params);
	}
	
	
	/**
	 * ��ҳ��ѯ--����ʵ���಻һ��
	 * @param pageNum �ڼ�ҳ����
	 * @param size ÿҳ��ʾ���ټ�¼
	 * @return 
	 */
	public abstract Object queryPagenate(int pageNum,int size);
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}