package com.sunrain.timetablev4.thread.input_course;

import android.os.Handler;
import android.os.Looper;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.bean.CourseClassroomBean;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.fragment.settings.MoreFragment;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputCourseSaveThread extends Thread {

    private final WeakReference<MoreFragment> mAboutFragmentWeakReference;
    private final List<ClassBean> mList;


    public InputCourseSaveThread(MoreFragment moreFragment, List<ClassBean> list) {
        mAboutFragmentWeakReference = new WeakReference<>(moreFragment);
        mList = list;
    }

    @Override
    public void run() {
        TableDao.clear();
        CourseClassroomDao.clear();
        Set<CourseClassroomBean> set = new HashSet<>();
        for (ClassBean bean : mList) {
            TableDao.insert(bean);
            set.add(new CourseClassroomBean(bean.course, bean.classroom));
        }

        for (CourseClassroomBean bean : set) {
            CourseClassroomDao.insert(bean);
        }

        final MoreFragment moreFragment = mAboutFragmentWeakReference.get();
        if (moreFragment == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                moreFragment.importCourseFinished();
            }
        });
    }
}
