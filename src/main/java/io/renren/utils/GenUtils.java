/**
 * Copyright 2018 人人开源 http://www.renren.io
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.renren.utils;

import io.renren.entity.ColumnEntity;
import io.renren.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class GenUtils {

	public static List<String> getTemplates(){
		List<String> templates = new ArrayList<String>();
		templates.add("template/Entity.java.vm");
		templates.add("template/Dao.java.vm");
		templates.add("template/Service.java.vm");
		templates.add("template/Controller.java.vm");
		templates.add("template/index.html.vm");
		templates.add("template/add.html.vm");
		templates.add("template/edit.html.vm");
		templates.add("template/index.txt.vm");
		return templates;
	}
	
	/**
	 * 生成代码
	 */
	public static void generatorCode(Map<String, String> table,
			List<ColumnEntity> columns, ZipOutputStream zip){
		//配置信息
		Configuration config = getConfig();
		boolean hasBigDecimal = false;
		boolean hasDate = false;
		//表信息
		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(table.get("tableName"));
		tableEntity.setComments(table.get("tableComment"));
		//表名转换成Java类名
		String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
		tableEntity.setClassName(className);
		tableEntity.setClassname(StringUtils.uncapitalize(className));
		
		//列信息
		List<ColumnEntity> columsList = new ArrayList<>();
		for(int i = 0;i < columns.size();i++){
			ColumnEntity column = columns.get(i);
			ColumnEntity columnEntity = new ColumnEntity();
			columnEntity.setColumnName(column.getColumnName());
			columnEntity.setDataType(column.getDataType());
			columnEntity.setComments(column.getColumnComment());
			columnEntity.setExtra(column.getExtra());
			columnEntity.setNumericPrecision(column.getNumericPrecision() == null ? "" : column.getNumericPrecision());
			columnEntity.setNumericScale(column.getNumericScale() == null ? "" : column.getNumericScale());
			columnEntity.setCharacterMaximumLength(column.getCharacterMaximumLength() == null ? "" : column.getCharacterMaximumLength());
			
			//列名转换成Java属性名        
			String attrName = columnToJava(columnEntity.getColumnName());
			columnEntity.setAttrName(attrName);
			columnEntity.setAttrname(StringUtils.uncapitalize(attrName));
			
			//列的数据类型，转换成Java类型
			String attrType = config.getString(columnEntity.getDataType(), "unknowType");
			columnEntity.setAttrType(attrType);
			if (!hasBigDecimal && attrType.equals("BigDecimal" )) {
				hasBigDecimal = true;
			}
			if (!hasDate && attrType.equals("Date")) {
				hasDate = true;
			}
			//是否主键
			if("PRI".equalsIgnoreCase(column.getColumnKey()) && tableEntity.getPk() == null){
				tableEntity.setPk(columnEntity);  
			}
			
			columsList.add(columnEntity);
		}
		tableEntity.setColumns(columsList);
		
		//没主键，则第一个字段为主键
		if(tableEntity.getPk() == null){
			tableEntity.setPk(tableEntity.getColumns().get(0));
		}
		
		//设置velocity资源加载器
		Properties prop = new Properties();  
		prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");  
		Velocity.init(prop);

		String mainPath = config.getString("mainPath" );
		mainPath = StringUtils.isBlank(mainPath) ? "com.zggk" : mainPath;
		
		//封装模板数据
		Map<String, Object> map = new HashMap<>();
		map.put("tableName", tableEntity.getTableName());
		map.put("comments", tableEntity.getComments());
		map.put("pk", tableEntity.getPk());
		map.put("className", tableEntity.getClassName());
		map.put("classname", tableEntity.getClassname());
		map.put("pathName", tableEntity.getClassname().toLowerCase());
		map.put("columns", tableEntity.getColumns());
		map.put("hasBigDecimal", hasBigDecimal);
		map.put("hasDate", hasDate);
		map.put("mainPath", mainPath);
		map.put("package", config.getString("package" ));
		map.put("moduleName", config.getString("moduleName" ));
		try {
			map.put("author", new String(config.getString("author").getBytes("ISO8859-1"),"UTF-8"));
		} catch (Exception e) {
			map.put("author", "qingyun");
		}
		map.put("email", config.getString("email"));
		map.put("databaseName", config.getString("databaseName"));
		map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);
        
        //获取模板列表
		List<String> templates = getTemplates();
		for(String template : templates){
			//渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, "UTF-8");
			tpl.merge(context, sw);
			
			try {
				//添加到zip
				zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package"), config.getString("moduleName"),tableEntity.getClassname())));
				IOUtils.write(sw.toString(), zip, "UTF-8");
				IOUtils.closeQuietly(sw);
				zip.closeEntry();
			} catch (IOException e) {
				throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
			}
		}
	}
	
	
	/**
	 * 列名转换成Java属性名
	 */
	public static String columnToJava(String columnName) {
		return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
	}
	
	/**
	 * 表名转换成Java类名
	 */
	public static String tableToJava(String tableName, String tablePrefix) {
		if(StringUtils.isNotBlank(tablePrefix)){
			tableName = tableName.replace(tablePrefix, "");
		}
		return columnToJava(tableName);
	}
	
	/**
	 * 获取配置信息
	 */
	public static Configuration getConfig(){
		try {
			return new PropertiesConfiguration("generator.properties");
		} catch (ConfigurationException e) {
			throw new RRException("获取配置文件失败，", e);
		}
	}

	/**
	 * 获取文件名
	 * className：开头大写的类名
	 * classname：开头小写的类名
	 */
	public static String getFileName(String template, String className, String packageName, String moduleName,String classname) {
		String packagePath = "main" + File.separator + "java" + File.separator;
		if (StringUtils.isNotBlank(packageName)) {
			packagePath += packageName.replace(".", File.separator) + File.separator;
		}

		if (template.contains("Entity.java.vm" )) {
			return packagePath + "entity" + File.separator  + moduleName + File.separator + className + ".java";
		}

		if (template.contains("Dao.java.vm" )) {
			return packagePath + "dao" + File.separator + moduleName + File.separator  + className + "Repository.java";
		}

		if (template.contains("Service.java.vm" )) {
			return packagePath + "service" + File.separator + moduleName + File.separator  + className + "Service.java";
		}

		if (template.contains("Controller.java.vm" )) {
			return packagePath + "controller" + File.separator + moduleName + File.separator+className + "Controller.java";
		}

		if (template.contains("index.html.vm" )) {
			return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
					+ moduleName + File.separator + classname + File.separator + "index.html";
		}
		
		if (template.contains("add.html.vm" )) {
			return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
					+ moduleName + File.separator + classname + File.separator + "add.html";
		}
		
		if (template.contains("edit.html.vm" )) {
			return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
					+ moduleName + File.separator + classname + File.separator + "edit.html";
		}

		if (template.contains("index.txt.vm" )) {
			return className.toLowerCase() + "_index.txt";
		}

		return null;
	}
}
