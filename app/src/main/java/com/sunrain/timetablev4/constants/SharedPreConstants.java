package com.sunrain.timetablev4.constants;

public class SharedPreConstants {

    public static final String SEMESTER_START_DATE = "semester_start_date";
    public static final String SEMESTER_END_DATE = "semester_end_date";
    public static final String SEMESTER_WEEK = "semester_week"; //从1开始
    public static final String WORK_DAY = "work_day";
    public static final String MORNING_CLASS_NUMBER = "morning_class_number";
    public static final String AFTERNOON_CLASS_NUMBER = "afternoon_class_number";
    public static final String EVENING_CLASS_NUMBER = "evening_class_number";
    public static final String DOUBLE_WEEK = "double_week";
    public static final String VERSION_CODE = "version_code";
    public static final String APPWIDGET_CURRENT_TIME_1 = "appwidget_current_time_1";

    public static final int DEFAULT_MORNING_CLASS_NUMBER = 2;
    public static final int DEFAULT_AFTERNOON_CLASS_NUMBER = 2;
    public static final int DEFAULT_EVENING_CLASS_NUMBER = 0;
    public static final int DEFAULT_DOUBLE_WEEK = 0;
    public static final int DEFAULT_WORK_DAY = 5;
    public static final int DEFAULT_SEMESTER_WEEK = 20;

    private SharedPreConstants() {
    }
}
