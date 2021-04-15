package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.cloudsync.CloudSyncManager;
import com.google.android.apps.forscience.whistlepunk.cloudsync.CloudSyncProvider;

public class GDriveSyncProvider implements CloudSyncProvider {

    public GDriveSyncProvider(final Context context) {
        // TODO
    }

    @Override
    public CloudSyncManager getServiceForAccount(AppAccount appAccount) {
        return new GDriveSyncManager(appAccount);
    }
}
