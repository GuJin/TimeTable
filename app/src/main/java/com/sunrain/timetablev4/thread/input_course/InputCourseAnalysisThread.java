package com.sunrain.timetablev4.thread.input_course;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.ui.fragment.settings.MoreFragment;
import com.sunrain.timetablev4.utils.ClassQrCodeHelper;
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

        List<ClassBean> list;
        if (json.startsWith("[")) {
            // 一代二维码
            list = getGenerationOneList(json);
        } else {
            try {
                list = ClassQrCodeHelper.decodeJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
                list = null;
            }
        }

        if (list == null) {
            ToastUtil.postShow("解析二维码错误");
            return;
        }

        final MoreFragment moreFragment = mAboutFragmentWeakReference.get();
        if (moreFragment == null) {
            ToastUtil.postShow("异常请重试");
            return;
        }

        final List<ClassBean> finalList = list;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                moreFragment.showImportClassDialog(finalList);
            }
        });
    }

    private List<ClassBean> getGenerationOneList(String json) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        int length = jsonArray.length();
        final List<ClassBean> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            try {
                list.add(new ClassBean(jsonArray.getJSONObject(i)));
            } catch (JSONException ignore) {
            }
        }
        return list;
    }
}
