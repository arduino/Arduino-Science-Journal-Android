package com.google.android.apps.forscience.whistlepunk.signin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.auth0.Auth0MFATokenCall;
import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

public class SignInMFAFragment extends AuthBaseFragment {

    private EditText mOtpEdit;
    private View mError;
    private View mNextButton;
    private String mMfaToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_in_mfa, container, false);
        mOtpEdit = view.findViewById(R.id.et_otp);
        mNextButton = view.findViewById(R.id.btn_next);
        mOtpEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isDataCompleted()) {
                    onCompleted();
                    return true;
                }
            }
            return false;
        });
        mNextButton.setOnClickListener(v -> onCompleted());
        mOtpEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNextButton.setEnabled(isDataCompleted());
            }
        });
        mError = view.findViewById(R.id.tv_error);
        return view;
    }

    @Override
    public void onFirstStart() {
        super.onFirstStart();
        mMfaToken = getArguments().getString("mfa", "");
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    private void onCompleted() {
        final String otp = getInputOtp();
        if (StringUtils.isEmpty(otp)) {
            return;
        }
        mError.setVisibility(View.INVISIBLE);
        setBlockUI(true);
        hideSoftKeyboard();
        new Auth0MFATokenCall(mContext, mMfaToken, otp).execute(new Callback<Auth0Token, Exception>() {
            @Override
            public void onResponse(Auth0Token token) {
                setBlockUI(false);
                if (token == null) {
                    mError.setVisibility(View.VISIBLE);
                } else {
                    notifyAuthCompleted(token);
                }
            }

            @Override
            public void onFailure(Exception failure) {
                setBlockUI(false);
                toast(R.string.error_generic);
            }

            @Override
            public boolean onNetworkError() {
                setBlockUI(false);
                toast(R.string.error_network);
                return true;
            }
        });
    }

    private String getInputOtp() {
        return mOtpEdit.getText().toString();
    }

    private boolean isOtpValid() {
        return !StringUtils.isEmpty(getInputOtp());
    }

    private boolean isDataCompleted() {
        return isOtpValid();
    }

}
