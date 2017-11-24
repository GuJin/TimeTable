package com.sunrain.timetablev4.bean;

import android.support.annotation.NonNull;

public class ClassTimeBean {

    public int week;
    public int section;
    public int time;
    public int startWeek;
    public int endWeek;

    public ClassTimeBean() {
    }

    public ClassTimeBean(@NonNull ClassBean classBean) {
        this.week = classBean.week;
        this.section = classBean.section;
        this.time = classBean.time;
        this.startWeek = classBean.startWeek;
        this.endWeek = classBean.endWeek;
    }
}
