package com.google.android.apps.forscience.whistlepunk.accounts.arduino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.whistlepunk.ActivityWithNavigationView;
import com.google.android.apps.forscience.whistlepunk.accounts.AbstractAccountsProvider;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

public class ArduinoAccountProvider extends AbstractAccountsProvider {

    private static final String LOG_TAG = "ArduinoAccountProvider";

    private ArduinoAccount arduinoAccount;

    public ArduinoAccountProvider(final Context context) {
        super(context);
        final SharedPreferences prefs = getSharedPreferences();
        final String jsonToken = prefs.getString("token", null);
        if (!StringUtils.isEmpty(jsonToken)) {
            try {
                final Auth0Token token = Auth0Token.fromJSON(jsonToken);
                arduinoAccount = new ArduinoAccount(context, token);
            } catch (Exception e) {
                Log.w(LOG_TAG, "Error decoding stored arduino account", e);
            }
        }
        if (arduinoAccount != null) {
            setCurrentAccount(arduinoAccount);
        }
    }

    @Override
    public boolean supportSignedInAccount() {
        return true;
    }

    @Override
    public boolean isSignedIn() {
        return arduinoAccount != null;
    }

    @Override
    public int getAccountCount() {
        return isSignedIn() ? 0 : 1;
    }

    @Override
    public void onLoginAccountsChanged(final Intent data) {
        final Auth0Token token = data.getParcelableExtra("token");
        if (token == null) {
            return;
        }
        final ArduinoAccount arduinoAccount = new ArduinoAccount(applicationContext, token);
        final AppAccount currentAccount = getCurrentAccount();
        if (arduinoAccount.equals(currentAccount)) {
            return;
        }
        setCurrentAccount(arduinoAccount);
        this.arduinoAccount = arduinoAccount;
        final SharedPreferences.Editor prefs = getSharedPreferences().edit();
        prefs.putString("token", token.toJSON());
        prefs.apply();
    }

    @Override
    public void undoSignIn() {
        final SharedPreferences.Editor prefs = getSharedPreferences().edit();
        prefs.remove("token");
        prefs.apply();
        arduinoAccount = null;
        super.undoSignIn();
    }

    @Override
    public void installAccountSwitcher(ActivityWithNavigationView activity) {
    }

    @Override
    public void disconnectAccountSwitcher(ActivityWithNavigationView activity) {
    }

    @Override
    public void showAddAccountDialog(Activity activity) {
    }

    @Override
    public void showAccountSwitcherDialog(Fragment fragment, int requestCode) {
    }

    private SharedPreferences getSharedPreferences() {
        try {
            final String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    "ArduinoAccountProvider",
                    masterKeyAlias,
                    applicationContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Unable to retrieve encrypted shared preferences", t);
            throw new RuntimeException("Unable to retrieve encrypted shared preferences", t);
        }
    }

}
