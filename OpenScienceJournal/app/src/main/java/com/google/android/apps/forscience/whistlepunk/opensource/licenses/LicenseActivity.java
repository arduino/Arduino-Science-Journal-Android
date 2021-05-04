/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.apps.forscience.whistlepunk.opensource.licenses;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import com.google.android.apps.forscience.whistlepunk.LoadStaticHtmlTaskUseCase;
import com.google.android.apps.forscience.whistlepunk.SettingsActivity;
import com.google.android.apps.forscience.whistlepunk.opensource.R;

import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/** Displays list of code modules. */
public class LicenseActivity extends AppCompatActivity {

  private static final String TAG = "LicenseActivity";
  // Tag and attribute names for parsing license file.
  private static final String TAG_LICENSE = "license";
  private static final String ATTRIB_KEY = "key";
  private static final String TAG_TITLE = "title";
  private static final String TAG_RESOURCE = "resource";
  private static final String TAG_HEADER = "copyrightHeader";

  /**
   * Placeholder text in a license html file which can be replaced. This allows several libraries to
   * share Apache 2, for example.
   */
  public static final String COPYRIGHT_HEADER_PLACEHOLDER = "[_INSERT_COPYRIGHT_HERE_]";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_licenses);

    if (getSupportFragmentManager().findFragmentByTag("list") == null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.add(R.id.container, new LicenseListFragment(), "list");
      ft.commit();
    }

    final ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      if (getSupportFragmentManager().popBackStackImmediate()) {
        return true;
      } else {
        Intent intent =
            SettingsActivity.getLaunchIntent(
                this, getString(R.string.action_about), SettingsActivity.TYPE_ABOUT);
        NavUtils.navigateUpTo(this, intent);
        return true;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  public static class LicenseListFragment extends ListFragment {
    private ArrayAdapter<License> adapter;
    private final LoadLicenseTask loadLicenseTask = new LoadLicenseTask(Schedulers.io(), AndroidSchedulers.mainThread());

    public LicenseListFragment() {
    }

    @Override
    public void onResume() {
      super.onResume();
      setEmptyText(getActivity().getString(R.string.licenses_empty));
      getActivity().setTitle(R.string.settings_open_source_title);
      fetchLicenses();
    }

    @Override
    public void onPause() {
      super.onPause();
      loadLicenseTask.discard();
    }

    private void fetchLicenses() {
      final LoadLicenseTask.Args args = new LoadLicenseTask.Args(
              getResources(),
              TAG_LICENSE,
              ATTRIB_KEY,
              TAG_TITLE,
              TAG_RESOURCE,
              TAG_HEADER,
              LICENSE_COMPARATOR
      );
      loadLicenseTask.invoke(args, new DisposableSingleObserver<List<License>>() {
        @Override
        public void onSuccess(@NonNull List<License> licenses) {
          adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, licenses);
          setListAdapter(adapter);
        }

        @Override
        public void onError(@NonNull Throwable e) {
          Log.e("LicenseListFragment", "loadLicenseTask", e);
        }
      });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
      ((LicenseActivity) getActivity()).showLicense(adapter.getItem(position));
    }
  }

  private void showLicense(License license) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.add(R.id.container, LicenseFragment.newInstance(license), "license");
    ft.addToBackStack(license.key);
    ft.commit();
  }

  public static class LicenseFragment extends Fragment {
    private String titleToRestore = null;

    private WebView webView;

    private final LoadStaticHtmlTaskUseCase loadStaticHtmlTaskUseCase = new LoadStaticHtmlTaskUseCase(Schedulers.io(), AndroidSchedulers.mainThread());

    public LicenseFragment() {}

    public static LicenseFragment newInstance(License license) {
      Bundle args = new Bundle();
      args.putString(TAG_TITLE, license.title);
      args.putString(TAG_HEADER, license.copyrightHeader);
      args.putString(TAG_RESOURCE, license.resource);
      LicenseFragment fragment = new LicenseFragment();
      fragment.setArguments(args);
      return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.licenses, container, false);
      webView = (WebView) view.findViewById(R.id.license_web_view);
      return view;
    }

    @Override
    public void onDestroyView() {
      super.onDestroyView();
      loadStaticHtmlTaskUseCase.discard();
    }

    @Override
    public void onStart() {
      super.onStart();
      int fileId = getResources().getIdentifier(getArguments().getString(TAG_RESOURCE), "raw", requireActivity().getPackageName());
      loadStaticHtmlTaskUseCase.invoke(getResources(), fileId, new DisposableSingleObserver<String>() {
        @Override
        public void onSuccess(@NonNull String data) {
          String copyrightHeader = getArguments().getString(TAG_HEADER);
          if (!TextUtils.isEmpty(copyrightHeader)) {
            data = data.replace(COPYRIGHT_HEADER_PLACEHOLDER, copyrightHeader);
          }
          webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
        }

        @Override
        public void onError(@NonNull Throwable throwable) {
          Log.e(TAG, "loadStaticHtmlTaskUseCase", throwable);
        }
      });
    }

    @Override
    public void onResume() {
      super.onResume();
      if (titleToRestore == null) {
        titleToRestore = getActivity().getTitle().toString();
      }
      getActivity().setTitle(getArguments().getString(TAG_TITLE));
    }

    @Override
    public void onPause() {
      if (titleToRestore != null) {
        getActivity().setTitle(titleToRestore);
        titleToRestore = null;
      }
      super.onPause();
    }
  }

  /** Represents a license file to show. */
  static class License {

    /** Key, not user shown. */
    String key;

    /** User readable title. Doesn't need to be translated since these are names. */
    String title;

    /** Name of a raw resource file with the license. */
    String resource;

    /** Optional copyright header to insert into license file. */
    String copyrightHeader;

    @Override
    public String toString() {
      return title;
    }

    public boolean isValid() {
      return !TextUtils.isEmpty(key) && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(resource);
    }
  }

  /** Sorts license objects by title. */
  static final Comparator<License> LICENSE_COMPARATOR =
      new Comparator<License>() {

        @Override
        public int compare(License lhs, License rhs) {
          return lhs.toString().compareTo(rhs.toString());
        }
      };
}
