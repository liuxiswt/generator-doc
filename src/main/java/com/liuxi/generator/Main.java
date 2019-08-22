package com.liuxi.generator;

import com.liuxi.generator.common.*;
import com.liuxi.generator.utils.GenUtils;
import org.nutz.dao.impl.SimpleDataSource;

import java.util.Scanner;

/**
 * Main
 *
 * @author Xit
 * @version 2018/10/6 0006
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("choose action:\n1:generator code\n2:generator database doc\n" +
                "Select the appropriate numbers choose action type\n" +
                "(Enter 'c' to cancel):\n");
        String actType = sc.nextLine();
        if("c".equals(actType) || Integer.valueOf(actType) <1 || Integer.valueOf(actType) >2){
            System.exit(-1);
        }
        System.out.print("choose database:\n1:MySQL\n2:Oracle\n3:PostgreSQL\n4:SQLServer\n" +
                "Select the appropriate numbers choose database type\n");
        String dbType = sc.nextLine();
        if (Integer.valueOf(dbType) < 1 || Integer.valueOf(dbType) > 4) {
            System.out.println("wrong number,will exit");
            System.exit(-1);
        }
        String serviceName = null;
        if ("2".equals(dbType)) {
            System.out.println("input service name:");
            serviceName = sc.nextLine();
        }
        String dbName = null;
        if ("1".equals(dbType) || "3".equals(dbType) || "4".equals(dbType)) {
            System.out.println("input database name:");
            dbName = sc.nextLine();
        }
        System.out.println("input host:");
        String ip = sc.nextLine();
        System.out.println("input port(default " + getDefaultPort(dbType) + "):");
        String port = sc.nextLine();
        if("".equals(port))
        {
            port = getDefaultPort(dbType);
        }

        System.out.println("input username:");
        String username = sc.nextLine();

        System.out.println("input password:");
        String passowrd = sc.nextLine();

        SimpleDataSource dataSource = new SimpleDataSource();
        if ("1".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + dbName+"?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false");
        } else if ("2".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:oracle:thin:@" + ip + ":" + port + ":" + serviceName);
        } else if ("3".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:postgresql://" + ip + ":" + port + "/" + dbName);
        } else if ("4".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:sqlserver://" + ip + ":" + port + ";common=" + dbName);
        }
        dataSource.setUsername(username);
        dataSource.setPassword(passowrd);
        Generator generator = null;
        switch (dbType) {
            case "1":
                generator = new MySQL(dbName, dataSource);
                break;
            case "2":
                generator = new Oracle(username, dataSource);
                break;
            case "3":
                generator = new PostgreSQL(dbName, dataSource);
                break;
            case "4":
                generator = new SqlServer(dbName, dataSource);
        }
        //生成路径
        System.out.println("input out path:");
        String path = sc.nextLine();

        if(Integer.valueOf(actType) == 2){
            //生成数据库文档
            generator.generateDoc(path);
        }else {
            //生成代码
            System.out.println("input table names:(多个用空格隔开)");
            String[] tables = sc.nextLine().split(" ");
            GenUtils.createFile(generator.generatorCode(tables),path,dbName);
        }

        System.out.println("|***********success************|");
    }

    private static String getDefaultPort(String dbType) {
        String defaultPort = "";

        switch (dbType) {
            case "1": {                defaultPort = "3306";
                break;
            }
            case "2": {
                defaultPort = "1521";
                break;
            }
            case "3": {
                defaultPort = "5432";
                break;
            }
            case "4": {
                defaultPort = "1433";
                break;
            }
            default: {
                defaultPort = "-";
                break;
            }
        }
        return defaultPort;
    }
}
