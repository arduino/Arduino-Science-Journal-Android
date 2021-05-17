package com.google.android.apps.forscience.whistlepunk.signin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.WhistlePunkApplication;
import com.google.android.apps.forscience.whistlepunk.accounts.AccountsProvider;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.accounts.arduino.ArduinoAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveShared;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveSyncSetupActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
            GDriveShared.clearCredentials(this);
            updateGDriveSyncUI();
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
        findViewById(R.id.redirect_to_id_page_image_view).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://id.arduino.cc"))));
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
    }

    private void updateGDriveSyncUI() {
        GDriveAccount gda = GDriveShared.getCredentials(this);
        if (gda == null) {
            mSyncSwitch.setOnCheckedChangeListener(null);
            mSyncSwitch.setChecked(false);
            mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);
            findViewById(R.id.section_google_drive_account_layout).setVisibility(View.GONE);
        } else {
            mSyncSwitch.setOnCheckedChangeListener(null);
            mSyncSwitch.setChecked(true);
            mSyncSwitch.setOnCheckedChangeListener(mSwitchListener);
            findViewById(R.id.section_google_drive_account_layout).setVisibility(View.VISIBLE);
            final TextView accountView = findViewById(R.id.section_google_drive_account_value);
            final TextView folderView = findViewById(R.id.section_google_drive_folder_value);
            accountView.setText(gda.email);
            folderView.setText(gda.folderPath);
        }
    }

}
