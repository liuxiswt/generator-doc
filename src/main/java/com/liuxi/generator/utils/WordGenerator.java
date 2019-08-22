package com.liuxi.generator.utils;

import com.liuxi.generator.model.vo.ColumnVo;
import com.liuxi.generator.model.vo.TableVo;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WordGenerator
 *
 * @author xit
 * @Time: 2019/3/19 10:20
 */
public class WordGenerator {
    private static Configuration configuration = null;

    static {

        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
        try {
            configuration.setClassForTemplateLoading(WordGenerator.class,"/template");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WordGenerator() {
        throw new AssertionError();
    }

    public static void createDoc(String dbName, String path, List<TableVo> list) {
        Map map = new HashMap();
        map.put("dbName", dbName);
        map.put("tables", list);
        try {
            Template template = configuration.getTemplate("/database.html");
            String name = path + File.separator + dbName + ".html";
            File f = new File(name);
            Writer w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
            template.process(map, w);
            w.close();
            new Html2DocConverter(path + File.separator + dbName + ".html", path + File
                    .separator + dbName + ".doc")
                    .writeWordFile();
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

}
