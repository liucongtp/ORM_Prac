package com.sorm.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sorm.bean.ColumnInfo;
import com.sorm.bean.JavaFieldGetSet;
import com.sorm.bean.TableInfo;
import com.sorm.core.DBManager;
import com.sorm.core.MySqlTypeConvertor;
import com.sorm.core.TableContext;
import com.sorm.core.TypeConvertor;

/**
 * �������ݿ���������Java����Ϣ
 * 
 * ��װ������Java�ļ�(Դ����)���õĲ���
 * @author liucong
 *
 */
public class JavaFileUtils {
	/**
	 * �����ֶ���Ϣ����java������Ϣ��
	 �磺varchar username-->private String username;�Լ���Ӧ��set��get����Դ��
	 * @param column �ֶ���Ϣ
	 * @param convertor ����ת�������˴��ǽ����ݿ���������ת��Java�������ͣ�
	 * @return java���Ժ�set/get����Դ��
	 */
	public static JavaFieldGetSet createFieldGetSetSRC(ColumnInfo column,TypeConvertor convertor) {
		JavaFieldGetSet jfgs = new JavaFieldGetSet();
		String javaFieldType = convertor.databaseType2JavaType(column.getDataType());
		
		//ƴ�� һ������(ǰ����\t,�����л���\n��Դ��ע��ֺźͿո�Ҫ��)
		jfgs.setFieldInfo("\tprivate "+javaFieldType+" "+column.getName()+";\n");
		
		//ƴ�� ����get������Դ����-- public String getUsername(){return username;}
		StringBuilder getSrc = new StringBuilder();
		getSrc.append("\tpublic "+javaFieldType+" get"+StringUtils.firstChar2UpperCase(column.getName())+"(){\n");
		getSrc.append("\t\treturn "+column.getName()+";\n");
		getSrc.append("\t}\n");
		jfgs.setGetInfo(getSrc.toString());
		
		//����set������Դ����-- public void setUsername(String username){this.username=username;}
		StringBuilder setSrc = new StringBuilder();
		setSrc.append("\tpublic void set"+StringUtils.firstChar2UpperCase(column.getName())+"(");
		setSrc.append(javaFieldType+" "+column.getName()+"){\n");
		setSrc.append("\t\tthis."+column.getName()+"="+column.getName()+";\n");
		setSrc.append("\t}\n");
		jfgs.setSetInfo(setSrc.toString());
		return jfgs;
	}
	
	/**
	 * ���ݱ���Ϣ����java���Դ����
	 * @param tableInfo ����Ϣ
	 * @param convertor ��������ת���� 
	 * @return java���Դ����
	 */
	public static String createJavaSrc(TableInfo tableInfo,TypeConvertor convertor) {
		Map<String,ColumnInfo> columns = tableInfo.getColumns();
		List<JavaFieldGetSet> javaFields = new ArrayList<JavaFieldGetSet>();
		
		for(ColumnInfo c:columns.values()){
			javaFields.add(createFieldGetSetSRC(c,convertor));
		}
		
		//ƴ��һ���������Դ��
		StringBuilder src = new StringBuilder();
		//����package���
		src.append("package "+DBManager.getConf().getPoPackage()+";\n\n");
		//����import���
		src.append("import java.sql.*;\n");
		src.append("import java.util.*;\n\n");
		//�������������
		src.append("public class "+StringUtils.firstChar2UpperCase(tableInfo.getTname())+"{\n\n");
		//���������б�
		for(JavaFieldGetSet f:javaFields) {
			src.append(f.getFieldInfo());
		}
		src.append("\n\n");
		//����get�����б�
		for(JavaFieldGetSet f:javaFields) {
			src.append(f.getGetInfo());
		}
		//����set�����б�
		for(JavaFieldGetSet f:javaFields){
			src.append(f.getSetInfo());
		}
		//���������
		//���������
		src.append("}\n");
		return src.toString();
	}
	/**
	 * ���ݱ�ṹ����PO�ļ������ɱ��Ӧ����
	 * @param t ����Ϣ
	 * @param mySqlTypeConvertor ����ת��
	 */
	public static void createJavaPOFile(TableInfo tableInfo, MySqlTypeConvertor convertor) {
		String src = createJavaSrc(tableInfo, convertor);//�õ�java���Դ��
		//���ַ���д��ָ���İ�
		String srcPath = DBManager.getConf().getSrcPath()+"/";
		String packagePath = DBManager.getConf().getPoPackage().replaceAll("\\.", "/");//��·���е�.����/
		
		File f = new File(srcPath+packagePath);
		System.out.println(f);
		if(!f.exists()) {//���ָ��Ŀ¼�����ڣ�������û�����
			f.mkdirs();
		}
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile()+"/"+StringUtils.firstChar2UpperCase(tableInfo.getTname())+".java"));
			bw.write(src);
//			System.out.println("������"+tableInfo.getTname()+"��Ӧ��java�ࣺ"+StringUtils.firstChar2UpperCase(tableInfo.getTname())+".java");
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	public static void main(String[] args) {
		//���ԣ��������ݿ��һ���ֶΣ��õ���Ӧjava�е����ԣ�set(),get()
		/*ColumnInfo ci = new ColumnInfo("salary", "double", 5000);
		JavaFieldGetSet f = createFieldGetSetSRC(ci,new MySqlTypeConvertor());
		System.out.println(f);
		*/
		
		/*Map<String,TableInfo> map = TableContext.tables;
		TableInfo t = map.get("emp");
		createJavaPOFile(t, new MySqlTypeConvertor());
		*/
		//���ݿ������б���po������java��
		Map<String,TableInfo> map = TableContext.tables;
		for(TableInfo t:map.values()) {
			createJavaPOFile(t, new MySqlTypeConvertor());
		}
	}

}
	

