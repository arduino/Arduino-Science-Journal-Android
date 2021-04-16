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

package com.google.android.apps.forscience.whistlepunk.project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.apps.forscience.whistlepunk.AppSingleton;
import com.google.android.apps.forscience.whistlepunk.PermissionUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.RxEvent;
import com.google.android.apps.forscience.whistlepunk.WhistlePunkApplication;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;

/**
 * Activity that allows a signed-in user to claim experiments that were created before signed-in
 * accounts were supported.
 */
public class ClaimExperimentsActivity extends AppCompatActivity {
    private static final String FRAGMENT_TAG = "ClaimExperiments";
    private static final String ARG_ACCOUNT_KEY = "accountKey";
    private static final String ARG_USE_PANES = "usePanes";

    private boolean mFirstStartDone = false;

    private AlertDialog mInfoDialog;

    /**
     * Receives an event every time the activity pauses
     */
    private final RxEvent pause = new RxEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_claim_experiments);
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        if (!isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        findViewById(R.id.iv_back_action).setOnClickListener(v -> finish());

        AppAccount claimingAccount =
                WhistlePunkApplication.getAccount(this, getIntent(), ARG_ACCOUNT_KEY);

        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            Fragment fragment =
                    ExperimentListFragment.newInstanceForClaimExperimentsMode(
                            this, claimingAccount, getIntent().getBooleanExtra(ARG_USE_PANES, true));
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, fragment, FRAGMENT_TAG)
                    .commit();
        }

        final AlertDialog.Builder d = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        final View infoView = getLayoutInflater().inflate(R.layout.claim_experiments_info, null);
        infoView.findViewById(R.id.claim_all_btn).setOnClickListener(v -> mInfoDialog.dismiss());
        ((TextView) infoView.findViewById(R.id.info_1)).setText(Html.fromHtml(getString(R.string.claim_experiments_info_1, claimingAccount.getAccountName())));
        ((TextView) infoView.findViewById(R.id.info_3)).setText(Html.fromHtml(getString(R.string.claim_experiments_info_3, claimingAccount.getAccountName())));
        d.setView(infoView);
        d.setCancelable(false);
        mInfoDialog = d.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppSingleton appSingleton = AppSingleton.getInstance(this);
        appSingleton.setResumedActivity(this);
        pause.happensNext().subscribe(() -> appSingleton.setNoLongerResumedActivity(this));

        if (!mFirstStartDone) {
            mFirstStartDone = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                mInfoDialog.show();
            }, 1000L);
        }
    }

    @Override
    protected void onPause() {
        pause.onHappened();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof ExperimentListFragment
                && ((ExperimentListFragment) fragment).handleOnBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    static void launch(Context context, AppAccount appAccount, boolean usePanes) {
        Intent intent = new Intent(context, ClaimExperimentsActivity.class);
        intent.putExtra(ARG_ACCOUNT_KEY, appAccount.getAccountKey());
        intent.putExtra(ARG_USE_PANES, usePanes);
        context.startActivity(intent);
    }
}
