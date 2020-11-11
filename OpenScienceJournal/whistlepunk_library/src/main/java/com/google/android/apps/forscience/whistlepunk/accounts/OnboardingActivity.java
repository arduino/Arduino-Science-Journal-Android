package com.google.android.apps.forscience.whistlepunk.accounts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.apps.forscience.whistlepunk.R;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private static final String KEY_SHOULD_LAUNCH = "key_should_launch_onboarding_activity";

    public static boolean shouldLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOULD_LAUNCH, true);
    }

    static void setShouldLaunch(Context context, boolean shouldLaunch) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_SHOULD_LAUNCH, shouldLaunch)
                .apply();
    }

    private final PagerAdapter mPagerAdapter = new MyPagerAdapter();

    private LinearLayout mIndicators;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        findViewById(R.id.onboarding_header_action_close).setOnClickListener(v -> onClose());
        buildIndicators();
        updateIndicators(0);
        mViewPager = findViewById(R.id.onboarding_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
    }

    private void onPreviousPage() {
        final int position = mViewPager.getCurrentItem();
        if (position > 0) {
            mViewPager.setCurrentItem(position - 1, true);
        }
    }

    private void onNextPage() {
        final int position = mViewPager.getCurrentItem();
        if (position < mPagerAdapter.getCount()) {
            mViewPager.setCurrentItem(position + 1, true);
        }
    }

    private void onClose() {
        setShouldLaunch(this, false);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void buildIndicators() {
        mIndicators = findViewById(R.id.onboarding_page_indicators);
        final int size = mPagerAdapter.getCount();
        final LayoutInflater inflater = getLayoutInflater();
        final float weight = 1f / size;
        final int margin = getResources().getDimensionPixelSize(R.dimen.onboarding_indicator_margin_h);
        for (int i = 0; i < size; i++) {
            final View view = inflater.inflate(R.layout.activity_onboarding_page_indicator, mIndicators, false);
            view.setAlpha(INDICATOR_INACTIVE_ALPHA);
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, view.getLayoutParams().height);
            params.weight = weight;
            params.leftMargin = margin;
            params.rightMargin = margin;
            mIndicators.addView(view, params);
        }
    }

    private void updateIndicators(final int position) {
        final int size = mIndicators.getChildCount();
        for (int i = 0; i < size; i++) {
            final View view = mIndicators.getChildAt(i);
            view.setAlpha(i <= position ? INDICATOR_ACTIVE_ALPHA : INDICATOR_INACTIVE_ALPHA);
        }
    }

    private View buildPage(int resource, final ViewGroup container) {
        final LayoutInflater inflater = getLayoutInflater();
        final View page = inflater.inflate(R.layout.activity_onboarding_page_container, container, false);
        page.findViewById(R.id.onboarding_tap_previous).setOnClickListener(v -> onPreviousPage());
        page.findViewById(R.id.onboarding_tap_next).setOnClickListener(v -> onNextPage());
        final ViewGroup contents = page.findViewById(R.id.onboarding_page_contents);
        inflater.inflate(resource, contents, true);
        return page;
    }

    private class MyPagerAdapter extends PagerAdapter {

        private final Map<Object, View> mPages = new HashMap<>();

        @Override
        public int getCount() {
            return 7;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view;
            switch (position) {
                case 0:
                    view = buildPage(R.layout.activity_onboarding_page_1, container);
                    break;
                case 1:
                    view = buildPage(R.layout.activity_onboarding_page_2, container);
                    break;
                case 2:
                    view = buildPage(R.layout.activity_onboarding_page_3, container);
                    break;
                case 3:
                    view = buildPage(R.layout.activity_onboarding_page_4, container);
                    break;
                case 4:
                    view = buildPage(R.layout.activity_onboarding_page_5, container);
                    break;
                case 5:
                    view = buildPage(R.layout.activity_onboarding_page_6, container);
                    break;
                case 6:
                    view = buildPage(R.layout.activity_onboarding_page_7, container);
                    break;
                default:
                    throw new RuntimeException("Unknown onboarding page index " + position);
            }
            mPages.put(position, view);
            container.addView(view);
            return position;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            final View view = mPages.get(position);
            if (view != null) {
                container.removeView(view);
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            if (object instanceof Integer) {
                int position = (Integer) object;
                return mPages.get(position) == view;
            }
            return false;
        }

    }

    private static final float INDICATOR_ACTIVE_ALPHA = 1f;

    private static final float INDICATOR_INACTIVE_ALPHA = 0.4f;

}
