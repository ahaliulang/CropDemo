package com.yalantis.ucrop.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.util.ViewUtils;

/**
 * author:tdn
 * time:2020/6/22
 * description:
 */
public class HorizontalScaleScrollView extends View {

    private int mMaxScale; //最大刻度
    private int mMinScale; // 最小刻度
    private int mScaleWidth;//刻度宽度
    private int mScaleBottom;//刻度底边距
    private int mCurrentScale; //当前刻度
    private int mScaleInterval; //刻度间距
    private int mScaleShortHeight; //短刻度线的高度
    private int mScaleLongHeight; //长刻度线高度
    private int mIndicatorWidth;//指示器宽度
    private int mIndicatorHeight;//指示器高度

    private int mLastTouchedX;
    private Paint mPaint;
    private float mTotalScrollDistance;
    private boolean mScrollStarted;

    private final Rect mCanvasClipBounds = new Rect();


    public HorizontalScaleScrollView(Context context) {
        this(context, null);
    }

    public HorizontalScaleScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalScaleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_HorizontalScaleScrollView);
        mMinScale = a.getInteger(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_min_scale, -90);
        mMaxScale = a.getInteger(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_max_scale, 90);
        mScaleInterval = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_interval, 16);
        mScaleShortHeight = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_short_line_height, 15);
        mScaleLongHeight = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_long_line_height, 24);
        mScaleWidth = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_scale_width, 3);
        mScaleBottom = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_scale_bottom, 12);
        mIndicatorWidth = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_indicator_width, 6);
        mIndicatorHeight = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_indicator_height, 48);
        a.recycle();

        init();
    }

    private void init() {

        mScaleLongHeight = ViewUtils.dip2px(getContext(), 8);
        mIndicatorWidth = ViewUtils.dip2px(getContext(), 2);
        mIndicatorHeight = ViewUtils.dip2px(getContext(), 16);


        // 画笔
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(ViewUtils.dip2px(getContext(), 12));

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec(ViewUtils.dip2px(getContext(), 40), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(mCanvasClipBounds);
        drawScale(canvas); //画刻度
        drawIndicator(canvas); //画指针
    }


    /**
     * 画刻度
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas) {

        int linesCount = mCanvasClipBounds.width() / (mScaleInterval + mScaleWidth);

        //drawLeft
        for (int i = 0; i <= linesCount / 2; i++) {
            int drawScale = mCurrentScale - i;
            if (drawScale % 10 == 0) { //整值
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(mScaleWidth);
                mPaint.setColor(Color.parseColor("#ffffff"));
                canvas.drawLine(mCanvasClipBounds.centerX() - i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleLongHeight - mScaleBottom - mScaleWidth / 2f,
                        mCanvasClipBounds.centerX() - i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleBottom - mScaleWidth / 2f, mPaint);
                canvas.drawText(String.valueOf(drawScale), mCanvasClipBounds.centerX() - i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mIndicatorHeight - ViewUtils.dip2px(getContext(), 8), mPaint);
            } else {
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(mScaleWidth);
                mPaint.setColor(Color.parseColor("#99ffffff"));
                canvas.drawLine(mCanvasClipBounds.centerX() - i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleShortHeight - mScaleBottom - mScaleWidth / 2f,
                        mCanvasClipBounds.centerX() - i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleBottom - mScaleWidth / 2f, mPaint);
            }
        }

        //drawRight
        for (int i = 0; i <= linesCount / 2; i++) {
            int drawScale = mCurrentScale + i;
            if (drawScale % 10 == 0) { //整值
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(mScaleWidth);
                mPaint.setColor(Color.parseColor("#ffffff"));
                canvas.drawLine(mCanvasClipBounds.centerX() + i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleLongHeight - mScaleBottom - mScaleWidth / 2f,
                        mCanvasClipBounds.centerX() + i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleBottom - mScaleWidth / 2f, mPaint);
                canvas.drawText(String.valueOf(drawScale), mCanvasClipBounds.centerX() + i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mIndicatorHeight - ViewUtils.dip2px(getContext(), 8), mPaint);
            } else {
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(mScaleWidth);
                mPaint.setColor(Color.parseColor("#99ffffff"));
                canvas.drawLine(mCanvasClipBounds.centerX() + i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleShortHeight - mScaleBottom - mScaleWidth / 2f,
                        mCanvasClipBounds.centerX() + i * (mScaleInterval + mScaleWidth), getMeasuredHeight() - mScaleBottom - mScaleWidth / 2f, mPaint);
            }
        }

    }


    /**
     * 画指示器
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#ff2a9b"));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mIndicatorWidth);
        canvas.drawLine(mCanvasClipBounds.centerX(), getMeasuredHeight() - mIndicatorHeight - mIndicatorWidth / 2f, mCanvasClipBounds.centerX(), getMeasuredHeight() - mIndicatorWidth / 2f, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
                    onScrollEvent(event, distance);
                }

                break;
            case MotionEvent.ACTION_UP:
//                if (mCurrentScale < mMinScale) mCurrentScale = mMinScale;
//                if (mCurrentScale > mMaxScale) mCurrentScale = mMaxScale;
//                int finalX = (mCurrentScale - mMidCountScale) * mScaleInterval;
//                mScroller.setFinalX(finalX); //纠正指针位置
//                postInvalidate();

                if (mScrollListener != null) {
                    mScrollStarted = false;
                    mScrollListener.onScrollEnd();
                }

                break;
        }
        return true;
    }


    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        mCurrentScale = (int) (mTotalScrollDistance / (mScaleInterval + mScaleWidth));
        postInvalidate();
        mLastTouchedX = (int) event.getX();
        if (mScrollListener != null) {
            mScrollListener.onScroll(-distance, mTotalScrollDistance);
        }
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

        void onScroll(float delta, float totalDistance);

        void onScrollEnd();
    }

}
