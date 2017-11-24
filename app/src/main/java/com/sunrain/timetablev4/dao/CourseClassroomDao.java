package com.sunrain.timetablev4.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.sunrain.timetablev4.bean.CourseClassroomBean;

import java.util.ArrayList;
import java.util.List;

public class CourseClassroomDao extends BaseDao {

    private static final String TABLE_NAME = "course_classroom";

    public static void insert(CourseClassroomBean bean) {
        SQLiteDatabase database = DBManager.getDb();
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("course", bean.course);
        contentValues.put("classroom", bean.classroom);
        insert(database, TABLE_NAME, contentValues);
        DBManager.close(database);
    }

    public static void delete(CourseClassroomBean bean) {
        SQLiteDatabase database = DBManager.getDb();
        delete(database, TABLE_NAME, "course = ? and classroom = ?", new String[]{bean.course, bean.classroom});
        DBManager.close(database);
    }

    @NonNull
    public static List<CourseClassroomBean> getAll() {
        SQLiteDatabase database = DBManager.getDb();
        Cursor cursor = query(database, TABLE_NAME, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return new ArrayList<>(0);
        }

        int courseIndex = cursor.getColumnIndex("course");
        int classroomIndex = cursor.getColumnIndex("classroom");

        List<CourseClassroomBean> list = new ArrayList<>(count);
        while (cursor.moveToNext()) {
            CourseClassroomBean bean = new CourseClassroomBean();
            bean.course = cursor.getString(courseIndex);
            bean.classroom = cursor.getString(classroomIndex);
            list.add(bean);
        }
        cursor.close();
        DBManager.close(database);
        return list;
    }

    public static boolean exists(CourseClassroomBean bean) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "course = ? and classroom = ?";
        String[] selectionArgs = {bean.course, bean.classroom};

        Cursor cursor = query(db, TABLE_NAME, selection, selectionArgs);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        DBManager.close(db);
        return exists;
    }

    public static void clear() {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, null, null);
        DBManager.close(db);
    }
}
