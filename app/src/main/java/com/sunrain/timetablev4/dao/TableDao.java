package com.sunrain.timetablev4.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.bean.ClassTimeBean;
import com.sunrain.timetablev4.bean.CourseClassroomBean;

import java.util.ArrayList;
import java.util.List;

public class TableDao extends BaseDao {

    private static final String TABLE_NAME = "table_1";

    public static boolean isDataBaseEmpty() {
        SQLiteDatabase database = DBManager.getDb();
        Cursor cursor = query(database, TABLE_NAME, null, null);
        boolean isEmpty = cursor.getCount() == 0;
        cursor.close();
        DBManager.close(database);
        return isEmpty;
    }

    public static long insert(ClassBean classBean) {
        SQLiteDatabase database = DBManager.getDb();
        ContentValues values = new ContentValues(8);
        values.put("week", classBean.week);
        values.put("section", classBean.section);
        values.put("time", classBean.time);
        values.put("startWeek", classBean.startWeek);
        values.put("endWeek", classBean.endWeek);
        values.put("doubleWeek", classBean.doubleWeek);
        values.put("course", classBean.course);
        values.put("classroom", classBean.classroom);
        long _id = database.insert(TABLE_NAME, null, values);
        DBManager.close(database);
        return _id;
    }

    /**
     * 根据上课地点和上课时间，获取课程List
     */
    @NonNull
    public static List<ClassBean> getClassesList(CourseClassroomBean courseClassroomBean) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "course = ? and classroom = ?";
        String[] selectionArgs = {courseClassroomBean.course, courseClassroomBean.classroom};
        Cursor cursor = query(db, TABLE_NAME, selection, selectionArgs);

        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return new ArrayList<>(0);
        }

        int idIndex = cursor.getColumnIndex("_id");
        int weekIndex = cursor.getColumnIndex("week");
        int sectionIndex = cursor.getColumnIndex("section");
        int timeIndex = cursor.getColumnIndex("time");
        int startWeekIndex = cursor.getColumnIndex("startWeek");
        int endWeekIndex = cursor.getColumnIndex("endWeek");
        int doubleWeekIndex = cursor.getColumnIndex("doubleWeek");
        int courseIndex = cursor.getColumnIndex("course");
        int classroomIndex = cursor.getColumnIndex("classroom");

        List<ClassBean> list = new ArrayList<>(count);
        while (cursor.moveToNext()) {
            ClassBean bean = new ClassBean();
            bean._id = cursor.getLong(idIndex);
            bean.week = cursor.getInt(weekIndex);
            bean.section = cursor.getInt(sectionIndex);
            bean.time = cursor.getInt(timeIndex);
            bean.startWeek = cursor.getInt(startWeekIndex);
            bean.endWeek = cursor.getInt(endWeekIndex);
            bean.doubleWeek = cursor.getInt(doubleWeekIndex);
            bean.course = cursor.getString(courseIndex);
            bean.classroom = cursor.getString(classroomIndex);
            list.add(bean);
        }
        cursor.close();
        DBManager.close(db);
        return list;
    }

    @NonNull
    public static List<ClassBean> getClasses(int week, int section, int time) {
        SQLiteDatabase db = DBManager.getDb();

        String selection = "week == ? and section == ? and time == ?";
        String[] selectionArgs = {String.valueOf(week), String.valueOf(section), String.valueOf(time)};
        String[] columns = {"_id", "startWeek", "endWeek", "doubleWeek", "course", "classroom"};

        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);

        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return new ArrayList<>(0);
        }

        int idIndex = cursor.getColumnIndex("_id");
        int startWeekIndex = cursor.getColumnIndex("startWeek");
        int endWeekIndex = cursor.getColumnIndex("endWeek");
        int doubleWeekIndex = cursor.getColumnIndex("doubleWeek");
        int courseIndex = cursor.getColumnIndex("course");
        int classroomIndex = cursor.getColumnIndex("classroom");

        List<ClassBean> list = new ArrayList<>(count);

        while (cursor.moveToNext()) {
            ClassBean bean = new ClassBean();
            bean._id = cursor.getLong(idIndex);
            bean.week = week;
            bean.section = section;
            bean.time = time;
            bean.startWeek = cursor.getInt(startWeekIndex);
            bean.endWeek = cursor.getInt(endWeekIndex);
            bean.doubleWeek = cursor.getInt(doubleWeekIndex);
            bean.course = cursor.getString(courseIndex);
            bean.classroom = cursor.getString(classroomIndex);

            list.add(bean);
        }

        cursor.close();
        DBManager.close(db);
        return list;
    }

    @NonNull
    public static List<ClassBean> getClasses() {
        SQLiteDatabase db = DBManager.getDb();
        Cursor cursor = query(db, TABLE_NAME, null, null);

        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return new ArrayList<>(0);
        }

        int weekIndex = cursor.getColumnIndex("week");
        int sectionIndex = cursor.getColumnIndex("section");
        int timeIndex = cursor.getColumnIndex("time");
        int startWeekIndex = cursor.getColumnIndex("startWeek");
        int endWeekIndex = cursor.getColumnIndex("endWeek");
        int doubleWeekIndex = cursor.getColumnIndex("doubleWeek");
        int courseIndex = cursor.getColumnIndex("course");
        int classroomIndex = cursor.getColumnIndex("classroom");

        List<ClassBean> list = new ArrayList<>(count);

        while (cursor.moveToNext()) {
            ClassBean bean = new ClassBean();
            bean.week = cursor.getInt(weekIndex);
            bean.section = cursor.getInt(sectionIndex);
            bean.time = cursor.getInt(timeIndex);
            bean.startWeek = cursor.getInt(startWeekIndex);
            bean.endWeek = cursor.getInt(endWeekIndex);
            bean.doubleWeek = cursor.getInt(doubleWeekIndex);
            bean.course = cursor.getString(courseIndex);
            bean.classroom = cursor.getString(classroomIndex);

            list.add(bean);
        }

        cursor.close();
        DBManager.close(db);
        return list;
    }

    @NonNull
    public static SparseArray<ClassBean> getClasses(int currentWeek) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "? >= startWeek and ? <= endWeek";
        String[] selectionArgs = {String.valueOf(currentWeek), String.valueOf(currentWeek)};

        Cursor cursor = query(db, TABLE_NAME, selection, selectionArgs);

        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return new SparseArray<>(0);
        }

        int idIndex = cursor.getColumnIndex("_id");
        int weekIndex = cursor.getColumnIndex("week");
        int sectionIndex = cursor.getColumnIndex("section");
        int timeIndex = cursor.getColumnIndex("time");
        int startWeekIndex = cursor.getColumnIndex("startWeek");
        int endWeekIndex = cursor.getColumnIndex("endWeek");
        int doubleWeekIndex = cursor.getColumnIndex("doubleWeek");
        int courseIndex = cursor.getColumnIndex("course");
        int classroomIndex = cursor.getColumnIndex("classroom");

        SparseArray<ClassBean> sparseArray = new SparseArray<>(count);

        while (cursor.moveToNext()) {
            ClassBean bean = new ClassBean();
            bean._id = cursor.getLong(idIndex);
            bean.week = cursor.getInt(weekIndex);
            bean.section = cursor.getInt(sectionIndex);
            bean.time = cursor.getInt(timeIndex);
            bean.startWeek = cursor.getInt(startWeekIndex);
            bean.endWeek = cursor.getInt(endWeekIndex);
            bean.doubleWeek = cursor.getInt(doubleWeekIndex);
            bean.course = cursor.getString(courseIndex);
            bean.classroom = cursor.getString(classroomIndex);

            sparseArray.put(bean.week * 100 + bean.section * 10 + bean.time, bean);
        }

        cursor.close();
        DBManager.close(db);
        return sparseArray;
    }

    public static void delete(ClassBean classBean) {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, "_id = ?", new String[]{String.valueOf(classBean._id)});
        DBManager.close(db);
    }

    public static void deleteInBackground(final ClassBean classBean) {
        DaoExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                delete(classBean);
            }
        });
    }

    public static void delete(CourseClassroomBean courseClassroomBean) {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, "course = ? and classroom = ?", new String[]{courseClassroomBean.course, courseClassroomBean.classroom});
        DBManager.close(db);
    }

    public static void deleteInBackground(final CourseClassroomBean courseClassroomBean) {
        DaoExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                delete(courseClassroomBean);
            }
        });
    }

    public static void updateClassTime(long id, ClassTimeBean bean) {
        SQLiteDatabase db = DBManager.getDb();
        ContentValues contentValues = new ContentValues(5);
        contentValues.put("week", bean.week);
        contentValues.put("section", bean.section);
        contentValues.put("time", bean.time);
        contentValues.put("startWeek", bean.startWeek);
        contentValues.put("endWeek", bean.endWeek);
        update(db, TABLE_NAME, contentValues, "_id = ?", new String[]{String.valueOf(id)});
        DBManager.close(db);
    }

    public static void updateClassTimeInBackground(final long id, final ClassTimeBean bean) {
        DaoExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                updateClassTime(id, bean);
            }
        });
    }

    public static void clear() {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, null, null);
        DBManager.close(db);
    }

    public static void clearInBackground() {
        DaoExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                clear();
            }
        });
    }
}
