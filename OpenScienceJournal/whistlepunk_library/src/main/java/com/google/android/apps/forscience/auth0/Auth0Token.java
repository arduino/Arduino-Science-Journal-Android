package com.google.android.apps.forscience.auth0;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Auth0Token implements Parcelable {

    String accessToken;

    String refreshToken;

    String idToken;

    String scope;

    long expiresAt;

    Auth0Token() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getScope() {
        return scope;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    private Auth0Token(Parcel in) {
        accessToken = in.readString();
        refreshToken = in.readString();
        idToken = in.readString();
        scope = in.readString();
        expiresAt = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(refreshToken);
        dest.writeString(idToken);
        dest.writeString(scope);
        dest.writeLong(expiresAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Auth0Token> CREATOR = new Creator<Auth0Token>() {
        @Override
        public Auth0Token createFromParcel(Parcel in) {
            return new Auth0Token(in);
        }

        @Override
        public Auth0Token[] newArray(int size) {
            return new Auth0Token[size];
        }
    };

    public String toJSON() {
        try {
            final JSONObject json = new JSONObject();
            json.put("accessToken", accessToken);
            json.put("refreshToken", refreshToken);
            json.put("idToken", idToken);
            json.put("scope", scope);
            json.put("expiresAt", expiresAt);
            return json.toString();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Auth0Token fromJSON(final String jsonString) {
        try {
            final JSONObject json = new JSONObject(jsonString);
            final Auth0Token token = new Auth0Token();
            token.accessToken = json.getString("accessToken");
            token.refreshToken = json.getString("refreshToken");
            token.idToken = json.getString("idToken");
            token.scope = json.getString("scope");
            token.expiresAt = json.getLong("expiresAt");
            return token;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
