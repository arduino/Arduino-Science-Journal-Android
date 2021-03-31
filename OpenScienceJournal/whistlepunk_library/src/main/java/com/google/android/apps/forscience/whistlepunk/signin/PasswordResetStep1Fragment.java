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

import com.google.android.apps.forscience.auth0.Auth0PasswordResetCall;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

public class PasswordResetStep1Fragment extends AuthBaseFragment {

    private EditText mEmailEdit;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_password_reset_1, container, false);
        mEmailEdit = view.findViewById(R.id.et_email);
        mNextButton = view.findViewById(R.id.btn_next);
        mEmailEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isDataCompleted()) {
                    onCompleted();
                    return true;
                }
            }
            return false;
        });
        mNextButton.setOnClickListener(v -> onCompleted());
        mEmailEdit.addTextChangedListener(new TextWatcher() {
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
        return view;
    }

    @Override
    public void onFirstStart() {
        super.onFirstStart();
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final String email = bundle.getString("email");
            if (!StringUtils.isEmpty(email)) {
                mEmailEdit.setText(email);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    private void onCompleted() {
        final String email = getInputEmail();
        if (StringUtils.isEmpty(email)) {
            return;
        }
        setBlockUI(true);
        hideSoftKeyboard();
        new Auth0PasswordResetCall(mContext, email).execute(new Callback<Boolean, Exception>() {
            @Override
            public void onResponse(Boolean response) {
                setBlockUI(false);
                startFragment(PasswordResetCompletedFragment.class, null);
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

    private String getInputEmail() {
        return mEmailEdit.getText().toString().trim();
    }

    private boolean isEmailValid() {
        final String email = getInputEmail();
        return Commons.isEmailValid(email) || Commons.isEmailValid(email);
    }

    private boolean isDataCompleted() {
        return isEmailValid();
    }

}
