package com.guiji.apiautomationfinal.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final String PATTERN_TIMESTAMP = "yyyyMMddHHmmss";
    private static final String PATTERN_TIME = "HHmmss";
    private static final String PATTERN_DATE = "yyyyMMdd";

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat(PATTERN_TIMESTAMP).format(new Date());
    }

    public static String getTime() {
        return new SimpleDateFormat(PATTERN_TIME).format(new Date());
    }

    public static String getOffsetDate(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        return new SimpleDateFormat(PATTERN_DATE).format(calendar.getTime());
    }
}