package com.google.android.apps.forscience.whistlepunk.opensource.modules;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.cloudsync.CloudSyncProvider;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveSyncProvider;

import dagger.Module;
import dagger.Provides;

/** Creates a cloud sync provider which is backed by Google Drive. */
@Module
public class GoogleDriveSyncModule {
  @Provides
  CloudSyncProvider provideCloudSyncProvider(Context context) {
    return new GDriveSyncProvider(context);
  }
}
