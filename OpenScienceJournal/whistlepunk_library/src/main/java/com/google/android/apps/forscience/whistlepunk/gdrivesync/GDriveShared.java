package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class GDriveShared {

    public static void saveCredentials(final Context context, final String accountId, final String email, final String token, final String folderId, final String folderPath) {
        final SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
        prefs.putString(KEY_ACCOUNT_ID, accountId);
        prefs.putString(KEY_EMAIL, email);
        prefs.putString(KEY_TOKEN, token);
        prefs.putString(KEY_SYNC_FOLDER_ID, folderId);
        prefs.putString(KEY_SYNC_FOLDER_PATH, folderPath);
        prefs.apply();
    }

    public static void clearCredentials(final Context context) {
        final SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
        prefs.remove(KEY_ACCOUNT_ID);
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_SYNC_FOLDER_ID);
        prefs.remove(KEY_SYNC_FOLDER_PATH);
        prefs.apply();
    }

    public static GDriveAccount getCredentials(final Context context) {
        final SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(KEY_ACCOUNT_ID)) {
            final GDriveAccount account = new GDriveAccount();
            account.accountId = prefs.getString(KEY_ACCOUNT_ID, "");
            account.email = prefs.getString(KEY_EMAIL, "");
            account.token = prefs.getString(KEY_TOKEN, "");
            account.folderId = prefs.getString(KEY_SYNC_FOLDER_ID, "");
            account.folderPath = prefs.getString(KEY_SYNC_FOLDER_PATH, "");
            return account;
        } else {
            return null;
        }
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        try {
            final String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PREFS_NAME = "GoogleDriveSync";

    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_SYNC_FOLDER_ID = "folder_id";
    private static final String KEY_SYNC_FOLDER_PATH = "folder_path";

}
