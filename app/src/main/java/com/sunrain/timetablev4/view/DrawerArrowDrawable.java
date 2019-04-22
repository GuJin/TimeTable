package com.sunrain.timetablev4.view;


import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;

import com.sunrain.timetablev4.application.MyApplication;

import java.lang.ref.WeakReference;

import static android.graphics.Color.WHITE;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.BUTT;
import static android.graphics.Paint.SUBPIXEL_TEXT_FLAG;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.PixelFormat.TRANSLUCENT;

/**
 * 原作者：https://github.com/ChrisRenke/DrawerArrowDrawable
 */
public class DrawerArrowDrawable extends Drawable {

    private final static float PATH_GEN_DENSITY = 3;

    private final static float DIMEN_DP = 23.5f;
    private final static float STROKE_WIDTH_DP = 2;
    private final ArrowTimer mArrowTimer;
    private BridgingLine topLine;
    private BridgingLine middleLine;
    private BridgingLine bottomLine;
    private final Rect bounds;

    private final Paint linePaint;
    private boolean flip;

    private float parameter;
    private final float coordsA[] = {0f, 0f};
    private final float coordsB[] = {0f, 0f};

    private final ArrowHandler mHandler;
    private AnimationListener mAnimationListener;

    public DrawerArrowDrawable() {

        mHandler = new ArrowHandler(this);
        mArrowTimer = new ArrowTimer();

        float density = MyApplication.sContext.getResources().getDisplayMetrics().density;
        float strokeWidthPixel = STROKE_WIDTH_DP * density;

        linePaint = new Paint(SUBPIXEL_TEXT_FLAG | ANTI_ALIAS_FLAG);
        linePaint.setStrokeCap(BUTT);
        linePaint.setColor(WHITE);
        linePaint.setStyle(STROKE);
        linePaint.setStrokeWidth(strokeWidthPixel);

        int dimen = (int) (DIMEN_DP * density);
        bounds = new Rect(0, 0, dimen, dimen);

        drawLine(density);
    }

    public void setAnimationListener(AnimationListener animationListener) {
        mAnimationListener = animationListener;
    }

    @Override
    public int getIntrinsicHeight() {
        return bounds.height();
    }

    @Override
    public int getIntrinsicWidth() {
        return bounds.width();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (flip) {
            canvas.save();
            canvas.scale(1f, -1f, getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
        }

        topLine.draw(canvas);
        middleLine.draw(canvas);
        bottomLine.draw(canvas);

        if (flip) {
            canvas.restore();
        }
    }

    @Override
    public void setAlpha(int alpha) {
        linePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        linePaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return TRANSLUCENT;
    }

    private void setParameter(float parameter) {
        this.parameter = parameter;
        invalidateSelf();
    }

    public void startArrowAnim() {
        mArrowTimer.prepare(ArrowTimer.TYPE_ARROW);
        mHandler.sendEmptyMessage(ArrowHandler.TIME_TASK);
    }

    public void startHamburgerAnim() {
        mArrowTimer.prepare(ArrowTimer.TYPE_HAMBURGER);
        mHandler.sendEmptyMessage(ArrowHandler.TIME_TASK);
    }

    public interface AnimationListener {
        void onAnimationFinish();
    }

    /**
     * When false, rotates from 3 o'clock to 9 o'clock between a drawer icon and a back arrow.
     * When true, rotates from 9 o'clock to 3 o'clock between a back arrow and a drawer icon.
     */
    private void setFlip(boolean flip) {
        this.flip = flip;
    }

    /**
     * Scales the paths to the given screen density. If the density matches the
     * {@link DrawerArrowDrawable#PATH_GEN_DENSITY}, no scaling needs to be done.
     */
    private static void scalePath(Path path, float density) {
        if (density == PATH_GEN_DENSITY)
            return;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(density / PATH_GEN_DENSITY, density / PATH_GEN_DENSITY, 0, 0);
        path.transform(scaleMatrix);
    }

    private class ArrowTimer {

        private static final int TYPE_ARROW = 0;
        private static final int TYPE_HAMBURGER = 1;

        private boolean isArrow;
        private float delta;
        private float current;
        private boolean finish;

        void prepare(int type) {
            isArrow = type == 0;
            delta = isArrow ? 0.05f : -0.05f;
            current = isArrow ? 0f : 1f;
            finish = false;
        }

        void tick() {
            current += delta;

            if (current >= 1f && isArrow) {
                current = 1f;
                finish = true;
            } else if (current <= 0f && !isArrow) {
                current = 0f;
                finish = true;
            }

            setParameter(current);

            if (finish) {
                setFlip(isArrow);
                mHandler.sendEmptyMessage(ArrowHandler.ANIM_FINISH);
                return;
            }

            mHandler.sendEmptyMessage(ArrowHandler.TIME_TASK);
        }
    }


    private static class ArrowHandler extends Handler {

        private static final int TIME_TASK = 1;
        private static final int ANIM_FINISH = 2;
        private final WeakReference<DrawerArrowDrawable> mDrawerArrowDrawableWeakReference;

        private ArrowHandler(DrawerArrowDrawable drawable) {
            super(Looper.getMainLooper());
            mDrawerArrowDrawableWeakReference = new WeakReference<>(drawable);
        }

        @Override
        public void handleMessage(Message msg) {

            DrawerArrowDrawable drawerArrowDrawable = mDrawerArrowDrawableWeakReference.get();
            if (drawerArrowDrawable == null) {
                return;
            }

            switch (msg.what) {
                case TIME_TASK:
                    drawerArrowDrawable.mArrowTimer.tick();
                    break;
                case ANIM_FINISH:
                    if (drawerArrowDrawable.mAnimationListener != null) {
                        drawerArrowDrawable.mAnimationListener.onAnimationFinish();
                    }
                    break;
            }
        }
    }

    private void drawLine(float density) {
        // Top
        Path first = new Path();
        first.moveTo(5.042f, 20f);
        first.rCubicTo(8.125f, -16.317f, 39.753f, -27.851f, 55.49f, -2.765f);
        Path second = new Path();
        second.moveTo(60.531f, 17.235f);
        second.rCubicTo(11.301f, 18.015f, -3.699f, 46.083f, -23.725f, 43.456f);
        scalePath(first, density);
        scalePath(second, density);
        JoinedPath joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 20f);
        first.rCubicTo(4.457f, 16.75f, 1.512f, 37.982f, -22.557f, 42.699f);
        second = new Path();
        second.moveTo(42.402f, 62.699f);
        second.cubicTo(18.333f, 67.418f, 8.807f, 45.646f, 8.807f, 32.823f);
        scalePath(first, density);
        scalePath(second, density);
        JoinedPath joinedB = new JoinedPath(first, second);
        topLine = new BridgingLine(joinedA, joinedB);

        // Middle
        first = new Path();
        first.moveTo(5.042f, 35f);
        first.cubicTo(5.042f, 20.333f, 18.625f, 6.791f, 35f, 6.791f);
        second = new Path();
        second.moveTo(35f, 6.791f);
        second.rCubicTo(16.083f, 0f, 26.853f, 16.702f, 26.853f, 28.209f);
        scalePath(first, density);
        scalePath(second, density);
        joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 35f);
        first.rCubicTo(0f, 10.926f, -8.709f, 26.416f, -29.958f, 26.416f);
        second = new Path();
        second.moveTo(35f, 61.416f);
        second.rCubicTo(-7.5f, 0f, -23.946f, -8.211f, -23.946f, -26.416f);
        scalePath(first, density);
        scalePath(second, density);
        joinedB = new JoinedPath(first, second);
        middleLine = new BridgingLine(joinedA, joinedB);

        // Bottom
        first = new Path();
        first.moveTo(5.042f, 50f);
        first.cubicTo(2.5f, 43.312f, 0.013f, 26.546f, 9.475f, 17.346f);
        second = new Path();
        second.moveTo(9.475f, 17.346f);
        second.rCubicTo(9.462f, -9.2f, 24.188f, -10.353f, 27.326f, -8.245f);
        scalePath(first, density);
        scalePath(second, density);
        joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 50f);
        first.rCubicTo(-7.021f, 10.08f, -20.584f, 19.699f, -37.361f, 12.74f);
        second = new Path();
        second.moveTo(27.598f, 62.699f);
        second.rCubicTo(-15.723f, -6.521f, -18.8f, -23.543f, -18.8f, -25.642f);
        scalePath(first, density);
        scalePath(second, density);
        joinedB = new JoinedPath(first, second);
        bottomLine = new BridgingLine(joinedA, joinedB);
    }

    private static class JoinedPath {

        private final PathMeasure measureFirst;
        private final PathMeasure measureSecond;
        private final float lengthFirst;
        private final float lengthSecond;

        private JoinedPath(Path pathFirst, Path pathSecond) {
            measureFirst = new PathMeasure(pathFirst, false);
            measureSecond = new PathMeasure(pathSecond, false);
            lengthFirst = measureFirst.getLength();
            lengthSecond = measureSecond.getLength();
        }

        private void getPointOnLine(float parameter, float[] coords) {
            if (parameter <= .5f) {
                parameter *= 2;
                measureFirst.getPosTan(lengthFirst * parameter, coords, null);
            } else {
                parameter -= .5f;
                parameter *= 2;
                measureSecond.getPosTan(lengthSecond * parameter, coords, null);
            }
        }
    }

    private class BridgingLine {

        private final JoinedPath pathA;
        private final JoinedPath pathB;

        private BridgingLine(JoinedPath pathA, JoinedPath pathB) {
            this.pathA = pathA;
            this.pathB = pathB;
        }

        private void draw(Canvas canvas) {
            pathA.getPointOnLine(parameter, coordsA);
            pathB.getPointOnLine(parameter, coordsB);
            canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);
        }
    }
}
