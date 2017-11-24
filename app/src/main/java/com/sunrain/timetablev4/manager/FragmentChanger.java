package com.sunrain.timetablev4.manager;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.TextUtils;

import com.sunrain.timetablev4.R;

public class FragmentChanger {
    private final FragmentManager mFragmentManager;
    private final int mContentId;

    private String mLastFragmentName;

    public FragmentChanger(FragmentManager fragmentManager, int contentId) {
        mFragmentManager = fragmentManager;
        mContentId = contentId;
    }

    @SuppressLint("CommitTransaction")
    public void showFragment(Class<? extends Fragment> toFragmentClass) {
        String currentFragmentName = toFragmentClass.getSimpleName();
        if (!TextUtils.isEmpty(mLastFragmentName) && mLastFragmentName.equals(currentFragmentName)) {
            return;
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        hideFragment(mFragmentManager.findFragmentByTag(mLastFragmentName), ft);
        Fragment toFragment = mFragmentManager.findFragmentByTag(currentFragmentName);
        if (toFragment != null) {
            ft.show(toFragment);
        } else {
            try {
                toFragment = toFragmentClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ignored) {
            }
            ft.add(mContentId, toFragment, currentFragmentName);
        }
        mLastFragmentName = currentFragmentName;
        ft.commitAllowingStateLoss();
    }

    private void hideFragment(Fragment hideFragment, FragmentTransaction ft) {
        if (hideFragment != null && hideFragment.isVisible()) {
            ft.hide(hideFragment);
        }
    }

    public String getLastFragmentName() {
        return mLastFragmentName;
    }
}
