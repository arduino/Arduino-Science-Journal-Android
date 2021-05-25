package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.signin.WebActivity;

public class GDriveLearnMoreActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GDriveLearnMoreActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_learn_more_modal);

        findViewById(R.id.drive_learn_more_modal_close).setOnClickListener(v -> finish());

        findViewById(R.id.drive_learn_more_modal_terms_link).setOnClickListener(v -> {
            final Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra(WebActivity.EXTRA_KEY_TITLE, this.getString(R.string.arduino_auth_terms));
            intent.putExtra(WebActivity.EXTRA_KEY_URL, this.getString(R.string.config_auth_terms));
            this.startActivity(intent);
        });

        findViewById(R.id.drive_learn_more_modal_privacy_link).setOnClickListener(v -> {
            final Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra(WebActivity.EXTRA_KEY_TITLE, this.getString(R.string.arduino_auth_privacy));
            intent.putExtra(WebActivity.EXTRA_KEY_URL, this.getString(R.string.config_auth_privacy));
            this.startActivity(intent);
        });
    }
}

