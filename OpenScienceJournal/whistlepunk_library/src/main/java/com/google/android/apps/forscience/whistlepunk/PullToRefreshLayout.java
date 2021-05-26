package com.google.android.apps.forscience.whistlepunk;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PullToRefreshLayout extends FrameLayout {
    private Integer MAX_REFRESH_HEIGHT = 0;
    private Float initialY = 0f ;
    private Boolean onTopReached = true;
    private MarginLayoutParams marginParams;
    private RecyclerView.OnScrollListener onScrollListener = null;
    private boolean refreshing = false;
    private OnRefreshListener onRefreshListener = null;

    class MyOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            onTopReached = !recyclerView.canScrollVertically(-1);
        }
    }

    public static interface OnRefreshListener {
        public abstract void onRefresh ();
    }

    public PullToRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                float y = event.getY();

                if (y - initialY < 0) {
                    return false;
                } else {
                    return y - initialY > 0 && onTopReached;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        float y = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                y = event.getY();

                if (((int) (y - initialY)) > MAX_REFRESH_HEIGHT) {
                    // user has pulled the refresh view to its extent,
                    // no need to increase the margin on the child.
                } else {
                    RecyclerView targetView = (RecyclerView) getChildAt(1);
                    marginParams = (MarginLayoutParams) targetView.getLayoutParams();
                    marginParams.topMargin = (int) (y - initialY);
                    targetView.setLayoutParams(marginParams);
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                y = event.getY();

                if (((int) (y - initialY)) < MAX_REFRESH_HEIGHT) {
                    resetMargin((int) (y - initialY));
                } else {
                    // This is the place where the callback to trigger the refresh is placed.
                    // For demonstration purposes I have replaced it with a delay and finally called the resetMargin function()
                    if (onRefreshListener != null) {
                        refreshing = true;
                        onRefreshListener.onRefresh();
                    } else {
                        resetMargin(MAX_REFRESH_HEIGHT);
                    }
                }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        MAX_REFRESH_HEIGHT = getChildAt(0).getHeight();
        RecyclerView targetView = (RecyclerView) getChildAt(1);

        if (onScrollListener == null) {
            onScrollListener = new MyOnScrollListener();

            targetView.addOnScrollListener(onScrollListener);
        }
    }

    private void resetMargin(Integer topMargin) {
        initialY = 0f;
        refreshing = false;

        ValueAnimator marginAnimator = ValueAnimator.ofInt(topMargin, 0);

        marginAnimator.addUpdateListener(valueAnimator -> {
            RecyclerView targetView = (RecyclerView) getChildAt(1);
            MarginLayoutParams params = (MarginLayoutParams) targetView.getLayoutParams();
            params.topMargin = (int) valueAnimator.getAnimatedValue();
            targetView.setLayoutParams(params);
        });
        marginAnimator.start();
    }

    public void setOnRefreshListener(PullToRefreshLayout.OnRefreshListener listener) {
        onRefreshListener = listener;
    }
    public void stopRefreshing() {
        resetMargin(MAX_REFRESH_HEIGHT);
    }
};



