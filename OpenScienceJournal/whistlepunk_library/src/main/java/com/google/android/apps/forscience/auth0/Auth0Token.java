package com.google.android.apps.forscience.auth0;

import android.os.Parcel;
import android.os.Parcelable;

public class Auth0Token implements Parcelable {

    String accessToken;

    String refreshToken;

    String idToken;

    String scope;

    long expiresAt;

    Auth0Token() {
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

}
