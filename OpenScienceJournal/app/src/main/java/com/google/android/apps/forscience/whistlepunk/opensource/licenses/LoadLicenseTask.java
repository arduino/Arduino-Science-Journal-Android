package com.google.android.apps.forscience.whistlepunk.opensource.licenses;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.apps.forscience.whistlepunk.opensource.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;

public class LoadLicenseTask {
    public static class Args {
        @NonNull
        final Resources resources;

        @NonNull
        final String tag_license;

        @NonNull
        final String attribute_key;

        @NonNull
        final String tag_title;

        @NonNull
        final String tag_resource;

        @NonNull
        final String tag_header;

        @NonNull
        final Comparator<LicenseActivity.License> comparator;

        public Args(@NonNull Resources resources, @NonNull String tag_license, @NonNull String attribute_key, @NonNull String tag_title, @NonNull String tag_resource, @NonNull String tag_header, @NonNull Comparator<LicenseActivity.License> comparator) {
            this.resources = resources;
            this.tag_license = tag_license;
            this.attribute_key = attribute_key;
            this.tag_title = tag_title;
            this.tag_resource = tag_resource;
            this.tag_header = tag_header;
            this.comparator = comparator;
        }
    }

    public static final String TAG = "TAG";

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final Scheduler operationScheduler;
    private final Scheduler notificationScheduler;

    public LoadLicenseTask(@NonNull Scheduler operationScheduler, @NonNull Scheduler notificationScheduler) {
        this.operationScheduler = operationScheduler;
        this.notificationScheduler = notificationScheduler;
    }

    public void invoke(@NonNull Args args, @NonNull DisposableSingleObserver<List<LicenseActivity.License>> observer) {
        disposable.add(performOperation(args)
                .subscribeOn(operationScheduler)
                .observeOn(notificationScheduler)
                .subscribeWith(observer));
    }

    @NonNull
    private Single<List<LicenseActivity.License>> performOperation(@NonNull Args args) {
        return getLicenses(args);
    }

    private Single<List<LicenseActivity.License>> getLicenses(@NonNull Args args) {
        return Single.create(e -> {
            DocumentBuilder builder = null;
            List<LicenseActivity.License> licenses = new ArrayList<>();
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document licenseDoc = builder.parse(args.resources.openRawResource(R.raw.license_list));
                NodeList licenseNodes = licenseDoc.getElementsByTagName(args.tag_license);
                final int size = licenseNodes.getLength();
                for (int index = 0; index < size; index++) {
                    Node licenseNode = licenseNodes.item(index);
                    LicenseActivity.License license = new LicenseActivity.License();
                    license.key = licenseNode.getAttributes().getNamedItem(args.attribute_key).getNodeValue();
                    Node childNode = licenseNode.getFirstChild();
                    while (childNode != null) {
                        String nodeName = childNode.getNodeName();
                        String nodeValue =
                                childNode.getFirstChild() != null ? childNode.getFirstChild().getNodeValue() : null;
                        if (args.tag_title.equals(nodeName)) {
                            license.title = nodeValue;
                        } else if (args.tag_resource.equals(nodeName)) {
                            license.resource = nodeValue;
                        } else if (args.tag_header.equals(nodeName)) {
                            license.copyrightHeader = nodeValue;
                        }
                        childNode = childNode.getNextSibling();
                    }
                    if (license.isValid()) {
                        licenses.add(license);
                    } else {
                        Log.e(TAG, "Not adding invalid license: " + license);
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException exception) {
                Log.e(TAG, "Could not parse license file.", exception);
            }
            Collections.sort(licenses, args.comparator);
            e.onSuccess(licenses);
        });
    }

    public void discard() {
        disposable.clear();
    }
}
