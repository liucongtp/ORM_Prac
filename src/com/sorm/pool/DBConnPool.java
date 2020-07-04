package com.sorm.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sorm.core.DBManager;

/**
 * �������ӳ� �Ż�SORM
 * @author liucong
 *���ӳص���
 */
public class DBConnPool {
	/**
	 * ���ӳض���
	 */
	private List<Connection> pool;
	/**
	 * ���������
	 */
	private static final int POOL_MAX_SIZE = DBManager.getConf().getPoolMaxSize();
	/**
	 * ��С������
	 */
	private static final int POOL_MIN_SIZE = DBManager.getConf().getPoolMinSize();
	/**
	 * ��ʼ�����ӳأ�ʹ���е����Ӹ����ﵽ��Сֵ
	 */
	public void initPool() {
		if(pool == null) {
			pool = new ArrayList<Connection>();
		}
		
		while(pool.size() < DBConnPool.POOL_MIN_SIZE) {
			pool.add(DBManager.createConn());//�ӳ�����ȡ����
			System.out.println("��ʼ����  ���ӳظ�����" + pool.size());
		}
	}
	
	/**
	 * �����ӳ���ȡ��һ������,��ͬ������ֹ���̳߳���
	 * @return ����
	 */
	public synchronized Connection getConnection() {
		int last_index = pool.size()-1;
		//�õ����Ӻ󣬽����Ӵӳ���ɾ������ֹ�������ݿ�����
		Connection conn = pool.get(last_index);
		pool.remove(last_index);
		return conn;
	}
	
	/**
	 * �����ӷŻ����ӳ�
	 * @param conn
	 */
	public synchronized void close(Connection conn) {
		if(pool.size() >= POOL_MAX_SIZE) {
			//��Ĺص�����
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		pool.add(conn);
	}
	
	/**
	 * �ڹ�������ֱ�ӳ�ʼ�����ӳ�
	 */
	public DBConnPool() {
		initPool();
	}
}
