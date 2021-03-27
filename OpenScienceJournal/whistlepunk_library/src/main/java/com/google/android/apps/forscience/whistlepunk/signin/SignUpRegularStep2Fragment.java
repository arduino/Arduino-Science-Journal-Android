package com.google.android.apps.forscience.whistlepunk.signin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.auth0.Auth0SignUpCall;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;

public class SignUpRegularStep2Fragment extends AuthBaseFragment {

    private CheckBox mOptionPrivacyAndTermsCheckBox;
    private CheckBox mOptionNewsletterCheckBox;
    private CheckBox mOptionMarketingCheckBox;
    private CheckBox mOptionTrackingCheckBox;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_regular_step_2, container, false);
        mOptionPrivacyAndTermsCheckBox = view.findViewById(R.id.cb_option_privacy_and_terms);
        mOptionNewsletterCheckBox = view.findViewById(R.id.cb_option_newsletter);
        mOptionMarketingCheckBox = view.findViewById(R.id.cb_option_marketing);
        mOptionTrackingCheckBox = view.findViewById(R.id.cb_option_tracking);
        mOptionPrivacyAndTermsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mNextButton.setEnabled(isDataCompleted()));
        final TextView tvOptionPrivacyAndTerms = view.findViewById(R.id.tv_option_privacy_and_terms);
        tvOptionPrivacyAndTerms.setText(getPrivacyTermsWithLinks());
        tvOptionPrivacyAndTerms.setMovementMethod(LinkMovementMethod.getInstance());
        view.findViewById(R.id.tv_option_newsletter).setOnClickListener(v -> mOptionNewsletterCheckBox.toggle());
        view.findViewById(R.id.tv_option_marketing).setOnClickListener(v -> mOptionMarketingCheckBox.toggle());
        view.findViewById(R.id.tv_option_tracking).setOnClickListener(v -> mOptionTrackingCheckBox.toggle());
        mNextButton = view.findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(v -> {
            if (isDataCompleted()) {
                doSignUp();
            }
        });
        return view;
    }

    private void doSignUp() {
        final Bundle args = getArguments();
        final String email = args.getString("email");
        final String username = args.getString("username");
        final String password = args.getString("password");
        final boolean optionPrivacyAndTerms = mOptionPrivacyAndTermsCheckBox.isChecked();
        final boolean optionNewsletter = mOptionNewsletterCheckBox.isChecked();
        final boolean optionMarketing = mOptionMarketingCheckBox.isChecked();
        final boolean optionTracking = mOptionTrackingCheckBox.isChecked();
        setBlockUI(true);
        hideSoftKeyboard();
        new Auth0SignUpCall(
                mContext,
                username,
                email,
                password,
                optionPrivacyAndTerms,
                optionPrivacyAndTerms,
                optionNewsletter,
                optionMarketing,
                optionTracking
        ).execute(new Callback<Auth0SignUpCall.Response, Exception>() {
            @Override
            public void onResponse(Auth0SignUpCall.Response response) {
                setBlockUI(false);
                if (response.success) {
                    onCompleted();
                } else if (response.code != null) {
                    if (response.code == Auth0SignUpCall.Response.Code.USER_EXISTS) {
                        alert(R.string.arduino_auth_sign_up_user_exists);
                    } else {
                        toast(R.string.error_generic);
                    }
                }
            }

            @Override
            public void onFailure(Exception failure) {
                setBlockUI(false);
                toast(R.string.error_generic);
            }

            @Override
            public boolean onNetworkError() {
                setBlockUI(false);
                toast(R.string.error_network);
                return true;
            }
        });
    }

    private void onCompleted() {
        startFragment(SignUpRegularCompletedFragment.class, null);
    }

    private Spannable getPrivacyTermsWithLinks() {
        final String text = mContext.getString(R.string.arduino_auth_sign_up_option_privacy_and_terms);
        final SpannableString spannable = new SpannableString(text);
        final int color = mContext.getResources().getColor(R.color.arduino_teal_3);
        final String privacyLabel = mContext.getString(R.string.arduino_auth_sign_up_option_privacy_and_terms__privacy_label);
        final int privacyIndex = text.indexOf(privacyLabel);
        if (privacyIndex > 0) {
            spannable.setSpan(new ForegroundColorSpan(color), privacyIndex, privacyIndex + privacyLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), privacyIndex, privacyIndex + privacyLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new NoUnderlineClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Commons.openPrivacy(widget.getContext());
                }
            }, privacyIndex, privacyIndex + privacyLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        final String termsLabel = mContext.getString(R.string.arduino_auth_sign_up_option_privacy_and_terms__terms_label);
        final int termsIndex = text.indexOf(termsLabel);
        if (termsIndex > 0) {
            spannable.setSpan(new ForegroundColorSpan(color), termsIndex, termsIndex + termsLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), termsIndex, termsIndex + termsLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new NoUnderlineClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Commons.openTerms(widget.getContext());
                }
            }, termsIndex, termsIndex + termsLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private boolean isDataCompleted() {
        return mOptionPrivacyAndTermsCheckBox.isChecked();
    }

    private abstract static class NoUnderlineClickableSpan extends ClickableSpan {

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }

    }

}
