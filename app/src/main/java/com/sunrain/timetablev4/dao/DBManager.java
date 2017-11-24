package com.sunrain.timetablev4.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

class DBManager {

    private static AtomicInteger sOpenCounter = new AtomicInteger();

    static synchronized SQLiteDatabase getDb() {
        sOpenCounter.incrementAndGet();
        return DataBaseHelper.getInstance().getWritableDatabase();
    }


    static synchronized void close(SQLiteDatabase database) {
        if (sOpenCounter.decrementAndGet() == 0) {
            if (database != null) {
                database.close();
            }
        }
    }
}
