package com.sunrain.timetablev4.view.table;

import android.util.SparseArray;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.dao.TableDao;

import java.util.ArrayList;
import java.util.List;

public class TableData {

    private static final int REFRESH_MEASURE = 1;
    private static final int REFRESH_DRAW = 2;

    private TableView mTableView;
    private int mCurrentWeek;
    private SparseArray<ClassBean> mClasses;
    private List<Integer> mKeyList;
    private boolean isContentChange;
    private boolean isLayoutChange;

    private int mTableViewRefresh;

    public static TableData getInstance() {
        return TableDataHolder.sInstance;
    }

    void setTableView(TableView tableView) {
        mTableView = tableView;
        refreshData();
    }

    public void setContentChange() {
        isContentChange = true;
    }

    public void setLayoutChange() {
        isLayoutChange = true;
    }

    public void refreshDataIfNeed() {
        if ((!isContentChange) && (!isLayoutChange)) {
            return;
        }

        if (isLayoutChange) {
            mTableViewRefresh = REFRESH_MEASURE;
            mTableView.refreshConfig();
        } else { // isContentChange : true
            mTableViewRefresh = REFRESH_DRAW;
        }

        isLayoutChange = false;
        isContentChange = false;

        refreshData();
    }


    public ClassBean getClassBean(int week, int section, int time) {
        return mClasses.get(week * 100 + section * 10 + time);
    }

    public SparseArray<ClassBean> getClasses() {
        return mClasses;
    }

    public SparseArray<ClassBean> getClassesCopy() {
        return mClasses.clone();
    }

    public ClassBean getClassBean(int key) {
        return mClasses.get(key);
    }

    public List<Integer> getClassesKey() {
        return mKeyList;
    }

    public void setWorkdays(int workdays) {
        mTableView.setWorkdays(workdays);
    }

    public void setMorningClasses(int morningClasses) {
        mTableView.setMorningClasses(morningClasses);
    }

    public void setAfternoonClasses(int afternoonClasses) {
        mTableView.setAfternoonClasses(afternoonClasses);
    }

    public void setEveningClasses(int eveningClasses) {
        mTableView.setEveningClasses(eveningClasses);
    }

    private void refreshClassesKey() {
        int size = mClasses.size();
        mKeyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            mKeyList.add(mClasses.keyAt(i));
        }
    }

    public void setCurrentWeek(int currentWeek) {
        if (currentWeek == mCurrentWeek) {
            return;
        }
        mCurrentWeek = currentWeek;
    }

    public void refreshData() {
        mClasses = TableDao.getClasses(mCurrentWeek);
        refreshClassesKey();

        if (mTableViewRefresh == REFRESH_MEASURE) {
            mTableView.requestLayout();
        } else {
            mTableView.postInvalidateOnAnimation();
        }
    }

    private TableData() {
    }

    private static final class TableDataHolder {
        private static final TableData sInstance = new TableData();
    }

}