/*
 *  Copyright 2018 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.apps.forscience.whistlepunk.accounts;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.apps.forscience.whistlepunk.AppSingleton;
import com.google.android.apps.forscience.whistlepunk.LoggingConsumer;
import com.google.android.apps.forscience.whistlepunk.PermissionUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.filemetadata.Experiment;

/**
 * Activity that tells the user to explore their world.
 */
public class GetStartedActivity extends AppCompatActivity {
    private static final String TAG = "GetStartedActivity";

    private static final String KEY_SHOULD_LAUNCH = "key_should_launch_get_started_activity";

    private View[] mSteps = new View[3];
    private int mCurrentStep = 0;

    public static boolean shouldLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOULD_LAUNCH, true);
    }

    static void setShouldLaunch(Context context, boolean shouldLaunch) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_SHOULD_LAUNCH, shouldLaunch)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_started);
        /*
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        if (!isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSteps[0] = findViewById(R.id.step_1);
        mSteps[1] = findViewById(R.id.step_2);
        mSteps[2] = findViewById(R.id.step_3);

        final View btnStep1 = findViewById(R.id.step_1_btn_get_started);
        btnStep1.setVisibility(View.INVISIBLE);
        btnStep1.setOnClickListener(view -> {
            onStep2();
        });

        final View btnStep2 = findViewById(R.id.step_2_btn_understand);
        btnStep2.setOnClickListener(view -> {
            onStep2_LocationPermission();
        });

        final View btnStep3 = findViewById(R.id.step_3_btn_done);
        btnStep3.setOnClickListener(view -> {
            onCompleted();
        });

        // Before letting the user sign in, get the DataController for the NonSignedInAccount and
        // call DataController.getLastUsedUnarchivedExperiment, which will upgrade the database, if
        // necessary.
        NonSignedInAccount nonSignedInAccount = NonSignedInAccount.getInstance(this);
        AppSingleton.getInstance(this)
                .getDataController(nonSignedInAccount)
                .getLastUsedUnarchivedExperiment(
                        new LoggingConsumer<Experiment>(
                                TAG, "getting last used experiment to force database upgrade") {
                            @Override
                            public void success(Experiment experiment) {
                                // If the activity has already been dismissed, don't bother with the fragment.
                                if (isDestroyed()) {
                                    return;
                                }
                                btnStep1.setVisibility(View.VISIBLE);
                            }
                        });
    }

    private void onStep2() {
        if (PermissionUtils.hasPermission(this, PermissionUtils.REQUEST_ACCESS_FINE_LOCATION)) {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                onStep3();
                return;
            }
        }
        mSteps[mCurrentStep].setVisibility(View.GONE);
        mSteps[mCurrentStep = 1].setVisibility(View.VISIBLE);
    }

    private void onStep2_LocationPermission() {
        if (!PermissionUtils.hasPermission(this, PermissionUtils.REQUEST_ACCESS_FINE_LOCATION)) {
            PermissionUtils.tryRequestingPermission(this, PermissionUtils.REQUEST_ACCESS_FINE_LOCATION, new PermissionUtils.PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    onStep2_BluetoothEnabled();
                }

                @Override
                public void onPermissionDenied() {
                    onStep2_BluetoothEnabled();
                }

                @Override
                public void onPermissionPermanentlyDenied() {
                    onStep2_BluetoothEnabled();
                }
            });
            return;
        }
        onStep2_BluetoothEnabled();
    }

    private void onStep2_BluetoothEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
            return;
        }
        onStep3();
    }

    private void onStep3() {
        mSteps[mCurrentStep].setVisibility(View.GONE);
        mSteps[mCurrentStep = 2].setVisibility(View.VISIBLE);
    }

    private void onCompleted() {
        GetStartedActivity.setShouldLaunch(this, false);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            onStep3();
        }
    }

    private static final int REQUEST_ENABLE_BLUETOOTH = 100;

}
