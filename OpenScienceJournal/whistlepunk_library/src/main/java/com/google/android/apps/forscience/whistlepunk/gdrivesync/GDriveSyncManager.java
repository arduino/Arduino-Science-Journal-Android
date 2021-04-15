package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.forscience.whistlepunk.AppSingleton;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.cloudsync.CloudSyncManager;

import java.io.IOException;

public class GDriveSyncManager implements CloudSyncManager {

    private static final String LOG_TAG = "GDriveSyncManager";

    private final AppAccount appAccount;

    public GDriveSyncManager(final AppAccount appAccount) {
        this.appAccount = appAccount;
    }

    @Override
    public void syncExperimentLibrary(Context context, String logMessage) throws IOException {
        Log.i(LOG_TAG, "syncExperimentLibrary: " + logMessage);
        if (!appAccount.isSignedIn() || appAccount.isMinor()) {
            return;
        }
        GDriveAccount gda = GDriveShared.getCredentials(context);
        if (gda == null) {
            return;
        }
        if (isCurrentlySyncing(context)) {
            return;
        }
        if (GDriveSyncService.syncExperimentLibrary(context, appAccount)) {
            AppSingleton.getInstance(context).setSyncServiceBusy(true);
        }
    }

    @Override
    public void logCloudInfo(String tag) {
    }

    private Boolean isCurrentlySyncing(Context context) {
        return AppSingleton.getInstance(context)
                .whenSyncBusyChanges()
                .blockingMostRecent(false)
                .iterator()
                .next();
    }

}
