package com.google.android.apps.forscience.whistlepunk.signin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.WhistlePunkApplication;
import com.google.android.apps.forscience.whistlepunk.accounts.AccountsProvider;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.accounts.arduino.ArduinoAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveLearnMoreActivity;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveShared;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveSyncSetupActivity;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;
import java.net.URL;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

@SuppressLint("SetJavaScriptEnabled")
public class ArduinoSettingsActivity extends AppCompatActivity {

    private AccountsProvider mAccountsProvider;

    private DisposableObserver<AppAccount> mAppAccountObserver;

    private SwitchCompat mSyncSwitch;

    private final CompoundButton.OnCheckedChangeListener mSwitchListener = (buttonView, isChecked) -> {
        if (isChecked) {
            startActivity(new Intent(this, GDriveSyncSetupActivity.class));
        } else {
            confirmDisablingGDrive();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // configure layout
        setContentView(R.layout.activity_arduino_auth_settings);
        findViewById(R.id.iv_back_action).setOnClickListener(v -> finish());
        mAccountsProvider = WhistlePunkApplication.getAppServices(this).getAccountsProvider();
        findViewById(R.id.sign_out).setOnClickListener(v -> {
            AlertDialog.Builder alert = new MaterialAlertDialogBuilder(ArduinoSettingsActivity.this, R.style.AlertDialogTheme);
            alert.setMessage(R.string.arduino_auth_settings_sign_out_confirm);
            alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                GDriveShared.clearCredentials(this);
                mAccountsProvider.undoSignIn();
                finish();
            });
            alert.setNegativeButton(android.R.string.cancel, null);
            alert.setCancelable(true);
            alert.show();
        });
        mSyncSwitch = findViewById(R.id.section_google_drive_switch);
        mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);
        findViewById(R.id.section_google_drive_switch_change).setOnClickListener(v -> startActivity(new Intent(this, GDriveSyncSetupActivity.class)));
        findViewById(R.id.section_arduino_account_settings_advanced_settings_button).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://id.arduino.cc"))));

        findViewById(R.id.section_google_drive_learn_more).setOnClickListener(v -> {
            startActivity(new Intent(this, GDriveLearnMoreActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppAccountObserver = mAccountsProvider.getObservableCurrentAccount().subscribeWith(new DisposableObserver<AppAccount>() {
            @Override
            public void onNext(@NonNull AppAccount appAccount) {
                onAppAccount(appAccount);
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAppAccountObserver.dispose();
    }

    private void onAppAccount(final AppAccount appAccount) {
        if (!(appAccount instanceof ArduinoAccount)) {
            finish();
            return;
        }
        final ArduinoAccount aa = (ArduinoAccount) appAccount;
        final ImageView avatarView = findViewById(R.id.section_arduino_account_avatar);
        final TextView nicknameView = findViewById(R.id.section_arduino_account_nickname);
        final TextView emailView = findViewById(R.id.section_arduino_account_email);
        nicknameView.setText(aa.getAccountName());
        if (appAccount.isMinor()) {
            emailView.setVisibility(View.INVISIBLE);
            findViewById(R.id.section_google_drive_sync_layout).setVisibility(View.GONE);
        } else {
            emailView.setText(aa.getEmail());
            findViewById(R.id.section_google_drive_sync_layout).setVisibility(View.VISIBLE);
            updateGDriveSyncUI();
        }

        final String avatar = appAccount.getAccountAvatar();
        if (StringUtils.isEmpty(avatar)) {
            avatarView.setImageResource(R.drawable.ic_navigation_user_avatar);
        } else {
            if (avatar.toLowerCase().endsWith(".svg")) {
                new Thread(() -> {
                    final Resources r = getResources();
                    final int side = r.getDimensionPixelSize(R.dimen.main_arduino_auth_avatar_side);
                    final Bitmap bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
                    final Canvas canvas = new Canvas(bitmap);
                    canvas.drawRGB(255, 255, 255);
                    try {
                        final SVG svg;
                        try (final InputStream inputStream = new URL(avatar).openConnection().getInputStream()) {
                            svg = SVG.getFromInputStream(inputStream);
                        }
                        svg.setDocumentWidth(side);
                        svg.setDocumentHeight(side);
                        svg.renderToCanvas(canvas, new RectF(0, 0, side, side));
                        runOnUiThread(() -> Glide.with(this).load(bitmap).circleCrop().into(avatarView));
                    } catch (Exception e) {
                        Log.e("Main", "Unable to decode SVG", e);
                        runOnUiThread(() -> avatarView.setImageResource(R.drawable.ic_navigation_user_avatar));
                    }
                }).start();
            } else {
                Glide.with(this).load(avatar).circleCrop().into(avatarView);
            }
        }
    }

    private void confirmDisablingGDrive() {
        final AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);

        builder.setTitle(R.string.arduino_auth_settings_disable_sync_title);
        builder.setMessage(R.string.arduino_auth_settings_disable_sync_message);
        builder.setNegativeButton(R.string.arduino_auth_settings_disable_sync_cancel, (dialog, which) -> {
            mSyncSwitch.setOnCheckedChangeListener(null);
            mSyncSwitch.setChecked(true);
            mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);
            dialog.cancel();
        });
        builder.setPositiveButton(
            R.string.arduino_auth_settings_disable_sync_confirm,
            (dialog, which) -> {
                GDriveShared.clearCredentials(this);
                updateGDriveSyncUI();

                dialog.dismiss();
            });
        builder.create().show();
    }

    private void updateGDriveSyncUI() {
        GDriveAccount gda = GDriveShared.getCredentials(this);
        if (gda == null) {
            mSyncSwitch.setOnCheckedChangeListener(null);
            mSyncSwitch.setChecked(false);
            mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);
            findViewById(R.id.section_google_drive_description_wrapper).setVisibility(View.VISIBLE);
            findViewById(R.id.section_google_drive_account_layout).setVisibility(View.GONE);
        } else {
            mSyncSwitch.setOnCheckedChangeListener(null);
            mSyncSwitch.setChecked(true);
            mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);

            findViewById(R.id.section_google_drive_description_wrapper).setVisibility(View.GONE);
            findViewById(R.id.section_google_drive_account_layout).setVisibility(View.VISIBLE);
            final TextView accountView = findViewById(R.id.section_google_drive_account_value);
            final TextView folderView = findViewById(R.id.section_google_drive_folder_value);
            accountView.setText(gda.email);
            folderView.setText(gda.folderPath);

            findViewById(R.id.section_google_drive_account_info).setOnClickListener(v -> {
                startActivity(new Intent(this, GDriveLearnMoreActivity.class));
            });
        }
    }

}
