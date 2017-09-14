package com.example.jmf.hencoder;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/09/14 15:14
 * 修改人员：Robi
 * 修改时间：2017/09/14 15:14
 * 修改备注：
 * Version: 1.0.0
 */
public class SliderLayout extends FrameLayout {

    /**
     * 默认状态是, 每个View之间的间隔
     */
    public final int DEFAULT_MIN_OFFSET = (int) (40 * density());
    /**
     * 每一个View, 最多可以消耗多少距离
     */
    public final int DEFAULT_MAX_OFFSET = (int) (80 * density());

    /**
     * 当前需要消耗的高度
     */
    int currentOffset = 0;

    private OverScroller mOverScroller = new OverScroller(getContext());

    /**
     * 滑动手势监听
     */
    private GestureDetectorCompat mGestureDetectorCompat = new GestureDetectorCompat(getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    //L.e("call: onScroll([e1, e2, distanceX, distanceY])-> " + distanceY);
                    setCurrentOffset(currentOffset - distanceY);
                    postInvalidate();
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    //L.e("call: onFling([e1, e2, velocityX, velocityY])-> ");
                    mOverScroller.fling(0, currentOffset,
                            0, (int) velocityY * 3 / 4 /*控制快速滑动时的速度*/,
                            0, 0,
                            0, getMaxOffset());
                    postInvalidate();
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    public SliderLayout(Context context) {
        this(context, null);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    private void setCurrentOffset(float value) {
        int offset = (int) Math.min(Math.max(0, value), getMaxOffset());
        if (currentOffset != offset) {
            currentOffset = offset;
            refreshLayout();
        }
    }

    /**
     * 所有Item允许消耗的最大距离
     */
    private int getMaxOffset() {
        return DEFAULT_MAX_OFFSET * (getChildCount() - 1);//由于第一个item不参与消耗,所以-1
    }

    private void initLayout() {

    }

    /**
     * 此方法用来配合OverScroller做fling操作
     */
    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            setCurrentOffset(mOverScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshLayout();
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            final int finalI = i;
            childAt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // do something
                }
            });
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            //拿到事件, 防止外面包了一层刷新控件, 导致收不到touch事件
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return mGestureDetectorCompat.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetectorCompat.onTouchEvent(event);
        return true;
    }

    /**
     * View消耗了多少高度
     */
    private int getViewTop(View view) {
        Object tag = view.getTag();
        if (tag == null) {
            return 0;
        }
        return (int) tag;
    }

    private void refreshLayout() {
        int top = getPaddingTop();
        int childCount = getChildCount();

        //计算每个item view,能够效果的高度
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            int needOffset = (childCount - 1 - i) * DEFAULT_MAX_OFFSET;
            if (currentOffset > needOffset) {
                needOffset = Math.min(currentOffset - needOffset, DEFAULT_MAX_OFFSET);

                if (i == 0) {
                    //如果是第一个item, 限制能够消耗的高度
                    //needOffset = Math.min(DEFAULT_MIN_OFFSET, needOffset);
                    needOffset = 0;
                }
            } else {
                needOffset = 0;
            }
            childAt.setTag(needOffset);
        }

        //开始布局item 
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            int offsetTop = getViewTop(childAt);
            int layoutTop = top + offsetTop;
            childAt.layout(getPaddingLeft(), layoutTop, getMeasuredWidth() - getPaddingRight(), layoutTop + childAt.getMeasuredHeight());
            top += DEFAULT_MIN_OFFSET + offsetTop;
        }
    }

    private float density() {
        return getResources().getDisplayMetrics().density;
    }
}
