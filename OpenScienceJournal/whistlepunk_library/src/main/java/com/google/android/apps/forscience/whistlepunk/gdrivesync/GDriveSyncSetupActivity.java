package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.api.GDriveBrowseCall;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.api.GDriveCreateFolderCall;
import com.google.android.apps.forscience.whistlepunk.gdrivesync.api.GDriveFile;
import com.google.android.apps.forscience.whistlepunk.remote.Callback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GDriveSyncSetupActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GDriveSyncSetupActivity";

    private final PagerAdapter mPagerAdapter = new MyPagerAdapter();

    private ViewPager mViewPager;

    private View mLoader;

    private String mAccountId;

    private String mEmail;

    private String mToken;

    private String mFolderId;

    private boolean mLoading;

    private final List<GDriveFile> mPath = new ArrayList<>();

    private final List<GDriveFile> mFiles = new ArrayList<>();

    private int mCheckedFileIndex = -1;

    private final FileAdapter mFileAdapter = new FileAdapter();

    private View mBrowseBackView;

    private TextView mNavigationParentText;

    private View mNavigationParentSep;

    private TextView mNavigationCurrentText;

    private View mSelectFolderView;

    private TextView mStep3Description;

    private TextView mStep3Folder;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_setup);
        findViewById(R.id.drive_header_action_close).setOnClickListener(v -> finish());
        findViewById(R.id.drive_header_action_back).setOnClickListener(v -> onHeaderBack());
        mViewPager = findViewById(R.id.drive_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnTouchListener((v, event) -> true);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                findViewById(R.id.drive_header_action_back).setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });
        mLoader = findViewById(R.id.drive_loader);
    }

    @Override
    public void onBackPressed() {
        if (mLoading) {
            return;
        }
        final int current = mViewPager.getCurrentItem();
        if (current == 0) {
            finish();
            return;
        }
        if (current == 1) {
            if (mPath.size() > 0) {
                mPath.remove(mPath.size() - 1);
                loadFolder();
                return;
            }
        }
        mViewPager.setCurrentItem(current - 1, true);
    }

    private void onHeaderBack() {
        if (mLoading) {
            return;
        }
        final int current = mViewPager.getCurrentItem();
        if (current == 0) {
            finish();
        } else {
            mViewPager.setCurrentItem(current - 1, true);
        }
    }

    private View onCreateStep1(@NonNull ViewGroup container) {
        final View view = getLayoutInflater().inflate(R.layout.activity_drive_setup__step_1, container, false);
        final View popUpBg = view.findViewById(R.id.drive_popup_bg);
        final View popUp = view.findViewById(R.id.drive_popup);
        view.findViewById(R.id.drive_learn_more).setOnClickListener(v -> {
            popUpBg.setVisibility(View.VISIBLE);
            popUp.setVisibility(View.VISIBLE);
        });
        popUpBg.setOnClickListener(v -> {
            popUpBg.setVisibility(View.GONE);
            popUp.setVisibility(View.GONE);
        });
        view.findViewById(R.id.drive_btn_signin).setOnClickListener(v -> onGoogleSignIn());
        view.findViewById(R.id.drive_btn_skip).setOnClickListener(v -> finish());
        return view;
    }

    private void onGoogleSignIn() {
        final GoogleSignInOptions options = new GoogleSignInOptions.Builder()
                .requestId()
                .requestEmail()
                .requestScopes(GDRIVE_SCOPES[0], GDRIVE_SCOPES[1], GDRIVE_SCOPES[2], GDRIVE_SCOPES[3])
                .build();
        final GoogleSignInClient client = GoogleSignIn.getClient(this, options);
        showLoader();
        client.signOut()
                .addOnCompleteListener(this, task -> startActivityForResult(client.getSignInIntent(), RC_SIGN_IN))
                .addOnFailureListener(this, task -> startActivityForResult(client.getSignInIntent(), RC_SIGN_IN));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            hideLoader();
            if (resultCode == RESULT_OK) {
                final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    onAccountAvailable(Objects.requireNonNull(task.getResult(ApiException.class)));
                } catch (ApiException e) {
                    Log.e(LOG_TAG, "FAILURE", e);
                    showError();
                }
            }
        }
    }

    private void onAccountAvailable(final GoogleSignInAccount account) {
        Log.d(LOG_TAG, "ACCOUNT: " + account.getEmail());
        new Thread(() -> {
            try {
                mAccountId = account.getId();
                mEmail = account.getEmail();
                if (mEmail == null || mEmail.length() == 0) {
                    throw new Exception("Account email is null or empty");
                }
                Log.d(LOG_TAG, "EMAIL: " + mEmail);
                final Set<String> scopes = new HashSet<>();
                for (final Scope scope : GDRIVE_SCOPES) {
                    scopes.add(scope.getScopeUri());
                }
                final GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, scopes);
                credential.setSelectedAccount(account.getAccount());
                mToken = credential.getToken();
                if (mToken == null || mToken.length() == 0) {
                    throw new Exception("Account token is null or empty");
                }
                Log.d(LOG_TAG, "TOKEN: " + mToken);
                runOnUiThread(this::goToStep2);
            } catch (Exception e) {
                Log.e(LOG_TAG, "ERROR", e);
                runOnUiThread(this::showError);
            }
        }).start();
    }

    private View onCreateStep2(@NonNull ViewGroup container) {
        final View view = getLayoutInflater().inflate(R.layout.activity_drive_setup__step_2, container, false);
        final RecyclerView recycler = view.findViewById(R.id.drive_file_browser);
        recycler.setAdapter(mFileAdapter);
        mBrowseBackView = view.findViewById(R.id.drive_browse_back);
        mBrowseBackView.setOnClickListener(v -> {
            if (mPath.size() > 0) {
                mPath.remove(mPath.size() - 1);
                loadFolder();
            }
        });
        view.findViewById(R.id.drive_new_folder).setOnClickListener(v -> onNewFolder());
        mNavigationParentText = view.findViewById(R.id.drive_parent_name);
        mNavigationParentSep = view.findViewById(R.id.drive_parent_sep);
        mNavigationCurrentText = view.findViewById(R.id.drive_current);
        mSelectFolderView = view.findViewById(R.id.drive_btn_select);
        mSelectFolderView.setOnClickListener(v -> onFolderSelected());
        return view;
    }

    private void goToStep2() {
        mPath.clear();
        mFiles.clear();
        showLoader();
        loadFolder();
        mViewPager.setCurrentItem(1, true);
    }

    private void loadFolder() {
        mFiles.clear();
        mCheckedFileIndex = -1;
        mSelectFolderView.setEnabled(false);
        final int index = mPath.size() - 1;
        final String folderId;
        if (index < 0) {
            folderId = "root";
            mBrowseBackView.setVisibility(View.INVISIBLE);
            mNavigationParentText.setText("");
            mNavigationParentText.setVisibility(View.GONE);
            mNavigationParentSep.setVisibility(View.GONE);
            mNavigationCurrentText.setText(R.string.gdrive_root);
        } else {
            final GDriveFile f = mPath.get(index);
            folderId = f.id;
            mBrowseBackView.setVisibility(View.VISIBLE);
            if (index == 0) {
                mNavigationParentText.setText(R.string.gdrive_root);
            } else {
                mNavigationParentText.setText(mPath.get(index - 1).name);
            }
            mNavigationParentText.setVisibility(View.VISIBLE);
            mNavigationParentSep.setVisibility(View.VISIBLE);
            mNavigationCurrentText.setText(f.name);
        }
        updateFiles();
        showLoader();
        loadFolders(folderId, null);
    }

    private void loadFolders(final String folderId, final String pageToken) {
        showLoader();
        new GDriveBrowseCall(this, mToken, folderId, true, pageToken).execute(new Callback<GDriveBrowseCall.Result, Exception>() {
            @Override
            public void onResponse(GDriveBrowseCall.Result response) {
                Collections.addAll(mFiles, response.files);
                if (response.nextPageToken != null) {
                    loadFolders(folderId, response.nextPageToken);
                } else {
                    hideLoader();
                    updateFiles();
                }
            }

            @Override
            public void onFailure(Exception e) {
                hideLoader();
                updateFiles();
                showError();
            }
        });
    }

    private void updateFiles() {
        mFileAdapter.notifyDataSetChanged();
    }

    private void onNewFolder() {
        final View view = getLayoutInflater().inflate(R.layout.activity_drive_setup__prompt, null);
        final EditText et = view.findViewById(R.id.et);
        et.setText(R.string.drive_create_folder_def_name);
        et.selectAll();
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setView(view);
        b.setTitle(R.string.drive_create_folder_title);
        b.setPositiveButton(R.string.drive_create_folder_action, (dialog, which) -> {
            dialog.dismiss();
            final String folder = et.getText().toString().trim();
            if (folder.length() > 0) {
                onCreateFolder(folder);
            }
        });
        b.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setCancelable(true);
        b.show();
    }

    private void onCreateFolder(final String name) {
        showLoader();
        final String parent;
        if (mPath.size() == 0) {
            parent = "root";
        } else {
            parent = mPath.get(mPath.size() - 1).id;
        }
        new GDriveCreateFolderCall(this, mToken, name, parent).execute(new Callback<String, Exception>() {
            @Override
            public void onResponse(String newFolderId) {
                Log.d(LOG_TAG, "FOLDER CREATED WITH ID: " + newFolderId);
                loadFolder();
            }

            @Override
            public void onFailure(Exception failure) {
                Log.e(LOG_TAG, "ERROR CREATING FOLDER", failure);
                showError();
                hideLoader();
            }
        });
    }

    private void onFolderSelected() {
        goToStep3();
    }

    private View onCreateStep3(@NonNull ViewGroup container) {
        final View view = getLayoutInflater().inflate(R.layout.activity_drive_setup__step_3, container, false);
        mStep3Description = view.findViewById(R.id.drive_description);
        mStep3Folder = view.findViewById(R.id.drive_folder);
        view.findViewById(R.id.drive_btn_sync).setOnClickListener(v -> onCompleted());
        return view;
    }

    private void goToStep3() {
        final GDriveFile f = mFiles.get(mCheckedFileIndex);
        mFolderId = f.id;
        mStep3Description.setText(getString(R.string.drive_setup_step_3_description, f.name));
        mStep3Folder.setText(f.name);
        mViewPager.setCurrentItem(2, true);
    }

    private void onCompleted() {
        try {
            GDriveShared.saveCredentials(this, mAccountId, mEmail, mToken, mFolderId);
        } catch (Exception e) {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            return;
        }
        setResult(RESULT_OK);
        finish();
    }

    private void showLoader() {
        mLoading = true;
        mLoader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mLoading = false;
        mLoader.setVisibility(View.GONE);
    }

    private void showError() {
        Toast.makeText(this, R.string.gdrive_error, Toast.LENGTH_SHORT).show();
    }

    private class MyPagerAdapter extends PagerAdapter {

        private final Map<Object, View> mPages = new HashMap<>();

        @Override
        public int getCount() {
            return 3;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view;
            switch (position) {
                case 0:
                    view = onCreateStep1(container);
                    break;
                case 1:
                    view = onCreateStep2(container);
                    break;
                case 2:
                    view = onCreateStep3(container);
                    break;
                default:
                    throw new RuntimeException("Unknown drive setup page index " + position);
            }
            mPages.put(position, view);
            container.addView(view);
            return position;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            final View view = mPages.get(position);
            if (view != null) {
                container.removeView(view);
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            if (object instanceof Integer) {
                int position = (Integer) object;
                return mPages.get(position) == view;
            }
            return false;
        }

    }

    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

        @Override
        public int getItemCount() {
            return mFiles.size();
        }

        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FileViewHolder(getLayoutInflater().inflate(R.layout.activity_drive_setup__browser_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, final int position) {
            final GDriveFile f = mFiles.get(position);
            holder.mNameView.setText(f.name);
            if (f.isFolder) {
                holder.setChecked(position == mCheckedFileIndex);
                holder.setOnCheckedListener(() -> {
                    final int previouslyChecked = mCheckedFileIndex;
                    mCheckedFileIndex = position;
                    if (previouslyChecked >= 0) {
                        notifyItemChanged(previouslyChecked);
                    }
                    mSelectFolderView.setEnabled(true);
                });
                holder.setOnUncheckedListener(() -> {
                    mCheckedFileIndex = -1;
                    mSelectFolderView.setEnabled(false);
                });
                holder.setOnEnterClickListener(v -> {
                    mPath.add(f);
                    loadFolder();
                });
            }
        }

        @Override
        public void onViewRecycled(@NonNull FileViewHolder holder) {
            super.onViewRecycled(holder);
            holder.reset();
        }

    }

    private static class FileViewHolder extends RecyclerView.ViewHolder {

        private boolean mChecked;

        private final ImageView mIconView;

        private final ImageView mCheckedView;

        private final ImageView mEnterView;

        private final TextView mNameView;

        private Runnable mCheckedListener;

        private Runnable mUncheckedListener;

        private FileViewHolder(@NonNull View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.file_icon);
            mCheckedView = itemView.findViewById(R.id.file_checked);
            mEnterView = itemView.findViewById(R.id.file_enter);
            mNameView = itemView.findViewById(R.id.file_name);
            final View.OnClickListener onClick = v -> {
                setChecked(!mChecked);
                if (mChecked) {
                    if (mCheckedListener != null) {
                        mCheckedListener.run();
                    }
                } else {
                    if (mUncheckedListener != null) {
                        mUncheckedListener.run();
                    }
                }
            };
            mIconView.setOnClickListener(onClick);
            mNameView.setOnClickListener(onClick);
        }

        private void setOnEnterClickListener(final View.OnClickListener listener) {
            mEnterView.setOnClickListener(listener);
        }

        private void setOnCheckedListener(final Runnable listener) {
            mCheckedListener = listener;
        }

        private void setOnUncheckedListener(final Runnable listener) {
            mUncheckedListener = listener;
        }

        private void reset() {
            setChecked(false);
            setOnEnterClickListener(null);
            setOnCheckedListener(null);
            setOnUncheckedListener(null);
        }

        private void setChecked(boolean checked) {
            if (checked == mChecked) {
                return;
            }
            mChecked = checked;
            final int color;
            if (mChecked) {
                color = mNameView.getResources().getColor(R.color.arduino_teal_3);
                mIconView.setVisibility(View.INVISIBLE);
                mCheckedView.setVisibility(View.VISIBLE);
            } else {
                color = mNameView.getResources().getColor(R.color.arduino_gris);
                mIconView.setVisibility(View.VISIBLE);
                mCheckedView.setVisibility(View.INVISIBLE);
            }
            mEnterView.setColorFilter(color);
            mNameView.setTextColor(color);
        }

    }

    private static final Scope[] GDRIVE_SCOPES = {
            new Scope("https://www.googleapis.com/auth/drive"),
            new Scope("https://www.googleapis.com/auth/drive.appdata"),
            new Scope("https://www.googleapis.com/auth/drive.file"),
            new Scope("https://www.googleapis.com/auth/drive.metadata")
    };

    private static final int RC_SIGN_IN = 1000;

}
