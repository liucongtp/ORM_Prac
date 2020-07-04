package com.sorm.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sorm.core.DBManager;

/**
 * 增加连接池 优化SORM
 * @author liucong
 *连接池的类
 */
public class DBConnPool {
	/**
	 * 连接池对象
	 */
	private List<Connection> pool;
	/**
	 * 最大连接数
	 */
	private static final int POOL_MAX_SIZE = DBManager.getConf().getPoolMaxSize();
	/**
	 * 最小连接数
	 */
	private static final int POOL_MIN_SIZE = DBManager.getConf().getPoolMinSize();
	/**
	 * 初始化连接池，使池中的连接个数达到最小值
	 */
	public void initPool() {
		if(pool == null) {
			pool = new ArrayList<Connection>();
		}
		
		while(pool.size() < DBConnPool.POOL_MIN_SIZE) {
			pool.add(DBManager.createConn());//从池里面取连接
			System.out.println("初始化，  连接池个数：" + pool.size());
		}
	}
	
	/**
	 * 从连接池中取出一个连接,加同步，防止多线程出错
	 * @return 连接
	 */
	public synchronized Connection getConnection() {
		int last_index = pool.size()-1;
		//拿到连接后，将连接从池中删除，防止其他数据库连接
		Connection conn = pool.get(last_index);
		pool.remove(last_index);
		return conn;
	}
	
	/**
	 * 将连接放回连接池
	 * @param conn
	 */
	public synchronized void close(Connection conn) {
		if(pool.size() >= POOL_MAX_SIZE) {
			//真的关掉连接
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
	 * 在构造器中直接初始化连接池
	 */
	public DBConnPool() {
		initPool();
	}
}
