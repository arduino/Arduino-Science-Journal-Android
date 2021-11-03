package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class GDriveShared {
    private static final String LOG_TAG = "GDriveShared";

    private static boolean mAccountLoaded = false;

    private static GDriveAccount mAccount = null;

    public static void saveCredentials(final Context context, final String accountId, final String email, final String token, final String folderId, final String folderPath) {
        mAccountLoaded = true;
        mAccount = new GDriveAccount(accountId, email, token, folderId, folderPath);
        final SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
        prefs.putString(KEY_ACCOUNT_ID, accountId);
        prefs.putString(KEY_EMAIL, email);
        prefs.putString(KEY_TOKEN, token);
        prefs.putString(KEY_SYNC_FOLDER_ID, folderId);
        prefs.putString(KEY_SYNC_FOLDER_PATH, folderPath);
        prefs.apply();
    }

    public static void clearCredentials(final Context context) {
        mAccountLoaded = true;
        mAccount = null;
        final SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
        prefs.remove(KEY_ACCOUNT_ID);
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_SYNC_FOLDER_ID);
        prefs.remove(KEY_SYNC_FOLDER_PATH);
        prefs.apply();
    }

    public static GDriveAccount getCredentials(final Context context) {
        if (!mAccountLoaded) {
            final SharedPreferences prefs = getSharedPreferences(context);
            if (prefs.contains(KEY_ACCOUNT_ID)) {
                mAccount = new GDriveAccount();
                mAccount.accountId = prefs.getString(KEY_ACCOUNT_ID, "");
                mAccount.email = prefs.getString(KEY_EMAIL, "");
                mAccount.token = prefs.getString(KEY_TOKEN, "");
                mAccount.folderId = prefs.getString(KEY_SYNC_FOLDER_ID, "");
                mAccount.folderPath = prefs.getString(KEY_SYNC_FOLDER_PATH, "");
            } else {
                return null;
            }
            mAccountLoaded = true;
        }
        return mAccount;
    }

    private static SharedPreferences getBrandNewSharedPreferences(Context context) {
        MasterKey masterKey = null;

        try {
            File sharedPrefsFile = new File(context.getFilesDir().getParent() + "/shared_prefs/" + SHARED_PREFS_FILENAME + ".xml");
            boolean deleted = sharedPrefsFile.delete();

            Log.d(LOG_TAG, String.format("Shared prefs file deleted: %s", deleted));

            // delete MasterKey
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS);

            // build MasterKey
            masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // create shared preferences
            return EncryptedSharedPreferences.create(
                    context,
                    SHARED_PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Unable to retrieve encrypted shared preferences", e);
            throw new RuntimeException("Unable to retrieve encrypted shared preferences", e);
        }
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        MasterKey masterKey = null;
        try {
            // build MasterKey
            masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // get or create shared preferences
            return EncryptedSharedPreferences.create(
                    context,
                    SHARED_PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Unable to retrieve encrypted shared preferences, regenerating master key.", e);
            return getBrandNewSharedPreferences(context);
        }
    }

    private static final String SHARED_PREFS_FILENAME = "GoogleDriveSync";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_SYNC_FOLDER_ID = "folder_id";
    private static final String KEY_SYNC_FOLDER_PATH = "folder_path";

}
