package com.sunrain.timetablev4.utils;

import com.sunrain.timetablev4.constants.SharedPreConstants;

public class CalendarUtil {

    public static int getCurrentWeek() {
        return getCurrentWeek(0);
    }

    public static int getCurrentWeek(long currentDateTime) {
        long startDateTime = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        if (startDateTime == 0) {
            return 0;
        }

        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        if (endDate == 0) {
            return 0;
        }

        if (currentDateTime == 0) {
            currentDateTime = System.currentTimeMillis();
        }

        return (int) ((currentDateTime - startDateTime) / 604800000);
    }

}
