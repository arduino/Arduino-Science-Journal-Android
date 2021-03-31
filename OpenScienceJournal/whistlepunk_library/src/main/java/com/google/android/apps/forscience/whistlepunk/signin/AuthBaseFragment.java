package com.google.android.apps.forscience.whistlepunk.signin;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.auth0.Auth0Token;

abstract class AuthBaseFragment extends BaseFragment {

    private Listener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressWarnings("SameParameterValue")
    protected final void startFragment(Class<? extends AuthBaseFragment> cls, Bundle args) {
        if (mListener != null) {
            mListener.onStartFragment(cls, args);
        }
    }

    protected final void setBlockUI(final boolean block) {
        if (mListener != null) {
            mListener.onBlockUI(block);
        }
    }

    protected final void setBackEnabled(final boolean enabled) {
        if (mListener != null) {
            mListener.onBackEnabled(enabled);
        }
    }

    protected final void goBack() {
        if (mListener != null) {
            mListener.onBack();
        }
    }

    protected final void goBegin() {
        if (mListener != null) {
            mListener.onBegin();
        }
    }

    protected final void notifyAuthCompleted(final Auth0Token token) {
        if (mListener != null) {
            mListener.onAuthCompleted(token);
        }
    }

    public interface Listener {

        void onStartFragment(Class<? extends AuthBaseFragment> cls, Bundle args);

        void onBlockUI(boolean block);

        void onBackEnabled(boolean enabled);

        void onBack();

        void onBegin();

        void onAuthCompleted(final Auth0Token token);

    }

}
