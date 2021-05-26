package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.whistlepunk.R;

public class GDriveLearnMoreActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GDriveLearnMoreActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_learn_more_modal);

        findViewById(R.id.drive_learn_more_modal_close).setOnClickListener(v -> finish());

        findViewById(R.id.drive_learn_more_modal_terms_link).setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/drive/terms-of-service"));
            this.startActivity(intent);
        });

        findViewById(R.id.drive_learn_more_modal_privacy_link).setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"));
            this.startActivity(intent);
        });
    }
}

