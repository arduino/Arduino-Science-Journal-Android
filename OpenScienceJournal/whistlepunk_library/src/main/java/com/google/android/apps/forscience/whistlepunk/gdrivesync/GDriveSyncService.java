package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.apps.forscience.javalib.MaybeConsumer;
import com.google.android.apps.forscience.javalib.Success;
import com.google.android.apps.forscience.whistlepunk.AppSingleton;
import com.google.android.apps.forscience.whistlepunk.DataController;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.RxDataController;
import com.google.android.apps.forscience.whistlepunk.WhistlePunkApplication;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.filemetadata.Experiment;
import com.google.android.apps.forscience.whistlepunk.filemetadata.ExperimentLibraryManager;
import com.google.android.apps.forscience.whistlepunk.filemetadata.FileMetadataUtil;
import com.google.android.apps.forscience.whistlepunk.filemetadata.LocalSyncManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GDriveSyncService extends Service {

    private static final String LOG_TAG = "GDriveSyncService";

    private volatile ServiceHandler mServiceHandler;

    public static boolean syncExperimentLibrary(final Context context, final AppAccount appAccount) {
        if (!canSync(context, appAccount)) {
            return false;
        }
        if (AppSingleton.getInstance(context)
                .getRecorderController(appAccount)
                .watchRecordingStatus()
                .blockingFirst()
                .isRecording()) {
            return false;
        }
        final Intent intent = new Intent(context, GDriveSyncService.class);
        intent.putExtra("account_key", appAccount.getAccountKey());
        return startService(context, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final HandlerThread thread = new HandlerThread("GDriveSyncService");
        thread.start();
        final Looper looper = thread.getLooper();
        mServiceHandler = new ServiceHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_NOT_STICKY;
    }

    private void onHandleIntent(Intent intent) {
        final Context context = getApplicationContext();
        final AppAccount appAccount = WhistlePunkApplication.getAccount(context, intent, "account_key");
        final AppSingleton appSingleton = AppSingleton.getInstance(context);
        final ExperimentLibraryManager elm = appSingleton.getExperimentLibraryManager(appAccount);
        final LocalSyncManager lsm = appSingleton.getLocalSyncManager(appAccount);
        final GDriveAccount gda = GDriveShared.getCredentials(this);
        if (gda == null) {
            AppSingleton.getInstance(getApplicationContext()).setSyncServiceBusy(false);
            return;
        }
        try {
            final List<String> syncedLocalExperimentIds = new ArrayList<>();
            final Set<String> localExperimentIds = elm.getKnownExperiments();
            final GDriveApi api = new GDriveApi(this, gda.email, gda.folderId);
            final List<GDriveApi.RemoteExperiment> remoteExperiments = api.listRemoteExperiments();
            for (final GDriveApi.RemoteExperiment remote : remoteExperiments) {
                Log.i(LOG_TAG, "Remote Experiment detected: " + remote);
                final String remoteExpId = remote.experimentId;
                boolean found = false;
                boolean remoteIsNewer = false;
                boolean localIsNewer = false;
                boolean localIsDeleted = false;
                boolean conflict = false;
                long localVersion = -1;
                for (final String localExpId : localExperimentIds) {
                    if (localExpId.equals(remoteExpId) && !elm.isDeleted(localExpId)) {
                        found = true;
                        localVersion = lsm.getLastSyncedVersion(localExpId);
                        if (localVersion < 1) {
                            localVersion = 1;
                        }
                        if (elm.isDeleted(localExpId)) {
                            localIsDeleted = true;
                        } else {
                            final boolean localDirty = lsm.getDirty(localExpId);
                            if (localVersion < remote.version) {
                                if (localDirty) {
                                    conflict = true;
                                } else {
                                    remoteIsNewer = true;
                                }
                            } else if (localVersion > remote.version || localDirty) {
                                localIsNewer = true;
                            }
                        }
                        syncedLocalExperimentIds.add(localExpId);
                        break;
                    }
                }
                if (found) {
                    if (localIsDeleted) {
                        if (false) {
                            Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " found on local library in deleted state. Deleting on remote as well.");
                            deleteRemoteExperiment(api, remote);
                            Log.i(LOG_TAG, "Remote experiment with file id " + remote.file.getId() + " deleted.");
                        }
                    } else if (remoteIsNewer) {
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " found on local library. Remote is newer.");
                        importRemoteExperiment(context, appAccount, api, remote);
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " imported.");
                    } else if (localIsNewer) {
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " found on local library. Local is newer.");
                        exportLocalExperiment(context, appAccount, remoteExpId, api, remote, localVersion + 1);
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " updated from local version");
                    } else if (conflict) {
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " found on local library. Conflict detected.");
                        final ConflictAnswer answer = showConflict(context, appAccount, remoteExpId);
                        switch (answer) {
                            case MINE: {
                                Log.i(LOG_TAG, "User answer for experiment " + remoteExpId + " conflict: overwrite remote. Exporting.");
                                exportLocalExperiment(context, appAccount, remoteExpId, api, remote, remote.version + 1);
                                Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " updated from local version");
                                break;
                            }
                            case THEIR: {
                                Log.i(LOG_TAG, "User answer for experiment " + remoteExpId + " conflict: overwrite local. Importing.");
                                importRemoteExperiment(context, appAccount, api, remote);
                                Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " imported.");
                                break;
                            }
                            default: {
                                Log.i(LOG_TAG, "User answer for experiment " + remoteExpId + " conflict: skip.");
                                break;
                            }
                        }
                    } else {
                        Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " found on local library. Already in sync.");
                    }
                } else {
                    Log.i(LOG_TAG, "Remote experiment " + remoteExpId + " not found on local library. Importing it.");
                    importRemoteExperiment(context, appAccount, api, remote);
                    Log.i(LOG_TAG, "Remote experiment id " + remoteExpId + " imported");
                }
            }
            for (final String localId : localExperimentIds) {
                if (!syncedLocalExperimentIds.contains(localId) && !elm.isDeleted(localId)) {
                    final String remoteFileId = elm.getFileId(localId);
                    if (remoteFileId == null) {
                        Log.i(LOG_TAG, "Local experiment with id " + localId + " is new and needs to be uploaded.");
                        final GDriveApi.RemoteExperiment remote = new GDriveApi.RemoteExperiment();
                        remote.experimentId = localId;
                        remote.archived = elm.isArchived(localId);
                        exportLocalExperiment(context, appAccount, localId, api, remote, 1);
                        Log.i(LOG_TAG, "Local experiment with id " + localId + " has been uploaded to remote experiment with file id: " + remote.file.getId());
                    } else {
                        Log.i(LOG_TAG, "Local experiment with id " + localId + " is linked to remote file id " + remoteFileId + " which is not found. The local experiment will be deleted.");
                        deleteLocalExperiment(context, appAccount, localId);
                        Log.i(LOG_TAG, "Local experiment with id " + localId + " deleted.");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "SYNC FAILED", e);
            showToast(R.string.sync_failed);
        }
        AppSingleton.getInstance(getApplicationContext()).notifyNewExperimentSynced();
        AppSingleton.getInstance(getApplicationContext()).setSyncServiceBusy(false);
    }

    private void importRemoteExperiment(final Context context, final AppAccount appAccount, final GDriveApi api, GDriveApi.RemoteExperiment exp) throws Exception {
        final File tmpFile = api.downloadFile(exp);
        try {
            final Object[] lock = {Boolean.FALSE, null};
            final DataController dc = AppSingleton.getInstance(context).getDataController(appAccount);
            dc.importExperimentFromZip(Uri.fromFile(tmpFile), context.getContentResolver(), exp.experimentId, exp.archived, new MaybeConsumer<String>() {
                @Override
                public void success(String value) {
                    synchronized (lock) {
                        lock[0] = Boolean.TRUE;
                        lock[1] = value;
                        lock.notifyAll();
                    }
                }

                @Override
                public void fail(Exception e) {
                    synchronized (lock) {
                        lock[0] = Boolean.TRUE;
                        lock[1] = e;
                        lock.notifyAll();
                    }
                }
            });
            final Object result;
            synchronized (lock) {
                if (Boolean.FALSE.equals(lock[0])) {
                    lock.wait();
                }
                result = lock[1];
            }
            if (result instanceof String) {
                final AppSingleton appSingleton = AppSingleton.getInstance(context);
                final ExperimentLibraryManager elm = appSingleton.getExperimentLibraryManager(appAccount);
                elm.update(exp.experimentId, exp.file.getId(), exp.file.getModifiedDate().getValue(), exp.archived, false);
                final LocalSyncManager lsm = appSingleton.getLocalSyncManager(appAccount);
                lsm.update(exp.experimentId, exp.version, false);
                AppSingleton.getInstance(getApplicationContext()).notifyNewExperimentSynced();
            } else if (result instanceof Exception) {
                throw (Exception) result;
            } else {
                throw new Exception("Unexpected import experiment result: " + result);
            }
        } finally {
            if (tmpFile != null) {
                //noinspection ResultOfMethodCallIgnored
                tmpFile.delete();
            }
        }
    }

    private void deleteRemoteExperiment(final GDriveApi api, GDriveApi.RemoteExperiment exp) throws Exception {
        api.deleteFile(exp);
    }

    private void exportLocalExperiment(final Context context, final AppAccount appAccount, final String localExpId, final GDriveApi api, final GDriveApi.RemoteExperiment remoteExperiment, final long version) throws IOException {
        final AppSingleton appSingleton = AppSingleton.getInstance(context);
        final ExperimentLibraryManager elm = appSingleton.getExperimentLibraryManager(appAccount);
        final LocalSyncManager lsm = appSingleton.getLocalSyncManager(appAccount);
        final DataController dc = AppSingleton.getInstance(context).getDataController(appAccount);
        final Experiment experiment = RxDataController.getExperimentById(dc, localExpId).blockingGet();
        experiment.cleanTrials(this, appAccount);
        remoteExperiment.version = version;
        remoteExperiment.archived = elm.isArchived(localExpId);
        final File tmpFile = FileMetadataUtil.getInstance()
                .getFileForExport(getApplicationContext(), appAccount, experiment, dc)
                .blockingGet();
        try {
            if (remoteExperiment.file == null) {
                api.insertFile(tmpFile, remoteExperiment, experiment.getTitle());
            } else {
                api.updateFile(tmpFile, remoteExperiment, experiment.getTitle());
            }
        } finally {
            //noinspection ResultOfMethodCallIgnored
            tmpFile.delete();
        }
        elm.setFileId(localExpId, remoteExperiment.file.getId());
        lsm.update(localExpId, remoteExperiment.version, false);
    }

    private void deleteLocalExperiment(final Context context, final AppAccount appAccount, final String experimentId) throws Exception {
        final Object[] lock = {Boolean.FALSE, null};
        final AppSingleton appSingleton = AppSingleton.getInstance(context);
        final ExperimentLibraryManager elm = appSingleton.getExperimentLibraryManager(appAccount);
        final LocalSyncManager lsm = appSingleton.getLocalSyncManager(appAccount);
        elm.setDeleted(experimentId, true);
        elm.setFileId(experimentId, null);
        lsm.setDirty(experimentId, false);
        final DataController dc = AppSingleton.getInstance(context).getDataController(appAccount);
        dc.deleteExperiment(experimentId, new MaybeConsumer<Success>() {
            @Override
            public void success(Success value) {
                synchronized (lock) {
                    lock[0] = Boolean.TRUE;
                    lock[1] = value;
                    lock.notifyAll();
                }
            }

            @Override
            public void fail(Exception e) {
                synchronized (lock) {
                    lock[0] = Boolean.TRUE;
                    lock[1] = e;
                    lock.notifyAll();
                }
            }
        });
        final Object result;
        synchronized (lock) {
            if (Boolean.FALSE.equals(lock[0])) {
                lock.wait();
            }
            result = lock[1];
        }
        if (result instanceof Exception) {
            throw (Exception) result;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
        }
    }

    private static boolean canSync(final Context context, final AppAccount appAccount) {
        return appAccount != null && appAccount.isSignedIn() && !appAccount.isMinor() && GDriveShared.getCredentials(context) != null;
    }

    private static boolean startService(Context context, Intent intent) {
        try {
            context.getApplicationContext().startService(intent);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void showToast(int stringRes) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context baseContext = getBaseContext();
            if (baseContext != null) {
                Resources resources = baseContext.getResources();
                Toast toast = Toast.makeText(baseContext, stringRes, Toast.LENGTH_LONG);
                View view = toast.getView();
                TextView textView = view.findViewById(android.R.id.message);
                textView.setTextColor(resources.getColor(R.color.snackbar_text_color));
                view.getBackground().setColorFilter(resources.getColor(R.color.snackbar_background_color), PorterDuff.Mode.SRC_IN);
                toast.show();
            }
        });
    }

    private ConflictAnswer showConflict(final Context context, final AppAccount appAccount, final String expId) throws Exception {
        AppSingleton app = AppSingleton.getInstance(context);
        final DataController dc = app.getDataController(appAccount);
        final Experiment experiment = RxDataController.getExperimentById(dc, expId).blockingGet();
        if (experiment == null) {
            return ConflictAnswer.SKIP;
        }
        final Object[] lock = new Object[]{null};
        new Handler(Looper.getMainLooper()).post(() -> {
            final Activity activity = app.onNextActivity().blockingGet();
            if (activity != null) {
                AlertDialog.Builder d = new AlertDialog.Builder(activity);
                d.setMessage(R.string.drive_sync_conflict);
                d.setNeutralButton(R.string.drive_sync_conflict_remote, (dialog, which) -> {
                    synchronized (lock) {
                        lock[0] = 0;
                        lock.notifyAll();
                    }
                });
                d.setPositiveButton(R.string.drive_sync_conflict_local, (dialog, which) -> {
                    synchronized (lock) {
                        lock[0] = 1;
                        lock.notifyAll();
                    }
                });
                d.setOnCancelListener(dialog -> {
                    synchronized (lock) {
                        lock[0] = -1;
                        lock.notifyAll();
                    }
                });
                d.setCancelable(true);
                try {
                    d.show();
                    return;
                } catch (Exception ignored) {
                }
            }
            synchronized (lock) {
                lock[0] = -1;
                lock.notifyAll();
            }
        });
        synchronized (lock) {
            if (lock[0] == null) {
                lock.wait();
            }
            if (lock[0] instanceof Integer) {
                int result = (Integer) lock[0];
                if (result == 0) {
                    return ConflictAnswer.THEIR;
                } else if (result == 1) {
                    return ConflictAnswer.MINE;
                }
            }
        }
        return ConflictAnswer.SKIP;
    }

    private enum ConflictAnswer {
        MINE, THEIR, SKIP
    }

}