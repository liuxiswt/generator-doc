package com.liuxi.generator.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: Xit
 * @Time: 2019/3/28 13:55
 */
public class DateUtils {

    /** 时间格式(yyyy-MM-dd) */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /** 时间格式(yyyy-MM-dd HH:mm:ss) */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String format(LocalDateTime date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(LocalDateTime date, String pattern) {
        if(date != null){
            DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
            return df.format(date);
        }
        return null;
    }
}
