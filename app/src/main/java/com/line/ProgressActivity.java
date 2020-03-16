package com.line;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class ProgressActivity extends Activity {
    private Progress mArcProgressOne;
    private TextView mArcTv;
    private Progress mArcProgressTwo;
    private Progress mArcProgressThree;
    private Progress mArcProgressFour;
    private Progress mArcProgressFive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_progress);
        initView();
        initData();
    }

    public void initView() {
        mArcProgressOne = findViewById(R.id.arc_progress_one);
        mArcTv = findViewById(R.id.arc_tv);
        mArcProgressTwo = findViewById(R.id.arc_progress_two);
        mArcProgressThree = findViewById(R.id.arc_progress_three);
        mArcProgressFour = findViewById(R.id.arc_progress_four);
        mArcProgressFive = findViewById(R.id.arc_progress_five);
    }

    public void initData() {
        //1.2
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(3000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            mArcProgressOne.setProgressValue(value);
            mArcProgressTwo.setProgressValue(value);
            mArcTv.setText(value + "%");
        });
        valueAnimator.start();

        //3
        ValueAnimator valueAnimator1 = ValueAnimator.ofFloat(0, 100);
        valueAnimator1.setDuration(2000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator1.setInterpolator(new LinearInterpolator());
        valueAnimator1.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (value < 50) {
                mArcProgressThree.setProgressValue(value * 2);
                if (!mArcProgressThree.isClockwiseAdd()) {
                    mArcProgressThree.setClockwiseAdd(true);
                }
            } else if (value >= 50) {
                mArcProgressThree.setProgressValue(100 - (value - 50) * 2);
                if (mArcProgressThree.isClockwiseAdd()) {
                    mArcProgressThree.setClockwiseAdd(false);
                }
            }
        });
        valueAnimator1.start();
        mArcProgressThree.startRotate(true, 2000);

        //4
        ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0, 100, 0);
        valueAnimator4.setDuration(2000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator4.setInterpolator(new LinearInterpolator());
        valueAnimator4.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mArcProgressFour.setProgressValue(value);
        });
        valueAnimator4.start();
        mArcProgressFour.startRotate(true, 2000);

        //5
        ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0, 80, 0);
        valueAnimator5.setDuration(3000).setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator5.setInterpolator(new LinearInterpolator());
        valueAnimator5.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mArcProgressFive.setProgressValue(value);
        });
        valueAnimator5.start();
        mArcProgressFive.startRotate(true, 1000);
    }

}
