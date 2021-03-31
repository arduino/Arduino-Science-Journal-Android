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

import com.google.android.apps.forscience.auth0.Auth0LoginTokenCall;
import com.google.android.apps.forscience.auth0.Auth0Token;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;
import com.google.android.apps.forscience.whistlepunk.remote.StringUtils;

public class SignInAdultFragment extends AuthBaseFragment {

    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private View mError;
    private View mNextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_in_adult, container, false);
        mUsernameEdit = view.findViewById(R.id.et_username);
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
        mPasswordEdit.addTextChangedListener(new TextWatcher() {
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
        view.findViewById(R.id.tv_password_reset).setOnClickListener(v -> {
            final String username = getInputUsername();
            final Bundle args = new Bundle();
            if (Commons.isEmailValid(username)) {
                args.putString("email", username);
            }
            startFragment(PasswordResetStep1Fragment.class, args);
        });
        view.findViewById(R.id.iv_tp_github).setOnClickListener(v -> launchGitHubSignUp());
        view.findViewById(R.id.iv_tp_google).setOnClickListener(v -> launchGoogleSignUp());
        view.findViewById(R.id.iv_tp_apple).setOnClickListener(v -> launchAppleSignUp());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    private void onCompleted() {
        final String username = getInputUsername();
        final String password = getInputPassword();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return;
        }
        mError.setVisibility(View.INVISIBLE);
        setBlockUI(true);
        hideSoftKeyboard();
        new Auth0LoginTokenCall(
                mContext,
                username,
                password
        ).execute(new Callback<Auth0LoginTokenCall.Response, Exception>() {
            @Override
            public void onResponse(Auth0LoginTokenCall.Response response) {
                setBlockUI(false);
                if (response == null) {
                    mError.setVisibility(View.VISIBLE);
                } else if (!StringUtils.isEmpty(response.mfa)) {
                    final Bundle args = new Bundle();
                    args.putString("mfa", response.mfa);
                    startFragment(SignInMFAFragment.class, args);
                } else if (response.token != null) {
                    notifyAuthCompleted(response.token);
                } else {
                    toast(R.string.error_generic);
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

    private String getInputPassword() {
        return mPasswordEdit.getText().toString();
    }

    private boolean isUsernameValid() {
        final String username = getInputUsername();
        return Commons.isUsernameValid(username) || Commons.isEmailValid(username);
    }

    private boolean isPasswordValid() {
        return Commons.isPasswordValid(getInputPassword());
    }

    private boolean isDataCompleted() {
        return isUsernameValid() && isPasswordValid();
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
