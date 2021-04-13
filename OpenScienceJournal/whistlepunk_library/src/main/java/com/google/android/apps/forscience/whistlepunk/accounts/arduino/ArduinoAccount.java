package com.google.android.apps.forscience.whistlepunk.accounts.arduino;

import android.content.Context;

import com.auth0.android.jwt.JWT;
import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.whistlepunk.accounts.AbstractAccount;
import com.google.android.apps.forscience.whistlepunk.accounts.AccountsUtils;

import java.io.File;
import java.util.Objects;

public class ArduinoAccount extends AbstractAccount {

    public static final String NAMESPACE = "ArduinoAuth0";

    private static final String LOG_TAG = "ArduinoAccount";

    private final String accessToken;

    private final String refreshToken;

    private final String id;

    private final String nickname;

    private final String email;

    private final String picture;

    private final Boolean isMinor;

    private final String accountKey;

    public ArduinoAccount(final Context context, final Auth0Token token) {
        super(context);
        assert token != null;
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
        final JWT jwt = new JWT(token.getIdToken());
        this.id = jwt.getClaim("http://arduino.cc/id").asString();
        this.nickname = jwt.getClaim("nickname").asString();
        this.email = jwt.getClaim("email").asString();
        this.picture = jwt.getClaim("picture").asString();
        this.isMinor = jwt.getClaim("http://arduino.cc/is_minor").asBoolean();
        this.accountKey = AccountsUtils.makeAccountKey(NAMESPACE, this.id);
    }

    @Override
    public String getAccountName() {
        return nickname;
    }

    @Override
    public String getAccountAvatar() {
        return picture;
    }

    @Override
    public boolean isMinor() {
        return Boolean.TRUE.equals(isMinor);
    }

    @Override
    public String getAccountKey() {
        return accountKey;
    }

    @Override
    public boolean isSignedIn() {
        return true;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArduinoAccount that = (ArduinoAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getEmail() {
        return email;
    }

}
