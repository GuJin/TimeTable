package com.sunrain.timetablev4.view.table;

import android.util.SparseArray;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;

import java.util.concurrent.CopyOnWriteArraySet;

public class TableData {

    private static final int REFRESH_MEASURE = 1;
    private static final int REFRESH_DRAW = 2;
    private final CopyOnWriteArraySet<OnTableDataChangedListener> mOnChangedListeners;

    private TableView mTableView;

    private int mCurrentWeek;
    private SparseArray<ClassBean> mClasses;
    private boolean isContentChange;
    private boolean isLayoutChange;

    private int mTableViewRefresh;

    public static TableData getInstance() {
        return TableDataHolder.sInstance;
    }

    void bindTableView(TableView tableView) {
        mTableView = tableView;
        refreshData();
    }

    void unBindTableView() {
        mTableView = null;
    }

    public void setContentChange() {
        isContentChange = true;
        mCurrentWeek = CalendarUtil.getCurrentWeek();
        for (OnTableDataChangedListener onChangedListener : mOnChangedListeners) {
            onChangedListener.onContentChange();
        }
    }

    public void setLayoutChange() {
        isLayoutChange = true;
        for (OnTableDataChangedListener onChangedListener : mOnChangedListeners) {
            onChangedListener.onLayoutChange();
        }
    }

    public void refreshDataIfNeed() {
        if ((!isContentChange) && (!isLayoutChange)) {
            return;
        }

        if (isLayoutChange) {
            mTableViewRefresh = REFRESH_MEASURE;
            if (mTableView != null) {
                mTableView.refreshConfig();
            }
        } else { // isContentChange : true
            mTableViewRefresh = REFRESH_DRAW;
        }

        isLayoutChange = false;
        isContentChange = false;

        refreshData();
    }

    public void registerOnTableDataChangedListener(OnTableDataChangedListener listener) {
        mOnChangedListeners.add(listener);
    }

    public void unregisterOnTableDataChangedListener(OnTableDataChangedListener listener) {
        mOnChangedListeners.remove(listener);
    }

    public interface OnTableDataChangedListener {

        void onContentChange();

        void onLayoutChange();
    }

    SparseArray<ClassBean> getClasses() {
        return mClasses;
    }

    public void setCurrentWeek(int currentWeek) {
        if (currentWeek == mCurrentWeek) {
            return;
        }
        mCurrentWeek = currentWeek;
    }

    public int getCurrentWeek() {
        return mCurrentWeek;
    }

    public void refreshData() {
        boolean isDoubleWeekEnabled = SharedPreUtils.getInt(SharedPreConstants.DOUBLE_WEEK, SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 1;
        if (isDoubleWeekEnabled) {
            mClasses = TableDao.getClasses(mCurrentWeek, CalendarUtil.isDoubleWeek(mCurrentWeek));
        } else {
            mClasses = TableDao.getClasses(mCurrentWeek);
        }

        if (mTableView != null) {
            if (mTableViewRefresh == REFRESH_MEASURE) {
                mTableView.requestLayout();
            } else {
                mTableView.postInvalidateOnAnimation();
            }
        }
    }

    private TableData() {
        mCurrentWeek = CalendarUtil.getCurrentWeek();
        mOnChangedListeners = new CopyOnWriteArraySet<>();
    }

    private static final class TableDataHolder {

        private static final TableData sInstance = new TableData();
    }

}