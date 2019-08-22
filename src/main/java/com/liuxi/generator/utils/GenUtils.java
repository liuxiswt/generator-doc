package com.liuxi.generator.utils;


import com.liuxi.generator.model.dto.Column;
import com.liuxi.generator.model.dto.Table;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: Xit
 * @Time: 2019/3/28 11:17
 */
public class GenUtils {


    /**
     * 代码生成工具类
     * @param tabMap
     * @param colMap
     * @param zip
     */
    public static void generatorCode(Map<String, String> tabMap, List<Map> colMap, ZipOutputStream zip){
        //获取配置
        Configuration config = getConfig();
        boolean hasBigDecimal = false;
        //表信息
        Table table = new Table();
        table.setTableName(tabMap.get("tablename"));//表名
        table.setComments(tabMap.get("tablecomment"));//表注释
        String className = tableToJava(table.getTableName(), config.getString("tablePrefix" ));//表明含有tb_转换类名
        table.setClassName(className);
        table.setClassname(StringUtils.uncapitalize(className));
        //列信息
        List<Column> columsList = new ArrayList<>();
        for(Map<String, String> col : colMap){
            Column column = new Column();
            column.setColumnName(col.get("columnname" ));
            column.setDataType(col.get("datatype" ));
            column.setComments(col.get("columncomment" ));
            column.setExtra(col.get("extra" ));
            //列名转换成Java属性名
            String attrName = columnToJava(column.getColumnName());
            column.setAttrName(attrName);
            column.setAttrname(StringUtils.uncapitalize(attrName));
            //列的数据类型，转换成Java类型
            String attrType = config.getString(column.getDataType(), "unknowType" );
            column.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal" )) {
                hasBigDecimal = true;
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(col.get("columnkey" )) && table.getPk() == null) {
                table.setPk(column);
            }
            columsList.add(column);
        }
        table.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (table.getPk() == null) {
            table.setPk(table.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        Velocity.init(prop);
        String mainPath = config.getString("mainPath" );
        mainPath = StringUtils.isBlank(mainPath)? "com.liuxi" :mainPath;
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", table.getTableName());
        map.put("comments", table.getComments());
        map.put("pk", table.getPk());
        map.put("className", table.getClassName());
        map.put("classname", table.getClassname());
        map.put("pathName", table.getClassname().toLowerCase());
        map.put("columns", table.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package" ));
        map.put("moduleName", config.getString("moduleName" ));
//        map.put("author", config.getString("author" ));
//        map.put("email", config.getString("email" ));
        map.put("datetime",DateUtils.format(LocalDateTime.now(),DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            try ( StringWriter sw = new StringWriter()){
                Template tpl = Velocity.getTemplate(template, "UTF-8" );
                tpl.merge(context, sw);
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, table.getClassName(), config.getString("package" ), config.getString("moduleName" ))));
                IOUtils.write(sw.toString(), zip, "UTF-8" );
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + table.getTableName()+": "+e);
            }
        }
    }


    /**
     * 获取模板
     * @return
     */
    public static List<String> getTemplates(){
        List<String> templates = new ArrayList<String>();
        templates.add("template/Entity.java.vm");
        templates.add("template/Mapper.java.vm");
        templates.add("template/Mapper.xml.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");

        return templates;
    }
    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "" );
    }

    /**
     * 表名转换为java类名
     * @param tableName
     * @param tablePrefix
     * @return
     */
    public static String tableToJava(String tableName,String tablePrefix){
        if(StringUtils.isNotBlank(tablePrefix)){
            tableName = tableName.replaceFirst(tablePrefix,"");
        }
        return columnToJava(tableName);
    }

    /**
     * 读取类型配置
     */
    public static Configuration getConfig(){
        try {
            return new PropertiesConfiguration("generator.properties" );
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，"+e.getMessage());
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator /*+ moduleName + File.separator*/;
        }

        if (template.contains("template/Entity.java.vm")) {
            return packagePath + "entity" + File.separator + className + ".java";
        }

        if (template.contains("template/Mapper.java.vm")) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

        if (template.contains("template/Service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }


        if (template.contains("template/ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("template/Mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator /*+ moduleName + File.separator*/ + className + "Mapper.xml";
        }



        return null;
    }

    public static void createFile(byte[] bfile, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "\\" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }
}
