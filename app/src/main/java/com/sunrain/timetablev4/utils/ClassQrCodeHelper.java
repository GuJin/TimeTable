package com.sunrain.timetablev4.utils;

import com.sunrain.timetablev4.bean.ClassBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClassQrCodeHelper {

    public static final int version = 2;

    public static JSONObject toJSONObject(List<ClassBean> list) throws JSONException {

        JSONObject courseJsonObject = new JSONObject();

        for (ClassBean classBean : list) {

            JSONObject classroomJsonObject;
            JSONArray classBeanJsonArray;

            if (!courseJsonObject.has(classBean.course)) {
                // 不含有则直接添加
                classroomJsonObject = new JSONObject();
                classBeanJsonArray = new JSONArray();
                classBeanJsonArray.put(zipForVersion2(classBean));
                classroomJsonObject.put(classBean.classroom, classBeanJsonArray);
                courseJsonObject.put(classBean.course, classroomJsonObject);
                continue;
            }

            classroomJsonObject = courseJsonObject.getJSONObject(classBean.course);
            if (!classroomJsonObject.has(classBean.classroom)) {
                classBeanJsonArray = new JSONArray();
                classBeanJsonArray.put(zipForVersion2(classBean));
                classroomJsonObject.put(classBean.classroom, classBeanJsonArray);
                continue;
            }

            classBeanJsonArray = classroomJsonObject.getJSONArray(classBean.classroom);
            classBeanJsonArray.put(zipForVersion2(classBean));
        }

        courseJsonObject.put("v", version);
        return courseJsonObject;
    }

    public static List<ClassBean> decodeJson(String json) throws JSONException {

        JSONObject courseJsonObject = new JSONObject(json);

        if (!courseJsonObject.has("v")) {
            return null;
        }

        int version;
        try {
            version = courseJsonObject.getInt("v");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        if (version != 2) {
            // 如果version有升级 需要switch
            return null;
        }

        courseJsonObject.remove("v");

        List<ClassBean> classBeanList = new LinkedList<>();

        Iterator<String> courseKeys = courseJsonObject.keys();
        while (courseKeys.hasNext()) {
            String course = courseKeys.next();
            JSONObject classroomJsonObject = courseJsonObject.getJSONObject(course);
            Iterator<String> classroomKeys = classroomJsonObject.keys();
            while (classroomKeys.hasNext()) {
                String classroom = classroomKeys.next();
                JSONArray classBeanJsonArray = classroomJsonObject.getJSONArray(classroom);
                for (int i = 0; i < classBeanJsonArray.length(); i++) {
                    int zip = classBeanJsonArray.getInt(i);
                    ClassBean classBean = new ClassBean();
                    classBean.course = course;
                    classBean.classroom = classroom;
                    unzipForVersion2(classBean, zip);
                    classBeanList.add(classBean);
                }
            }
        }

        return classBeanList;
    }

    /**
     * 不含有course classroom信息
     */
    private static int zipForVersion2(ClassBean classBean) {
        // 如果还要增加字段的话需要留意int最大值
        return classBean.doubleWeek * 10000000 + classBean.endWeek * 100000 + classBean.startWeek * 1000 + classBean.week * 100 +
                classBean.section * 10 + classBean.time;
    }

    private static void unzipForVersion2(ClassBean classBean, int zip) {
        classBean.doubleWeek = zip / 10000000;
        zip = zip - classBean.doubleWeek * 10000000;

        classBean.endWeek = zip / 100000;
        zip = zip - classBean.endWeek * 100000;

        classBean.startWeek = zip / 1000;
        zip = zip - classBean.startWeek * 1000;

        classBean.week = zip / 100;
        zip = zip - classBean.week * 100;

        classBean.section = zip / 10;
        zip = zip - classBean.section * 10;

        classBean.time = zip;
    }
}
