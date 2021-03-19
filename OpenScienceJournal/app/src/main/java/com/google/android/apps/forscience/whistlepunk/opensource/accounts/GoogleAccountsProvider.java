package com.google.android.apps.forscience.whistlepunk.opensource.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.apps.forscience.whistlepunk.ActivityWithNavigationView;
import com.google.android.apps.forscience.whistlepunk.accounts.AbstractAccountsProvider;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.accounts.NonSignedInAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveShared;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * An accounts provider which is backed by Google Accounts.
 */
@SuppressWarnings("RedundantIfStatement")
public final class GoogleAccountsProvider extends AbstractAccountsProvider {
    private static final String TAG = "GoogleAccountsProvider";

    GoogleSignInClient googleSignInClient;
    Context context;

    public GoogleAccountsProvider(Context context) {
        super(context);
        this.context = context;
        setShowSignInActivityIfNotSignedIn(false);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
        GDriveAccount gda = null;
        try {
            gda = GDriveShared.getCredentials(applicationContext);
        } catch (Exception ignored) {
        }
        if (googleSignInAccount != null && gda != null) {
            signInCurrentAccount(googleSignInAccount);
        }
    }

    @Override
    protected void afterSetCurrentAccount(AppAccount currentAccount) {
        super.afterSetCurrentAccount(currentAccount);
    }

    @Override
    public boolean supportSignedInAccount() {
        return true;
    }

    @Override
    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        GDriveAccount gda = null;
        try {
            gda = GDriveShared.getCredentials(applicationContext);
        } catch (Exception ignored) {
        }
        return account != null && gda != null;
    }

    @Override
    public int getAccountCount() {
        return isSignedIn() ? 0 : 1;
    }

    @Override
    public void installAccountSwitcher(ActivityWithNavigationView activity) {

    }

    @Override
    public void disconnectAccountSwitcher(ActivityWithNavigationView activity) {
        return;
    }

    @Override
    public void onLoginAccountsChanged(Intent data) {
        try {
            //Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            //GoogleSignInAccount account = task.getResult(ApiException.class);
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
            signInCurrentAccount(googleSignInAccount);
        } catch (Exception e) {
            Log.e(TAG, "GoogleSignIn api exception", e);
        }
    }

    private void signInCurrentAccount(GoogleSignInAccount googleSignInAccount) {
        GoogleAccount googleAccount = new GoogleAccount(context, null, googleSignInAccount);
        GDriveAccount gda = null;
        try {
            gda = GDriveShared.getCredentials(applicationContext);
        } catch (Exception ignored) {
        }
        if (googleAccount == null || gda == null) {
            return;
        }

        AppAccount currentAccount = getCurrentAccount();
        boolean sameAsCurrentAccount = googleAccount.equals(currentAccount);
        if (sameAsCurrentAccount) {
            return;
        }

        setCurrentAccount(googleAccount);
    }

    @Override
    public void showAddAccountDialog(Activity activity) {
        return;
    }

    @Override
    public void showAccountSwitcherDialog(Fragment fragment, int requestCode) {
        if (getCurrentAccount() instanceof GoogleAccount) {
            googleSignInClient.signOut();
            setCurrentAccount(NonSignedInAccount.getInstance(context));
            return;
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        fragment.getActivity().startActivityForResult(signInIntent, requestCode);
    }
}
