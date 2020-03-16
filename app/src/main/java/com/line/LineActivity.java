package com.line;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class LineActivity extends Activity {

    private Line mLineView;
    private Line mloadingview1;
    private Line mloadingview2;
    private Line mloadingview3;
    private Line mloadingview4;
    private Line mloadingview5;
    private Line mloadingview6;
    private Line mprogressbar1;
    private Line mprogressbar2;
    private TextView mprogressbartext1;
    private TextView mprogressbartext2;
    private ValueAnimator valueAnimator;
    private ValueAnimator valueAnimator2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_line);
        initView();
        initData();
    }

    public void initView() {
        mLineView = findViewById(R.id.lineview);
        mloadingview1 = findViewById(R.id.loadingview1);
        mloadingview2 = findViewById(R.id.loadingview2);
        mloadingview3 = findViewById(R.id.loadingview3);
        mloadingview4 = findViewById(R.id.loadingview4);
        mloadingview5 = findViewById(R.id.loadingview5);
        mloadingview6 = findViewById(R.id.loadingview6);
        mprogressbar1 = findViewById(R.id.progressbar1);
        mprogressbar2 = findViewById(R.id.progressbar2);
        mprogressbartext1 = findViewById(R.id.progressbartext1);
        mprogressbartext2 = findViewById(R.id.progressbartext2);
    }

    public void initData() {
        mprogressbartext1.setText(mprogressbar1.getProgressValue() * 100 / mprogressbar1.getProgressMax() + "%");
        mprogressbartext2.setText(mprogressbar2.getProgressValue() * 100 / mprogressbar2.getProgressMax() + "%");

        ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0, 100);
        valueAnimator4.setDuration(2000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator4.setInterpolator(new LinearInterpolator());
        valueAnimator4.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (value < 50) {
                mloadingview4.setLoadingSweepAngle(value * 2/100*360);
            } else if (value >= 50) {
                mloadingview4.setLoadingSweepAngle(-(100 - (value - 50) * 2)/100*360);
            }
        });
        valueAnimator4.start();
        mloadingview4.startLoadingLine();

        ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0, 100, 0);
        valueAnimator5.setDuration(2000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator5.setInterpolator(new LinearInterpolator());
        valueAnimator5.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mloadingview5.setLoadingSweepAngle(value/100*360);
        });
        valueAnimator5.start();
        mloadingview5.startLoadingLine();

        ValueAnimator valueAnimator6 = ValueAnimator.ofFloat(0, 80, 0);
        valueAnimator6.setDuration(3000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator6.setInterpolator(new LinearInterpolator());
        valueAnimator6.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mloadingview6.setLoadingSweepAngle(value/100*360);
        });
        valueAnimator6.start();
        mloadingview6.startLoadingLine();
    }

    public void startloading1(View view) {
        mloadingview1.startLoadingLine();
    }

    public void endloading1(View view) {
        mloadingview1.endLoadingLine();
    }

    public void startloading2(View view) {
        mloadingview2.startLoadingLine();
    }

    public void endloading2(View view) {
        mloadingview2.endLoadingLine();
    }

    public void startloading3(View view) {
        mloadingview3.startLoadingLine();
    }

    public void endloading3(View view) {
        mloadingview3.endLoadingLine();
    }

    public void startprogress1(View view) {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.end();
        }
        valueAnimator = ValueAnimator.ofInt((int) mprogressbar1.getProgressValue(), 100);
        valueAnimator.setDuration(3000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            mprogressbar1.setProgressValue(value);
            mprogressbar1.setProgressSecondaryValue(value / 2);
            mprogressbartext1.setText(value * 100 / mprogressbar1.getProgressMax() + "%");
        });

        valueAnimator.start();
    }

    public void backprogress1(View view) {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.end();
        }
        valueAnimator = ValueAnimator.ofInt((int)mprogressbar1.getProgressValue(), 0);
        valueAnimator.setDuration(3000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            mprogressbar1.setProgressValue(value);
            mprogressbar1.setProgressSecondaryValue(value / 2);
            mprogressbartext1.setText(value * 100 / mprogressbar1.getProgressMax() + "%");
        });
        valueAnimator.start();
    }

    public void startprogress2(View view) {
        if (valueAnimator2 != null && valueAnimator2.isRunning()) {
            valueAnimator2.removeAllUpdateListeners();
            valueAnimator2.end();
        }
        valueAnimator2 = ValueAnimator.ofInt((int)mprogressbar2.getProgressValue(), 100);
        valueAnimator2.setDuration(3000);
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            mprogressbar2.setProgressValue(value);
            mprogressbar2.setProgressSecondaryValue(value / 2);
            mprogressbartext2.setText(value * 100 / mprogressbar2.getProgressMax() + "%");
        });
        valueAnimator2.start();
    }

    public void backprogress2(View view) {
        if (valueAnimator2 != null && valueAnimator2.isRunning()) {
            valueAnimator2.removeAllUpdateListeners();
            valueAnimator2.end();
        }
        valueAnimator2 = ValueAnimator.ofInt((int)mprogressbar2.getProgressValue(), 0);
        valueAnimator2.setDuration(3000);
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            mprogressbar2.setProgressValue(value);
            mprogressbar2.setProgressSecondaryValue(value / 2);
            mprogressbartext2.setText(value * 100 / mprogressbar2.getProgressMax() + "%");
        });
        valueAnimator2.start();
    }
}
