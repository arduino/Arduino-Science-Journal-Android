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

import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.whistlepunk.R;

public class SignUpAdultStep1Fragment extends AuthBaseFragment {

    private EditText mEmailEdit;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_adult_step_1, container, false);
        mUsernameEdit = view.findViewById(R.id.et_username);
        mEmailEdit = view.findViewById(R.id.et_email);
        mPasswordEdit = view.findViewById(R.id.et_password);
        mNextButton = view.findViewById(R.id.btn_next);
        mPasswordEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isDataCompleted()) {
                    onCompleted();
                    return true;
                }
            }
            return false;
        });
        mNextButton.setOnClickListener(v -> onCompleted());
        view.findViewById(R.id.iv_tp_github).setOnClickListener(v -> launchGitHubSignUp());
        view.findViewById(R.id.iv_tp_google).setOnClickListener(v -> launchGoogleSignUp());
        view.findViewById(R.id.iv_tp_apple).setOnClickListener(v -> launchAppleSignUp());
        final TextView tvEmailError = view.findViewById(R.id.tv_error_email);
        final TextView tvUsernameError = view.findViewById(R.id.tv_error_username);
        final TextView tvPasswordError = view.findViewById(R.id.tv_error_password);
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
        mUsernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUsernameValid()) {
                    tvUsernameError.setVisibility(View.INVISIBLE);
                } else {
                    tvUsernameError.setVisibility(View.VISIBLE);
                    if (s.toString().trim().length() == 0) {
                        tvUsernameError.setText(R.string.arduino_auth_missing_username);
                    } else {
                        tvUsernameError.setText(R.string.arduino_auth_invalid_username);
                    }
                }
                mNextButton.setEnabled(isDataCompleted());
            }
        });
        mPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isPasswordValid()) {
                    tvPasswordError.setVisibility(View.INVISIBLE);
                } else {
                    tvPasswordError.setVisibility(View.VISIBLE);
                    if (s.toString().trim().length() == 0) {
                        tvPasswordError.setText(R.string.arduino_auth_missing_password);
                    } else {
                        tvPasswordError.setText(R.string.arduino_auth_invalid_password);
                    }
                }
                mNextButton.setEnabled(isDataCompleted());
            }
        });
        view.findViewById(R.id.iv_username_info).setOnClickListener(v -> alert(R.string.arduino_auth_username_info));
        final View showPassword = view.findViewById(R.id.iv_show_password);
        showPassword.setOnClickListener(v -> {
            showPassword.setVisibility(View.INVISIBLE);
            mPasswordEdit.setTransformationMethod(null);
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(false);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void onCompleted() {
        final Bundle args = new Bundle();
        args.putString("email", getInputEmail());
        args.putString("username", getInputUsername());
        args.putString("password", getInputPassword());
        startFragment(SignUpAdultStep2Fragment.class, args);
    }

    private String getInputUsername() {
        return mUsernameEdit.getText().toString().trim();
    }

    private String getInputEmail() {
        return mEmailEdit.getText().toString().trim().toLowerCase();
    }

    private String getInputPassword() {
        return mPasswordEdit.getText().toString();
    }

    private boolean isUsernameValid() {
        return Commons.isUsernameValid(getInputUsername());
    }

    private boolean isEmailValid() {
        return Commons.isEmailValid(getInputEmail());
    }

    private boolean isPasswordValid() {
        return Commons.isPasswordValid(getInputPassword());
    }

    private boolean isDataCompleted() {
        return isUsernameValid() && isEmailValid() && isPasswordValid();
    }

    private void launchGitHubSignUp() {
        Commons.doGitHubSignIn(mContext, new Commons.SocialSignInCallback() {
            @Override
            public void onError(Exception exception) {
                toast(R.string.error_generic);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onSuccess(Auth0Token token) {
                notifyAuthCompleted(token);
            }
        });
    }

    private void launchGoogleSignUp() {
        Commons.doGoogleSignIn(mContext, new Commons.SocialSignInCallback() {
            @Override
            public void onError(Exception exception) {
                toast(R.string.error_generic);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onSuccess(Auth0Token token) {
                notifyAuthCompleted(token);
            }
        });
    }

    private void launchAppleSignUp() {
        Commons.doAppleSignIn(mContext, new Commons.SocialSignInCallback() {
            @Override
            public void onError(Exception exception) {
                toast(R.string.error_generic);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onSuccess(Auth0Token token) {
                notifyAuthCompleted(token);
            }
        });
    }

}
