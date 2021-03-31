package com.google.android.apps.forscience.whistlepunk.signin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.auth0.Auth0SignUpCall;
import com.google.android.apps.forscience.auth0.Auth0SignUpTeenCall;
import com.google.android.apps.forscience.auth0.JuniorSignUpCall;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;

public class SignUpJuniorStep2Fragment extends AuthBaseFragment {

    private static final String LOG_TAG = "ArduinoAuth";

    private EditText mEmailEdit;
    private EditText mEmailConfirmEdit;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_junior_step_2, container, false);
        mEmailEdit = view.findViewById(R.id.et_email);
        mEmailConfirmEdit = view.findViewById(R.id.et_email_confirm);
        mNextButton = view.findViewById(R.id.btn_next);
        mEmailConfirmEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isDataCompleted()) {
                    onCompleted();
                    return true;
                }
            }
            return false;
        });
        mNextButton.setOnClickListener(v -> onCompleted());
        final TextView tvEmailError = view.findViewById(R.id.tv_error_email);
        final TextView tvEmailConfirmError = view.findViewById(R.id.tv_error_email_confirm);
        mEmailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEmailValid()) {
                    tvEmailError.setVisibility(View.INVISIBLE);
                } else {
                    tvEmailError.setVisibility(View.VISIBLE);
                    if (s.toString().trim().length() == 0) {
                        tvEmailError.setText(R.string.arduino_auth_missing_email);
                    } else {
                        tvEmailError.setText(R.string.arduino_auth_invalid_email);
                    }
                }
                mNextButton.setEnabled(isDataCompleted());
            }
        });
        mEmailConfirmEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEmailConfirmValid()) {
                    tvEmailConfirmError.setVisibility(View.INVISIBLE);
                } else {
                    tvEmailConfirmError.setVisibility(View.VISIBLE);
                    tvEmailConfirmError.setText(R.string.arduino_auth_invalid_email_confirm);
                }
                mNextButton.setEnabled(isDataCompleted());
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void onCompleted() {
        final Bundle args = getArguments();
        final String birthday = args.getString("birthday", null);
        final String email = args.getString("email");
        final String username = args.getString("username");
        final String password = args.getString("password");
        final String avatar = args.getString("avatar", null);
        final String parentEmail = getInputEmail();
        final String flow = args.getString("flow");
        setBlockUI(true);
        hideSoftKeyboard();
        if ("teen".equals(flow)) {
            new Auth0SignUpTeenCall(
                    mContext,
                    username,
                    email,
                    password,
                    birthday,
                    parentEmail
            ).execute(new Callback<Auth0SignUpCall.Response, Exception>() {
                @Override
                public void onResponse(Auth0SignUpCall.Response response) {
                    setBlockUI(false);
                    if (response.success) {
                        final Bundle args = getArguments();
                        args.putString("email", parentEmail);
                        startFragment(SignUpJuniorCompletedFragment.class, args);
                    } else if (response.code != null) {
                        if (response.code == Auth0SignUpCall.Response.Code.USER_EXISTS) {
                            alert(R.string.arduino_auth_sign_up_user_exists);
                        } else {
                            toast(R.string.error_generic);
                        }
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
        } else {
            new JuniorSignUpCall(mContext, birthday, parentEmail, avatar, username, password).execute(new Callback<Void, Exception>() {
                @Override
                public void onResponse(Void response) {
                    setBlockUI(false);
                    final Bundle args = getArguments();
                    args.putString("email", parentEmail);
                    startFragment(SignUpJuniorCompletedFragment.class, args);
                }

                @Override
                public void onFailure(Exception failure) {
                    setBlockUI(false);
                    alert(R.string.error_generic);
                }

                @Override
                public boolean onNetworkError() {
                    setBlockUI(false);
                    alert(R.string.error_network);
                    return true;
                }
            });
        }
    }

    private String getInputEmail() {
        return mEmailEdit.getText().toString().trim().toLowerCase();
    }

    private String getInputEmailConfirm() {
        return mEmailConfirmEdit.getText().toString().trim().toLowerCase();
    }

    private boolean isEmailValid() {
        return Commons.isEmailValid(getInputEmail());
    }

    private boolean isEmailConfirmValid() {
        return getInputEmailConfirm().equals(getInputEmail());
    }

    private boolean isDataCompleted() {
        return isEmailValid() && isEmailConfirmValid();
    }

}
