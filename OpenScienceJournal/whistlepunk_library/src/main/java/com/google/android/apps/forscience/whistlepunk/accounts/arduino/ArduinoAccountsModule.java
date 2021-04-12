package com.google.android.apps.forscience.whistlepunk.accounts.arduino;

import android.content.Context;

import com.google.android.apps.forscience.whistlepunk.accounts.AccountsProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ArduinoAccountsModule {
    @Provides
    AccountsProvider provideAccountsProvider(Context context) {
        return new ArduinoAccountProvider(context);
    }
}
