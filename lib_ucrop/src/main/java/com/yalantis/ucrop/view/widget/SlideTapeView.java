package com.yalantis.ucrop.view.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.yalantis.ucrop.R;

import java.math.BigDecimal;

/**
 * author:tandeneck
 * time:2020/6/28
 * description:
 */
public final class SlideTapeView extends View {

    private final Paint mTextPaint;
    private final Paint mShortPaint;
    private final Paint mLongPaint;
    private final Paint mIndicatorPaint;
    private final Rect mContentRect = new Rect();
    private final Rect mTextRect = new Rect();

    //长指针
    private int mLongPointWidth;
    private int mLongPointHeight;
    //长指针之间的间隔
    private int mLongPointInterval;
    //长指针的数量
    private int mLongPointCount;
    //短指针
    private int mShortPointWidth;
    private int mShortPointHeight;
    //短指针之间的间隔
    private float mShortPointInterval;
    //短指针的数量
    private int mShortPointCount;
    //指示器
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    //左边的偏移量，向左为正
    private int mOffsetLeft;
    private int mMinOffsetLeft, mMaxOffsetLeft;
    private final OverScroller mScroller;
    private int mLastFlingX;
    private boolean mIsBeingDragged;
    //起始值，结束值
    private int mStartValue, mEndValue;
    //长指针单位
    private int mLongUnix;
    //短指针单位
    private BigDecimal mShortUnix;

    private VelocityTracker mVelocityTracker;
    private final int mMaximumFlingVelocity;
    private final int mMinimumFlingVelocity;

    private final int mTouchSlop;
    private float mDownMotionX;
    private float mLastMotionX;
    private boolean mStartFling;
    private final int mMinimumHeight;
    private CallBack mCallBack;
    private ValueAnimator mRunAnimator;

    private EdgeEffect mEdgeGlowLeft;
    private EdgeEffect mEdgeGlowRight;

    private int mActivePointerId = INVALID_POINTER;

    {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setTextSize(sp2px(16f));

        mShortPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShortPaint.setColor(POINT_COLOR);

        mLongPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPaint.setColor(POINT_COLOR);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(INDICATOR_COLOR);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    private static final int INDICATOR_COLOR = Color.rgb(77, 166, 104);
    private static final int BACKGROUP_COLOR = Color.rgb(244, 248, 243);
    private static final int POINT_COLOR = Color.rgb(210, 215, 209);
    private static final int TEXT_COLOR = Color.BLACK;
    private static final int MAXIMUM_SHORT_POINT_COUNT = 10;
    private static final int INVALID_POINTER = -1;

    private static final String TAG = "SlideTapeView";

    public SlideTapeView(Context context) {
        this(context, null);
    }

    public SlideTapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideTapeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mMinimumHeight = dp2px(95);

        mIndicatorWidth = dp2px(5);
        mIndicatorHeight = dp2px(50);

        mLongPointWidth = dp2px(2);
        mLongPointHeight = dp2px(40);

        mShortPointWidth = dp2px(1);
        mShortPointHeight = dp2px(20);

        mLongPointInterval = dp2px(100);

        mLongUnix = 1;

        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        viewConfiguration.getScaledTouchSlop();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();

        mScroller = new OverScroller(context);

        init(context, attrs);

        if (isInEditMode()) {
            if (mStartValue == 0 && mEndValue == 0) {
                setValue(0, 100);
            }
            if (mShortPointCount == 0) {
                setShortPointCount(10);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wantWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int wantHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();
        //将起始和结束数作为最大字符考虑
        int maxTextHeight;
        mTextPaint.getTextBounds(String.valueOf(mStartValue), 0, String.valueOf(mStartValue).length(), mTextRect);
        maxTextHeight = mTextRect.height();
        mTextPaint.getTextBounds(String.valueOf(mEndValue), 0, String.valueOf(mEndValue).length(), mTextRect);
        maxTextHeight = Math.max(maxTextHeight, mTextRect.height()) + dp2px(5);
        Log.d(TAG, "onMeasure: maxTextHeight:" + maxTextHeight);
        int drawMaxHeight = Math.max(mIndicatorHeight, Math.max(mLongPointHeight, mShortPointHeight)) + maxTextHeight;
        wantHeight += drawMaxHeight;
        wantHeight = Math.max(wantHeight, mMinimumHeight);
        setMeasuredDimension(resolveSize(wantWidth, widthMeasureSpec), resolveSize(wantHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mContentRect.left = getPaddingLeft();
        mContentRect.top = getPaddingTop();
        mContentRect.right = w - getPaddingRight();
        mContentRect.bottom = h - getPaddingBottom();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRunAnimator != null) {
            mRunAnimator.cancel();
        }
    }

    @Override
    @SuppressWarnings({"SuspiciousNameCombination"})
    public void draw(Canvas canvas) {
        super.draw(canvas);
        //HorzontalScrollView
        if (mEdgeGlowLeft != null && !mEdgeGlowLeft.isFinished()) {
            final int restoreCount = canvas.save();

            canvas.rotate(270);
            //坐标系旋转后，需要将绘制的 EdgeEffect 偏移回来
            canvas.translate(-getHeight(), 0);

            mEdgeGlowLeft.setSize(getHeight(), getWidth());
            if (mEdgeGlowLeft.draw(canvas)) {
                postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        } else if (mEdgeGlowRight != null && !mEdgeGlowRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.rotate(90);
            canvas.translate(0, -getWidth());

            mEdgeGlowRight.setSize(getHeight(), getWidth());
            if (mEdgeGlowRight.draw(canvas)) {
                postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景色
        canvas.drawColor(BACKGROUP_COLOR);

        if (correctRangeOfValues()) {
            //绘制短指针
            if (mShortPointCount > 0) {
                drawShortPaints(canvas);
            }

            //绘制长指针
            drawLongPaints(canvas);

            //绘制文字
            drawText(canvas);


            if (mCallBack != null) {
                @SuppressLint("DrawAllocation")
                BigDecimal result = new BigDecimal(mOffsetLeft % mLongPointInterval)
                        .multiply(mShortUnix)
                        .divide(new BigDecimal(mShortPointInterval), 2, BigDecimal.ROUND_HALF_UP)
                        .add(new BigDecimal(mOffsetLeft / mLongPointInterval * mLongUnix + mStartValue));
                mCallBack.onSlide(result.floatValue());

            }
        }
        //绘制指示器
        drawIndicator(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!correctRangeOfValues()) {
            return super.onTouchEvent(event);
        }
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initOrResetVelocityTracker();
                if (mRunAnimator != null) {
                    mRunAnimator.cancel();
                }
                mScroller.computeScrollOffset();
                if (mIsBeingDragged = !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mStartFling = false;
                if (mIsBeingDragged) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                mActivePointerId = event.getPointerId(0);
                mDownMotionX = event.getX(0);
                mLastMotionX = event.getX(0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = event.getActionIndex();
                mDownMotionX = event.getX(index);
                mLastMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "onTouchEvent: Invalid pointerId = " + activePointerId);
                    break;
                }

                float distanceX = mLastMotionX - event.getX(pointerIndex);
                Log.d(TAG, "onTouchEvent: distanceX:" + distanceX);
                if (!mIsBeingDragged && Math.abs(mDownMotionX - event.getX(pointerIndex)) > mTouchSlop) {
                    mIsBeingDragged = true;
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (mIsBeingDragged) {
                    mOffsetLeft += distanceX;

                    final int range = getOffsetLeftRange();
                    final int overscrollMode = getOverScrollMode();
                    boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS
                            && range > 0);

                    if (mOffsetLeft > mMaxOffsetLeft) {
                        if (canOverscroll){
                            ensureGlows();
                            mEdgeGlowRight.onPull(distanceX / getWidth(), event.getY(pointerIndex) / getHeight());
                            if (!mEdgeGlowLeft.isFinished()) {
                                mEdgeGlowLeft.onRelease();
                            }
                        }
                        mOffsetLeft = mMaxOffsetLeft;
                    }
                    if (mOffsetLeft < mMinOffsetLeft) {
                        if (canOverscroll){
                            //edge
                            ensureGlows();
                            mEdgeGlowLeft.onPull(distanceX / getWidth(), 1 - event.getY(pointerIndex) / getHeight());
                            Log.d(TAG, "onTouchEvent: deltaDistance: " + distanceX / getWidth());
                            if (!mEdgeGlowRight.isFinished()) {
                                mEdgeGlowRight.onRelease();
                            }
                        }
                        mOffsetLeft = mMinOffsetLeft;
                    }
                    postInvalidateOnAnimation();

                }
                mLastMotionX = event.getX(pointerIndex);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                if (Math.abs(velocityTracker.getXVelocity()) > mMinimumFlingVelocity) {
                    //fling
                    Log.d(TAG, "onTouchEvent: velocityX:" + velocityTracker.getXVelocity());
                    mScroller.fling(0, 0, (int) (velocityTracker.getXVelocity()), 0, -mMaxOffsetLeft, mMaxOffsetLeft, 0, 0);
                    mStartFling = true;
                    mLastFlingX = mScroller.getStartX();
                    postInvalidateOnAnimation();
                }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && !mStartFling) {
                    checkOffsetLeft();
                }
                mIsBeingDragged = false;
                recycleVelocityTracker();
                if (mEdgeGlowLeft != null) {
                    mEdgeGlowLeft.onRelease();
                }
                if (mEdgeGlowRight != null) {
                    mEdgeGlowRight.onRelease();
                }
                mActivePointerId = INVALID_POINTER;
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }

        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int newOffsetLeft = mOffsetLeft + (mLastFlingX - mScroller.getCurrX());

            final int range = getOffsetLeftRange();
            final int overscrollMode = getOverScrollMode();
            boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS
                    && range > 0);

            if (canOverscroll && mOffsetLeft < mMaxOffsetLeft && newOffsetLeft > mMaxOffsetLeft) {
                ensureGlows();
                mEdgeGlowRight.onAbsorb((int) mScroller.getCurrVelocity());
            } else if (canOverscroll && mOffsetLeft > mMinOffsetLeft && newOffsetLeft < mMinOffsetLeft) {
                ensureGlows();
                mEdgeGlowLeft.onAbsorb((int) mScroller.getCurrVelocity());
            }

            if (newOffsetLeft > mMaxOffsetLeft) {
                newOffsetLeft = mMaxOffsetLeft;
            }
            if (newOffsetLeft < mMinOffsetLeft) {
                newOffsetLeft = mMinOffsetLeft;
            }

            mOffsetLeft = newOffsetLeft;

            postInvalidateOnAnimation();
            mLastFlingX = mScroller.getCurrX();
        } else {
            if (mStartFling) {
                mStartFling = false;
                Log.d(TAG, "computeScroll: checkOffsetLeft");
                checkOffsetLeft();
            }
        }

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superParcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superParcelable);
        savedState.offsetLeft = mOffsetLeft;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mOffsetLeft = savedState.offsetLeft;
            postInvalidate();
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 设置起始值和结束值
     * 结束值必须大于起始值
     *
     * @param startValue 起始值
     * @param endValue   结束值
     */
    public void setValue(int startValue, int endValue) {
        if (!(endValue > startValue)) {
            throw new IllegalArgumentException("endValue 必须大于 startValue");
        }
        if (mStartValue == startValue && mEndValue == endValue) {
            return;
        }
        mStartValue = startValue;
        mEndValue = endValue;
        if (mLongUnix > mEndValue) {
            mLongUnix = mEndValue;
        }
        calculate();
        postInvalidateOnAnimation();
    }

    /**
     * 设置短指针的个数
     * 简单处理
     *
     * @param shortPointCount 短指针个数
     */
    public void setShortPointCount(int shortPointCount) {
        if (shortPointCount > MAXIMUM_SHORT_POINT_COUNT) {
            shortPointCount = MAXIMUM_SHORT_POINT_COUNT;
        }
        if (mShortPointCount == shortPointCount) {
            return;
        }
        mShortPointCount = shortPointCount;
        calculate();
        postInvalidate();
    }

    public void setLongUnix(int longUnix) {
        if (longUnix == 0) {
            throw new IllegalArgumentException("longUnix 不能为 0");
        }
        if (mLongUnix == longUnix) {
            return;
        }
        if (longUnix > mEndValue) {
            longUnix = mEndValue;
        }
        mLongUnix = longUnix;
        if (correctRangeOfValues()) {
            calculate();
            postInvalidateOnAnimation();
        }
    }

    public void moveToValue(float value) {
        if (!correctRangeOfValues()) {
            return;
        }
        if (value > mEndValue) {
            value = mEndValue;
        }
        if (value < mStartValue) {
            value = mStartValue;
        }

        int offsetLeft = (int) ((value - mStartValue) / mLongUnix) * mLongPointInterval;
        if (mShortPointCount != 0) {
            BigDecimal count = new BigDecimal(value % mLongUnix).divide(mShortUnix, 2, BigDecimal.ROUND_DOWN);
            offsetLeft += count.intValue() * mShortPointInterval;
        }
        Log.d(TAG, "moveToValue: offsetLeft:" + offsetLeft);
        offsetAnim(offsetLeft);
    }

    public void setCallBack(CallBack callback) {
        if (mCallBack == callback) {
            return;
        }
        mCallBack = callback;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideTapeView, 0, 0);
        try {
            int startValue = typedArray.getInt(R.styleable.SlideTapeView_startValue, 0);
            int endValue = typedArray.getInt(R.styleable.SlideTapeView_endValue, 0);
            if (!(startValue == 0 && endValue == 0)) {
                setValue(startValue, endValue);
            }
            setShortPointCount(typedArray.getInt(R.styleable.SlideTapeView_shortPointCount, 0));
            setLongUnix(typedArray.getInt(R.styleable.SlideTapeView_longUnix, 1));
            moveToValue(typedArray.getInt(R.styleable.SlideTapeView_currentValue, 0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }

    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            mDownMotionX = ev.getX(newPointerIndex);
            mLastMotionX = ev.getX(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void ensureGlows() {
        if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
            if (mEdgeGlowLeft == null) {
                mEdgeGlowLeft = new EdgeEffect(getContext());
                mEdgeGlowLeft.setColor(INDICATOR_COLOR);
            }
            if (mEdgeGlowRight == null) {
                mEdgeGlowRight = new EdgeEffect(getContext());
                mEdgeGlowRight.setColor(INDICATOR_COLOR);
            }
        } else {
            mEdgeGlowLeft = null;
            mEdgeGlowRight = null;
        }
    }

    private boolean correctRangeOfValues() {
        return !((mStartValue == 0) && (mEndValue == 0));
    }

    private void checkOffsetLeft() {
        if (mShortPointCount == 0) {
            int current = mOffsetLeft / mLongPointInterval;
            int offset = mOffsetLeft % mLongPointInterval;
            Log.d(TAG, "computeScroll: current：" + current + "  offset:" + offset);
            if (offset > (mLongPointInterval / 2f)) {
                current++;
                Log.d(TAG, "computeScroll: current：" + current + "  offset:" + offset);
            }
            offsetAnim(current * mLongPointInterval);
        } else {
            int current = (int) (mOffsetLeft / mShortPointInterval);
            int offset = (int) (mOffsetLeft % mShortPointInterval);
            Log.d(TAG, "computeScroll: current：" + current + "  offset:" + offset);
            if (offset > (mShortPointInterval / 2f)) {
                current++;
                Log.d(TAG, "computeScroll: current：" + current + "  offset:" + offset);
            }
            offsetAnim((int) (current * mShortPointInterval));
        }
    }

    private void offsetAnim(int offsetLeft) {
        if (mRunAnimator != null) {
            mRunAnimator.cancel();
        }
        mRunAnimator = ValueAnimator.ofInt(mOffsetLeft, offsetLeft);
        mRunAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffsetLeft = (int) animation.getAnimatedValue();
                postInvalidateOnAnimation();
            }
        });
        mRunAnimator.start();
    }

    private void drawShortPaints(Canvas canvas) {
        mShortPaint.setStrokeWidth(mShortPointWidth);
        final int halfWdith = (int) (getWidth() / 2f);
        for (int i = 0; i < (mLongPointCount - 1); i++) {
            final float longStartX = halfWdith + i * mLongPointInterval - mOffsetLeft;
            for (int j = 1; j < mShortPointCount; j++) {
                final float startX = longStartX + j * mShortPointInterval;
                if (!mContentRect.contains((int) startX, 0)) {
                    continue;
                }
                canvas.drawLine(startX, mContentRect.top, startX, mShortPointHeight, mShortPaint);
            }
        }

    }

    private void drawLongPaints(Canvas canvas) {
        mLongPaint.setStrokeWidth(mLongPointWidth);
        final int halfWdith = (int) (getWidth() / 2f);
        for (int i = 0; i < mLongPointCount; i++) {
            final float startX = halfWdith + i * mLongPointInterval - mOffsetLeft;
            if (!mContentRect.contains((int) startX, 0)) {
                continue;
            }
            canvas.drawLine(startX, mContentRect.top, startX, mLongPointHeight, mLongPaint);
        }
    }

    private void drawIndicator(Canvas canvas) {
        mIndicatorPaint.setStrokeWidth(mIndicatorWidth);
        final int halfWdith = (int) (getWidth() / 2f);
        canvas.drawLine(halfWdith, mContentRect.top, halfWdith, mIndicatorHeight, mIndicatorPaint);
    }

    private void drawText(Canvas canvas) {
        final int halfWdith = (int) (getWidth() / 2f);
        for (int i = 0; i < mLongPointCount; i++) {
            final float startX = halfWdith + i * mLongPointInterval - mOffsetLeft;
            final String text = String.valueOf(mStartValue + i * mLongUnix);
            if (TextUtils.isEmpty(text)) {
                continue;
            }
            mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            canvas.drawText(text, startX - mTextRect.width() / 2f, mContentRect.bottom - mTextRect.bottom, mTextPaint);
        }
    }

    private void calculate() {
        mLongPointCount = (mEndValue - mStartValue + 1) / mLongUnix;
        mMinOffsetLeft = 0;
        mMaxOffsetLeft = (mLongPointCount - 1) * mLongPointInterval;
        if (mShortPointCount > 0) {
            mShortPointInterval = (float) mLongPointInterval / mShortPointCount;
            mShortUnix = new BigDecimal(mLongUnix).divide(new BigDecimal(mShortPointCount), 2, BigDecimal.ROUND_DOWN);
            Log.d(TAG, "calculate: mShortUnix:" + mShortUnix);
        }

        Log.d(TAG, "calculate: mLongPointInterval:" + mLongPointInterval + "    mShortPointInterval:" + mShortPointInterval);

    }

    private int getOffsetLeftRange() {
        return Math.max(0, mMaxOffsetLeft);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        } else {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
    }

    private int sp2px(float sp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics()));
    }

    private int dp2px(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    public static interface CallBack {
        void onSlide(float current);
    }

    private static class SavedState extends View.BaseSavedState {

        private int offsetLeft;

        private SavedState(Parcel source) {
            super(source);
            offsetLeft = source.readInt();
        }

        private SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(offsetLeft);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

}
