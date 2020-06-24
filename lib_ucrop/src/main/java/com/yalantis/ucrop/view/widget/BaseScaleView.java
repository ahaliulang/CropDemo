package com.yalantis.ucrop.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.util.ViewUtils;

/**
 * author:tdn
 * time:2020/6/22
 * description:
 */
public abstract class BaseScaleView extends View {

    protected int mMax; //最大刻度
    protected int mMin; // 最小刻度
    protected int mScaleWidth;//刻度宽度
    protected int mScaleBottom;//刻度底边距
    protected int mCountScale; //滑动的总刻度

    protected int mScaleScrollViewRange;

    protected int mScaleMargin; //刻度间距
    protected int mScaleShortHeight; //刻度线的高度
    protected int mScaleLongHeight; //整刻度线高度
    protected int mIndicatorWidth;//指示器宽度
    protected int mIndicatorHeight;//指示器高度

    protected int mRectWidth; //总宽度
    protected int mRectHeight; //高度

    protected Scroller mScroller;
    protected int mScrollLastX;

    protected int mTempScale; // 用于判断滑动方向
    protected int mMidCountScale; //中间刻度

    protected OnScrollListener mScrollListener;

    public interface OnScrollListener {
        void onScaleScroll(int scale);
    }
    public BaseScaleView(Context context) {
        this(context, null);
    }

    public BaseScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public BaseScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_HorizontalScaleScrollView);
        init(a);
    }


    protected void init(TypedArray a) {
        // 获取自定义属性
        mMin = a.getInteger(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_min_scale,0);
        mMax = a.getInteger(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_max_scale,0);
        mScaleMargin = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_interval, 16);
        mScaleShortHeight = a.getDimensionPixelOffset(R.styleable.ucrop_HorizontalScaleScrollView_ucrop_short_line_height, 15);

        mScaleWidth = ViewUtils.dip2px(getContext(),1);
        mScaleBottom = ViewUtils.dip2px(getContext(),6);

        a.recycle();
        mScroller = new Scroller(getContext());
        initVar();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画笔
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        // 抗锯齿
        paint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setDither(true);
        // 空心
        paint.setStyle(Paint.Style.STROKE);
        // 文字居中
        paint.setTextAlign(Paint.Align.CENTER);

        onDrawLine(canvas, paint);
        onDrawScale(canvas, paint); //画刻度
        onDrawPointer(canvas, paint); //画指针

        super.onDraw(canvas);
    }

    protected abstract void initVar();

    // 画线
    protected abstract void onDrawLine(Canvas canvas, Paint paint);

    // 画刻度
    protected abstract void onDrawScale(Canvas canvas, Paint paint);

    // 画指针
    protected abstract void onDrawPointer(Canvas canvas, Paint paint);

    // 滑动到指定刻度
    public abstract void scrollToScale(int val);

    public void setCurScale(int val) {
        if (val >= mMin && val <= mMax) {
            scrollToScale(val);
            postInvalidate();
        }
    }

    /**
     * 使用Scroller时需重写
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        // 判断Scroller是否执行完毕
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 通过重绘来不断调用computeScroll
            invalidate();
        }
    }

    public void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
    }

    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }
}