package com.cadernonline.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final String DATE_FORMAT = "dd/MM/yy";

    private DateUtil() { }

    public static String getPrettyDate(Date date){
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

}