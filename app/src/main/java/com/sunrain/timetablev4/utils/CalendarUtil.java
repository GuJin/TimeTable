package com.sunrain.timetablev4.utils;

import com.sunrain.timetablev4.constants.SharedPreConstants;

import java.util.Calendar;

public class CalendarUtil {
    /**
     * @return 0：星期一
     */
    public static int getDayOfWeek(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTimeInMillis(time);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static int getCurrentWeek() {
        return getCurrentWeek(System.currentTimeMillis());
    }

    public static int getCurrentWeek(long currentDateTime) {
        long startDateTime = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        if (startDateTime == 0) {
            return 0;
        }

        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);
        if (endDate == 0) {
            return 0;
        }

        if (currentDateTime == 0) {
            currentDateTime = System.currentTimeMillis();
        }

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setFirstDayOfWeek(Calendar.MONDAY);
        end.setFirstDayOfWeek(Calendar.MONDAY);

        start.setTimeInMillis(startDateTime);
        end.setTimeInMillis(currentDateTime);

        start.setMinimalDaysInFirstWeek(7);
        end.setMinimalDaysInFirstWeek(7);

        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        return (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / 604800000);
    }

    public static boolean isDoubleWeek(int currentWeek) {
        // 周数从0开始
        return (currentWeek + 1) % 2 == 0;
    }

    private CalendarUtil() {
    }

}
