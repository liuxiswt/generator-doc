package com.liuxi.generator.common;

import com.liuxi.generator.model.vo.ColumnVo;
import com.liuxi.generator.model.vo.TableVo;
import com.liuxi.generator.utils.GenUtils;
import com.liuxi.generator.utils.WordGenerator;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.impl.SimpleDataSource;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * Generator
 *
 * @author Xit
 * @Time: 2019/3/28 13:55
 */
public abstract class Generator {
    private SimpleDataSource dataSource;
    protected Dao dao = null;
    protected String dbName;
    protected String docPath;

    public Generator(String dbName, SimpleDataSource dataSource) {
        this.dataSource = dataSource;
        dao = new NutDao(dataSource);
        this.dbName = dbName;
        this.docPath = dbName + "-doc";
    }

    /**
     * 获取表结构数据
     *
     * @return
     */
    public abstract List<TableVo> getTableData();

    public abstract Map<String, String> getTable(String tableName);

    public abstract List<Map> getColumns(String tableName);

    //生成代码(字节数组)
    public byte[] generatorCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //转成zip
        try(ZipOutputStream zip = new ZipOutputStream(outputStream)){
            for(String tableName : tableNames){
                //查询表信息
                Map<String, String> table = getTable(tableName);
                //查询列信息
                List<Map> columns = getColumns(tableName);
                //生成代码
                GenUtils.generatorCode(table, columns, zip);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public void generateDoc(String pat) {
        String path = pat + File.separator + dbName + "_doc";
        File docDir = new File(path);
        if (docDir.exists()) {
            throw new RuntimeException("该文件夹" + path + "已存在");
        } else {
            docDir.mkdirs();
        }
        List<TableVo> list = getTableData();
        save2File(list,path);
        //保存word
        WordGenerator.createDoc(dbName,path,list);

    }

    public void save2File(List<TableVo> tables,String path) {
        saveSummary(tables, path);
        saveReadme(tables, path);
        for (TableVo tableVo : tables) {
            saveTableFile(tableVo, path);
        }

    }

    private void saveSummary(List<TableVo> tables, String path) {
        StringBuilder builder = new StringBuilder("# Summary").append("\r\n").append("* [Introduction](README.md)")
                .append("\r\n");
        for (TableVo tableVo : tables) {
            String name = Strings.isEmpty(tableVo.getComment()) ? tableVo.getTable() : tableVo.getComment();
            builder.append("* [" + name + "](" + tableVo.getTable() + ".md)").append("\r\n");
        }
        try {
            Files.write(new File(path + File
                    .separator + "SUMMARY.md"), builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveReadme(List<TableVo> tables, String path) {
        StringBuilder builder = new StringBuilder("# " + dbName + "数据库文档").append("\r\n");
        for (TableVo tableVo : tables) {
            builder.append("- [" + (Strings.isEmpty(tableVo.getComment()) ? tableVo.getTable() : tableVo.getComment())
                    + "]" +
                    "(" + tableVo
                    .getTable() + ".md)")
                    .append
                            ("\r\n");
        }
        try {
            Files.write(new File(path + File
                    .separator + "README.md"), builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveTableFile(TableVo table,String path) {

        StringBuilder builder = new StringBuilder("# " + (Strings.isBlank(table.getComment()) ? table.getTable() : table
                .getComment()) + "(" + table.getTable() + ")").append("\r\n");
        builder.append("| 列名   | 类型   | KEY  | 可否为空 | 注释   |").append("\r\n");
        builder.append("| ---- | ---- | ---- | ---- | ---- |").append("\r\n");
        List<ColumnVo> columnVos = table.getColumns();
        for (int i = 0; i < columnVos.size(); i++) {
            ColumnVo column = columnVos.get(i);
            builder.append("|").append(column.getName()).append("|").append(column.getType()).append("|").append
                    (Strings.sNull(column.getKey())).append("|").append(column.getIsNullable()).append("|").append
                    (column.getComment()).append("|\r\n");
        }
        try {
            Files.write(new File(path + File
                    .separator + table.getTable() + ".md"), builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Record> getList(String sqlStr) {
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(Sqls.callback.records());
        dao.execute(sql);
        List<Record> list = sql.getList(Record.class);
        return list;
    }
}
