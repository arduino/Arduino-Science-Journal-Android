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

import com.google.android.apps.forscience.auth0.JuniorPasswordResetCall;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

public class PasswordResetJuniorStep1Fragment extends AuthBaseFragment {

    private EditText mUsernameEdit;
    private EditText mEmailEdit;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_password_reset_junior_1, container, false);
        mUsernameEdit = view.findViewById(R.id.et_username);
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
        mUsernameEdit.addTextChangedListener(new TextWatcher() {
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
        view.findViewById(R.id.iv_username_info).setOnClickListener(v -> alert(R.string.arduino_auth_password_reset_1_field_username_info));
        return view;
    }

    @Override
    public void onFirstStart() {
        super.onFirstStart();
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final String username = bundle.getString("username");
            if (!StringUtils.isEmpty(username)) {
                mUsernameEdit.setText(username);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    private void onCompleted() {
        final String username = getInputUsername();
        final String email = getInputEmail();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(email)) {
            return;
        }
        setBlockUI(true);
        hideSoftKeyboard();
        new JuniorPasswordResetCall(mContext, email, username).execute(new Callback<Boolean, Exception>() {
            @Override
            public void onResponse(Boolean response) {
                setBlockUI(false);
                if (Boolean.TRUE.equals(response)) {
                    startFragment(PasswordResetJuniorCompletedFragment.class, getArguments());
                } else {
                    alert(R.string.arduino_auth_password_reset_1_not_found);
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

    private String getInputUsername() {
        return mUsernameEdit.getText().toString().trim();
    }

    private String getInputEmail() {
        return mEmailEdit.getText().toString().trim().toLowerCase();
    }

    private boolean isUsernameValid() {
        return Commons.isUsernameValid(getInputUsername());
    }

    private boolean isEmailValid() {
        final String email = getInputEmail();
        return Commons.isEmailValid(email) || Commons.isEmailValid(email);
    }

    private boolean isDataCompleted() {
        return isUsernameValid() && isEmailValid();
    }

}
