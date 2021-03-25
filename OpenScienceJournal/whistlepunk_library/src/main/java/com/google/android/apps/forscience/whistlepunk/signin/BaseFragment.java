package com.google.android.apps.forscience.whistlepunk.signin;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.apps.forscience.whistlepunk.R;

abstract class BaseFragment extends Fragment {

    protected Context mContext;

    private boolean mFirstStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mFirstStart) {
            mFirstStart = true;
            onFirstStart();
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onFirstStart() {
    }

    protected final void alert(int resId) {
        alert(mContext.getString(resId));
    }

    protected final void alert(String message) {
        final AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setMessage(message);
        b.setCancelable(false);
        b.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        b.show();
    }

    protected final void toast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    protected final void toast(int message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    protected final void alertNetworkError(final Runnable onRetry) {
        final AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setMessage(R.string.error_network);
        b.setCancelable(false);
        b.setPositiveButton(R.string.retry, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            onRetry.run();
        });
        b.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        b.show();
    }

    protected void showSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    protected void hideSoftKeyboard() {
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
