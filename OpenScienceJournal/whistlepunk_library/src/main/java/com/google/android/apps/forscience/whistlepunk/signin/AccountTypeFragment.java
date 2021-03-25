package com.google.android.apps.forscience.whistlepunk.signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.whistlepunk.R;

public class AccountTypeFragment extends AuthBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_account_type, container, false);
        view.findViewById(R.id.btn_regular).setOnClickListener(v -> startFragment(SignInRegularFragment.class, null));
        view.findViewById(R.id.tv_create_2).setOnClickListener(v -> startFragment(SignUpBirthDateFragment.class, null));
        return view;
    }

}
