package com.google.android.apps.forscience.whistlepunk.signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.whistlepunk.R;

public class SignUpJuniorCompletedFragment extends AuthBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_junior_completed, container, false);
        final TextView tv = view.findViewById(R.id.tv_text);
        tv.setText(getString(R.string.arduino_auth_sign_up_junior_completed_text, getArguments().getString("email", "")));
        view.findViewById(R.id.btn_completed).setOnClickListener(v -> goClose());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(false);
    }

    @Override
    public boolean onBackPressed() {
        goClose();
        return true;
    }

}
