package com.liuxi.generator.model.vo;

import java.util.List;

/**
 * TableVo
 *
 * @author zt
 * @Time: 2019/3/19 10:20
 */
public class TableVo {
    private String table;
    private String comment;
    private List<ColumnVo> columns;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ColumnVo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnVo> columns) {
        this.columns = columns;
    }
}
