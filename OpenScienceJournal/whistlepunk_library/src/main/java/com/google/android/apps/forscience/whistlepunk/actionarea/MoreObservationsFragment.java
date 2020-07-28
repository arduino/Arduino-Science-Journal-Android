/*
 *  Copyright 2019 Google Inc. All Rights Reserved.
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

package com.google.android.apps.forscience.whistlepunk.actionarea;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.apps.forscience.whistlepunk.ExperimentActivity;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;

/** Fragment for adding more observations (drawing, velocity, etc) in the ExperimentActivity. */
public class MoreObservationsFragment extends ActionFragment {

  private RecyclerView gridView;
  private ObservationsAdapter adapter;

  public static MoreObservationsFragment newInstance(AppAccount appAccount, String experimentId) {
    MoreObservationsFragment fragment = new MoreObservationsFragment();
    Bundle args = new Bundle();
    args.putString(KEY_ACCOUNT_KEY, appAccount.getAccountKey());
    args.putString(KEY_EXPERIMENT_ID, experimentId);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.more_observations_fragment, null);
    int columnCount = rootView.getResources().getInteger(R.integer.more_observations_column_count);
    gridView = rootView.findViewById(R.id.grid_view);
    gridView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));
    if (adapter != null) {
      gridView.setAdapter(adapter);
    }
    return rootView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = getActivity();
    if (activity != null) {
      ObservationOption[] moreObservationOptions =
          ((ExperimentActivity) activity).getMoreObservationOptions();
      adapter =
          new ObservationsAdapter(activity.getLayoutInflater(), moreObservationOptions);
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    adapter = null;
    gridView.setAdapter(null);
  }

  private class ObservationsAdapter extends RecyclerView.Adapter<ObservationsAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final ObservationOption[] moreObservationOptions;

    private ObservationsAdapter(
        LayoutInflater localInflater, ObservationOption[] observationOptions) {
      inflater = localInflater;
      moreObservationOptions = observationOptions;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = inflater.inflate(R.layout.more_observations_item, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      holder.bind(moreObservationOptions[position]);
    }

    @Override
    public int getItemCount() {
      return moreObservationOptions.length;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
      final TextView title;
      final TextView description;
      final ImageView icon;

      private ViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        icon = itemView.findViewById(R.id.icon);
      }

      private void bind(final ObservationOption item) {
        title.setText(item.titleId);
        description.setText(item.descriptionId);
        icon.setImageResource(item.iconId);
        itemView.setOnClickListener(item.listener);
      }
    }
  }

  public static class ObservationOption {
    final int titleId;
    final int descriptionId;
    final int iconId;
    final OnClickListener listener;

    public ObservationOption(int titleId, int descriptionId, int iconId, OnClickListener listener) {
      this.titleId = titleId;
      this.descriptionId = descriptionId;
      this.iconId = iconId;
      this.listener = listener;
    }
  }

  @Override
  protected String getTitle() {
    return getString(R.string.action_bar_more);
  }
}
