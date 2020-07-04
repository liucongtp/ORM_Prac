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
 * �����ȡ�������ݿ����б�ṹ����ṹ�Ĺ�ϵ,
 * ���ҿ���ͨ����ṹ������ṹ
 * @author liucong
 *ͨ��JDBC������ݿ��Դ��Ϣ
 */
public class TableContext {
	/**
	 * ����Ϊkey������Ϣ����Ϊvalue�����ݱ�����ȡ���������Ϣ��
	 */
	public static  Map<String,TableInfo>  tables = new HashMap<String,TableInfo>();
	/**
	 * ��po��class����ͱ���Ϣ��������������������ã�������Java�е���õ����ݿ��еı�
	 */
	public static  Map<Class,TableInfo>  poClassTableMap = new HashMap<Class,TableInfo>();
	private TableContext(){
	 
	}
	
	static {
		try {
			//��ʼ����ñ����Ϣ
			Connection con = DBManager.getConn();
			DatabaseMetaData dbmd = con.getMetaData(); //��� ���ݿ��Դ��Ϣ
			 
			ResultSet tableRet = dbmd.getTables(null, "%","%",new String[]{"TABLE"}); 
			 
			while(tableRet.next()){
				String tableName = (String) tableRet.getObject("TABLE_NAME");
				 
				TableInfo ti = new TableInfo(tableName, new ArrayList<ColumnInfo>(),new HashMap<String, ColumnInfo>());
				tables.put(tableName, ti);
				 
				ResultSet set = dbmd.getColumns(null, "%", tableName, "%");  
				//��ѯ���е������ֶ�
				while(set.next()){//������е��ֶ���Ϣ��ͬʱ���ֶ���䵽����Ϣ��
					ColumnInfo ci = new ColumnInfo(set.getString("COLUMN_NAME"), 
					set.getString("TYPE_NAME"), 0);
					ti.getColumns().put(set.getString("COLUMN_NAME"), ci);
				}
			 
				ResultSet set2 = dbmd.getPrimaryKeys(null, "%", tableName);  
				//��ѯt_user���е�����
				while(set2.next()){//��ñ������е�����
					ColumnInfo ci2 = (ColumnInfo) ti.getColumns().get(set2.getObject("COLUMN_NAME"));
					ci2.setKeyType(1);  //����Ϊ��������
					ti.getPriKeys().add(ci2);
				}
				 
				if(ti.getPriKeys().size()>0){  //ȡΨһ������������ʹ�á������������������Ϊ�գ�
					ti.setOnlyPriKey(ti.getPriKeys().get(0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		//������ṹ
		updateJavaPOFile();
		 
		//����po���������е��࣬�������ã����Ч�ʣ�
		loadPOTables();
	}
	
	/**
	 * ���ݱ�ṹ���������õ�po�������java��
	 * ʵ���˴ӱ�ṹת������ṹ
	 */
	private static void updateJavaPOFile() {
		Map<String,TableInfo> map = TableContext.tables;
		for(TableInfo t:map.values()){
			JavaFileUtils.createJavaPOFile(t,new MySqlTypeConvertor());
		}
	}
	
	/**
	 * ����po���������
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
