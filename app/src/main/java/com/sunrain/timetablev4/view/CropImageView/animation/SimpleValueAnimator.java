package com.sunrain.timetablev4.view.CropImageView.animation;

public interface SimpleValueAnimator {

    void startAnimation(long duration);

    void cancelAnimation();

    void setAnimatorListener(SimpleValueAnimatorListener animatorListener);
}
