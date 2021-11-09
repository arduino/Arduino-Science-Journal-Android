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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.apps.forscience.whistlepunk.GlideApp;
import com.google.android.apps.forscience.whistlepunk.NoteTakingActivity;
import com.google.android.apps.forscience.whistlepunk.PictureUtils;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.RxEvent;
import com.google.android.apps.forscience.whistlepunk.accounts.AppAccount;
import com.google.android.apps.forscience.whistlepunk.actionarea.PhotoAsyncLoader.Image;
import com.google.android.apps.forscience.whistlepunk.filemetadata.FileMetadataUtil;
import com.google.android.apps.forscience.whistlepunk.filemetadata.Label;
import com.google.android.apps.forscience.whistlepunk.metadata.GoosciLabel;
import com.google.android.apps.forscience.whistlepunk.metadata.GoosciPictureLabelValue;
import com.google.android.apps.forscience.whistlepunk.project.experiment.UpdateExperimentFragment;
import com.google.android.apps.forscience.whistlepunk.review.RunReviewActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;

/**
 * Fragment controlling adding picture notes from the gallery in the ExperimentActivity.
 *
 * This fragment assumes that it does not outlive its initial context.
 **/
public class GalleryNoteFragment extends ActionFragment
    implements LoaderManager.LoaderCallbacks<List<Image>> {
  private static final String TAG = "GalleryNoteFragment";

  private static final int PHOTO_LOADER_INDEX = 1;
  private static final String KEY_SELECTED_PHOTOS = "selected_photos";

  private Listener listener;
  private GalleryItemAdapter galleryAdapter;
  private List<Integer> initiallySelectedPhotos;
  private SingleSubject<LoaderManager> whenLoaderManager = SingleSubject.create();
  private BehaviorSubject<Boolean> addButtonEnabled = BehaviorSubject.create();
  private BehaviorSubject<Boolean> permissionGranted = BehaviorSubject.create();
  private RxEvent destroyed = new RxEvent();
  private FloatingActionButton addButton;

  public interface Listener {
    String getExperimentId();

    void onPictureLabelTaken(Label label);

    RxPermissions getPermissions();
  }

  public interface ListenerProvider {
    Listener getGalleryListener();
  }

  public static ActionFragment newInstance(AppAccount appAccount, String experimentId) {
    GalleryNoteFragment fragment = new GalleryNoteFragment();
    Bundle args = new Bundle();
    args.putString(KEY_ACCOUNT_KEY, appAccount.getAccountKey());
    args.putString(KEY_EXPERIMENT_ID, experimentId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initiallySelectedPhotos =
        savedInstanceState != null
            ? savedInstanceState.getIntegerArrayList(KEY_SELECTED_PHOTOS)
            : new ArrayList<>();

    galleryAdapter =
        new GalleryItemAdapter(
            getActivity(),
            () -> {
              addButtonEnabled.onNext(!galleryAdapter.selectedIndices.isEmpty());
              updateTitle();
            });

    permissionGranted
        .distinctUntilChanged()
        .takeUntil(destroyed.happens())
        .subscribe(
            granted -> {
              if (granted) {
                loadImages();
              } else {
                complainPermissions();
              }
            });

    whenLoaderManager.onSuccess(getLoaderManager());
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    listener = ((GalleryNoteFragment.ListenerProvider) context).getGalleryListener();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }

  private void requestPermission() {
    if (listener == null) {
      return;
    }
    RxPermissions permissions = listener.getPermissions();
    if (permissions == null) {
      return;
    }
    permissions
        .request(Manifest.permission.READ_EXTERNAL_STORAGE)
        .subscribe(
            granted -> {
              permissionGranted.onNext(granted);
            });
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.gallery_note_fragment, null);

    RecyclerView gallery = rootView.findViewById(R.id.gallery);
    GridLayoutManager layoutManager =
        new GridLayoutManager(
            gallery.getContext(),
            gallery.getContext().getResources().getInteger(R.integer.gallery_column_count));
    gallery.setLayoutManager(layoutManager);
    gallery.setItemAnimator(new DefaultItemAnimator());
    gallery.setAdapter(galleryAdapter);
    addButton = rootView.findViewById(R.id.btn_add);

    requestPermission();
    attachAddButton(addButton);
    actionController.attachAddButton(addButton);
    actionController.attachProgressBar(rootView.findViewById(R.id.recording_progress_bar));
    setUpTitleBar(rootView, false, R.string.action_bar_gallery, R.drawable.ic_gallery);

    return rootView;
  }

  private void complainPermissions() {
    View rootView = getView();
    if (rootView != null) {
      rootView.findViewById(R.id.gallery).setVisibility(View.GONE);
      rootView.findViewById(R.id.complaint).setVisibility(View.VISIBLE);

      RxView.clicks(rootView.findViewById(R.id.open_settings))
          .subscribe(click -> requestPermission());
    }
  }

  public void attachAddButton(FloatingActionButton addButton) {
    addButton.setOnClickListener(
        view -> {
          Activity activity = getActivity();
          if (activity == null) {
            return;
          }
          final long timestamp =
              ((NoteTakingActivity) activity).getTimestamp(addButton.getContext());
          GoosciPictureLabelValue.PictureLabelValue.Builder labelValue =
              GoosciPictureLabelValue.PictureLabelValue.newBuilder();

          if (listener != null) {
            String experimentId = listener.getExperimentId();
            List<String> selectedImages = galleryAdapter.getSelectedImages();
            for (String selectedImage : selectedImages) {

              Label result = Label.newLabel(timestamp, GoosciLabel.Label.ValueType.PICTURE);
              File imageFile =
                  PictureUtils.createImageFile(
                      getActivity(), getAppAccount(), experimentId, result.getLabelId());

              try {
                UpdateExperimentFragment.copyUriToFile(
                    addButton.getContext(), Uri.parse(selectedImage), imageFile);
                labelValue.setFilePath(
                    FileMetadataUtil.getInstance()
                        .getRelativePathInExperiment(experimentId, imageFile));
              } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, e.getMessage());
                }
                labelValue.clearFilePath();
              }

              result.setLabelProtoData(labelValue.build());
              if (listener != null) {
                listener.onPictureLabelTaken(result);
              }
            }
            galleryAdapter.deselectImages();
            addButtonEnabled.onNext(false);
          }
        });

    addButton.setEnabled(false);

    addButtonEnabled.subscribe(enabled -> addButton.setEnabled(enabled));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putIntegerArrayList(KEY_SELECTED_PHOTOS, galleryAdapter.selectedIndices);
  }

  private void loadImages() {
    View rootView = getView();
    if (rootView != null) {
      rootView.findViewById(R.id.gallery).setVisibility(View.VISIBLE);
      rootView.findViewById(R.id.complaint).setVisibility(View.GONE);
    }

    if (getActivity() != null) {
      whenLoaderManager.subscribe(manager -> manager.initLoader(PHOTO_LOADER_INDEX, null, this));
    }
  }

  @Override
  public Loader<List<Image>> onCreateLoader(int i, Bundle bundle) {
    return new PhotoAsyncLoader(getActivity().getApplicationContext());
  }

  @Override
  public void onLoadFinished(
      Loader<List<PhotoAsyncLoader.Image>> loader, List<PhotoAsyncLoader.Image> images) {

    galleryAdapter.setImages(images);
    if (!initiallySelectedPhotos.isEmpty()) {
      // Resume from saved state if needed.
      for (int selectedPhoto : initiallySelectedPhotos) {
        galleryAdapter.setSelectedIndex(selectedPhoto);
      }
      addButtonEnabled.onNext(!initiallySelectedPhotos.isEmpty());
      initiallySelectedPhotos.clear();
    }
  }

  @Override
  public void onLoaderReset(Loader<List<PhotoAsyncLoader.Image>> loader) {
    galleryAdapter.clearImages();
  }

  private static class GalleryItemAdapter
      extends RecyclerView.Adapter<GalleryItemAdapter.ViewHolder> {

    interface ImageClickListener {
      void onImageClicked();
    }

    private List<PhotoAsyncLoader.Image> images;
    private final Context context;
    private final ImageClickListener listener;
    private final ArrayList<Integer> selectedIndices = new ArrayList<>();
    private final String contentDescriptionPrefixSelected;
    private final String contentDescriptionPrefixNotSelected;

    public GalleryItemAdapter(Context context, ImageClickListener listener) {
      images = new ArrayList<>();
      this.context = context;
      this.listener = listener;
      contentDescriptionPrefixSelected =
          context.getResources().getString(R.string.gallery_image_content_description_selected);
      contentDescriptionPrefixNotSelected =
          context.getResources().getString(R.string.gallery_image_content_description);
    }

    public void setImages(List<PhotoAsyncLoader.Image> images) {
      this.images.clear();
      this.images.addAll(images);
      notifyDataSetChanged();
    }

    public void clearImages() {
      images.clear();
      notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(context).inflate(R.layout.gallery_image, parent, false);
      return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      String path = images.get(position).path;

      GlideApp.with(context)
          .load(path)
          .listener(
              new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(
                    @Nullable GlideException e,
                    Object model,
                    Target<Drawable> target,
                    boolean isFirstResource) {
                  return false;
                }

                @Override
                public boolean onResourceReady(
                    Drawable resource,
                    Object model,
                    Target<Drawable> target,
                    DataSource dataSource,
                    boolean isFirstResource) {
                  holder.image.setBackground(null);
                  return false;
                }
              })
          .thumbnail(.5f)
          .into(holder.image);

      boolean selected = selectedIndices.contains(position);
      holder.selectedIndicator.setSelected(selected);
      if (selected) {
        int selectedNumber = selectedIndices.indexOf(position) + 1;
        holder.selectedText.setText(
            String.format(Locale.getDefault(), "%d", selectedNumber));
        holder.image.setContentDescription(
            String.format(
                contentDescriptionPrefixSelected,
                selectedNumber,
                DateUtils.formatDateTime(
                    context,
                    images.get(position).timestampTaken,
                    DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_ABBREV_ALL)));
        holder.image.setScaleX(.9F);
        holder.image.setScaleY(.9F);
      } else {
        holder.selectedText.setText("");
        holder.image.setContentDescription(
            String.format(
                contentDescriptionPrefixNotSelected,
                DateUtils.formatDateTime(
                    context,
                    images.get(position).timestampTaken,
                    DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_ABBREV_ALL)));
        holder.image.setScaleX(1F);
        holder.image.setScaleY(1F);
      }

      holder.image.setOnClickListener(
          view -> {
            final int adapterPosition = holder.getAdapterPosition();
            // Ignore invalid clicks that happened between layouts.
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            boolean newlySelected = !selectedIndices.contains(adapterPosition);
            if (newlySelected) {
              // It was clicked for the first time.
              setSelectedIndex(adapterPosition);
            } else {
              // A second click on the same image is a deselect.
              selectedIndices.remove(selectedIndices.indexOf(adapterPosition));
              // Update other selected items labels.
              for (int i = 0; i < selectedIndices.size(); ++i) {
                notifyItemChanged(selectedIndices.get(i));
              }
              // Update current selection
              notifyItemChanged(adapterPosition);
            }

            listener.onImageClicked();
          });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
      holder.image.setBackgroundColor(context.getResources().getColor(R.color.background_color));
      super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
      return images.size();
    }

    public List<String> getSelectedImages() {
      List<String> imagePaths = new ArrayList<>();
      for (int index : selectedIndices) {
        imagePaths.add(images.get(index).path);
      }
      return imagePaths;
    }

    public void deselectImages() {
      for (int index : selectedIndices) {
        notifyItemChanged(index);
      }
      selectedIndices.clear();
    }

    public void setSelectedIndex(int indexToSelect) {
      if (indexToSelect < images.size()) {
        selectedIndices.add(indexToSelect);
        notifyItemChanged(indexToSelect);
      }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      public ImageView image;
      public View selectedIndicator;
      public TextView selectedText;

      public ViewHolder(View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.image);
        selectedIndicator = itemView.findViewById(R.id.selected_indicator);
        selectedText = itemView.findViewById(R.id.selected_text);
      }
    }
  }

  @Override
  protected String getTitle() {
    NoteTakingActivity activity = (NoteTakingActivity) getActivity();
    boolean isRecording = false;
    if (activity != null && activity.isRecording()) {
      isRecording = true;
    }
    String title = getString(isRecording ?
        R.string.action_bar_gallery_recording : R.string.action_bar_gallery);
    int selectedImagesCount = galleryAdapter.getSelectedImages().size();
    if (selectedImagesCount > 0) {
      title = String.format(getString(R.string.gallery_selected_title), selectedImagesCount);
    }
    if (activity instanceof RunReviewActivity) {
      if (currentTime != null && selectedImagesCount == 0) {
        return String.format(
            getString(R.string.add_gallery_note_to_time_text),
            currentTime);
      }
    }
    return title;
  }
}
