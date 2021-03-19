package com.google.android.apps.forscience.whistlepunk.opensource.accounts;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.apps.forscience.whistlepunk.accounts.AbstractAccount;
import com.google.android.apps.forscience.whistlepunk.accounts.AccountsUtils;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveAccount;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.GDriveShared;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;

import io.reactivex.subjects.BehaviorSubject;

/**
 * An implementation of {@link com.google.android.apps.forscience.whistlepunk.accounts.AppAccount} backed by a Google Account.
 */
public class GoogleAccount extends AbstractAccount {
    public static final String NAMESPACE = "google3p";

    private final BehaviorSubject<Account> accountSubject;
    private final GoogleSignInAccount googleSignInAccount;
    private final String accountKey;
    private final Account account;

    /**
     * Constructs a new GoogleAccount instance.
     */

    GoogleAccount(Context context, @Nullable Account account, GoogleSignInAccount googleSignInAccount) {
        super(context);
        accountSubject =
                (account != null) ? BehaviorSubject.createDefault(account) : BehaviorSubject.create();
        this.googleSignInAccount = googleSignInAccount;
        this.account = googleSignInAccount.getAccount();
        setAccount(account);
        this.accountKey = AccountsUtils.makeAccountKey(NAMESPACE, this.googleSignInAccount.getId());
    }

    @Nullable
    @Override
    public Account getAccount() {
        return account;
    }

    void setAccount(Account account) {
        accountSubject.onNext(account);
    }

    @Override
    public String getAccountName() {
        return googleSignInAccount.getEmail();
    }

    @Override
    public String getAccountKey() {
        return accountKey;
    }

    @Override
    public boolean isSignedIn() {
        GDriveAccount gda = null;
        try {
            gda = GDriveShared.getCredentials(applicationContext);
        } catch (Exception ignored) {
        }
        return gda != null;
    }

    @Override
    public File getFilesDir() {
        return AccountsUtils.getFilesDir(accountKey, applicationContext);
    }

    @Override
    public String getDatabaseFileName(String name) {
        return AccountsUtils.getDatabaseFileName(accountKey, name);
    }

    @Override
    public String getSharedPreferencesName() {
        return AccountsUtils.getSharedPreferencesName(accountKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GoogleAccount)) {
            return false;
        }
        return googleSignInAccount == ((GoogleAccount) o).googleSignInAccount;
    }

    @Override
    public int hashCode() {
        return googleSignInAccount.hashCode();
    }
}
