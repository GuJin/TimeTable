package com.sunrain.timetablev4.thread.input_course;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.ui.fragment.settings.MoreFragment;
import com.sunrain.timetablev4.utils.ZipUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import tech.gujin.toast.ToastUtil;

public class InputCourseAnalysisThread extends Thread {

    private final String mResult;
    private final WeakReference<MoreFragment> mAboutFragmentWeakReference;

    public InputCourseAnalysisThread(MoreFragment moreFragment, String result) {
        mResult = result;
        mAboutFragmentWeakReference = new WeakReference<>(moreFragment);
    }

    @Override
    public void run() {
        String json = ZipUtil.unzipString(mResult);
        if (TextUtils.isEmpty(json)) {
            ToastUtil.postShow("解析二维码错误");
            return;
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
            ToastUtil.postShow("解析二维码错误");
            return;
        }

        int length = jsonArray.length();
        final List<ClassBean> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            try {
                list.add(new ClassBean(jsonArray.getJSONObject(i)));
            } catch (JSONException ignore) {
            }
        }

        final MoreFragment moreFragment = mAboutFragmentWeakReference.get();
        if (moreFragment == null) {
            ToastUtil.postShow("异常请重试");
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                moreFragment.showImportClassDialog(list);
            }
        });
    }
}
