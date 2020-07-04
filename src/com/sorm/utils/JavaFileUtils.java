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
 * 根据数据库内容生成Java类信息
 * 
 * 封装了生成Java文件(源代码)常用的操作
 * @author liucong
 *
 */
public class JavaFileUtils {
	/**
	 * 根据字段信息生成java属性信息。
	 如：varchar username-->private String username;以及相应的set和get方法源码
	 * @param column 字段信息
	 * @param convertor 类型转化器（此处是将数据库数据类型转成Java数据类型）
	 * @return java属性和set/get方法源码
	 */
	public static JavaFieldGetSet createFieldGetSetSRC(ColumnInfo column,TypeConvertor convertor) {
		JavaFieldGetSet jfgs = new JavaFieldGetSet();
		String javaFieldType = convertor.databaseType2JavaType(column.getDataType());
		
		//拼出 一个属性(前面有\t,后面有换行\n，源码注意分号和空格不要忘)
		jfgs.setFieldInfo("\tprivate "+javaFieldType+" "+column.getName()+";\n");
		
		//拼出 生成get方法的源代码-- public String getUsername(){return username;}
		StringBuilder getSrc = new StringBuilder();
		getSrc.append("\tpublic "+javaFieldType+" get"+StringUtils.firstChar2UpperCase(column.getName())+"(){\n");
		getSrc.append("\t\treturn "+column.getName()+";\n");
		getSrc.append("\t}\n");
		jfgs.setGetInfo(getSrc.toString());
		
		//生成set方法的源代码-- public void setUsername(String username){this.username=username;}
		StringBuilder setSrc = new StringBuilder();
		setSrc.append("\tpublic void set"+StringUtils.firstChar2UpperCase(column.getName())+"(");
		setSrc.append(javaFieldType+" "+column.getName()+"){\n");
		setSrc.append("\t\tthis."+column.getName()+"="+column.getName()+";\n");
		setSrc.append("\t}\n");
		jfgs.setSetInfo(setSrc.toString());
		return jfgs;
	}
	
	/**
	 * 根据表信息生成java类的源代码
	 * @param tableInfo 表信息
	 * @param convertor 数据类型转化器 
	 * @return java类的源代码
	 */
	public static String createJavaSrc(TableInfo tableInfo,TypeConvertor convertor) {
		Map<String,ColumnInfo> columns = tableInfo.getColumns();
		List<JavaFieldGetSet> javaFields = new ArrayList<JavaFieldGetSet>();
		
		for(ColumnInfo c:columns.values()){
			javaFields.add(createFieldGetSetSRC(c,convertor));
		}
		
		//拼出一个类的整个源码
		StringBuilder src = new StringBuilder();
		//生成package语句
		src.append("package "+DBManager.getConf().getPoPackage()+";\n\n");
		//生成import语句
		src.append("import java.sql.*;\n");
		src.append("import java.util.*;\n\n");
		//生成类声明语句
		src.append("public class "+StringUtils.firstChar2UpperCase(tableInfo.getTname())+"{\n\n");
		//生成属性列表
		for(JavaFieldGetSet f:javaFields) {
			src.append(f.getFieldInfo());
		}
		src.append("\n\n");
		//生成get方法列表
		for(JavaFieldGetSet f:javaFields) {
			src.append(f.getGetInfo());
		}
		//生成set方法列表
		for(JavaFieldGetSet f:javaFields){
			src.append(f.getSetInfo());
		}
		//生成类结束
		//生成类结束
		src.append("}\n");
		return src.toString();
	}
	/**
	 * 根据表结构生成PO文件，生成表对应的类
	 * @param t 表信息
	 * @param mySqlTypeConvertor 类型转换
	 */
	public static void createJavaPOFile(TableInfo tableInfo, MySqlTypeConvertor convertor) {
		String src = createJavaSrc(tableInfo, convertor);//得到java类的源码
		//将字符串写给指定的包
		String srcPath = DBManager.getConf().getSrcPath()+"/";
		String packagePath = DBManager.getConf().getPoPackage().replaceAll("\\.", "/");//将路径中的.换成/
		
		File f = new File(srcPath+packagePath);
		System.out.println(f);
		if(!f.exists()) {//如果指定目录不存在，则帮助用户建立
			f.mkdirs();
		}
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile()+"/"+StringUtils.firstChar2UpperCase(tableInfo.getTname())+".java"));
			bw.write(src);
//			System.out.println("建立表"+tableInfo.getTname()+"对应的java类："+StringUtils.firstChar2UpperCase(tableInfo.getTname())+".java");
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
		//测试：给出数据库的一个字段，得到相应java中的属性，set(),get()
		/*ColumnInfo ci = new ColumnInfo("salary", "double", 5000);
		JavaFieldGetSet f = createFieldGetSetSRC(ci,new MySqlTypeConvertor());
		System.out.println(f);
		*/
		
		/*Map<String,TableInfo> map = TableContext.tables;
		TableInfo t = map.get("emp");
		createJavaPOFile(t, new MySqlTypeConvertor());
		*/
		//数据库中所有表，在po下生成java类
		Map<String,TableInfo> map = TableContext.tables;
		for(TableInfo t:map.values()) {
			createJavaPOFile(t, new MySqlTypeConvertor());
		}
	}

}
	

