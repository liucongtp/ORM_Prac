package com.sorm.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.sorm.bean.Configuration;
import com.sorm.pool.DBConnPool;

/**
 * 根据配置信息,维持连接对象的管理(增加连接池功能)
 * @author liucong
 *
 */
public class DBManager {
	/**
	 * 配置信息
	 */
	private static Configuration conf;
	/**
	 * 连接池对象
	 *//*
	private static DBConnPool pool;*/
	
	static {//获得配置文件
		Properties pros = new Properties();
		try {
			pros.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		conf = new Configuration();
		conf.setDriver(pros.getProperty("driver"));
		conf.setUrl(pros.getProperty("url"));
		conf.setUser(pros.getProperty("user"));
		conf.setPwd(pros.getProperty("pwd"));
		conf.setSrcPath(pros.getProperty("srcPath"));
		conf.setUsingDB(pros.getProperty("usingDB"));
		conf.setPoPackage(pros.getProperty("poPackage"));
		conf.setQueryClass(pros.getProperty("queryClass"));
		conf.setPoolMaxSize(Integer.parseInt(pros.getProperty("poolMaxSize")));
		conf.setPoolMinSize(Integer.parseInt(pros.getProperty("poolMinSize")));
	}
	/**
	 * 连接数据库--此处直接进行连接的建立,后期增加连接池处理,提高效率
	 * @return 连接
	 */
	public static Connection getConn() {
		try {
			Class.forName(conf.getDriver());
			return DriverManager.getConnection(conf.getUrl(),conf.getUser(),conf.getPwd());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		/*//通过连接池
		if(pool == null) {
			 pool = new DBConnPool();
		}
		return pool.getConnection();*/
	}
	
	/**
	 * 从连接池里获得连接，提高效率
	 * @return 连接
	 */
	public static Connection createConn() {
		try {
			Class.forName(conf.getDriver());
			return DriverManager.getConnection(conf.getUrl(),conf.getUser(),conf.getPwd());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 关闭传入的Statement，Connection对象
	 * @param ps SQL语句
	 * @param conn 连接
	 */
	public static void close(Statement ps,Connection conn) {
		if(ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		/*//通过连接池
		pool.close(conn);*/
	}
	
	/**
	 * 资源关闭--3种重载方法
	 * @param rs 结果集
	 * @param ps SQL语句
	 * @param conn 数据库连接
	 */
	public static void close(ResultSet rs, Statement ps,Connection conn) {
		if(ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		/*//通过连接池
		pool.close(conn);*/
	}
	
	public static void close(Connection conn) {
		
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		/*//通过连接池
		pool.close(conn);*/
	}
	/**
	 * 返回Configuration对象
	 * @return 连接
	 */
	public static Configuration getConf() {
		return conf;
	}
}
