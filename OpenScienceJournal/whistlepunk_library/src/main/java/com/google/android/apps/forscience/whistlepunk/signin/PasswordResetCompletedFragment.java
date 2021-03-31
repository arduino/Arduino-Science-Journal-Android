package com.google.android.apps.forscience.whistlepunk.signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.whistlepunk.R;

public class PasswordResetCompletedFragment extends AuthBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_password_reset_completed, container, false);
        view.findViewById(R.id.btn_completed).setOnClickListener(v -> goBegin());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    @Override
    public boolean onBackPressed() {
        goBegin();
        return true;
    }

}
