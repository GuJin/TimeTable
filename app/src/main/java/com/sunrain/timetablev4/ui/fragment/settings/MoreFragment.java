package com.sunrain.timetablev4.ui.fragment.settings;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.client.android.CaptureActivity;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.manager.PermissionManager;
import com.sunrain.timetablev4.manager.WallpaperManager;
import com.sunrain.timetablev4.thread.input_course.InputCourseAnalysisThread;
import com.sunrain.timetablev4.thread.input_course.InputCourseSaveThread;
import com.sunrain.timetablev4.ui.activity.CropActivity;
import com.sunrain.timetablev4.ui.dialog.DonationDialog;
import com.sunrain.timetablev4.ui.dialog.InputCourseDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.ui.dialog.ShareClassDialog;
import com.sunrain.timetablev4.utils.WebUtil;
import com.sunrain.timetablev4.view.table.TableData;

import java.util.List;

import tech.gujin.toast.ToastUtil;

public class MoreFragment extends BaseFragment implements View.OnClickListener, PermissionManager.OnRequestPermissionsListener {

    private final int REQUEST_BACKGROUND_PICK_IMG = 1;
    private final int REQUEST_INPUT_COURSE = 2;
    private final int REQUEST_BACKGROUND_CROP_IMG = 3;

    private final int REQUEST_PERMISSION_BACKGROUND = 1;
    private final int REQUEST_PERMISSION_INPUT_COURSE = 2;
    private final int REQUEST_PERMISSION_SAVE_QR_CODE = 3;

    private PermissionManager mPermissionManager;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void initData() {
        mPermissionManager = new PermissionManager(this);
        setListener();
    }

    private void setListener() {
        View view = getView();
        view.findViewById(R.id.btn_background).setOnClickListener(this);
        view.findViewById(R.id.btn_input_course).setOnClickListener(this);
        view.findViewById(R.id.btn_share_course).setOnClickListener(this);
        view.findViewById(R.id.btn_clear_course).setOnClickListener(this);
        view.findViewById(R.id.btn_github).setOnClickListener(this);
        view.findViewById(R.id.btn_praise).setOnClickListener(this);
        view.findViewById(R.id.btn_donation).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_donation:
                showDonationDialog();
                break;
            case R.id.btn_praise:
                goPraise();
                break;
            case R.id.btn_background:
                checkBackGroundPermission();
                break;
            case R.id.btn_share_course:
                checkSaveQrCodePermission();
                break;
            case R.id.btn_input_course:
                checkInputCoursePermission();
                break;
            case R.id.btn_clear_course:
                showClearCourseDialog();
                break;
            case R.id.btn_github:
                WebUtil.gotoWeb(mActivity, "");
                break;
        }
    }

    private void showClearCourseDialog() {
        new MessageDialog(mActivity).setMessage("清空所有课程数据？").setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("清空", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                TableDao.clear();
                CourseClassroomDao.clear();
                TableData.getInstance().setContentChange();
                ToastUtil.show("已清空");
            }
        }).show();
    }

    private void checkTableDataValid() {
        if (TableDao.isDataBaseEmpty()) {
            ToastUtil.show("课表空");
            return;
        }
        new ShareClassDialog(mActivity).show();
    }

    private void checkInputCoursePermission() {
        mPermissionManager.checkPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                REQUEST_PERMISSION_INPUT_COURSE, R.string.permission_carema_message);
    }

    private void checkSaveQrCodePermission() {
        mPermissionManager.checkPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_SAVE_QR_CODE, R.string.permission_write_message_qr_code);
    }

    private void goPraise() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + mActivity.getPackageName()));
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            ToastUtil.show("跳转应用市场失败");
        }
    }

    private void showDonationDialog() {
        new DonationDialog(mActivity).show();
    }

    @Override
    public void onGranted(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_BACKGROUND) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                    REQUEST_BACKGROUND_PICK_IMG);
        } else if (requestCode == REQUEST_PERMISSION_INPUT_COURSE) {
            startActivityForResult(new Intent(mActivity, CaptureActivity.class), REQUEST_INPUT_COURSE);
        } else if (requestCode == REQUEST_PERMISSION_SAVE_QR_CODE) {
            checkTableDataValid();
        }
    }

    @Override
    public void onDenied(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_BACKGROUND) {
            ToastUtil.show(R.string.permission_read_fail_background);
        } else if (requestCode == REQUEST_PERMISSION_INPUT_COURSE) {
            ToastUtil.show(R.string.permission_carema_fail);
        } else if (requestCode == REQUEST_PERMISSION_SAVE_QR_CODE) {
            ToastUtil.show(R.string.permission_write_fail_qr_code);
        }
    }

    public void importCourseFinished() {
        ToastUtil.show("导入成功");
        TableData.getInstance().setContentChange();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BACKGROUND_PICK_IMG && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(mActivity, CropActivity.class);
            intent.putExtra("imageUrl", data.getData());
            startActivityForResult(intent, REQUEST_BACKGROUND_CROP_IMG);
        } else if (requestCode == REQUEST_BACKGROUND_CROP_IMG && resultCode == Activity.RESULT_OK) {
            WallpaperManager.getInstance().refreshWallpaperInBackground(getActivity());
        } else if (requestCode == REQUEST_INPUT_COURSE && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("result");
            if (TextUtils.isEmpty(result)) {
                ToastUtil.show("导入失败");
                return;
            }
            new InputCourseAnalysisThread(this, result).start();
        }
    }

    public void showImportClassDialog(final List<ClassBean> list) {
        new InputCourseDialog(mActivity, list).setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("导入", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.show("导入中");
                new InputCourseSaveThread(MoreFragment.this, list).start();
            }
        }).show();
    }

    private void checkBackGroundPermission() {
        mPermissionManager.checkPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                .WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_BACKGROUND, R.string.permission_read_message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionManager.onRequestPermissionsResult(requestCode, grantResults);
    }
}
