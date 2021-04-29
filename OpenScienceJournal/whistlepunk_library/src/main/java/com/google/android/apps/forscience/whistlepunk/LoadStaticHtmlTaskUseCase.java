package com.google.android.apps.forscience.whistlepunk;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStreamReader;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;

/**
 * Loads static HTML files into string data.
 */
public class LoadStaticHtmlTaskUseCase {
    private static final String TAG = "LoadStaticHtmlTask";
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final Scheduler operationScheduler;
    private final Scheduler notificationScheduler;

    public LoadStaticHtmlTaskUseCase(@NonNull Scheduler operationScheduler, @NonNull Scheduler notificationScheduler) {
        this.operationScheduler = operationScheduler;
        this.notificationScheduler = notificationScheduler;
    }

    public void invoke(@NonNull Resources resources, int fileId, @NonNull DisposableSingleObserver<String> observer) {
        disposable.add(performOperation(resources, fileId)
                .subscribeOn(operationScheduler)
                .observeOn(notificationScheduler)
                .subscribeWith(observer));
    }

    private Single<String> performOperation(@NonNull Resources resources, int fileId) {
        return Single.create(emitter -> {
            InputStreamReader inputReader = null;
            // This will store the HTML file as a string. 4kB seems comfortable: this string will
            // get released when we're done with this activity.
            StringBuilder data = new StringBuilder(4096);
            try {
                // Read the resource in 2kB chunks.
                char[] tmp = new char[2048];
                int numRead;

                inputReader = new InputStreamReader(resources.openRawResource(fileId));
                while ((numRead = inputReader.read(tmp)) >= 0) {
                    data.append(tmp, 0, numRead);
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not read static HTML page", e);
            } finally {
                try {
                    if (inputReader != null) {
                        inputReader.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not close stream", e);
                }
            }
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Loaded string of " + data.length());
            }
            emitter.onSuccess(data.toString());
        });
    }

    public void discard() {
        disposable.clear();
    }
}
