package com.line;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

public class Line extends View {

    private int mWidth;
    private int mHeight;
    private RectF mSquare;
    private float mLen;//mSquare边长

    //以下几个属性为基础属性，必获取，不设置则获取默认值
    private int mLineViewColor;//线条颜色，纯色时使用，默认黑色
    private int mLineViewStartColor;//线条渐变起始颜色，默认黑色
    private int mLineViewEndColor;//线条渐变结束颜色，默认黑色
    private int mLineViewStrokeWidth;//线条宽度，默认1
    //线条两端是否设置圆角，默认true，圆角是在线条两端之外再另加的，所以path的长短需要计算，减去两端的半圆
    //斜线的时候，不是圆角，需要外层布局clipChildren=false，以免被裁减，圆角的情况都有处理自动缩减
    private boolean mLineViewRound;
    private int mRoundRadius;

    @IntDef({ DASHLINE, STRAIGHTLINE, ARROW, LOADINGLINE, TABLINE, RIGHTANGLEARROW, PROGRESSBAR })
    public @interface LineViewType {
    }

    private @LineViewType
    int mLineViewType;//线条类型，默认直线STRAIGHTLINE
    public static final int DASHLINE = 0;//虚线
    public static final int STRAIGHTLINE = 1;//直线
    public static final int ARROW = 2;//箭头
    public static final int RIGHTANGLEARROW = 3;//直角箭头
    public static final int TABLINE = 4;
    public static final int LOADINGLINE = 5;//loading线，不断加载中
    public static final int PROGRESSBAR = 6;//进度条

    //直线
    @IntDef({ HORIZONTAL, VERTICAL, RISING, DECLING })
    public @interface LineViewOrientation {
    }

    private @LineViewOrientation
    int mLineViewOrientation = HORIZONTAL;//线条方向，水平竖直斜上斜下，默认水平
    public static final int HORIZONTAL = 0;//水平
    public static final int VERTICAL = 1;//竖直
    public static final int RISING = 2;//斜上
    public static final int DECLING = 3;//斜下

    //虚线
    private int mDashWidth;//虚线中显示部分宽度，默认10
    private int mDashGap;//虚线中每个显示部分之间空隙，空白部分宽度，默认5
    private int mRealDashWidth = 10;
    private int mRealDashGap = 5;

    @IntDef({ LINESTYLE, CIRCLESTYLE, RECTSTYLE, RHOMBUSSTYLE })
    public @interface DashStyle {
    }

    private @DashStyle
    int mDashStyle = LINESTYLE;//虚线样式
    public static final int LINESTYLE = 0;//直线样式
    public static final int CIRCLESTYLE = 1;//圆形样式
    public static final int RECTSTYLE = 2;//方形样式
    public static final int RHOMBUSSTYLE = 3;//菱形样式
    private Path mDashPath = null;

    private int mLineViewNum = 1;//线条数量，默认1
    private int mLineViewInterval = 10;//线条间隔，线条数量不为1时，默认10

    //箭头
    @IntDef({ ARROW_LEFT, ARROW_TOP, ARROW_RIGHT, ARROW_BOTTOM })
    public @interface ArrowOrientation {
    }

    private @ArrowOrientation
    int mArrowOrientation = ARROW_RIGHT;//箭头方向，ltrb，默认right
    public static final int ARROW_LEFT = 0;
    public static final int ARROW_TOP = 1;
    public static final int ARROW_RIGHT = 2;
    public static final int ARROW_BOTTOM = 3;
    private boolean mArrowRound = false;//箭头的顶端是否设置圆角，默认false，为锐角

    //TABLINE
    private int mTabNum = 4;//tabLine的tab个数，默认4个
    private int mTabLength;//每一段tab的长度
    private int mTabPosition = 0;//tab的位置，默认0
    private int mTabColor = 0xffff0000;//tab的线条颜色，默认红色
    private int mTabStartColor = 0xff000000;//tab线条渐变起始颜色，默认黑色
    private int mTabEndColor = 0xff000000;//tab线条渐变结束颜色，默认黑色
    private int mTabWidth = 1;//tab的线条宽度，默认1
    private int mTabGap = 0;//tab的线条两端空隙，默认0
    private boolean mTabRound = true;//tab的线条是否两端圆角，默认true
    private int mTabRoundRadius;

    @IntDef({ TAB_TOP, TAB_CENTER, TAB_BOTTOM })
    public @interface TabLocation {
    }

    private @TabLocation
    int mTabLocation = TAB_TOP;//tab在tabLine上的位置，top，center，bottom，默认为top
    private int mTabOffset = 0;//tab与tabLine的位置间距，不是紧贴着的，默认为0，上边为负，下边为正
    public static final int TAB_TOP = 0;
    public static final int TAB_CENTER = 1;
    public static final int TAB_BOTTOM = 2;

    private Paint mTabPaint = null;
    private Path mTabPath = null;
    private boolean mSwitchTabAnim = false;//tab切换时是否需要平移的动画
    private int mSwitchTabAnimDuration = 500;//tab切换时平移动画时长
    private int mTabAnimValue;
    private boolean mTabAnimStart = false;

    //LOADINGLINE
    //loading线，不断加载中
    @IntDef({ LOADING_LINE, LOADING_CIRCLE })
    public @interface LoadingType {
    }

    private @LoadingType
    int mLoadingType = LOADING_LINE;//loading的类型，线形和圆形的
    public static final int LOADING_LINE = 0;
    public static final int LOADING_CIRCLE = 1;

    //LOADING_LINE
    @IntDef({ LOADINGSTART_LEFT, LOADINGSTART_CENTER })
    public @interface LoadingStartPosition {
    }

    private @LoadingStartPosition
    int mLoadingStartPosition = LOADINGSTART_CENTER;//loading起始加载地方
    public static final int LOADINGSTART_LEFT = 0;//loading从左向右加载
    public static final int LOADINGSTART_CENTER = 1;//loading从中间向两边加载

    //LOADING_CIRCLE
    private float mLoadingStartAngle = 0;//开始的角度
    private float mLoadingSweepAngle = 90;//弧度
    private boolean mLoadingClockwise = true;//顺时针

    private int mLoadingDuration = 1000;//loading效果时长
    private int mLoadingCount = ValueAnimator.INFINITE;//loading循环次数，默认无限循环
    private ValueAnimator mLoadingAnimator;
    private int mLoadingAnimValue;
    private boolean mLoadingAnimRunning = false;
    private OnLoadingLineListener mLoadingLineListener;

    //PROGRESSBAR
    //进度条
    @IntDef({ PROGRESSBAR_LINE, PROGRESSBAR_CIRCLE })
    public @interface ProgressBarType {
    }

    private @ProgressBarType
    int mProgressType = PROGRESSBAR_LINE;//ProgressBar的类型，线形和圆形的
    public static final int PROGRESSBAR_LINE = 0;
    public static final int PROGRESSBAR_CIRCLE = 1;

    private float mProgressMax = 100;//背景进度条
    private float mProgressValue = 0;//第一进度条(播放)
    private float mProgressSecondaryValue = 0;//第二进度条(缓存)
    //mLineViewColor,mLineViewStrokeWidth,mLineViewRound,第一进度条的颜色，线宽，圆角
    private int mProgressBackgroundColor = 0x00000000;//背景进度条的颜色，默认透明，不显示
    private int mProgressSecondaryColor = 0x00000000;//第二进度条的颜色，默认透明，不显示
    private int mProgressBackgroundWidth;//背景进度条的线宽
    private int mProgressSecondaryWidth;//第二进度条的线宽
    private boolean mProgressBackgroundRound = true;//背景进度条的圆角
    private boolean mProgressSecondaryRound = true;//第二进度条的圆角
    private int mProgressBackgroundRoundRadius;
    private int mProgressSecondaryRoundRadius;
    private Paint mProgressBackgroundPaint = null;
    private Path mProgressBackgroundPath = null;
    private Paint mProgressSecondaryPaint = null;
    private Path mProgressSecondaryPath = null;

    //PROGRESSBAR_CIRCLE
    private float mProgressStartAngle = 0;//开始的角度
    private boolean mProgressClockwise = true;//是否顺时针旋转，默认画圆弧就是顺时针的

    private Paint mPaint = null;//画笔
    private Path mPath = null;//路径
    private PathEffect mPathEffect = null;//虚线效果

    public Line(Context context) {
        this(context, null);
    }

    public Line(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Line(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributeSet(attrs, defStyleAttr);
        init();
    }

    private void setAttributeSet(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.Line, defStyleAttr, 0);
        mLineViewColor = typedArray.getColor(R.styleable.Line_lineColor, 0xff000000);
        mLineViewStartColor =
                typedArray.getColor(R.styleable.Line_lineStartColor, 0xff000000);
        mLineViewEndColor = typedArray.getColor(R.styleable.Line_lineEndColor, 0xff000000);
        mLineViewStrokeWidth =
                typedArray.getDimensionPixelSize(R.styleable.Line_lineStrokeWidth, 1);
        mLineViewType = typedArray.getInt(R.styleable.Line_lineType, STRAIGHTLINE);
        mLineViewRound = typedArray.getBoolean(R.styleable.Line_lineRound, true);
        if (mLineViewType == STRAIGHTLINE || mLineViewType == DASHLINE) {
            mLineViewOrientation =
                    typedArray.getInt(R.styleable.Line_lineOrientation, HORIZONTAL);
            mLineViewNum = typedArray.getInt(R.styleable.Line_lineNum, 1);
            mLineViewNum = mLineViewNum < 1 ? 1 : mLineViewNum;
            if (mLineViewNum > 1) {
                mLineViewInterval =
                        typedArray.getDimensionPixelSize(R.styleable.Line_lineInterval, 10);
                mLineViewInterval = mLineViewInterval < 0 ? 10 : mLineViewInterval;
            }
            if (mLineViewType == DASHLINE) {
                mRealDashWidth =
                        typedArray.getDimensionPixelSize(R.styleable.Line_dashWidth, 10);
                mRealDashGap = typedArray.getDimensionPixelSize(R.styleable.Line_dashGap, 5);
                mDashStyle = typedArray.getInt(R.styleable.Line_dashStyle, LINESTYLE);
            }
        } else if (mLineViewType == ARROW || mLineViewType == RIGHTANGLEARROW) {
            mArrowOrientation =
                    typedArray.getInt(R.styleable.Line_arrowOrientation, ARROW_RIGHT);
            mArrowRound = typedArray.getBoolean(R.styleable.Line_arrowRound, false);
        } else if (mLineViewType == TABLINE) {
            mTabNum = typedArray.getInt(R.styleable.Line_tabNum, 4);
            mTabPosition = typedArray.getInt(R.styleable.Line_tabPosition, 0);
            mTabColor = typedArray.getColor(R.styleable.Line_tabColor, 0xffff0000);
            mTabStartColor = typedArray.getColor(R.styleable.Line_tabStartColor, 0xff000000);
            mTabEndColor = typedArray.getColor(R.styleable.Line_tabEndColor, 0xff000000);
            mTabWidth = typedArray.getDimensionPixelSize(R.styleable.Line_tabWidth, 1);
            mTabGap = typedArray.getDimensionPixelSize(R.styleable.Line_tabGap, 0);
            mTabRound = typedArray.getBoolean(R.styleable.Line_tabRound, true);
            mTabLocation = typedArray.getInt(R.styleable.Line_tabLocation, TAB_TOP);
            mTabOffset = typedArray.getInt(R.styleable.Line_tabOffset, 0);
            mSwitchTabAnim = typedArray.getBoolean(R.styleable.Line_switchTabAnim, false);
            mSwitchTabAnimDuration =
                    typedArray.getInt(R.styleable.Line_switchTabAnimDuration, 500);
        } else if (mLineViewType == LOADINGLINE) {
            mLoadingType = typedArray.getInt(R.styleable.Line_loadingType, LOADING_LINE);
            if (mLoadingType == LOADING_LINE) {
                mLoadingStartPosition = typedArray.getInt(R.styleable.Line_loadingStartPosition,
                        LOADINGSTART_CENTER);
            } else {
                mLoadingStartAngle = typedArray.getFloat(R.styleable.Line_loadingStartAngle, 0);
                mLoadingSweepAngle =
                        typedArray.getFloat(R.styleable.Line_loadingSweepAngle, 90);
                mLoadingClockwise =
                        typedArray.getBoolean(R.styleable.Line_loadingClockwise, true);
            }
            mLoadingDuration = typedArray.getInt(R.styleable.Line_loadingDuration, 1000);
            mLoadingCount =
                    typedArray.getInt(R.styleable.Line_loadingCount, ValueAnimator.INFINITE);
        } else if (mLineViewType == PROGRESSBAR) {
            mProgressType = typedArray.getInt(R.styleable.Line_progressType, PROGRESSBAR_LINE);
            mProgressMax = typedArray.getFloat(R.styleable.Line_progressMax, 100);
            mProgressValue = typedArray.getFloat(R.styleable.Line_progressValue, 0);
            mProgressSecondaryValue =
                    typedArray.getFloat(R.styleable.Line_progressSecondaryValue, 0);
            mProgressBackgroundColor =
                    typedArray.getColor(R.styleable.Line_progressBackgroundColor, 0x00000000);
            mProgressSecondaryColor =
                    typedArray.getColor(R.styleable.Line_progressSecondaryColor, 0x00000000);
            mProgressBackgroundWidth =
                    typedArray.getDimensionPixelSize(R.styleable.Line_progressBackgroundWidth,
                            mLineViewStrokeWidth);
            mProgressSecondaryWidth =
                    typedArray.getDimensionPixelSize(R.styleable.Line_progressSecondaryWidth,
                            mLineViewStrokeWidth);
            mProgressBackgroundRound =
                    typedArray.getBoolean(R.styleable.Line_progressBackgroundRound, true);
            mProgressSecondaryRound =
                    typedArray.getBoolean(R.styleable.Line_progressSecondaryRound, true);
            if (mProgressType == PROGRESSBAR_CIRCLE) {
                mProgressStartAngle =
                        typedArray.getFloat(R.styleable.Line_progressCircleStartAngle, 0);
                mProgressClockwise =
                        typedArray.getBoolean(R.styleable.Line_progressClockwise, true);
            }
            mProgressValue = mProgressValue > mProgressMax ? mProgressMax : mProgressValue;
            mProgressValue = mProgressValue < 0 ? 0 : mProgressValue;
            mProgressSecondaryValue =
                    mProgressSecondaryValue > mProgressMax ? mProgressMax : mProgressSecondaryValue;
            mProgressSecondaryValue = mProgressSecondaryValue < 0 ? 0 : mProgressSecondaryValue;
        }
        typedArray.recycle();//回收
    }

    private void init() {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        //初始化一些画笔参数
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mLineViewStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mLineViewColor);

        initLineViewType();
        initRound();
        initDashPathEffect();
        initArrowRound();
        initTabRound();
        initProgressRound();
    }

    private void initLineViewType() {
        clearLineViewType();
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_CIRCLE) {
            mSquare = new RectF();
            mProgressBackgroundPaint = new Paint();
            mProgressSecondaryPaint = new Paint();
            mProgressBackgroundPaint.setStyle(Paint.Style.STROKE);
            mProgressBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
            mProgressBackgroundPaint.setAntiAlias(true);
            mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
            mProgressSecondaryPaint.setStyle(Paint.Style.STROKE);
            mProgressSecondaryPaint.setStrokeWidth(mProgressSecondaryWidth);
            mProgressSecondaryPaint.setAntiAlias(true);
            mProgressSecondaryPaint.setColor(mProgressSecondaryColor);
            if (mPath != null) {
                mPath = null;
            }
            return;
        } else if (mLineViewType == LOADINGLINE && mLoadingType == LOADING_CIRCLE) {
            mSquare = new RectF();
            if (mPath != null) {
                mPath = null;
            }
            return;
        } else {
            if (mPath == null) {
                mPath = new Path();
            }
        }
        if (mLineViewType == DASHLINE && mDashStyle != LINESTYLE) {
            mDashPath = new Path();
            return;
        }
        if (mLineViewType == TABLINE) {
            mTabPaint = new Paint();
            mTabPath = new Path();
            mTabPaint.setStyle(Paint.Style.STROKE);
            mTabPaint.setStrokeWidth(mTabWidth);
            mTabPaint.setAntiAlias(true);
            mTabPaint.setColor(mTabColor);
            return;
        }
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_LINE) {
            mProgressBackgroundPaint = new Paint();
            mProgressBackgroundPath = new Path();
            mProgressSecondaryPaint = new Paint();
            mProgressSecondaryPath = new Path();
            mProgressBackgroundPaint.setStyle(Paint.Style.STROKE);
            mProgressBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
            mProgressBackgroundPaint.setAntiAlias(true);
            mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
            mProgressSecondaryPaint.setStyle(Paint.Style.STROKE);
            mProgressSecondaryPaint.setStrokeWidth(mProgressSecondaryWidth);
            mProgressSecondaryPaint.setAntiAlias(true);
            mProgressSecondaryPaint.setColor(mProgressSecondaryColor);
            return;
        }
    }

    private void clearLineViewType() {
        if (mDashPath != null) {
            mDashPath = null;
        }
        if (mTabPaint != null) {
            mTabPaint = null;
        }
        if (mTabPath != null) {
            mTabPath = null;
        }
        if (mProgressBackgroundPaint != null) {
            mProgressBackgroundPaint = null;
        }
        if (mProgressBackgroundPath != null) {
            mProgressBackgroundPath = null;
        }
        if (mProgressSecondaryPaint != null) {
            mProgressSecondaryPaint = null;
        }
        if (mProgressSecondaryPath != null) {
            mProgressSecondaryPath = null;
        }
        if (mSquare != null) {
            mSquare = null;
        }
    }

    private void initRound() {
        if (mLineViewRound) {
            //mPaint.setStrokeCap(Paint.Cap.BUTT);//没有样式
            //mPaint.setStrokeCap(Paint.Cap.ROUND);//圆形
            //mPaint.setStrokeCap(Paint.Cap.SQUARE);//方形
            //两端圆角
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mRoundRadius = mLineViewStrokeWidth / 2;
            //虚线设置圆角效果，需要处理一下长度
            if (mLineViewType == DASHLINE) {
                mDashWidth = mRealDashWidth - mLineViewStrokeWidth > 0 ? mRealDashWidth
                        - mLineViewStrokeWidth : 0;
                mDashGap = mRealDashGap + mLineViewStrokeWidth;
            }
        } else {
            mPaint.setStrokeCap(Paint.Cap.BUTT);
            mRoundRadius = 0;
            if (mLineViewType == DASHLINE) {
                mDashWidth = mRealDashWidth;
                mDashGap = mRealDashGap;
            }
        }
        if (mLineViewType == ARROW || mLineViewType == RIGHTANGLEARROW) {
            //箭头的时候，不管有没有设置圆角，都缩减一下
            mRoundRadius = mLineViewStrokeWidth / 2;
        }
    }

    private void initDashPathEffect() {
        if (mLineViewType == DASHLINE) {
            if (mDashStyle == LINESTYLE) {
                //虚线效果
                //intervals on/off（显示部分和空白部分）的成对值，所以至少需要两个以上的数值，可以实现不同的虚线效果
                //phase 偏移值
                //DashPathEffect(float intervals[], float phase)
                mPathEffect = new DashPathEffect(new float[] { mDashWidth, mDashGap }, 0);
            } else if (mDashStyle == CIRCLESTYLE && mDashPath != null) {
                //shape Path画的图形
                //advance 间隔
                //phase 偏移
                //style 样式
                //TRANSLATE 直接平移
                //ROTATE旋转成跟路径一致的方向
                //MORPH 衔接线条交汇处，但是有可能会拉伸
                //PathDashPathEffect(Path shape, float advance, float phase,Style style)
                mDashPath.addCircle(0, 0, mLineViewStrokeWidth * 0.5f, Path.Direction.CW);
                mPathEffect =
                        new PathDashPathEffect(mDashPath, mRealDashGap + mLineViewStrokeWidth, 0,
                                PathDashPathEffect.Style.TRANSLATE);
            } else if (mDashStyle == RECTSTYLE && mDashPath != null) {
                mDashPath.addRect(-mLineViewStrokeWidth * 0.5f, -mLineViewStrokeWidth * 0.5f,
                        mLineViewStrokeWidth * 0.5f, mLineViewStrokeWidth * 0.5f,
                        Path.Direction.CW);
                mPathEffect =
                        new PathDashPathEffect(mDashPath, mRealDashGap + mLineViewStrokeWidth, 0,
                                PathDashPathEffect.Style.TRANSLATE);
            } else if (mDashStyle == RHOMBUSSTYLE && mDashPath != null) {
                mDashPath.moveTo(-mLineViewStrokeWidth * 0.5f, 0);
                mDashPath.lineTo(0, -mLineViewStrokeWidth * 0.5f);
                mDashPath.lineTo(mLineViewStrokeWidth * 0.5f, 0);
                mDashPath.lineTo(0, mLineViewStrokeWidth * 0.5f);
                mDashPath.close();
                mPathEffect =
                        new PathDashPathEffect(mDashPath, mRealDashGap + mLineViewStrokeWidth, 0,
                                PathDashPathEffect.Style.TRANSLATE);
            }
            mPaint.setPathEffect(mPathEffect);
        }
    }

    private void initArrowRound() {
        if (mLineViewType == ARROW || mLineViewType == RIGHTANGLEARROW) {
            if (mArrowRound) {
                //mPaint.setStrokeJoin(Paint.Join.MITER);//锐角
                //mPaint.setStrokeJoin(Paint.Join.ROUND);//圆角
                //mPaint.setStrokeJoin(Paint.Join.BEVEL);//直线
                mPaint.setStrokeJoin(Paint.Join.ROUND);
            } else {
                mPaint.setStrokeJoin(Paint.Join.MITER);
            }
        }
    }

    private void initTabRound() {
        if (mLineViewType == TABLINE && mTabNum > 0) {
            if (mTabRound) {
                mTabPaint.setStrokeCap(Paint.Cap.ROUND);
                mTabRoundRadius = mTabWidth / 2;
            } else {
                mTabPaint.setStrokeCap(Paint.Cap.BUTT);
                mTabRoundRadius = 0;
            }
        }
    }

    private void initProgressRound() {
        if (mLineViewType == PROGRESSBAR) {
            if (mProgressBackgroundRound) {
                mProgressBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
                mProgressBackgroundRoundRadius = mProgressBackgroundWidth / 2;
            } else {
                mProgressBackgroundPaint.setStrokeCap(Paint.Cap.BUTT);
                mProgressBackgroundRoundRadius = 0;
            }
            if (mProgressSecondaryRound) {
                mProgressSecondaryPaint.setStrokeCap(Paint.Cap.ROUND);
                mProgressSecondaryRoundRadius = mProgressSecondaryWidth / 2;
            } else {
                mProgressSecondaryPaint.setStrokeCap(Paint.Cap.BUTT);
                mProgressSecondaryRoundRadius = 0;
            }
        }
    }

    private void setTabLength() {
        if (mLineViewType == TABLINE && mTabNum > 0) {
            mTabLength = mWidth / mTabNum;
        }
    }

    /**
     * 设置渐变色
     */
    private void setShader() {
        setLineShader();
        setTabLength();
        setTabLineShader();
        setLoadingShader();
    }

    private void setLineShader() {
        if (mLineViewType == STRAIGHTLINE || mLineViewType == DASHLINE && mLineViewNum > 0) {
            if (mLineViewStartColor != mLineViewEndColor) {
                if (mLineViewOrientation == HORIZONTAL) {
                    mPaint.setShader(new LinearGradient(-mLineViewStrokeWidth * 0.5f, 0,
                            mWidth + mLineViewStrokeWidth * 0.5f, 0, mLineViewStartColor,
                            mLineViewEndColor, Shader.TileMode.REPEAT));
                } else if (mLineViewOrientation == VERTICAL) {
                    mPaint.setShader(new LinearGradient(0, -mLineViewStrokeWidth * 0.5f, 0,
                            mHeight + mLineViewStrokeWidth * 0.5f, mLineViewStartColor,
                            mLineViewEndColor, Shader.TileMode.REPEAT));
                } else if (mLineViewOrientation == RISING) {
                    mPaint.setShader(new LinearGradient(-mLineViewStrokeWidth * 0.5f,
                            mHeight + mLineViewStrokeWidth * 0.5f,
                            mWidth + mLineViewStrokeWidth * 0.5f, -mLineViewStrokeWidth * 0.5f,
                            mLineViewStartColor, mLineViewEndColor, Shader.TileMode.REPEAT));
                } else if (mLineViewOrientation == DECLING) {
                    mPaint.setShader(new LinearGradient(-mLineViewStrokeWidth * 0.5f,
                            -mLineViewStrokeWidth * 0.5f, mWidth + mLineViewStrokeWidth * 0.5f,
                            mHeight + mLineViewStrokeWidth * 0.5f, mLineViewStartColor,
                            mLineViewEndColor, Shader.TileMode.REPEAT));
                }
            }
        }
    }

    private void setTabLineShader() {
        if (mLineViewType == TABLINE && mTabNum > 0 && mLineViewStartColor != mLineViewEndColor) {
            mPaint.setShader(new LinearGradient(-mLineViewStrokeWidth * 0.5f, 0,
                    mWidth + mLineViewStrokeWidth * 0.5f, 0, mLineViewStartColor, mLineViewEndColor,
                    Shader.TileMode.REPEAT));
        }
        setTabShader();
    }

    private void setTabShader() {
        if (mLineViewType == TABLINE && mTabNum > 0 && mTabStartColor != mTabEndColor) {
            mTabPaint.setShader(
                    new LinearGradient(0, 0, mTabLength, 0, mTabStartColor, mTabEndColor,
                            Shader.TileMode.REPEAT));
        }
    }

    private void setLoadingShader() {
        mLen = Math.min(mWidth, mHeight) - mLineViewStrokeWidth;
        if (mLineViewType == LOADINGLINE && mLineViewStartColor != mLineViewEndColor) {
            if (mLoadingType == LOADING_LINE) {
                if (mLoadingStartPosition == LOADINGSTART_CENTER) {
                    mPaint.setShader(new LinearGradient(mWidth * 0.5f, mHeight * 0.5f,
                            -mLineViewStrokeWidth * 0.5f, mHeight * 0.5f, mLineViewStartColor,
                            mLineViewEndColor, Shader.TileMode.MIRROR));
                } else if (mLoadingStartPosition == LOADINGSTART_LEFT) {
                    mPaint.setShader(
                            new LinearGradient(-mLineViewStrokeWidth * 0.5f, mHeight * 0.5f,
                                    mWidth + mLineViewStrokeWidth * 0.5f, mHeight * 0.5f,
                                    mLineViewStartColor, mLineViewEndColor,
                                    Shader.TileMode.REPEAT));
                }
            } else {
                mPaint.setShader(new SweepGradient((float) mLineViewStrokeWidth / 2 + mLen / 2,
                        (float) mLineViewStrokeWidth / 2 + mLen / 2, new int[] {
                        mLineViewStartColor, mLineViewEndColor
                }, new float[] {
                        mLoadingStartAngle / 360, (mLoadingStartAngle + mLoadingSweepAngle) / 360
                }));
            }
        }
    }

    private void setPath() {
        setLinePath();
        setArrowPath();
        setTabLinePath();
        setLoadingPath();
        setRightAngleArrowPath();
        setProgressBarPath();
    }

    private void setLinePath() {
        if (mLineViewType == STRAIGHTLINE || mLineViewType == DASHLINE && mLineViewNum > 0) {
            mPath.reset();
            if (mLineViewOrientation == HORIZONTAL) {
                for (int i = 0; i < mLineViewNum; i++) {
                    mPath.moveTo(mRoundRadius, (mHeight - (mLineViewNum - 1) * (mLineViewInterval
                            + mLineViewStrokeWidth)) * 0.5f + i * (mLineViewInterval
                            + mLineViewStrokeWidth));
                    mPath.lineTo(mWidth - mRoundRadius, (mHeight - (mLineViewNum - 1) * (
                            mLineViewInterval
                                    + mLineViewStrokeWidth)) * 0.5f + i * (mLineViewInterval
                            + mLineViewStrokeWidth));
                }
            } else if (mLineViewOrientation == VERTICAL) {
                for (int i = 0; i < mLineViewNum; i++) {
                    mPath.moveTo((mWidth - (mLineViewNum - 1) * (mLineViewInterval
                            + mLineViewStrokeWidth)) * 0.5f + i * (mLineViewInterval
                            + mLineViewStrokeWidth), mRoundRadius);
                    mPath.lineTo((mWidth - (mLineViewNum - 1) * (mLineViewInterval
                            + mLineViewStrokeWidth)) * 0.5f + i * (mLineViewInterval
                            + mLineViewStrokeWidth), mHeight - mRoundRadius);
                }
            } else if (mLineViewOrientation == RISING) {
                mPath.moveTo(mRoundRadius, mHeight - mRoundRadius);
                mPath.lineTo(mWidth - mRoundRadius, mRoundRadius);
            } else if (mLineViewOrientation == DECLING) {
                mPath.moveTo(mRoundRadius, mRoundRadius);
                mPath.lineTo(mWidth - mRoundRadius, mHeight - mRoundRadius);
            }
        }
    }

    private void setArrowPath() {
        if (mLineViewType == ARROW) {
            mPath.reset();
            if (mArrowOrientation == ARROW_LEFT) {
                mPath.moveTo(mWidth - mRoundRadius, mRoundRadius);
                mPath.lineTo(mLineViewStrokeWidth, mHeight * 0.5f);
                mPath.lineTo(mWidth - mRoundRadius, mHeight - mRoundRadius);
            } else if (mArrowOrientation == ARROW_TOP) {
                mPath.moveTo(mRoundRadius, mHeight - mRoundRadius);
                mPath.lineTo(mWidth * 0.5f, mLineViewStrokeWidth);
                mPath.lineTo(mWidth - mRoundRadius, mHeight - mRoundRadius);
            } else if (mArrowOrientation == ARROW_RIGHT) {
                mPath.moveTo(mRoundRadius, mRoundRadius);
                mPath.lineTo(mWidth - mLineViewStrokeWidth, mHeight * 0.5f);
                mPath.lineTo(mRoundRadius, mHeight - mRoundRadius);
            } else if (mArrowOrientation == ARROW_BOTTOM) {
                mPath.moveTo(mRoundRadius, mRoundRadius);
                mPath.lineTo(mWidth * 0.5f, mHeight - mLineViewStrokeWidth);
                mPath.lineTo(mWidth - mRoundRadius, mRoundRadius);
            }
        }
    }

    private void setRightAngleArrowPath() {
        if (mLineViewType == RIGHTANGLEARROW) {
            mPath.reset();
            if (mArrowOrientation == ARROW_LEFT) {
                if (mHeight > 2 * mWidth) {
                    mPath.moveTo(mWidth - mRoundRadius,
                            mHeight * 0.5f - (mWidth - mRoundRadius - mLineViewStrokeWidth));
                    mPath.lineTo(mLineViewStrokeWidth, mHeight * 0.5f);
                    mPath.lineTo(mWidth - mRoundRadius,
                            mHeight * 0.5f + (mWidth - mRoundRadius - mLineViewStrokeWidth));
                } else {
                    mPath.moveTo(mLineViewStrokeWidth + mHeight * 0.5f - mRoundRadius,
                            mRoundRadius);
                    mPath.lineTo(mLineViewStrokeWidth, mHeight * 0.5f);
                    mPath.lineTo(mLineViewStrokeWidth + mHeight * 0.5f - mRoundRadius,
                            mHeight - mRoundRadius);
                }
            } else if (mArrowOrientation == ARROW_TOP) {
                if (2 * mHeight > mWidth) {
                    mPath.moveTo(mRoundRadius, mLineViewStrokeWidth + mWidth * 0.5f - mRoundRadius);
                    mPath.lineTo(mWidth * 0.5f, mLineViewStrokeWidth);
                    mPath.lineTo(mWidth - mRoundRadius,
                            mLineViewStrokeWidth + mWidth * 0.5f - mRoundRadius);
                } else {
                    mPath.moveTo(mWidth * 0.5f - (mHeight - mRoundRadius - mLineViewStrokeWidth),
                            mHeight - mRoundRadius);
                    mPath.lineTo(mWidth * 0.5f, mLineViewStrokeWidth);
                    mPath.lineTo(mWidth * 0.5f + (mHeight - mRoundRadius - mLineViewStrokeWidth),
                            mHeight - mRoundRadius);
                }
            } else if (mArrowOrientation == ARROW_RIGHT) {
                if (mHeight > 2 * mWidth) {
                    mPath.moveTo(mRoundRadius,
                            mHeight * 0.5f - (mWidth - mLineViewStrokeWidth - mRoundRadius));
                    mPath.lineTo(mWidth - mLineViewStrokeWidth, mHeight * 0.5f);
                    mPath.lineTo(mRoundRadius,
                            mHeight * 0.5f + (mWidth - mLineViewStrokeWidth - mRoundRadius));
                } else {
                    mPath.moveTo(mWidth - mLineViewStrokeWidth - (mHeight * 0.5f - mRoundRadius),
                            mRoundRadius);
                    mPath.lineTo(mWidth - mLineViewStrokeWidth, mHeight * 0.5f);
                    mPath.lineTo(mWidth - mLineViewStrokeWidth - (mHeight * 0.5f - mRoundRadius),
                            mHeight - mRoundRadius);
                }
            } else if (mArrowOrientation == ARROW_BOTTOM) {
                if (2 * mHeight > mWidth) {
                    mPath.moveTo(mRoundRadius,
                            mHeight - mLineViewStrokeWidth - (mWidth * 0.5f - mRoundRadius));
                    mPath.lineTo(mWidth * 0.5f, mHeight - mLineViewStrokeWidth);
                    mPath.lineTo(mWidth - mRoundRadius,
                            mHeight - mLineViewStrokeWidth - (mWidth * 0.5f - mRoundRadius));
                } else {
                    mPath.moveTo(mWidth * 0.5f - (mHeight - mLineViewStrokeWidth - mRoundRadius),
                            mRoundRadius);
                    mPath.lineTo(mWidth * 0.5f, mHeight - mLineViewStrokeWidth);
                    mPath.lineTo(mWidth * 0.5f + (mHeight - mLineViewStrokeWidth - mRoundRadius),
                            mRoundRadius);
                }
            }
        }
    }

    private void setTabLinePath() {
        if (mTabPosition < 0 || mTabPosition >= mTabNum) {
            return;
        }
        if (mLineViewType == TABLINE) {
            mPath.reset();
            mPath.moveTo(mRoundRadius, mHeight * 0.5f);
            mPath.lineTo(mWidth - mRoundRadius, mHeight * 0.5f);
            setTabPath();
        }
    }

    private void setTabPath() {
        if (mLineViewType == TABLINE && mTabNum > 0) {
            mTabPath.reset();
            if (mTabOffset == 0) {
                if (mTabLocation == TAB_TOP) {
                    mTabPath.moveTo(
                            mTabPosition * mTabLength + mTabRoundRadius + mTabGap + mTabAnimValue,
                            (mHeight - mLineViewStrokeWidth - mTabWidth) * 0.5f);
                    mTabPath.lineTo((mTabPosition + 1) * mTabLength - mTabRoundRadius - mTabGap
                            + mTabAnimValue, (mHeight - mLineViewStrokeWidth - mTabWidth) * 0.5f);
                } else if (mTabLocation == TAB_CENTER) {
                    mTabPath.moveTo(
                            mTabPosition * mTabLength + mTabRoundRadius + mTabGap + mTabAnimValue,
                            mHeight * 0.5f);
                    mTabPath.lineTo((mTabPosition + 1) * mTabLength - mTabRoundRadius - mTabGap
                            + mTabAnimValue, mHeight * 0.5f);
                } else if (mTabLocation == TAB_BOTTOM) {
                    mTabPath.moveTo(
                            mTabPosition * mTabLength + mTabRoundRadius + mTabGap + mTabAnimValue,
                            (mHeight + mLineViewStrokeWidth + mTabWidth) * 0.5f);
                    mTabPath.lineTo((mTabPosition + 1) * mTabLength - mTabRoundRadius - mTabGap
                            + mTabAnimValue, (mHeight + mLineViewStrokeWidth + mTabWidth) * 0.5f);
                }
            } else if (mTabOffset < 0) {
                //在tabLocation == TAB_TOP的基础上减去tabOffset
                mTabPath.moveTo(
                        mTabPosition * mTabLength + mTabRoundRadius + mTabGap + mTabAnimValue,
                        (mHeight - mLineViewStrokeWidth - mTabWidth) * 0.5f + mTabOffset);
                mTabPath.lineTo(
                        (mTabPosition + 1) * mTabLength - mTabRoundRadius - mTabGap + mTabAnimValue,
                        (mHeight - mLineViewStrokeWidth - mTabWidth) * 0.5f + mTabOffset);
            } else {
                //在tabLocation == TAB_BOTTOM的基础上加上tabOffset
                mTabPath.moveTo(
                        mTabPosition * mTabLength + mTabRoundRadius + mTabGap + mTabAnimValue,
                        (mHeight + mLineViewStrokeWidth + mTabWidth) * 0.5f + mTabOffset);
                mTabPath.lineTo(
                        (mTabPosition + 1) * mTabLength - mTabRoundRadius - mTabGap + mTabAnimValue,
                        (mHeight + mLineViewStrokeWidth + mTabWidth) * 0.5f + mTabOffset);
            }
        }
    }

    private void switchTab(int x, boolean switchTabAnim) {
        if (mLineViewType == TABLINE) {
            if (switchTabAnim) {
                switchTabWithAnim(x);
            } else {
                switchTabWithoutAnim(x);
            }
        }
    }

    private void switchTabWithAnim(int x) {
        if (!mTabAnimStart) {
            mTabAnimStart = true;
            final int position = x / mTabLength;
            final int p = position > mTabPosition ? 1 : -1;
            ValueAnimator tabAnimator =
                    ValueAnimator.ofInt(0, p * (position - mTabPosition) * mTabLength);
            tabAnimator.setDuration(mSwitchTabAnimDuration);
            tabAnimator.addUpdateListener(valueAnimator -> {
                mTabAnimValue = (int) valueAnimator.getAnimatedValue() * p;
                setTabPath();
                invalidate();
            });
            tabAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mTabAnimStart = false;
                    mTabAnimValue = 0;
                    mTabPosition = position;
                }
            });
            tabAnimator.start();
        }
    }

    private void switchTabWithoutAnim(int x) {
        mTabPosition = x / mTabLength;
        setTabPath();
        invalidate();
    }

    private void setLoadingPath() {
        if (mLineViewType == LOADINGLINE) {
            if (mLoadingType == LOADING_LINE) {
                mPath.reset();
                if (mLoadingAnimValue == 0) {
                    mPaint.setStrokeCap(Paint.Cap.BUTT);
                } else {
                    if (mLineViewRound) {
                        mPaint.setStrokeCap(Paint.Cap.ROUND);
                    }
                }
                if (mLoadingStartPosition == LOADINGSTART_CENTER) {
                    mPath.moveTo(mWidth * 0.5f, mHeight * 0.5f);
                    mPath.lineTo(mWidth * 0.5f - mLoadingAnimValue, mHeight * 0.5f);
                    mPath.moveTo(mWidth * 0.5f, mHeight * 0.5f);
                    mPath.lineTo(mWidth * 0.5f + mLoadingAnimValue, mHeight * 0.5f);
                } else if (mLoadingStartPosition == LOADINGSTART_LEFT) {
                    mPath.moveTo(mRoundRadius, mHeight * 0.5f);
                    mPath.lineTo(mRoundRadius + mLoadingAnimValue, mHeight * 0.5f);
                }
            } else {
                mSquare.set((float) mLineViewStrokeWidth / 2, (float) mLineViewStrokeWidth / 2,
                        mLen + (float) mLineViewStrokeWidth / 2,
                        mLen + (float) mLineViewStrokeWidth / 2);
            }
        }
    }

    public void startLoadingLine() {
        if (mLineViewType == LOADINGLINE && !mLoadingAnimRunning) {
            mLoadingAnimRunning = true;
            int start = 0;
            int end = 0;
            if (mLoadingType == LOADING_LINE) {
                if (mLoadingStartPosition == LOADINGSTART_CENTER) {
                    end = mWidth / 2 - mRoundRadius;
                } else if (mLoadingStartPosition == LOADINGSTART_LEFT) {
                    end = mWidth - 2 * mRoundRadius;
                }
            } else {
                end = 359;
                if (!mLoadingClockwise) {
                    start = 359;
                    end = 0;
                }
            }
            mLoadingAnimator = ValueAnimator.ofInt(start, end);
            mLoadingAnimator.setDuration(mLoadingDuration);
            mLoadingAnimator.setRepeatCount(mLoadingCount);
            mLoadingAnimator.setInterpolator(new LinearInterpolator());
            mLoadingAnimator.addUpdateListener(valueAnimator -> {
                mLoadingAnimValue = (int) valueAnimator.getAnimatedValue();
                if (mLoadingType == LOADING_LINE) {
                    setLoadingPath();
                    invalidate();
                } else {
                    setRotation(mLoadingAnimValue);
                }
            });
            mLoadingAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mLoadingAnimValue = 0;
                    mLoadingAnimRunning = false;
                    if (mLoadingLineListener != null) {
                        mLoadingLineListener.onLoadingLineAnimEnd();
                    }
                }
            });
            mLoadingAnimator.start();
        }
    }

    public void endLoadingLine() {
        if (mLoadingAnimator != null && mLoadingAnimator.isRunning()) {
            mLoadingAnimator.removeAllUpdateListeners();
            mLoadingAnimator.end();
            mLoadingAnimator = null;
        }
        if (mLoadingType == LOADING_LINE) {
            clearLoadingLine();
        } else {
            setRotation(0);
        }
    }

    private void clearLoadingLine() {
        setLoadingPath();
        invalidate();
    }

    public void addOnLoadingLineListener(OnLoadingLineListener onLoadingLineListener) {
        mLoadingLineListener = onLoadingLineListener;
    }

    /**
     * LoadingLine接口，监听动画结束
     */
    public interface OnLoadingLineListener {
        void onLoadingLineAnimEnd();
    }

    private void setProgressBarPath() {
        if (mLineViewType == PROGRESSBAR) {
            if (mProgressType == PROGRESSBAR_LINE) {
                setProgressPath();
                setProgressBackgroundPath();
                setProgressSecondaryPath();
            } else {
                float minLen = Math.min(mWidth, mHeight) - (float) mLineViewStrokeWidth;
                mSquare.set((float) mLineViewStrokeWidth / 2, (float) mLineViewStrokeWidth / 2,
                        minLen + (float) mLineViewStrokeWidth / 2,
                        minLen + (float) mLineViewStrokeWidth / 2);
            }
        }
    }

    private void setProgressPath() {
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_LINE) {
            mPath.reset();
            if (mProgressValue == 0) {
                mPaint.setStrokeCap(Paint.Cap.BUTT);
            } else {
                if (mLineViewRound) {
                    mPaint.setStrokeCap(Paint.Cap.ROUND);
                }
            }
            mPath.moveTo(mRoundRadius, mHeight * 0.5f);
            mPath.lineTo(mRoundRadius + (mWidth - 2 * mRoundRadius) * mProgressValue / mProgressMax,
                    mHeight * 0.5f);
        }
    }

    private void setProgressBackgroundPath() {
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_LINE) {
            mProgressBackgroundPath.reset();
            mProgressBackgroundPath.moveTo(mProgressBackgroundRoundRadius, mHeight * 0.5f);
            mProgressBackgroundPath.lineTo(mWidth - mProgressBackgroundRoundRadius, mHeight * 0.5f);
        }
    }

    private void setProgressSecondaryPath() {
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_LINE) {
            mProgressSecondaryPath.reset();
            if (mProgressSecondaryValue == 0) {
                mProgressSecondaryPaint.setStrokeCap(Paint.Cap.BUTT);
            } else {
                if (mProgressSecondaryRound) {
                    mProgressSecondaryPaint.setStrokeCap(Paint.Cap.ROUND);
                }
            }
            mProgressSecondaryPath.moveTo(mProgressSecondaryRoundRadius, mHeight * 0.5f);
            mProgressSecondaryPath.lineTo(mProgressSecondaryRoundRadius
                    + (mWidth - 2 * mProgressSecondaryRoundRadius) * mProgressSecondaryValue
                    / mProgressMax, mHeight * 0.5f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_CIRCLE) {
            canvas.drawArc(mSquare, 0, 360, false, mProgressBackgroundPaint);
            if (mProgressClockwise) {
                canvas.drawArc(mSquare, mProgressStartAngle, 360 * mProgressValue / mProgressMax,
                        false, mPaint);
                canvas.drawArc(mSquare, mProgressStartAngle,
                        360 * mProgressSecondaryValue / mProgressMax, false,
                        mProgressSecondaryPaint);
            } else {
                canvas.drawArc(mSquare, mProgressStartAngle, -360 * mProgressValue / mProgressMax,
                        false, mPaint);
                canvas.drawArc(mSquare, mProgressStartAngle,
                        -360 * mProgressSecondaryValue / mProgressMax, false,
                        mProgressSecondaryPaint);
            }
        } else if (mLineViewType == LOADINGLINE && mLoadingType == LOADING_CIRCLE) {
            canvas.drawArc(mSquare, mLoadingStartAngle, mLoadingSweepAngle, false, mPaint);
        } else {
            canvas.drawPath(mPath, mPaint);
        }
        if (mLineViewType == TABLINE) {
            canvas.drawPath(mTabPath, mTabPaint);
        } else if (mLineViewType == PROGRESSBAR && mProgressType == PROGRESSBAR_LINE) {
            canvas.drawPath(mProgressBackgroundPath, mProgressBackgroundPaint);
            canvas.drawPath(mProgressSecondaryPath, mProgressSecondaryPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        setShader();
        setPath();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽高有设定wrap_content的，就定死为20
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = 20
                    + mLineViewNum * mLineViewStrokeWidth
                    + (mLineViewNum - 1) * mLineViewInterval;
        }
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = 20
                    + mLineViewNum * mLineViewStrokeWidth
                    + (mLineViewNum - 1) * mLineViewInterval;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switchTab((int) event.getX(), mSwitchTabAnim);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setLineViewColor(@ColorInt int lineViewColor) {
        if (mLineViewColor == lineViewColor) {
            return;
        }
        mLineViewColor = lineViewColor;
        mPaint.setColor(lineViewColor);
        mPaint.setShader(null);
        mLineViewStartColor = lineViewColor;
        mLineViewEndColor = lineViewColor;
        invalidate();
    }

    public void setLineViewStartColor(@ColorInt int lineViewStartColor) {
        if (mLineViewStartColor == lineViewStartColor) {
            return;
        }
        mLineViewStartColor = lineViewStartColor;
        setShader();
        invalidate();
    }

    public void setLineViewEndColor(@ColorInt int lineViewEndColor) {
        if (mLineViewEndColor == lineViewEndColor) {
            return;
        }
        mLineViewEndColor = lineViewEndColor;
        setShader();
        invalidate();
    }

    public void setLineViewStrokeWidth(int lineViewStrokeWidth) {
        if (mLineViewStrokeWidth == lineViewStrokeWidth) {
            return;
        }
        mLineViewStrokeWidth = lineViewStrokeWidth;
        mPaint.setStrokeWidth(lineViewStrokeWidth);
        mProgressBackgroundWidth = mLineViewStrokeWidth;
        if (mProgressBackgroundPaint != null) {
            mProgressBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
        }
        mProgressSecondaryWidth = mLineViewStrokeWidth;
        if (mProgressSecondaryPaint != null) {
            mProgressSecondaryPaint.setStrokeWidth(mProgressSecondaryWidth);
        }
        initRound();
        initDashPathEffect();
        setShader();
        setPath();
        invalidate();
    }

    public void setLineViewType(@LineViewType int lineViewType) {
        if (mLineViewType == lineViewType) {
            return;
        }
        mLineViewType = lineViewType;
        initLineViewType();
        initRound();
        initDashPathEffect();
        initArrowRound();
        initTabRound();
        initProgressRound();
        setShader();
        setPath();
        invalidate();
    }

    public void setLineViewOrientation(@LineViewOrientation int lineViewOrientation) {
        if (mLineViewOrientation == lineViewOrientation) {
            return;
        }
        if (mLineViewType == STRAIGHTLINE || mLineViewType == DASHLINE) {
            mLineViewOrientation = lineViewOrientation;
            setLineShader();
            setLinePath();
            invalidate();
        }
    }

    public void setDashWidth(int dashWidth) {
        if (mLineViewType != DASHLINE || mRealDashWidth == dashWidth) {
            return;
        }
        mRealDashWidth = dashWidth;
        initRound();
        initDashPathEffect();
        invalidate();
    }

    public void setDashGap(int dashGap) {
        if (mLineViewType != DASHLINE || mRealDashGap == dashGap) {
            return;
        }
        mRealDashGap = dashGap;
        initRound();
        initDashPathEffect();
        invalidate();
    }

    public void setDashStyle(@DashStyle int dashStyle) {
        if (mLineViewType != DASHLINE || mDashStyle == dashStyle) {
            return;
        }
        mDashStyle = dashStyle;
        initLineViewType();
        initDashPathEffect();
        invalidate();
    }

    public void setLineViewNum(int lineViewNum) {
        if (mLineViewNum == lineViewNum) {
            return;
        }
        if (lineViewNum > 0) {
            mLineViewNum = lineViewNum;
            setLinePath();
            invalidate();
        }
    }

    public void setLineViewInterval(int lineViewInterval) {
        if (mLineViewInterval == lineViewInterval) {
            return;
        }
        if (mLineViewInterval > 0) {
            mLineViewInterval = lineViewInterval;
            setLinePath();
            invalidate();
        }
    }

    public void setLineViewRound(boolean lineViewRound) {
        if (mLineViewRound == lineViewRound) {
            return;
        }
        mLineViewRound = lineViewRound;
        initRound();
        initDashPathEffect();
        setPath();
        invalidate();
    }

    public void setArrowOrientation(@ArrowOrientation int arrowOrientation) {
        if (mArrowOrientation == arrowOrientation) {
            return;
        }
        if (mLineViewType == ARROW || mLineViewType == RIGHTANGLEARROW) {
            mArrowOrientation = arrowOrientation;
            setArrowPath();
            setRightAngleArrowPath();
            invalidate();
        }
    }

    public void setArrowRound(boolean arrowRound) {
        if (mArrowRound == arrowRound) {
            return;
        }
        if (mLineViewType == ARROW || mLineViewType == RIGHTANGLEARROW) {
            mArrowRound = arrowRound;
            initArrowRound();
            invalidate();
        }
    }

    public void setTabNum(int tabNum) {
        if (mLineViewType != TABLINE || mTabNum == tabNum) {
            return;
        }
        if (tabNum > 0) {
            mTabNum = tabNum;
            setTabLength();
            setTabShader();
            setTabPath();
            invalidate();
        }
    }

    public void setTabPosition(int tabPosition) {
        if (mLineViewType != TABLINE || mTabPosition == tabPosition) {
            return;
        }
        if (tabPosition >= 0 && tabPosition < mTabNum) {
            mTabPosition = tabPosition;
            setTabPath();
            invalidate();
        }
    }

    public void setTabColor(@ColorInt int tabColor) {
        if (mLineViewType != TABLINE || mTabColor == tabColor) {
            return;
        }
        mTabColor = tabColor;
        if (mTabPaint != null) {
            mTabPaint.setColor(mTabColor);
            mTabPaint.setShader(null);
        }
        mTabStartColor = tabColor;
        mTabEndColor = tabColor;
        invalidate();
    }

    public void setTabStartColor(@ColorInt int tabStartColor) {
        if (mLineViewType != TABLINE || mTabStartColor == tabStartColor) {
            return;
        }
        mTabStartColor = tabStartColor;
        setTabShader();
        invalidate();
    }

    public void setTabEndColor(@ColorInt int tabEndColor) {
        if (mLineViewType != TABLINE || mTabEndColor == tabEndColor) {
            return;
        }
        mTabEndColor = tabEndColor;
        setTabShader();
        invalidate();
    }

    public void setTabWidth(int tabWidth) {
        if (mLineViewType != TABLINE || mTabWidth == tabWidth) {
            return;
        }
        mTabWidth = tabWidth;
        if (mTabPaint != null) {
            mTabPaint.setStrokeWidth(mTabWidth);
            initTabRound();
            setTabPath();
            invalidate();
        }
    }

    public void setTabGap(int tabGap) {
        if (mLineViewType != TABLINE || mTabGap == tabGap) {
            return;
        }
        mTabGap = tabGap;
        setTabPath();
        invalidate();
    }

    public void setTabRound(boolean tabRound) {
        if (mLineViewType != TABLINE || mTabRound == tabRound) {
            return;
        }
        mTabRound = tabRound;
        initTabRound();
        setTabPath();
        invalidate();
    }

    public void setTabLocation(@TabLocation int tabLocation) {
        if (mLineViewType != TABLINE || mTabLocation == tabLocation) {
            return;
        }
        mTabLocation = tabLocation;
        setTabPath();
        invalidate();
    }

    public void setTabOffset(int tabOffset) {
        if (mLineViewType != TABLINE || mTabOffset == tabOffset) {
            return;
        }
        mTabOffset = tabOffset;
        setTabPath();
        invalidate();
    }

    public void setSwitchTabAnim(boolean switchTabAnim) {
        mSwitchTabAnim = switchTabAnim;
    }

    public void setSwitchTabAnimDuration(int switchTabAnimDuration) {
        mSwitchTabAnimDuration = switchTabAnimDuration;
    }

    public void setLoadingType(@LoadingType int loadingType) {
        if (mLineViewType != LOADINGLINE || mLoadingType == loadingType) {
            return;
        }
        endLoadingLine();
        mLoadingType = loadingType;
        initLineViewType();
        setLoadingShader();
        setLoadingPath();
        invalidate();
    }

    public void setLoadingStartPosition(@LoadingStartPosition int loadingStartPosition) {
        if (mLineViewType != LOADINGLINE
                || mLoadingType != LOADING_LINE
                || mLoadingStartPosition == loadingStartPosition) {
            return;
        }
        mLoadingStartPosition = loadingStartPosition;
        setLoadingShader();
        setLoadingPath();
        invalidate();
    }

    public void setLoadingStartAngle(float loadingStartAngle) {
        if (mLineViewType != LOADINGLINE
                || mLoadingType != LOADING_CIRCLE
                || Math.abs(mLoadingStartAngle - loadingStartAngle) < 0.00001) {
            return;
        }
        mLoadingStartAngle = loadingStartAngle;
        setLoadingShader();
        invalidate();
    }

    public void setLoadingSweepAngle(float loadingSweepAngle) {
        if (mLineViewType != LOADINGLINE
                || mLoadingType != LOADING_CIRCLE
                || Math.abs(mLoadingSweepAngle - loadingSweepAngle) < 0.00001) {
            return;
        }
        mLoadingSweepAngle = loadingSweepAngle;
        setLoadingShader();
        invalidate();
    }

    public void setLoadingClockwise(boolean loadingClockwise) {
        if (mLineViewType != LOADINGLINE
                || mLoadingType != LOADING_CIRCLE
                || mLoadingClockwise == loadingClockwise) {
            return;
        }
        endLoadingLine();
        mLoadingClockwise = loadingClockwise;
    }

    public void setLoadingDuration(int loadingDuration) {
        if (mLineViewType != LOADINGLINE || mLoadingDuration == loadingDuration) {
            return;
        }
        endLoadingLine();
        mLoadingDuration = loadingDuration;
    }

    public void setLoadingCount(int loadingCount) {
        if (mLineViewType != LOADINGLINE || mLoadingCount == loadingCount) {
            return;
        }
        endLoadingLine();
        mLoadingCount = loadingCount;
    }

    public void setProgressType(int progressType) {
        if (mLineViewType != PROGRESSBAR || mProgressType == progressType) {
            return;
        }
        mProgressType = progressType;
        initLineViewType();
        initProgressRound();
        setProgressBarPath();
        invalidate();
    }

    public void setProgressMax(float progressMax) {
        if (mLineViewType != PROGRESSBAR || Math.abs(mProgressMax - progressMax) < 0.00001) {
            return;
        }
        mProgressMax = progressMax;
        if (mProgressType == PROGRESSBAR_LINE) {
            setProgressBackgroundPath();
            setProgressSecondaryPath();
        }
        invalidate();
    }

    public void setProgressValue(float progressValue) {
        if (mLineViewType != PROGRESSBAR || Math.abs(mProgressValue - progressValue) < 0.00001) {
            return;
        }
        mProgressValue = progressValue;
        mProgressValue = mProgressValue > mProgressMax ? mProgressMax : mProgressValue;
        mProgressValue = mProgressValue < 0 ? 0 : mProgressValue;
        if (mProgressType == PROGRESSBAR_LINE) {
            setProgressPath();
        }
        invalidate();
    }

    public void setProgressSecondaryValue(float progressSecondaryValue) {
        if (mLineViewType != PROGRESSBAR
                || Math.abs(mProgressSecondaryValue - progressSecondaryValue) < 0.00001) {
            return;
        }
        mProgressSecondaryValue = progressSecondaryValue;
        mProgressSecondaryValue =
                mProgressSecondaryValue > mProgressMax ? mProgressMax : mProgressSecondaryValue;
        mProgressSecondaryValue = mProgressSecondaryValue < 0 ? 0 : mProgressSecondaryValue;
        if (mProgressType == PROGRESSBAR_LINE) {
            setProgressSecondaryPath();
        }
        invalidate();
    }

    public void setProgressBackgroundColor(@ColorInt int progressBackgroundColor) {
        if (mLineViewType != PROGRESSBAR || mProgressBackgroundColor == progressBackgroundColor) {
            return;
        }
        mProgressBackgroundColor = progressBackgroundColor;
        if (mProgressBackgroundPaint != null) {
            mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
        }
        invalidate();
    }

    public void setProgressSecondaryColor(@ColorInt int progressSecondaryColor) {
        if (mLineViewType != PROGRESSBAR || mProgressSecondaryColor == progressSecondaryColor) {
            return;
        }
        mProgressSecondaryColor = progressSecondaryColor;
        if (mProgressSecondaryPaint != null) {
            mProgressSecondaryPaint.setColor(mProgressSecondaryColor);
        }
        invalidate();
    }

    public void setProgressBackgroundWidth(int progressBackgroundWidth) {
        if (mLineViewType != PROGRESSBAR || mProgressBackgroundWidth == progressBackgroundWidth) {
            return;
        }
        mProgressBackgroundWidth = progressBackgroundWidth;
        if (mProgressBackgroundPaint != null) {
            mProgressBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
        }
        initProgressRound();
        setProgressBackgroundPath();
        invalidate();
    }

    public void setProgressSecondaryWidth(int progressSecondaryWidth) {
        if (mLineViewType != PROGRESSBAR || mProgressSecondaryWidth == progressSecondaryWidth) {
            return;
        }
        mProgressSecondaryWidth = progressSecondaryWidth;
        if (mProgressSecondaryPaint != null) {
            mProgressSecondaryPaint.setStrokeWidth(mProgressSecondaryWidth);
        }
        initProgressRound();
        setProgressSecondaryPath();
        invalidate();
    }

    public void setProgressBackgroundRound(boolean progressBackgroundRound) {
        if (mLineViewType != PROGRESSBAR || mProgressBackgroundRound == progressBackgroundRound) {
            return;
        }
        mProgressBackgroundRound = progressBackgroundRound;
        initProgressRound();
        setProgressBarPath();
        invalidate();
    }

    public void setProgressSecondaryRound(boolean progressSecondaryRound) {
        if (mLineViewType != PROGRESSBAR || mProgressSecondaryRound == progressSecondaryRound) {
            return;
        }
        mProgressSecondaryRound = progressSecondaryRound;
        initProgressRound();
        setProgressBarPath();
        invalidate();
    }

    public void setProgressStartAngle(float progressStartAngle) {
        if (mLineViewType != PROGRESSBAR
                || Math.abs(mProgressStartAngle - progressStartAngle) < 0.00001) {
            return;
        }
        if (mProgressType == PROGRESSBAR_CIRCLE) {
            mProgressStartAngle = progressStartAngle;
            invalidate();
        }
    }

    public void setProgressClockwise(boolean progressClockwise) {
        if (mLineViewType != PROGRESSBAR || mProgressClockwise == progressClockwise) {
            return;
        }
        if (mProgressType == PROGRESSBAR_CIRCLE) {
            mProgressClockwise = progressClockwise;
            invalidate();
        }
    }

    public int getLineViewColor() {
        return mLineViewColor;
    }

    public int getLineViewStartColor() {
        return mLineViewStartColor;
    }

    public int getLineViewEndColor() {
        return mLineViewEndColor;
    }

    public int getLineViewStrokeWidth() {
        return mLineViewStrokeWidth;
    }

    public boolean isLineViewRound() {
        return mLineViewRound;
    }

    public int getLineViewType() {
        return mLineViewType;
    }

    public int getLineViewOrientation() {
        return mLineViewOrientation;
    }

    public int getDashWidth() {
        return mRealDashWidth;
    }

    public int getDashGap() {
        return mRealDashGap;
    }

    public int getDashStyle() {
        return mDashStyle;
    }

    public int getLineViewNum() {
        return mLineViewNum;
    }

    public int getLineViewInterval() {
        return mLineViewInterval;
    }

    public int getArrowOrientation() {
        return mArrowOrientation;
    }

    public boolean isArrowRound() {
        return mArrowRound;
    }

    public int getTabNum() {
        return mTabNum;
    }

    public int getTabPosition() {
        return mTabPosition;
    }

    public int getTabColor() {
        return mTabColor;
    }

    public int getTabStartColor() {
        return mTabStartColor;
    }

    public int getTabEndColor() {
        return mTabEndColor;
    }

    public int getTabWidth() {
        return mTabWidth;
    }

    public int getTabGap() {
        return mTabGap;
    }

    public boolean isTabRound() {
        return mTabRound;
    }

    public int getTabLocation() {
        return mTabLocation;
    }

    public int getTabOffset() {
        return mTabOffset;
    }

    public boolean isSwitchTabAnim() {
        return mSwitchTabAnim;
    }

    public int getSwitchTabAnimDuration() {
        return mSwitchTabAnimDuration;
    }

    public int getLoadingType() {
        return mLoadingType;
    }

    public int getLoadingStartPosition() {
        return mLoadingStartPosition;
    }

    public float getLoadingStartAngle() {
        return mLoadingStartAngle;
    }

    public float getLoadingSweepAngle() {
        return mLoadingSweepAngle;
    }

    public boolean isLoadingClockwise() {
        return mLoadingClockwise;
    }

    public int getLoadingDuration() {
        return mLoadingDuration;
    }

    public int getLoadingCount() {
        return mLoadingCount;
    }

    public int getProgressType() {
        return mProgressType;
    }

    public float getProgressMax() {
        return mProgressMax;
    }

    public float getProgressValue() {
        return mProgressValue;
    }

    public float getProgressSecondaryValue() {
        return mProgressSecondaryValue;
    }

    public int getProgressBackgroundColor() {
        return mProgressBackgroundColor;
    }

    public int getProgressSecondaryColor() {
        return mProgressSecondaryColor;
    }

    public int getProgressBackgroundWidth() {
        return mProgressBackgroundWidth;
    }

    public int getProgressSecondaryWidth() {
        return mProgressSecondaryWidth;
    }

    public boolean isProgressBackgroundRound() {
        return mProgressBackgroundRound;
    }

    public boolean isProgressSecondaryRound() {
        return mProgressSecondaryRound;
    }

    public float getProgressStartAngle() {
        return mProgressStartAngle;
    }

    public boolean isProgressClockwise() {
        return mProgressClockwise;
    }
}
