package com.google.android.apps.forscience.whistlepunk.signin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.google.android.apps.forscience.auth0.JuniorAvatarsCall;
import com.google.android.apps.forscience.auth0.JuniorUsernameCall;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class SignUpJuniorStep1Fragment extends AuthBaseFragment {

    private static final String LOG_TAG = "ArduinoAuth";

    private ImageView mAvatarView;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private View mNextButton;

    private JuniorAvatarsCall.Avatar[] mAvatars;

    private JuniorAvatarsCall.Avatar mSelectedAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_arduino_auth_sign_up_junior_step_1, container, false);
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
        mAvatarView = view.findViewById(R.id.iv_avatar);
        mNextButton.setOnClickListener(v -> onCompleted());
        final TextView tvPasswordError = view.findViewById(R.id.tv_error_password);
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
        view.findViewById(R.id.iv_refresh_username).setOnClickListener(v -> generateUsername());
        view.findViewById(R.id.iv_username_info).setOnClickListener(v -> alert(R.string.arduino_auth_sign_up_junior_step_1_username_info));
        final View showPassword = view.findViewById(R.id.iv_show_password);
        showPassword.setOnClickListener(v -> {
            showPassword.setVisibility(View.INVISIBLE);
            mPasswordEdit.setTransformationMethod(null);
        });
        view.findViewById(R.id.l_avatar).setOnClickListener(v -> onEditAvatar());
        return view;
    }

    @Override
    public void onFirstStart() {
        super.onFirstStart();
        setBlockUI(true);
        new JuniorAvatarsCall(mContext).execute(new Callback<JuniorAvatarsCall.Avatar[], Exception>() {
            @Override
            public void onResponse(JuniorAvatarsCall.Avatar[] response) {
                mAvatars = response;
                if (mAvatars != null && mAvatars.length > 0) {
                    mSelectedAvatar = mAvatars[(int) Math.floor(Math.random() * mAvatars.length)];
                    setSVGAvatar(mContext, mAvatarView, mSelectedAvatar.data, true);
                }
                generateUsername();
            }

            @Override
            public void onFailure(Exception failure) {
                generateUsername();
            }
        });
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
        if (mSelectedAvatar != null) {
            args.putString("avatar", mSelectedAvatar.id);
        }
        args.putString("username", getInputUsername());
        args.putString("password", getInputPassword());
        startFragment(SignUpJuniorStep2Fragment.class, args);
    }

    private String getInputUsername() {
        return mUsernameEdit.getText().toString().trim();
    }

    private String getInputPassword() {
        return mPasswordEdit.getText().toString();
    }

    private boolean isUsernameValid() {
        return Commons.isUsernameValid(getInputUsername());
    }

    private boolean isPasswordValid() {
        return Commons.isPasswordValid(getInputPassword());
    }

    private boolean isDataCompleted() {
        return isUsernameValid() && isPasswordValid();
    }

    private void generateUsername() {
        setBlockUI(true);
        new JuniorUsernameCall(mContext).execute(new Callback<String, Exception>() {
            @Override
            public void onResponse(String response) {
                setBlockUI(false);
                mUsernameEdit.setText(response);
            }

            @Override
            public void onFailure(Exception failure) {
                Log.e(LOG_TAG, "Unable to load username", failure);
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

    @SuppressLint("InflateParams")
    private void onEditAvatar() {
        final View view = getLayoutInflater().inflate(R.layout.fragment_arduino_auth_sign_up_junior_step_1_avatar_dialog, null);
        final Dialog d = new Dialog(mContext);
        d.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        d.setCancelable(true);
        final Window window = d.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.findViewById(R.id.auth_header_action_close).setOnClickListener(v -> d.dismiss());
        final RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(mContext, 3));
        recycler.setAdapter(new AvatarAdapter() {
            @Override
            protected void onAvatarSelected(final JuniorAvatarsCall.Avatar avatar) {
                mSelectedAvatar = avatar;
                setSVGAvatar(mContext, mAvatarView, avatar.data, true);
                d.dismiss();
            }
        });
        d.show();
    }

    private abstract class AvatarAdapter extends RecyclerView.Adapter<AvatarViewHolder> {

        @Override
        public int getItemCount() {
            return mAvatars != null ? mAvatars.length : 0;
        }

        @NonNull
        @Override
        public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AvatarViewHolder(getLayoutInflater().inflate(R.layout.fragment_arduino_auth_sign_up_junior_step_1_avatar_dialog_vh, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarViewHolder holder, final int position) {
            setSVGAvatar(mContext, holder.iv, mAvatars[position].data, false);
            holder.itemView.setOnClickListener(v -> {
                onAvatarSelected(mAvatars[position]);
            });
        }

        protected abstract void onAvatarSelected(final JuniorAvatarsCall.Avatar avatar);

    }

    private static class AvatarViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_avatar);
        }

    }

    private static void setSVGAvatar(final Context context, final ImageView iv, final byte[] svgSource, final boolean mask) {
        final Resources r = context.getResources();
        final int side = r.getDimensionPixelSize(R.dimen.arduino_auth_sign_up_junior_avatar_side);
        final Bitmap bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(255, 255, 255);
        try {
            final SVG svg;
            try (final InputStream inputStream = new ByteArrayInputStream(svgSource)) {
                svg = SVG.getFromInputStream(inputStream);
            }
            svg.setDocumentWidth(side);
            svg.setDocumentHeight(side);
            svg.renderToCanvas(canvas, new RectF(0, 0, side, side));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to decode SVG", e);
        }
        if (mask) {
            final int h = r.getDimensionPixelSize(R.dimen.arduino_auth_sign_up_junior_avatar_side_mask_h);
            final RectF rect = new RectF(0f, side - h, side, side);
            final Paint p = new Paint();
            p.setColor(r.getColor(R.color.arduino_jet));
            p.setAntiAlias(false);
            canvas.drawRect(rect, p);
        }
        Glide.with(context).load(bitmap).circleCrop().into(iv);
    }

}
