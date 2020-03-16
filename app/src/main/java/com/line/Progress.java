package com.line;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class Progress extends View {

    private static final int MAX_VALUE = 100;

    private RectF mSquare;
    private Paint mPaint;

    private float mProgressStrokeWidth;
    private int mProgressStartAngle;
    private int mProgressColor;
    private boolean mIsClockwiseAdd;

    private float mProgressValue;

    private ValueAnimator mValueAnimator;

    public Progress(Context context) {
        this(context, null);
    }

    public Progress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Progress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr);
        init();
    }


    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.Progress, defStyleAttr, 0);
        mProgressStrokeWidth =
                a.getDimension(R.styleable.Progress_progressStrokeWidth, UiUtils.dp2px(5));
        mProgressStartAngle = a.getInteger(R.styleable.Progress_progressStartAngle, 0);
        mProgressColor = a.getColor(R.styleable.Progress_progressColor, 0xffffffff);
        mIsClockwiseAdd = a.getBoolean(R.styleable.Progress_isClockwiseAdd, true);
        a.recycle();
    }

    private void init() {
        mSquare = new RectF();
        mPaint = new Paint();
        mPaint.setColor(mProgressColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mProgressStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float minLen = Math.min(w, h) - mProgressStrokeWidth / 2;
        mSquare.set(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2, minLen, minLen);
    }

    public float getProgressStrokeWidth() {
        return mProgressStrokeWidth;
    }

    public void setProgressStrokeWidth(float progressStrokeWidth) {
        mProgressStrokeWidth = progressStrokeWidth;
    }

    public int getProgressStartAngle() {
        return mProgressStartAngle;
    }

    public void setProgressStartAngle(int progressStartAngle) {
        mProgressStartAngle = progressStartAngle;
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
    }

    public boolean isClockwiseAdd() {
        return mIsClockwiseAdd;
    }

    public void setClockwiseAdd(boolean clockwiseAdd) {
        mIsClockwiseAdd = clockwiseAdd;
    }

    public float getProgressValue() {
        return mProgressValue;
    }

    /**
     * @param value 0到100
     */
    public void setProgressValue(float value) {
        if (mProgressValue == value) {
            return;
        } else if (mProgressValue == MAX_VALUE && value >= MAX_VALUE) {
            return;
        }
        mProgressValue = mIsClockwiseAdd ? value : -value;
        invalidate();
    }

    /**
     * @param isClockwise 是否是逆时针旋转
     * @param duration 旋转一圈的时间
     */
    public void startRotate(boolean isClockwise, long duration) {
        int start = 0;
        int end = 359;
        if (!isClockwise) {
            start = 359;
            end = 0;
        }
        mValueAnimator = ValueAnimator.ofInt(start, end);
        mValueAnimator.setDuration(duration).setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(animation -> {
            int rotate = (int) animation.getAnimatedValue();
            setRotation(rotate);
        });
        mValueAnimator.start();
    }

    /**
     * 停止自转
     */
    public void endRotate() {
        if (mValueAnimator != null) {
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.end();
            mValueAnimator = null;
        }
        setRotation(0);
    }

    /**
     * @return 是否正在自转
     */
    public boolean isRotating() {
        return mValueAnimator != null && mValueAnimator.isRunning();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mProgressValue >= MAX_VALUE) {
            canvas.drawCircle(mSquare.centerX(), mSquare.centerY(), mSquare.width() / 2, mPaint);
        } else {
            canvas.drawArc(mSquare, mProgressStartAngle, 360 * mProgressValue / 100f, false,
                    mPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endRotate();
    }
}
