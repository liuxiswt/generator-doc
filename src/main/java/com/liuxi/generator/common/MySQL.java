package com.liuxi.generator.common;
import com.liuxi.generator.model.vo.ColumnVo;
import com.liuxi.generator.model.vo.TableVo;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.impl.SimpleDataSource;
import org.nutz.dao.sql.Sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabaseDocService
 *
 * @author Xit
 * @Time: 2019/3/28 13:55
 */
public class MySQL extends Generator {
    String sqlTables = "select table_name,table_comment from information_schema.tables where table_schema = '@dbname'" +
            " order by table_name asc";
    String sqlColumns = "select column_name,column_type,column_key,is_nullable,column_comment from information_schema" +
            ".columns where table_schema = '@dbname'  and table_name " +
            "='@tablename'";

    String sqlTable = "SELECT table_name AS tableName, engine, table_comment AS tableComment, create_time AS createTime" +
            "        FROM information_schema.tables" +
            " WHERE table_schema = '@dbname'" +
            " AND table_name ='@tablename'";
    String sqlColumn = "SELECT column_name AS columnName, data_type AS dataType, column_comment AS columnComment, column_key AS columnKey, extra" +
            "   FROM information_schema.columns" +
            "   WHERE table_name = '@tablename'" +
            "   AND table_schema = '@dbname'" +
            "   ORDER BY ordinal_position";
    public MySQL(String dbName, SimpleDataSource dataSource){
        super(dbName,dataSource);
    }

    @Override
    public Map<String, String> getTable(String tableName) {
        String sqlStr = sqlTable.replace("@dbname",dbName).replace("@tablename",tableName);
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(Sqls.callback.records());
        dao.execute(sql);
        List<Map> list = sql.getList(Map.class);
        Map<String, String> map = new HashMap<>();
        for(Map m : list){
            map.putAll(m);
        }
        return map;
    }

    @Override
    public List<Map> getColumns(String tableName) {
        String sqlStr = sqlColumn.replace("@dbname",dbName).replace("@tablename",tableName);
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(Sqls.callback.records());
        dao.execute(sql);
        List<Map> list = sql.getList(Map.class);
        return list;
    }

    @Override
    public List<TableVo> getTableData(){
        String sql = sqlTables.replace("@dbname",dbName);
        List<Record> list = getList(sql);
        List<TableVo> tables = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            Record record = list.get(i);
            String table = record.getString("table_name");
            String comment =record.getString("table_comment");
            TableVo tableVo = getTableInfo(table,comment);
            tables.add(tableVo);
        }
        return tables;
    }
    public TableVo getTableInfo(String table,String comment){
        TableVo tableVo = new TableVo();
        tableVo.setTable(table);
        tableVo.setComment(comment);
        String sql = sqlColumns.replace("@dbname",dbName);
        sql = sql.replace("@tablename",table);
        List<Record> list =getList(sql);
        List<ColumnVo> columns = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            Record record = list.get(i);
            ColumnVo column = new ColumnVo();
            column.setName(record.getString("column_name"));
            column.setType(record.getString("column_type"));
            column.setKey(record.getString("column_key"));
            column.setIsNullable(record.getString("is_nullable").equals("NO")?"否":"是");
            column.setComment(record.getString("column_comment"));
            columns.add(column);
        }
        tableVo.setColumns(columns);
        return tableVo;
    }

}