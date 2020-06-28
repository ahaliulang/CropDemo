package com.yalantis.ucrop.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.util.ViewUtils;

/**
 * author:tdn
 * time:2020/6/22
 * description:
 */
public class ScaleView extends View {

    private static final int DEFAULT_MIN_SCALE = -90;
    private static final int DEFAULT_MAX_SCALE = 90;
    private static final int DEFAULT_LINE_SPACE = 15;
    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final int DEFAULT_BASELINE_BOTTOM_MARGIN = 12;
    private static final int DEFAULT_SHORT_LINE_HEIGHT = 15;
    private static final int DEFAULT_LONG_LINE_HEIGHT = 24;
    private static final int DEFAULT_INDICATOR_WIDTH = 6;
    private static final int DEFAULT_INDICATOR_HEIGHT = 48;
    private static final int DEFAULT_TEXT_SIZE = 36;
    private static final int DEFAULT_SCALE_INTERVAL = 10;
    private static final int DEFAULT_TEXT_BOTTOM_MARGIN_TO_LONG_LINE = 30;


    private int mMaxScale; //最大刻度
    private int mMinScale; // 最小刻度
    private int mLineWidth;//刻度线宽度
    private int mLineSpace; //刻度线间距离
    private int mShortLineHeight; //短线高度
    private int mLongLineHeight; //长线高度
    private int mIndicatorWidth;//指示器宽度
    private int mIndicatorHeight;//指示器高度
    private int mShortLineColor; //短线颜色
    private int mLongLineColor;//长线颜色
    private int mIndicatorColor;//指示器颜色
    private int mTextColor;//文本颜色
    private int mTextSize;//文本大小
    private int mScaleInterval;//刻度间隔

    private int mBaseLineBottomMargin;
    private boolean mShowBaseLine;
    private int mTextBottomMarginToLongLine;


    private int mCurrentScale; //当前刻度
    private int mLastTouchedX;
    private float mTotalScrollDistance;
    private boolean mScrollStarted;
    private int mMaxScrollX;

    private Paint mShortLinePaint;
    private Paint mLongLinePaint;
    private Paint mBaseLinePaint;
    private Paint mIndicatorPaint;
    private Paint mTextPaint;


    private final Rect mCanvasClipBounds = new Rect();
    private int mWrapContentHeight;

    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;


    public ScaleView(Context context) {
        this(context, null);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sv_ScaleView);
        mMinScale = a.getInteger(R.styleable.sv_ScaleView_sv_min, DEFAULT_MIN_SCALE);
        mMaxScale = a.getInteger(R.styleable.sv_ScaleView_sv_max, DEFAULT_MAX_SCALE);
        mLineSpace = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_line_space, DEFAULT_LINE_SPACE);
        mLineWidth = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_line_width, DEFAULT_LINE_WIDTH);
        mShortLineHeight = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_short_line_height, DEFAULT_SHORT_LINE_HEIGHT);
        mLongLineHeight = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_long_line_height, DEFAULT_LONG_LINE_HEIGHT);
        mIndicatorWidth = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_indicator_width, DEFAULT_INDICATOR_WIDTH);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_indicator_height, DEFAULT_INDICATOR_HEIGHT);
        mShortLineColor = a.getColor(R.styleable.sv_ScaleView_sv_short_line_color, Color.GRAY);
        mLongLineColor = a.getColor(R.styleable.sv_ScaleView_sv_long_line_color, Color.WHITE);
        mIndicatorColor = a.getColor(R.styleable.sv_ScaleView_sv_indicator_color, Color.CYAN);
        mTextColor = a.getColor(R.styleable.sv_ScaleView_sv_text_color, Color.WHITE);
        mTextSize = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_text_size, DEFAULT_TEXT_SIZE);
        mScaleInterval = a.getInteger(R.styleable.sv_ScaleView_sv_interval, DEFAULT_SCALE_INTERVAL);
        mBaseLineBottomMargin = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_baseline_bottom_margin, DEFAULT_BASELINE_BOTTOM_MARGIN);
        mShowBaseLine = a.getBoolean(R.styleable.sv_ScaleView_sv_show_baseline, true);
        mTextBottomMarginToLongLine = a.getDimensionPixelSize(R.styleable.sv_ScaleView_sv_text_bottom_margin_to_long_line, DEFAULT_TEXT_BOTTOM_MARGIN_TO_LONG_LINE);
        a.recycle();

        init();
    }

    private void init() {


        mMaxScrollX = (mLineWidth + mLineSpace) * mMaxScale;

        mVelocityTracker = VelocityTracker.obtain();
        mScroller = new Scroller(getContext());

        mShortLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShortLinePaint.setColor(mShortLineColor);
        mShortLinePaint.setStrokeWidth(mLineWidth);

        mLongLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongLinePaint.setColor(mLongLineColor);
        mLongLinePaint.setStrokeWidth(mLineWidth);

        mBaseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBaseLinePaint.setColor(mShortLineColor);
        mBaseLinePaint.setStrokeWidth(mLineWidth);


        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mIndicatorColor);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        mIndicatorPaint.setStrokeWidth(mIndicatorWidth);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        int textHeight = (int) Math.ceil(fm.descent - fm.ascent);

        mWrapContentHeight = mBaseLineBottomMargin + mLongLineHeight + mTextBottomMarginToLongLine + textHeight;

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWrapContentHeight, mWrapContentHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWrapContentHeight, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, mWrapContentHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(mCanvasClipBounds);
        drawScale(canvas); //画刻度


        //draw Indicator
        canvas.drawLine(mCanvasClipBounds.centerX(),
                getMeasuredHeight() - mIndicatorHeight - mIndicatorWidth / 2f,
                mCanvasClipBounds.centerX(),
                getMeasuredHeight() - mIndicatorWidth / 2f, mIndicatorPaint);
    }


    private void drawScale(Canvas canvas) {
        int linesCount = (mCanvasClipBounds.width() - getPaddingLeft() - getPaddingRight()) / (mLineSpace + mLineWidth);

        for (int i = 0; i <= linesCount / 2; i++) {
            // draw LeftPart
            int leftScale = mCurrentScale - i;
            if (leftScale >= mMinScale) {
                int centerX = mCanvasClipBounds.centerX() - i * (mLineSpace + mLineWidth);
                if (leftScale % mScaleInterval == 0) {
                    //draw LongLine
                    canvas.drawLine(centerX, getMeasuredHeight() - mLongLineHeight - mBaseLineBottomMargin,
                            centerX, getMeasuredHeight() - mBaseLineBottomMargin, mLongLinePaint);
                    //draw Text
                    canvas.drawText(String.valueOf(leftScale), centerX, getMeasuredHeight() - mLongLineHeight - mTextBottomMarginToLongLine - mBaseLineBottomMargin, mTextPaint);
                } else {
                    //draw ShortLine
                    canvas.drawLine(centerX, getMeasuredHeight() - mShortLineHeight - mBaseLineBottomMargin,
                            centerX, getMeasuredHeight() - mBaseLineBottomMargin, mShortLinePaint);
                }

                //drawBaseLine
                if (mShowBaseLine) {
                    canvas.drawLine(mCanvasClipBounds.centerX(),
                            getMeasuredHeight() - mBaseLineBottomMargin,
                            centerX - mLineWidth / 2f,
                            getMeasuredHeight() - mBaseLineBottomMargin, mBaseLinePaint);
                }
            }

            //draw RightPart
            int rightScale = mCurrentScale + i;
            if (rightScale <= mMaxScale) {
                int centerX = mCanvasClipBounds.centerX() + i * (mLineSpace + mLineWidth);
                if (rightScale % mScaleInterval == 0) {
                    //draw LongLine
                    canvas.drawLine(centerX, getMeasuredHeight() - mLongLineHeight - mBaseLineBottomMargin,
                            centerX, getMeasuredHeight() - mBaseLineBottomMargin, mLongLinePaint);
                    //draw Text
                    canvas.drawText(String.valueOf(rightScale), centerX, getMeasuredHeight() - mLongLineHeight - mTextBottomMarginToLongLine - mBaseLineBottomMargin, mTextPaint);
                } else {
                    // draw ShortLine
                    canvas.drawLine(centerX, getMeasuredHeight() - mShortLineHeight - mBaseLineBottomMargin,
                            centerX, getMeasuredHeight() - mBaseLineBottomMargin, mShortLinePaint);
                }

                if (mShowBaseLine) {
                    canvas.drawLine(mCanvasClipBounds.centerX(),
                            getMeasuredHeight() - mBaseLineBottomMargin,
                            centerX + mLineWidth / 2f,
                            getMeasuredHeight() - mBaseLineBottomMargin, mBaseLinePaint);
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mScroller != null && mScroller.isFinished()){
                    mScroller.isFinished();
                }
                mLastTouchedX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastTouchedX;
                if (distance != 0) {
                    if (!mScrollStarted) {
                        mScrollStarted = true;
                        if (mScrollListener != null) {
                            mScrollListener.onScrollStart();
                        }
                    }

                    mTotalScrollDistance -= distance;
                    if (mTotalScrollDistance > mMaxScrollX) {
                        mTotalScrollDistance = mMaxScrollX;
                    } else if (mTotalScrollDistance <= -mMaxScrollX) {
                        mTotalScrollDistance = -mMaxScrollX;
                    }
                    mCurrentScale = (int) (mTotalScrollDistance / (mLineSpace + mLineWidth));
                    postInvalidate();

                    mLastTouchedX = (int) event.getX();
                    if (mScrollListener != null) {
                        mScrollListener.onScroll(mCurrentScale);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollListener != null) {
                    mScrollStarted = false;
                    mScrollListener.onScrollEnd();
                }

                break;
        }
        return true;
    }


    private OnScrollListener mScrollListener;

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }

    public interface OnScrollListener {
        void onScrollStart();

        void onScroll(int currentScale);

        void onScrollEnd();
    }

}
