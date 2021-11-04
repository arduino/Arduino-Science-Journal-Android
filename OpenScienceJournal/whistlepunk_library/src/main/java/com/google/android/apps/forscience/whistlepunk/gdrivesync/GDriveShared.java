package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.apps.forscience.whistlepunk.MainActivity;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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

    private static Boolean getKeystoreResetState(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(UNENCRYPTED_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_KEYSTORE_RESET, false);
    }
    private static void setKeystoreResetState(final Context context, Boolean state) {
        SharedPreferences prefs = context.getSharedPreferences(UNENCRYPTED_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_KEYSTORE_RESET, state).apply();
    }

    private static void resetKeystoreAndRestart(final Context context) {
        // delete shared preferences file
        File sharedPrefsFile = new File(context.getFilesDir().getParent() + "/shared_prefs/" + SHARED_PREFS_FILENAME + ".xml");
        if (sharedPrefsFile.exists()) {
            Boolean deleted = sharedPrefsFile.delete();
            Log.i(LOG_TAG, String.format("Shared prefs file \"%s\" deleted: %s", sharedPrefsFile.getAbsolutePath(), deleted));
        } else {
            Log.i(LOG_TAG, String.format("Shared prefs file \"%s\" non-existent", sharedPrefsFile.getAbsolutePath()));
        }

        // delete master key
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS);

            Log.i(LOG_TAG, "Master key deleted");
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.i(LOG_TAG, "Unable to delete master key");
            throw new RuntimeException("Unable to delete master key", e);
        }

        // save on (non-encrypted) shared preferences the reset state to avoid loops
        setKeystoreResetState(context, true);
        Log.i(LOG_TAG, "Set keystore reset state to TRUE.");

        // restart app
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.i(LOG_TAG, "Restarting application...");
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        MasterKey masterKey = null;
        try {
            // build MasterKey
            masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // get or create shared preferences
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context,
                    SHARED_PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // set keystore reset to false to enable future resets
            setKeystoreResetState(context, false);
            Log.i(LOG_TAG, "Set keystore reset state to FALSE.");

            return prefs;
        } catch (GeneralSecurityException | IOException e) {
            Boolean keystoreReset = getKeystoreResetState(context);
            if (keystoreReset) {
                // a keystore reset just occurred, interrupt execution to avoid loops
                throw new RuntimeException("Unable to retrieve encrypted shared preferences", e);
            } else {
                Log.i(LOG_TAG, "Unable to retrieve encrypted shared preferences, regenerating master key.", e);
                resetKeystoreAndRestart(context);
            }

            return null;
        }
    }


    private static final String SHARED_PREFS_FILENAME = "GoogleDriveSync";
    private static final String UNENCRYPTED_SHARED_PREFS_FILENAME = "ArduinoSharedPreferencesSafe";
    private static final String KEY_KEYSTORE_RESET = "keystore_reset";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_SYNC_FOLDER_ID = "folder_id";
    private static final String KEY_SYNC_FOLDER_PATH = "folder_path";

}
