package com.sorm.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sorm.bean.ColumnInfo;
import com.sorm.bean.TableInfo;
import com.sorm.utils.JavaFileUtils;
import com.sorm.utils.StringUtils;

/**
 * 负责获取管理数据库所有表结构和类结构的关系,
 * 并且可以通过表结构生成类结构
 * @author liucong
 *通过JDBC获得数据库的源信息
 */
public class TableContext {
	/**
	 * 表名为key，表信息对象为value（根据表名获取表的整个信息）
	 */
	public static  Map<String,TableInfo>  tables = new HashMap<String,TableInfo>();
	/**
	 * 将po的class对象和表信息对象关联起来，便于重用！（根据Java中的类得到数据库中的表）
	 */
	public static  Map<Class,TableInfo>  poClassTableMap = new HashMap<Class,TableInfo>();
	private TableContext(){
	 
	}
	
	static {
		try {
			//初始化获得表的信息
			Connection con = DBManager.getConn();
			DatabaseMetaData dbmd = con.getMetaData(); //获得 数据库的源信息
			 
			ResultSet tableRet = dbmd.getTables(null, "%","%",new String[]{"TABLE"}); 
			 
			while(tableRet.next()){
				String tableName = (String) tableRet.getObject("TABLE_NAME");
				 
				TableInfo ti = new TableInfo(tableName, new ArrayList<ColumnInfo>(),new HashMap<String, ColumnInfo>());
				tables.put(tableName, ti);
				 
				ResultSet set = dbmd.getColumns(null, "%", tableName, "%");  
				//查询表中的所有字段
				while(set.next()){//获得所有的字段信息，同时将字段填充到表信息里
					ColumnInfo ci = new ColumnInfo(set.getString("COLUMN_NAME"), 
					set.getString("TYPE_NAME"), 0);
					ti.getColumns().put(set.getString("COLUMN_NAME"), ci);
				}
			 
				ResultSet set2 = dbmd.getPrimaryKeys(null, "%", tableName);  
				//查询t_user表中的主键
				while(set2.next()){//获得表中所有的主键
					ColumnInfo ci2 = (ColumnInfo) ti.getColumns().get(set2.getObject("COLUMN_NAME"));
					ci2.setKeyType(1);  //设置为主键类型
					ti.getPriKeys().add(ci2);
				}
				 
				if(ti.getPriKeys().size()>0){  //取唯一主键。。方便使用。如果是联合主键。则为空！
					ti.setOnlyPriKey(ti.getPriKeys().get(0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		//更新类结构
		updateJavaPOFile();
		 
		//加载po包下面所有的类，便于重用，提高效率！
		loadPOTables();
	}
	
	/**
	 * 根据表结构，更新配置的po包下面的java类
	 * 实现了从表结构转化到类结构
	 */
	private static void updateJavaPOFile() {
		Map<String,TableInfo> map = TableContext.tables;
		for(TableInfo t:map.values()){
			JavaFileUtils.createJavaPOFile(t,new MySqlTypeConvertor());
		}
	}
	
	/**
	 * 加载po包下面的类
	 */
	private static void loadPOTables() {
		for(TableInfo tableInfo:tables.values()){
			try {
				@SuppressWarnings("rawtypes")
				Class c = Class.forName(DBManager.getConf().getPoPackage()
				+"."+StringUtils.firstChar2UpperCase(tableInfo.getTname()));
				poClassTableMap.put(c, tableInfo);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		Map<String,TableInfo> tables = TableContext.tables;
		System.out.println(tables);
	}
}
