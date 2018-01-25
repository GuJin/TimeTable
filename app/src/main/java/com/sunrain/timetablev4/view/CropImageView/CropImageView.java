package com.sunrain.timetablev4.view.CropImageView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.manager.WallpaperManager;
import com.sunrain.timetablev4.utils.FileUtil;
import com.sunrain.timetablev4.utils.ImageUtil;
import com.sunrain.timetablev4.view.CropImageView.animation.SimpleValueAnimator;
import com.sunrain.timetablev4.view.CropImageView.animation.SimpleValueAnimatorListener;
import com.sunrain.timetablev4.view.CropImageView.animation.ValueAnimatorV14;
import com.sunrain.timetablev4.view.CropImageView.callback.Callback;
import com.sunrain.timetablev4.view.CropImageView.callback.CropCallback;
import com.sunrain.timetablev4.view.CropImageView.callback.LoadCallback;
import com.sunrain.timetablev4.view.CropImageView.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 原作者：https://github.com/IsseiAoki/SimpleCropView
 */
public class CropImageView extends ImageView {

    private static final String TAG = CropImageView.class.getSimpleName();

    private static final int HANDLE_SIZE_IN_DP = 14;
    private static final int MIN_FRAME_SIZE_IN_DP = 50;
    private static final int FRAME_STROKE_WEIGHT_IN_DP = 1;
    private static final int GUIDE_STROKE_WEIGHT_IN_DP = 1;
    private static final float DEFAULT_INITIAL_FRAME_SCALE = 1f;
    private static final int DEFAULT_ANIMATION_DURATION_MILLIS = 100;

    private static final int TRANSPARENT = 0x00000000;
    private static final int TRANSLUCENT_WHITE = 0xBBFFFFFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TRANSLUCENT_BLACK = 0xBB000000;

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mScale = 1.0f;
    private float mAngle = 0.0f;
    private float mImgWidth = 0.0f;
    private float mImgHeight = 0.0f;

    private boolean mIsInitialized = false;
    private Matrix mMatrix = null;
    private Paint mPaintTranslucent;
    private Paint mPaintFrame;
    private Paint mPaintBitmap;
    private RectF mFrameRect;
    private RectF mImageRect;
    private PointF mCenter = new PointF();
    private float mLastX, mLastY;
    private boolean mIsRotating = false;
    private boolean mIsAnimating = false;
    private SimpleValueAnimator mAnimator = null;
    private final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    private Interpolator mInterpolator = DEFAULT_INTERPOLATOR;
    private LoadCallback mLoadCallback = null;
    private CropCallback mCropCallback = null;
    private ExecutorService mExecutor;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Uri mSourceUri = null;
    private int mExifRotation = 0;
    private int mOutputMaxWidth;
    private int mOutputMaxHeight;
    private int mOutputWidth = 0;
    private int mOutputHeight = 0;
    private boolean mIsCropping = false;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.WEBP;
    private int mCompressQuality = 100;
    private int mInputImageWidth = 0;
    private int mInputImageHeight = 0;
    private int mOutputImageWidth = 0;
    private int mOutputImageHeight = 0;
    private int mDisplayWidth = 0;
    private int mDisplayHeight = 0;
    private boolean mIsLoading = false;

    private TouchArea mTouchArea = TouchArea.OUT_OF_BOUNDS;

    private CropMode mCropMode = CropMode.CUSTOM;
    private float mMinFrameSize;
    private int mHandleSize;
    private int mTouchPadding = 0;
    private boolean mShowGuide;
    private boolean mShowHandle = true;
    private PointF mCustomRatio = new PointF(1.0f, 1.0f);
    private float mFrameStrokeWeight = 2.0f;
    private float mGuideStrokeWeight = 2.0f;
    private int mBackgroundColor;
    private int mOverlayColor;
    private int mFrameColor;
    private int mHandleColor;
    private int mGuideColor;
    private float mInitialFrameScale; // 0.01 ~ 1.0, 0.75 is default value
    private boolean mIsAnimationEnabled = true;
    private int mAnimationDurationMillis = DEFAULT_ANIMATION_DURATION_MILLIS;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mExecutor = Executors.newSingleThreadExecutor();
        float density = getDensity();
        mHandleSize = (int) (density * HANDLE_SIZE_IN_DP);
        mMinFrameSize = density * MIN_FRAME_SIZE_IN_DP;
        mFrameStrokeWeight = density * FRAME_STROKE_WEIGHT_IN_DP;
        mGuideStrokeWeight = density * GUIDE_STROKE_WEIGHT_IN_DP;

        mPaintFrame = new Paint();
        mPaintTranslucent = new Paint();
        mPaintBitmap = new Paint();
        mPaintBitmap.setFilterBitmap(true);

        mMatrix = new Matrix();
        mScale = 1.0f;
        mBackgroundColor = TRANSPARENT;
        mFrameColor = WHITE;
        mOverlayColor = TRANSLUCENT_BLACK;
        mHandleColor = WHITE;
        mGuideColor = TRANSLUCENT_WHITE;

        // handle Styleable
        handleStyleable(context, attrs, defStyle, density);
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.image = getBitmap();
        ss.mode = this.mCropMode;
        ss.backgroundColor = this.mBackgroundColor;
        ss.overlayColor = this.mOverlayColor;
        ss.frameColor = this.mFrameColor;
        ss.showGuide = this.mShowGuide;
        ss.showHandle = this.mShowHandle;
        ss.handleSize = this.mHandleSize;
        ss.touchPadding = this.mTouchPadding;
        ss.minFrameSize = this.mMinFrameSize;
        ss.customRatioX = this.mCustomRatio.x;
        ss.customRatioY = this.mCustomRatio.y;
        ss.frameStrokeWeight = this.mFrameStrokeWeight;
        ss.guideStrokeWeight = this.mGuideStrokeWeight;
        ss.handleColor = this.mHandleColor;
        ss.guideColor = this.mGuideColor;
        ss.initialFrameScale = this.mInitialFrameScale;
        ss.angle = this.mAngle;
        ss.isAnimationEnabled = this.mIsAnimationEnabled;
        ss.animationDuration = this.mAnimationDurationMillis;
        ss.exifRotation = this.mExifRotation;
        ss.sourceUri = this.mSourceUri;
        ss.compressFormat = this.mCompressFormat;
        ss.compressQuality = this.mCompressQuality;
        ss.outputMaxWidth = this.mOutputMaxWidth;
        ss.outputMaxHeight = this.mOutputMaxHeight;
        ss.outputWidth = this.mOutputWidth;
        ss.outputHeight = this.mOutputHeight;
        ss.inputImageWidth = this.mInputImageWidth;
        ss.inputImageHeight = this.mInputImageHeight;
        ss.outputImageWidth = this.mOutputImageWidth;
        ss.outputImageHeight = this.mOutputImageHeight;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mCropMode = ss.mode;
        this.mBackgroundColor = ss.backgroundColor;
        this.mOverlayColor = ss.overlayColor;
        this.mFrameColor = ss.frameColor;
        this.mShowGuide = ss.showGuide;
        this.mShowHandle = ss.showHandle;
        this.mHandleSize = ss.handleSize;
        this.mTouchPadding = ss.touchPadding;
        this.mMinFrameSize = ss.minFrameSize;
        this.mCustomRatio = new PointF(ss.customRatioX, ss.customRatioY);
        this.mFrameStrokeWeight = ss.frameStrokeWeight;
        this.mGuideStrokeWeight = ss.guideStrokeWeight;
        this.mHandleColor = ss.handleColor;
        this.mGuideColor = ss.guideColor;
        this.mInitialFrameScale = ss.initialFrameScale;
        this.mAngle = ss.angle;
        this.mIsAnimationEnabled = ss.isAnimationEnabled;
        this.mAnimationDurationMillis = ss.animationDuration;
        this.mExifRotation = ss.exifRotation;
        this.mSourceUri = ss.sourceUri;
        this.mCompressFormat = ss.compressFormat;
        this.mCompressQuality = ss.compressQuality;
        this.mOutputMaxWidth = ss.outputMaxWidth;
        this.mOutputMaxHeight = ss.outputMaxHeight;
        this.mOutputWidth = ss.outputWidth;
        this.mOutputHeight = ss.outputHeight;
        this.mInputImageWidth = ss.inputImageWidth;
        this.mInputImageHeight = ss.inputImageHeight;
        this.mOutputImageWidth = ss.outputImageWidth;
        this.mOutputImageHeight = ss.outputImageHeight;
        setImageBitmap(ss.image);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);

        mViewWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        mViewHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getDrawable() != null && !mIsCropping)
            setupLayout(mViewWidth, mViewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);

        if (mIsInitialized) {
            setMatrix();
            Bitmap bm = getBitmap();
            if (bm != null) {
                canvas.drawBitmap(bm, mMatrix, mPaintBitmap);
                // draw edit frame
                drawCropFrame(canvas);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mExecutor.shutdown();
        super.onDetachedFromWindow();
    }

    private void handleStyleable(Context context, AttributeSet attrs, int defStyle, float mDensity) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, defStyle, 0);
        Drawable drawable;
        try {
            drawable = ta.getDrawable(R.styleable.CropImageView_scv_img_src);
            if (drawable != null) {
                setImageDrawable(drawable);
            }
            mBackgroundColor = ta.getColor(R.styleable.CropImageView_scv_background_color, TRANSPARENT);
            mOverlayColor = ta.getColor(R.styleable.CropImageView_scv_overlay_color, TRANSLUCENT_BLACK);
            mFrameColor = ta.getColor(R.styleable.CropImageView_scv_frame_color, WHITE);
            mHandleColor = ta.getColor(R.styleable.CropImageView_scv_handle_color, WHITE);
            mGuideColor = ta.getColor(R.styleable.CropImageView_scv_guide_color, TRANSLUCENT_WHITE);

            mHandleSize = ta.getDimensionPixelSize(R.styleable.CropImageView_scv_handle_size, (int) (HANDLE_SIZE_IN_DP * mDensity));
            mTouchPadding = ta.getDimensionPixelSize(R.styleable.CropImageView_scv_touch_padding, 0);
            mMinFrameSize = ta.getDimensionPixelSize(R.styleable.CropImageView_scv_min_frame_size, (int) (MIN_FRAME_SIZE_IN_DP * mDensity));
            mFrameStrokeWeight = ta.getDimensionPixelSize(R.styleable.CropImageView_scv_frame_stroke_weight, (int)
                    (FRAME_STROKE_WEIGHT_IN_DP * mDensity));
            mGuideStrokeWeight = ta.getDimensionPixelSize(R.styleable.CropImageView_scv_guide_stroke_weight, (int)
                    (GUIDE_STROKE_WEIGHT_IN_DP * mDensity));
            mInitialFrameScale = constrain(ta.getFloat(R.styleable.CropImageView_scv_initial_frame_scale, DEFAULT_INITIAL_FRAME_SCALE),
                    DEFAULT_INITIAL_FRAME_SCALE);
            mIsAnimationEnabled = ta.getBoolean(R.styleable.CropImageView_scv_animation_enabled, true);
            mAnimationDurationMillis = ta.getInt(R.styleable.CropImageView_scv_animation_duration, DEFAULT_ANIMATION_DURATION_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ta.recycle();
        }
    }

    private void drawCropFrame(Canvas canvas) {
        if (mIsRotating)
            return;
        drawOverlay(canvas);
        drawFrame(canvas);
        drawHandles(canvas);
        if (mShowGuide) {
            drawGuidelines(canvas);
        }
    }

    private void drawOverlay(Canvas canvas) {
        mPaintTranslucent.setAntiAlias(true);
        mPaintTranslucent.setFilterBitmap(true);
        mPaintTranslucent.setColor(mOverlayColor);
        mPaintTranslucent.setStyle(Paint.Style.FILL);
        Path path = new Path();
        RectF overlayRect = new RectF((float) Math.floor(mImageRect.left), (float) Math.floor(mImageRect.top), (float) Math.ceil
                (mImageRect.right), (float) Math
                .ceil(mImageRect.bottom));
        path.addRect(overlayRect, Path.Direction.CW);
        path.addRect(mFrameRect, Path.Direction.CCW);
        canvas.drawPath(path, mPaintTranslucent);

    }

    private void drawFrame(Canvas canvas) {
        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setStyle(Paint.Style.STROKE);
        mPaintFrame.setColor(mFrameColor);
        mPaintFrame.setStrokeWidth(mFrameStrokeWeight);
        canvas.drawRect(mFrameRect, mPaintFrame);
    }

    private void drawGuidelines(Canvas canvas) {
        mPaintFrame.setColor(mGuideColor);
        mPaintFrame.setStrokeWidth(mGuideStrokeWeight);
        float h1 = mFrameRect.left + (mFrameRect.right - mFrameRect.left) / 3.0f;
        float h2 = mFrameRect.right - (mFrameRect.right - mFrameRect.left) / 3.0f;
        float v1 = mFrameRect.top + (mFrameRect.bottom - mFrameRect.top) / 3.0f;
        float v2 = mFrameRect.bottom - (mFrameRect.bottom - mFrameRect.top) / 3.0f;
        canvas.drawLine(h1, mFrameRect.top, h1, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(h2, mFrameRect.top, h2, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(mFrameRect.left, v1, mFrameRect.right, v1, mPaintFrame);
        canvas.drawLine(mFrameRect.left, v2, mFrameRect.right, v2, mPaintFrame);
    }

    private void drawHandles(Canvas canvas) {
        mPaintFrame.setStyle(Paint.Style.FILL);
        mPaintFrame.setColor(mHandleColor);
        canvas.drawCircle(mFrameRect.left, mFrameRect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.right, mFrameRect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.left, mFrameRect.bottom, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.right, mFrameRect.bottom, mHandleSize, mPaintFrame);
    }

    private void setMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(mCenter.x - mImgWidth * 0.5f, mCenter.y - mImgHeight * 0.5f);
        mMatrix.postScale(mScale, mScale, mCenter.x, mCenter.y);
        mMatrix.postRotate(mAngle, mCenter.x, mCenter.y);
    }

    private void setupLayout(int viewW, int viewH) {
        if (viewW == 0 || viewH == 0)
            return;
        setCenter(new PointF(getPaddingLeft() + viewW * 0.5f, getPaddingTop() + viewH * 0.5f));
        setScale(calcScale(viewW, viewH, mAngle));
        setMatrix();
        mImageRect = calcImageRect(new RectF(0f, 0f, mImgWidth, mImgHeight), mMatrix);
        mFrameRect = calcFrameRect(mImageRect);
        mIsInitialized = true;
        invalidate();
    }

    private void setRotateLayout(int viewW, int viewH) {
        if (viewW == 0 || viewH == 0)
            return;
        setCenter(new PointF(getPaddingLeft() + viewW * 0.5f, getPaddingTop() + viewH * 0.5f));
        setScale(calcScale(viewW, viewH, mAngle));
        setMatrix();
        mImageRect = calcImageRect(new RectF(0f, 0f, mImgWidth, mImgHeight), mMatrix);
        mFrameRect = calcFrameRect(mImageRect);
        mIsInitialized = true;
        invalidate();
    }

    private float calcScale(int viewW, int viewH, float angle) {
        mImgWidth = getDrawable().getIntrinsicWidth();
        mImgHeight = getDrawable().getIntrinsicHeight();
        if (mImgWidth <= 0)
            mImgWidth = viewW;
        if (mImgHeight <= 0)
            mImgHeight = viewH;
        float viewRatio = (float) viewW / (float) viewH;
        float imgRatio = getRotatedWidth(angle) / getRotatedHeight(angle);
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = viewW / getRotatedWidth(angle);
        } else if (imgRatio < viewRatio) {
            scale = viewH / getRotatedHeight(angle);
        }
        return scale;
    }

    private RectF calcImageRect(RectF rect, Matrix matrix) {
        RectF applied = new RectF();
        matrix.mapRect(applied, rect);
        return applied;
    }

    private RectF calcFrameRect(RectF imageRect) {
        float frameW = getRatioX(imageRect.width());
        float frameH = getRatioY(imageRect.height());
        float imgRatio = imageRect.width() / imageRect.height();
        float frameRatio = frameW / frameH;
        //        float imgRatio = imageRect.width() / imageRect.height();
        //        float frameRatio = 1.0f * mSize.x / mSize.y;
        float l = imageRect.left, t = imageRect.top, r = imageRect.right, b = imageRect.bottom;
        if (frameRatio >= imgRatio) {
            l = imageRect.left;
            r = imageRect.right;
            float hy = (imageRect.top + imageRect.bottom) * 0.5f;
            float hh = (imageRect.width() / frameRatio) * 0.5f;
            t = hy - hh;
            b = hy + hh;
        } else if (frameRatio < imgRatio) {
            t = imageRect.top;
            b = imageRect.bottom;
            float hx = (imageRect.left + imageRect.right) * 0.5f;
            float hw = imageRect.height() * frameRatio * 0.5f;
            l = hx - hw;
            r = hx + hw;
        }
        float w = r - l;
        float h = b - t;
        float cx = l + w / 2;
        float cy = t + h / 2;
        return new RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsInitialized)
            return false;
        if (mIsRotating)
            return false;
        if (mIsAnimating)
            return false;
        if (mIsLoading)
            return false;
        if (mIsCropping)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                if (mTouchArea != TouchArea.OUT_OF_BOUNDS) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                onCancel();
                return true;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                onUp();
                return true;
        }
        return false;
    }


    private void onDown(MotionEvent e) {
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
        checkTouchArea(e.getX(), e.getY());
    }

    private void onMove(MotionEvent e) {
        float diffX = e.getX() - mLastX;
        float diffY = e.getY() - mLastY;
        switch (mTouchArea) {
            case CENTER:
                moveFrame(diffX, diffY);
                break;
            case LEFT_TOP:
                moveHandleLT(diffX);
                break;
            case RIGHT_TOP:
                moveHandleRT(diffX);
                break;
            case LEFT_BOTTOM:
                moveHandleLB(diffX);
                break;
            case RIGHT_BOTTOM:
                moveHandleRB(diffX);
                break;
            case OUT_OF_BOUNDS:
                break;
        }
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
    }

    private void onUp() {
        mShowGuide = false;
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    private void onCancel() {
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    private void checkTouchArea(float x, float y) {
        if (isInsideCornerLeftTop(x, y)) {
            mTouchArea = TouchArea.LEFT_TOP;
            mShowGuide = true;
            return;
        }
        if (isInsideCornerRightTop(x, y)) {
            mTouchArea = TouchArea.RIGHT_TOP;
            mShowGuide = true;
            return;
        }
        if (isInsideCornerLeftBottom(x, y)) {
            mTouchArea = TouchArea.LEFT_BOTTOM;
            mShowGuide = true;
            return;
        }
        if (isInsideCornerRightBottom(x, y)) {
            mTouchArea = TouchArea.RIGHT_BOTTOM;
            mShowGuide = true;
            return;
        }
        if (isInsideFrame(x, y)) {
            mShowGuide = true;
            mTouchArea = TouchArea.CENTER;
            return;
        }
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
    }

    private boolean isInsideFrame(float x, float y) {
        if (mFrameRect.left <= x && mFrameRect.right >= x) {
            if (mFrameRect.top <= y && mFrameRect.bottom >= y) {
                mTouchArea = TouchArea.CENTER;
                return true;
            }
        }
        return false;
    }

    private boolean isInsideCornerLeftTop(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightTop(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerLeftBottom(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightBottom(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private void moveFrame(float x, float y) {
        mFrameRect.left += x;
        mFrameRect.right += x;
        mFrameRect.top += y;
        mFrameRect.bottom += y;
        checkMoveBounds();
    }

    private void moveHandleLT(float diffX) {

        float dy = diffX * getRatioY() / getRatioX();
        mFrameRect.left += diffX;
        mFrameRect.top += dy;
        if (isWidthTooSmall()) {
            float offsetX = mMinFrameSize - getFrameW();
            mFrameRect.left -= offsetX;
            float offsetY = offsetX * getRatioY() / getRatioX();
            mFrameRect.top -= offsetY;
        }
        if (isHeightTooSmall()) {
            float offsetY = mMinFrameSize - getFrameH();
            mFrameRect.top -= offsetY;
            float offsetX = offsetY * getRatioX() / getRatioY();
            mFrameRect.left -= offsetX;
        }
        float ox, oy;
        if (!isInsideHorizontal(mFrameRect.left)) {
            ox = mImageRect.left - mFrameRect.left;
            mFrameRect.left += ox;
            oy = ox * getRatioY() / getRatioX();
            mFrameRect.top += oy;
        }
        if (!isInsideVertical(mFrameRect.top)) {
            oy = mImageRect.top - mFrameRect.top;
            mFrameRect.top += oy;
            ox = oy * getRatioX() / getRatioY();
            mFrameRect.left += ox;
        }

    }

    private void moveHandleRT(float diffX) {

        float dy = diffX * getRatioY() / getRatioX();
        mFrameRect.right += diffX;
        mFrameRect.top -= dy;
        if (isWidthTooSmall()) {
            float offsetX = mMinFrameSize - getFrameW();
            mFrameRect.right += offsetX;
            float offsetY = offsetX * getRatioY() / getRatioX();
            mFrameRect.top -= offsetY;
        }
        if (isHeightTooSmall()) {
            float offsetY = mMinFrameSize - getFrameH();
            mFrameRect.top -= offsetY;
            float offsetX = offsetY * getRatioX() / getRatioY();
            mFrameRect.right += offsetX;
        }
        float ox, oy;
        if (!isInsideHorizontal(mFrameRect.right)) {
            ox = mFrameRect.right - mImageRect.right;
            mFrameRect.right -= ox;
            oy = ox * getRatioY() / getRatioX();
            mFrameRect.top += oy;
        }
        if (!isInsideVertical(mFrameRect.top)) {
            oy = mImageRect.top - mFrameRect.top;
            mFrameRect.top += oy;
            ox = oy * getRatioX() / getRatioY();
            mFrameRect.right -= ox;
        }

    }

    private void moveHandleLB(float diffX) {

        float dy = diffX * getRatioY() / getRatioX();
        mFrameRect.left += diffX;
        mFrameRect.bottom -= dy;
        if (isWidthTooSmall()) {
            float offsetX = mMinFrameSize - getFrameW();
            mFrameRect.left -= offsetX;
            float offsetY = offsetX * getRatioY() / getRatioX();
            mFrameRect.bottom += offsetY;
        }
        if (isHeightTooSmall()) {
            float offsetY = mMinFrameSize - getFrameH();
            mFrameRect.bottom += offsetY;
            float offsetX = offsetY * getRatioX() / getRatioY();
            mFrameRect.left -= offsetX;
        }
        float ox, oy;
        if (!isInsideHorizontal(mFrameRect.left)) {
            ox = mImageRect.left - mFrameRect.left;
            mFrameRect.left += ox;
            oy = ox * getRatioY() / getRatioX();
            mFrameRect.bottom -= oy;
        }
        if (!isInsideVertical(mFrameRect.bottom)) {
            oy = mFrameRect.bottom - mImageRect.bottom;
            mFrameRect.bottom -= oy;
            ox = oy * getRatioX() / getRatioY();
            mFrameRect.left += ox;

        }
    }

    private void moveHandleRB(float diffX) {

        float dy = diffX * getRatioY() / getRatioX();
        mFrameRect.right += diffX;
        mFrameRect.bottom += dy;
        if (isWidthTooSmall()) {
            float offsetX = mMinFrameSize - getFrameW();
            mFrameRect.right += offsetX;
            float offsetY = offsetX * getRatioY() / getRatioX();
            mFrameRect.bottom += offsetY;
        }
        if (isHeightTooSmall()) {
            float offsetY = mMinFrameSize - getFrameH();
            mFrameRect.bottom += offsetY;
            float offsetX = offsetY * getRatioX() / getRatioY();
            mFrameRect.right += offsetX;
        }
        float ox, oy;
        if (!isInsideHorizontal(mFrameRect.right)) {
            ox = mFrameRect.right - mImageRect.right;
            mFrameRect.right -= ox;
            oy = ox * getRatioY() / getRatioX();
            mFrameRect.bottom -= oy;
        }
        if (!isInsideVertical(mFrameRect.bottom)) {
            oy = mFrameRect.bottom - mImageRect.bottom;
            mFrameRect.bottom -= oy;
            ox = oy * getRatioX() / getRatioY();
            mFrameRect.right -= ox;
        }

    }


    private void checkMoveBounds() {
        float diff = mFrameRect.left - mImageRect.left;
        if (diff < 0) {
            mFrameRect.left -= diff;
            mFrameRect.right -= diff;
        }
        diff = mFrameRect.right - mImageRect.right;
        if (diff > 0) {
            mFrameRect.left -= diff;
            mFrameRect.right -= diff;
        }
        diff = mFrameRect.top - mImageRect.top;
        if (diff < 0) {
            mFrameRect.top -= diff;
            mFrameRect.bottom -= diff;
        }
        diff = mFrameRect.bottom - mImageRect.bottom;
        if (diff > 0) {
            mFrameRect.top -= diff;
            mFrameRect.bottom -= diff;
        }
    }

    private boolean isInsideHorizontal(float x) {
        return mImageRect.left <= x && mImageRect.right >= x;
    }

    private boolean isInsideVertical(float y) {
        return mImageRect.top <= y && mImageRect.bottom >= y;
    }

    private boolean isWidthTooSmall() {
        return getFrameW() < mMinFrameSize;
    }

    private boolean isHeightTooSmall() {
        return getFrameH() < mMinFrameSize;
    }


    private void recalculateFrameRect() {
        if (mImageRect == null)
            return;
        if (mIsAnimating) {
            getAnimator().cancelAnimation();
        }
        mFrameRect = calcFrameRect(mImageRect);
        invalidate();
    }

    private float getRatioX(float w) {
        switch (mCropMode) {
            case CUSTOM:
                return mCustomRatio.x;
            default:
                return w;
        }
    }

    private float getRatioY(float h) {
        switch (mCropMode) {
            case CUSTOM:
                return mCustomRatio.y;
            default:
                return h;
        }
    }

    private float getRatioX() {
        switch (mCropMode) {
            case CUSTOM:
                return mCustomRatio.x;
            default:
                return 1;
        }
    }

    private float getRatioY() {
        switch (mCropMode) {
            case CUSTOM:
                return mCustomRatio.y;
            default:
                return 1;
        }
    }

    private float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    private float sq(float value) {
        return value * value;
    }

    private float constrain(float val, float defaultVal) {
        if (val < 0.01f || val > 1.0f)
            return defaultVal;
        return val;
    }

    private void postErrorOnMainThread(final Callback callback) {
        if (callback == null)
            return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onError();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError();
                }
            });
        }
    }

    private Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable d = getDrawable();
        if (d != null && d instanceof BitmapDrawable) {
            bm = ((BitmapDrawable) d).getBitmap();
        }
        return bm;
    }

    private float getRotatedWidth(float angle) {
        return getRotatedWidth(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedWidth(float angle, float width, float height) {
        return angle % 180 == 0 ? width : height;
    }

    private float getRotatedHeight(float angle) {
        return getRotatedHeight(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedHeight(float angle, float width, float height) {
        return angle % 180 == 0 ? height : width;
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap) {
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(mAngle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, true);
    }

    private SimpleValueAnimator getAnimator() {
        setupAnimatorIfNeeded();
        return mAnimator;
    }

    private void setupAnimatorIfNeeded() {
        if (mAnimator == null) {
            mAnimator = new ValueAnimatorV14(mInterpolator);
        }
    }

    private Bitmap decodeRegion() {
        Bitmap cropped = null;
        InputStream is = null;
        try {
            is = getContext().getContentResolver().openInputStream(mSourceUri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            final int originalImageWidth = decoder.getWidth();
            final int originalImageHeight = decoder.getHeight();
            Rect cropRect = calcCropRect(originalImageWidth, originalImageHeight);
            if (mAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(-mAngle);
                RectF rotated = new RectF();
                matrix.mapRect(rotated, new RectF(cropRect));
                rotated.offset(rotated.left < 0 ? originalImageWidth : 0, rotated.top < 0 ? originalImageHeight : 0);
                cropRect = new Rect((int) rotated.left, (int) rotated.top, (int) rotated.right, (int) rotated.bottom);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = cropRect.width();
            options.outHeight = cropRect.height();
            options.inSampleSize = calculateInSampleSize(options);
            options.inJustDecodeBounds = false;
            cropped = decoder.decodeRegion(cropRect, options);
            if (mAngle != 0) {
                Bitmap rotated = getRotatedBitmap(cropped);
                if (cropped != getBitmap() && cropped != rotated) {
                    cropped.recycle();
                }
                cropped = rotated;
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(is);
        }
        return cropped;
    }

    public int calculateInSampleSize(BitmapFactory.Options options) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (height > mDisplayHeight || width > mDisplayWidth) {
            //使用需要的宽高的最大值来计算比率
            final int suitedValue = mDisplayHeight > mDisplayWidth ? mDisplayHeight : mDisplayWidth;
            final int heightRatio = Math.round((float) height / (float) suitedValue);
            final int widthRatio = Math.round((float) width / (float) suitedValue);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
        }
        return inSampleSize;
    }

    public void setOutputMaxWidth(int outputMaxWidth) {
        mOutputMaxWidth = outputMaxWidth;
    }

    public void setOutputMaxHeight(int outputMaxHeight) {
        mOutputMaxHeight = outputMaxHeight;
    }

    private Rect calcCropRect(int originalImageWidth, int originalImageHeight) {
        float scaleToOriginal = getRotatedWidth(mAngle, originalImageWidth, originalImageHeight) / mImageRect.width();
        float offsetX = mImageRect.left * scaleToOriginal;
        float offsetY = mImageRect.top * scaleToOriginal;
        int left = Math.round(mFrameRect.left * scaleToOriginal - offsetX);
        int top = Math.round(mFrameRect.top * scaleToOriginal - offsetY);
        int right = Math.round(mFrameRect.right * scaleToOriginal - offsetX);
        int bottom = Math.round(mFrameRect.bottom * scaleToOriginal - offsetY);
        int imageW = Math.round(getRotatedWidth(mAngle, originalImageWidth, originalImageHeight));
        int imageH = Math.round(getRotatedHeight(mAngle, originalImageWidth, originalImageHeight));
        return new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(right, imageW), Math.min(bottom, imageH));
    }

    private Bitmap scaleBitmapIfNeeded(Bitmap cropped) {
        int width = cropped.getWidth();
        int height = cropped.getHeight();
        int outWidth = 0;
        int outHeight = 0;
        float imageRatio = getRatioX(mFrameRect.width()) / getRatioY(mFrameRect.height());

        if (mOutputWidth > 0) {
            outWidth = mOutputWidth;
            outHeight = Math.round(mOutputWidth / imageRatio);
        } else if (mOutputHeight > 0) {
            outHeight = mOutputHeight;
            outWidth = Math.round(mOutputHeight * imageRatio);
        } else {
            if (mOutputMaxWidth > 0 && mOutputMaxHeight > 0 && (width > mOutputMaxWidth || height > mOutputMaxHeight)) {
                float maxRatio = (float) mOutputMaxWidth / (float) mOutputMaxHeight;
                if (maxRatio >= imageRatio) {
                    outHeight = mOutputMaxHeight;
                    outWidth = Math.round((float) mOutputMaxHeight * imageRatio);
                } else {
                    outWidth = mOutputMaxWidth;
                    outHeight = Math.round((float) mOutputMaxWidth / imageRatio);
                }
            }
        }

        if (outWidth > 0 && outHeight > 0) {
            Bitmap scaled = Utils.getScaledBitmap(cropped, outWidth, outHeight);
            if (cropped != getBitmap() && cropped != scaled) {
                cropped.recycle();
            }
            cropped = scaled;
        }
        return cropped;
    }


    /**
     * Set source image bitmap
     *
     * @param bitmap src image bitmap
     */
    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap); // calles setImageDrawable internally
    }

    /**
     * Set source image resource id
     *
     * @param resId source image resource id
     */
    @Override
    public void setImageResource(int resId) {
        mIsInitialized = false;
        super.setImageResource(resId);
        updateLayout();
    }

    /**
     * Set image drawable.
     *
     * @param drawable source image drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        mIsInitialized = false;
        super.setImageDrawable(drawable);
        updateLayout();
    }

    /**
     * Set image uri
     *
     * @param uri source image local uri
     */
    @Override
    public void setImageURI(Uri uri) {
        mIsInitialized = false;
        super.setImageURI(uri);
        updateLayout();
    }

    private void updateLayout() {
        resetImageInfo();
        Drawable d = getDrawable();
        if (d != null) {
            setupLayout(mViewWidth, mViewHeight);
        }
    }

    private void resetImageInfo() {
        if (mIsLoading)
            return;
        mSourceUri = null;
        mInputImageWidth = 0;
        mInputImageHeight = 0;
        mOutputImageWidth = 0;
        mOutputImageHeight = 0;
        mAngle = mExifRotation;
    }

    /**
     * Load image from Uri.
     *
     * @param sourceUri Image Uri
     * @param callback  Callback
     */
    public void startLoad(@NonNull Uri sourceUri, LoadCallback callback) {
        mLoadCallback = callback;
        mSourceUri = sourceUri;
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mIsLoading = true;
                mExifRotation = Utils.getExifOrientation(getContext(), mSourceUri) - 90;
                int maxSize = Utils.getMaxSize();
                int requestSize = Math.max(mViewWidth, mViewHeight);
                if (requestSize == 0)
                    requestSize = maxSize;
                try {
                    final Bitmap sampledBitmap = Utils.decodeSampledBitmapFromUri(getContext(), mSourceUri, requestSize);
                    mInputImageWidth = Utils.sInputImageWidth;
                    mInputImageHeight = Utils.sInputImageHeight;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAngle = mExifRotation;
                            setImageBitmap(sampledBitmap);
                            if (mLoadCallback != null) {
                                mLoadCallback.onSuccess();
                            }
                            mIsLoading = false;
                        }
                    });
                } catch (OutOfMemoryError | Exception e) {
                    postErrorOnMainThread(mLoadCallback);
                    mIsLoading = false;
                }
            }
        });
    }

    /**
     * Rotate image
     *
     * @param degrees        rotation angle
     * @param durationMillis animation duration in milliseconds
     */
    public void rotateImage(RotateDegrees degrees, int durationMillis) {
        if (mIsRotating) {
            getAnimator().cancelAnimation();
        }
        final float currentAngle = mAngle;
        final float newAngle = (mAngle + degrees.getValue());
        final float angleDiff = newAngle - currentAngle;
        final float currentScale = mScale;
        final float newScale = calcScale(mViewWidth, mViewHeight, newAngle);
        final float scaleDiff = newScale - currentScale;
        SimpleValueAnimator animator = getAnimator();
        animator.addAnimatorListener(new SimpleValueAnimatorListener() {
            @Override
            public void onAnimationStarted() {
                mIsRotating = true;
            }

            @Override
            public void onAnimationUpdated(float scale) {
                mAngle = currentAngle + angleDiff * scale;
                mScale = currentScale + scaleDiff * scale;
                setMatrix();
                invalidate();
            }

            @Override
            public void onAnimationFinished() {
                mAngle = newAngle % 360;
                mScale = newScale;
                setRotateLayout(mViewWidth, mViewHeight);
                mIsRotating = false;
            }
        });
        animator.startAnimation(durationMillis);

    }


    /**
     * Rotate image
     *
     * @param degrees rotation angle
     */
    public void rotateImage(RotateDegrees degrees) {
        rotateImage(degrees, mAnimationDurationMillis);
    }

    /**
     * Get cropped image bitmap
     *
     * @return cropped image bitmap
     */
    public Bitmap getCroppedBitmap() {
        Bitmap source = getBitmap();
        if (source == null)
            return null;

        Bitmap rotated = getRotatedBitmap(source);
        Rect cropRect = calcCropRect(source.getWidth(), source.getHeight());
        Bitmap cropped = Bitmap.createBitmap(rotated, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), null, false);
        if (rotated != cropped && rotated != source) {
            rotated.recycle();
        }
        return cropped;
    }

    /**
     * Crop image from Uri
     *
     * @param cropCallback Callback for cropping the image
     */
    public void startCrop(CropCallback cropCallback) {
        mCropCallback = cropCallback;
        if (mIsCropping) {
            postErrorOnMainThread(mCropCallback);
            return;
        }
        mIsCropping = true;
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap cropped;

                // Use thumbnail for crop
                if (mSourceUri == null) {
                    cropped = getCroppedBitmap();
                    Log.i(TAG, "mSourceUri is null");
                }
                // Use file for crop
                else {
                    cropped = decodeRegion();
                }

                if (cropped == null) {
                    postErrorOnMainThread(mCropCallback);
                    mIsCropping = false;
                    return;
                }

                cropped = scaleBitmapIfNeeded(cropped);

                cropped = ImageUtil.bitmapAddDark(cropped);
                File picFile = new File(MyApplication.sContext.getFilesDir(), WallpaperManager.FILE_NAME);

                if (picFile.exists()) {
                    picFile.delete();
                }

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(picFile);
                    cropped.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    postErrorOnMainThread(mCropCallback);
                    mIsCropping = false;
                    return;
                } finally {
                    FileUtil.close(fileOutputStream);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCropCallback != null) {
                            mCropCallback.onSuccess();
                        }
                    }
                });
            }
        });
    }

    public void setCustomRatio(int ratioX, int ratioY) {
        if (ratioX == 0 || ratioY == 0)
            return;
        mCropMode = CropMode.CUSTOM;
        mCustomRatio = new PointF(ratioX, ratioY);
        recalculateFrameRect();
    }

    public void setBackgroundColor(int bgColor) {
        this.mBackgroundColor = bgColor;
        invalidate();
    }

    private void setScale(float mScale) {
        this.mScale = mScale;
    }

    private void setCenter(PointF mCenter) {
        this.mCenter = mCenter;
    }

    private float getFrameW() {
        return (mFrameRect.right - mFrameRect.left);
    }

    private float getFrameH() {
        return (mFrameRect.bottom - mFrameRect.top);
    }

    public void setDisplay(int width, int height) {
        this.mDisplayWidth = width;
        this.mDisplayHeight = height;
    }

    private enum TouchArea {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    private enum CropMode {
        CUSTOM(7);
        private final int ID;

        CropMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    public enum RotateDegrees {
        ROTATE_90D(90), ROTATE_M90D(-90);

        private final int VALUE;

        RotateDegrees(final int value) {
            this.VALUE = value;
        }

        public int getValue() {
            return VALUE;
        }
    }

    private static class SavedState extends BaseSavedState {

        Bitmap image;
        CropMode mode;
        int backgroundColor;
        int overlayColor;
        int frameColor;
        boolean showGuide;
        boolean showHandle;
        int handleSize;
        int touchPadding;
        float minFrameSize;
        float customRatioX;
        float customRatioY;
        float frameStrokeWeight;
        float guideStrokeWeight;
        boolean isCropEnabled;
        int handleColor;
        int guideColor;
        float initialFrameScale;
        float angle;
        boolean isAnimationEnabled;
        int animationDuration;
        int exifRotation;
        Uri sourceUri;
        Uri saveUri;
        Bitmap.CompressFormat compressFormat;
        int compressQuality;
        boolean isDebug;
        int outputMaxWidth;
        int outputMaxHeight;
        int outputWidth;
        int outputHeight;
        boolean isHandleShadowEnabled;
        int inputImageWidth;
        int inputImageHeight;
        int outputImageWidth;
        int outputImageHeight;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            image = in.readParcelable(Bitmap.class.getClassLoader());
            mode = (CropMode) in.readSerializable();
            backgroundColor = in.readInt();
            overlayColor = in.readInt();
            frameColor = in.readInt();
            showGuide = (in.readInt() != 0);
            showHandle = (in.readInt() != 0);
            handleSize = in.readInt();
            touchPadding = in.readInt();
            minFrameSize = in.readFloat();
            customRatioX = in.readFloat();
            customRatioY = in.readFloat();
            frameStrokeWeight = in.readFloat();
            guideStrokeWeight = in.readFloat();
            isCropEnabled = (in.readInt() != 0);
            handleColor = in.readInt();
            guideColor = in.readInt();
            initialFrameScale = in.readFloat();
            angle = in.readFloat();
            isAnimationEnabled = (in.readInt() != 0);
            animationDuration = in.readInt();
            exifRotation = in.readInt();
            sourceUri = in.readParcelable(Uri.class.getClassLoader());
            saveUri = in.readParcelable(Uri.class.getClassLoader());
            compressFormat = (Bitmap.CompressFormat) in.readSerializable();
            compressQuality = in.readInt();
            isDebug = (in.readInt() != 0);
            outputMaxWidth = in.readInt();
            outputMaxHeight = in.readInt();
            outputWidth = in.readInt();
            outputHeight = in.readInt();
            isHandleShadowEnabled = (in.readInt() != 0);
            inputImageWidth = in.readInt();
            inputImageHeight = in.readInt();
            outputImageWidth = in.readInt();
            outputImageHeight = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flag) {
            super.writeToParcel(out, flag);
            out.writeParcelable(image, flag);
            out.writeSerializable(mode);
            out.writeInt(backgroundColor);
            out.writeInt(overlayColor);
            out.writeInt(frameColor);
            out.writeInt(showGuide ? 1 : 0);
            out.writeInt(showHandle ? 1 : 0);
            out.writeInt(handleSize);
            out.writeInt(touchPadding);
            out.writeFloat(minFrameSize);
            out.writeFloat(customRatioX);
            out.writeFloat(customRatioY);
            out.writeFloat(frameStrokeWeight);
            out.writeFloat(guideStrokeWeight);
            out.writeInt(isCropEnabled ? 1 : 0);
            out.writeInt(handleColor);
            out.writeInt(guideColor);
            out.writeFloat(initialFrameScale);
            out.writeFloat(angle);
            out.writeInt(isAnimationEnabled ? 1 : 0);
            out.writeInt(animationDuration);
            out.writeInt(exifRotation);
            out.writeParcelable(sourceUri, flag);
            out.writeParcelable(saveUri, flag);
            out.writeSerializable(compressFormat);
            out.writeInt(compressQuality);
            out.writeInt(isDebug ? 1 : 0);
            out.writeInt(outputMaxWidth);
            out.writeInt(outputMaxHeight);
            out.writeInt(outputWidth);
            out.writeInt(outputHeight);
            out.writeInt(isHandleShadowEnabled ? 1 : 0);
            out.writeInt(inputImageWidth);
            out.writeInt(inputImageHeight);
            out.writeInt(outputImageWidth);
            out.writeInt(outputImageHeight);
        }

        public static final Creator CREATOR = new Creator() {
            public SavedState createFromParcel(final Parcel inParcel) {
                return new SavedState(inParcel);
            }

            public SavedState[] newArray(final int inSize) {
                return new SavedState[inSize];
            }
        };
    }
}
