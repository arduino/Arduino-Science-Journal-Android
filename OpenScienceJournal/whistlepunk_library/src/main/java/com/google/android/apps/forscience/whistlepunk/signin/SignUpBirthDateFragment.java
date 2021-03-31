package com.google.android.apps.forscience.whistlepunk.signin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.forscience.whistlepunk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SignUpBirthDateFragment extends AuthBaseFragment {

    private Calendar mCalendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            mCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR) - 10);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_birth_date, container, false);
        final EditText etBirthDate = view.findViewById(R.id.et_birth_date);
        final View btnNext = view.findViewById(R.id.btn_next);
        view.findViewById(R.id.il_birth_date_cover).setOnClickListener(v1 -> {
            final DatePickerDialog d = new DatePickerDialog(
                    mContext,
                    (v2, year, monthOfYear, dayOfMonth) -> {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, monthOfYear);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etBirthDate.setText(DateFormat.getDateInstance(DateFormat.LONG).format(mCalendar.getTime()));
                        btnNext.setEnabled(computeAge(mCalendar) > 1);
                    },
                    mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH)
            );
            d.show();
        });
        btnNext.setOnClickListener(v -> onCompleted());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setBackEnabled(true);
    }

    @SuppressLint("SimpleDateFormat")
    private void onCompleted() {
        int age = computeAge(mCalendar);
        Bundle args = new Bundle();
        args.putString("birthday", new SimpleDateFormat("yyyy-MM-dd").format(mCalendar.getTime()));
        if (age >= 16) {
            args.putString("flow", "adult");
            startFragment(SignUpAdultStep1Fragment.class, args);
        } else if (age >= 14) {
            args.putString("flow", "teen");
            startFragment(SignUpAdultStep1Fragment.class, args);
        } else {
            args.putString("flow", "junior");
            startFragment(SignUpJuniorStep1Fragment.class, args);
        }
    }

    private int computeAge(final Calendar birth) {
        final Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        int nMonth = now.get(Calendar.MONTH);
        int bMonth = birth.get(Calendar.MONTH);
        if (nMonth < bMonth) {
            age--;
        } else if (nMonth == bMonth) {
            int nDay = now.get(Calendar.DAY_OF_MONTH);
            int bDay = birth.get(Calendar.DAY_OF_MONTH);
            if (nDay < bDay) {
                age--;
            }
        }
        return age;
    }

}
