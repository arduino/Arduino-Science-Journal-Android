package com.google.android.apps.forscience.whistlepunk;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private View refreshStatusView = null;
    private View refreshStatusIcon = null;
    private RecyclerView recyclerView = null;
    private Animation rotation = null;

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
                    float topMargin = y - initialY;
                    // set opacity
                    refreshStatusView.setAlpha(topMargin / MAX_REFRESH_HEIGHT);

                    // set rotation
                    refreshStatusIcon.setRotation(-90 + topMargin / MAX_REFRESH_HEIGHT * 90);

                    // set position
                    marginParams = (MarginLayoutParams) recyclerView.getLayoutParams();
                    marginParams.topMargin = Math.round(topMargin);
                    recyclerView.setLayoutParams(marginParams);
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

                        startIconRotation();

                        onRefreshListener.onRefresh();
                    } else {
                        resetMargin(MAX_REFRESH_HEIGHT);
                    }
                }
        }

        return super.onTouchEvent(event);
    }

    private void startIconRotation() {
        refreshStatusIcon.setEnabled(false);
        stopIconRotation();
        rotation = AnimationUtils.loadAnimation(refreshStatusIcon.getContext(), R.anim.refresh_rotate);
        refreshStatusIcon.startAnimation(rotation);
        refreshStatusIcon.setOnClickListener(null);
    }
    private void stopIconRotation() {
        if (rotation != null) {
            rotation.cancel();
            rotation = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        refreshStatusView = findViewById(R.id.refresh_status);
        refreshStatusIcon = findViewById(R.id.refresh_status_icon);
        recyclerView = findViewById(R.id.details);

        MAX_REFRESH_HEIGHT = refreshStatusView.getHeight();

        if (onScrollListener == null) {
            onScrollListener = new MyOnScrollListener();

            recyclerView.addOnScrollListener(onScrollListener);
        }
    }

    private void resetMargin(Integer topMargin) {
        initialY = 0f;
        refreshing = false;

        ValueAnimator marginAnimator = ValueAnimator.ofFloat(topMargin, 0);

        marginAnimator.addUpdateListener(valueAnimator -> {
            float value = (float) valueAnimator.getAnimatedValue();

            // set opacity
            refreshStatusView.setAlpha(value / MAX_REFRESH_HEIGHT);

            // set rotation
            refreshStatusIcon.setRotation(-90 + value / MAX_REFRESH_HEIGHT * 90);

            // set position
            MarginLayoutParams params = (MarginLayoutParams) recyclerView.getLayoutParams();
            params.topMargin = Math.round(value);
            recyclerView.setLayoutParams(params);
        });
        marginAnimator.start();
    }

    public void setOnRefreshListener(PullToRefreshLayout.OnRefreshListener listener) {
        onRefreshListener = listener;
    }
    public void stopRefreshing() {
        if (refreshing) {
            resetMargin(MAX_REFRESH_HEIGHT);
        }

        stopIconRotation();
    }
};



