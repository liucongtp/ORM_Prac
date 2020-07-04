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
 * ����������Ϣ,ά�����Ӷ���Ĺ���(�������ӳع���)
 * @author liucong
 *
 */
public class DBManager {
	/**
	 * ������Ϣ
	 */
	private static Configuration conf;
	/**
	 * ���ӳض���
	 *//*
	private static DBConnPool pool;*/
	
	static {//��������ļ�
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
	 * �������ݿ�--�˴�ֱ�ӽ������ӵĽ���,�����������ӳش���,���Ч��
	 * @return ����
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
		
		/*//ͨ�����ӳ�
		if(pool == null) {
			 pool = new DBConnPool();
		}
		return pool.getConnection();*/
	}
	
	/**
	 * �����ӳ��������ӣ����Ч��
	 * @return ����
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
	 * �رմ����Statement��Connection����
	 * @param ps SQL���
	 * @param conn ����
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
		/*//ͨ�����ӳ�
		pool.close(conn);*/
	}
	
	/**
	 * ��Դ�ر�--3�����ط���
	 * @param rs �����
	 * @param ps SQL���
	 * @param conn ���ݿ�����
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
		/*//ͨ�����ӳ�
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
		/*//ͨ�����ӳ�
		pool.close(conn);*/
	}
	/**
	 * ����Configuration����
	 * @return ����
	 */
	public static Configuration getConf() {
		return conf;
	}
}
