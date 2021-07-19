package com.google.android.apps.forscience.whistlepunk.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.signin.WebActivity;

public class NewTermsActivity extends AppCompatActivity {

    private static final String KEY_SHOULD_LAUNCH = "key_should_launch_new_terms_activity";

    public static boolean shouldLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOULD_LAUNCH, true);
    }

    @SuppressWarnings("SameParameterValue")
    static void setShouldLaunch(Context context, boolean shouldLaunch) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_SHOULD_LAUNCH, shouldLaunch)
                .apply();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_terms);

        TextView description = findViewById(R.id.new_terms_description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        description.setText(getDescriptionTextWithLinks());

        TextView agreeButton = findViewById(R.id.new_terms_btn_agree);
        agreeButton.setOnClickListener(v -> onClose());
    }

    private Spannable getDescriptionTextWithLinks() {
        final String text = getString(R.string.arduino_terms_agreement);
        final SpannableString spannable = new SpannableString(text);
        final int color = getResources().getColor(R.color.arduino_teal_3);
        final String linkLabel = getString(R.string.arduino_terms_agreement__link_label);
        final int linkIndex = text.indexOf(linkLabel);
        if (linkIndex > 0) {
            spannable.setSpan(new ForegroundColorSpan(color), linkIndex, linkIndex + linkLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), linkIndex, linkIndex + linkLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new NewTermsActivity.NoUnderlineClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    openTermsPage();
                }
            }, linkIndex, linkIndex + linkLabel.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    private void openTermsPage() {
        final Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.EXTRA_KEY_TITLE, getString(R.string.terms));
        intent.putExtra(WebActivity.EXTRA_KEY_URL, getString(R.string.config_auth_terms));
        startActivity(intent);
    }

    private void onClose() {
        setShouldLaunch(this, false);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private abstract static class NoUnderlineClickableSpan extends ClickableSpan {

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }

    }
}
