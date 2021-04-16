package com.google.android.apps.forscience.whistlepunk.gdrivesync;

import android.content.Context;

import com.google.android.apps.forscience.utils.StringUtils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Property;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class GDriveApi {

    private static final String[] SCOPES = {
            DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA,
    };

    private final Context context;

    private final String folderId;

    private final Drive drive;

    GDriveApi(final Context context, final String email, final String folderId) {
        this.context = context.getApplicationContext();
        this.folderId = folderId;
        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final List<String> scopeList = Arrays.asList(SCOPES);
        final GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(this.context, scopeList)
                        .setBackOff(new ExponentialBackOff())
                        .setSelectedAccountName(email);
        this.drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName(this.context.getPackageName())
                .build();
    }

    List<RemoteExperiment> listRemoteExperiments() throws IOException {
        final List<RemoteExperiment> experiments = new ArrayList<>();
        final FileList files = drive.files().list().setQ("'" + folderId + "' in parents and trashed = false").execute();
        for (final File f : files.getItems()) {
            boolean isExperiment = false;
            boolean archived = false;
            String experimentId = null;
            long version = 0;
            final List<Property> properties = f.getProperties();
            if (properties != null) {
                for (final Property p : properties) {
                    final String key = p.getKey();
                    final String value = p.getValue();
                    if ("scienceJournalExperiment".equals(key) && "true".equals(value)) {
                        isExperiment = true;
                    } else if ("experimentID".equals(key) && !StringUtils.isEmpty(value)) {
                        experimentId = value;
                    } else if ("version".equals(key) && !StringUtils.isEmpty(value)) {
                        try {
                            version = Long.parseLong(value);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if ("isArchived".equals(key) && "true".equals(value)) {
                        archived = true;
                    }
                }
            }
            if (isExperiment && experimentId != null && version > 0) {
                final RemoteExperiment e = new RemoteExperiment();
                e.experimentId = experimentId;
                e.version = version;
                e.archived = archived;
                e.file = f;
                experiments.add(e);
            }
        }
        return experiments;
    }

    java.io.File downloadFile(final RemoteExperiment exp) throws IOException {
        final java.io.File ret = new java.io.File(context.getCacheDir(), "gds-" + UUID.randomUUID().toString() + ".sj");
        try (final FileOutputStream fos = new FileOutputStream(ret)) {
            drive.files().get(exp.file.getId()).executeMediaAndDownloadTo(fos);
            fos.flush();
        }
        return ret;
    }

    void insertFile(java.io.File file, final RemoteExperiment exp, final String title) throws IOException {
        final String fileName = UUID.randomUUID().toString() + ".sj";
        final FileContent content = new FileContent("application/zip", file);
        final File remoteFile = new File();
        remoteFile.setTitle(fileName);
        remoteFile.setParents(Collections.singletonList(new ParentReference().setId(folderId)));
        setProperty(remoteFile, "scienceJournalExperiment", "true");
        setProperty(remoteFile, "experimentID", String.valueOf(exp.experimentId));
        setProperty(remoteFile, "version", String.valueOf(exp.version));
        setProperty(remoteFile, "isArchived", String.valueOf(exp.archived));
        setProperty(remoteFile, "userAgent", "Android");
        drive.files().insert(remoteFile, content).execute();
        final FileList files = drive.files().list().setQ("title = '" + fileName + "' and '" + folderId + "' in parents").execute();
        if (files.getItems().size() == 0) {
            throw new IOException("Uploaded file not found!");
        }
        exp.file = files.getItems().get(0);
        renameFile(exp, title);
    }

    void updateFile(java.io.File file, final RemoteExperiment exp, final String title) throws IOException {
        setProperty(exp.file, "scienceJournalExperiment", "true");
        setProperty(exp.file, "experimentID", String.valueOf(exp.experimentId));
        setProperty(exp.file, "version", String.valueOf(exp.version));
        setProperty(exp.file, "isArchived", String.valueOf(exp.archived));
        setProperty(exp.file, "userAgent", "Android");
        final FileContent content = new FileContent("application/zip", file);
        drive.files().update(exp.file.getId(), exp.file, content).execute();
        renameFile(exp, title);
    }

    void renameFile(final RemoteExperiment exp, String title) throws IOException {
        final String fileName;
        if (StringUtils.isEmpty(title)) {
            fileName = "Untitled.sj";
        } else {
            fileName = title + ".sj";
        }
        final File newFile = new File();
        newFile.setTitle(fileName);
        drive.files().patch(exp.file.getId(), newFile).setFields("title").execute();
    }

    void deleteFile(final RemoteExperiment exp) throws IOException {
        drive.files().delete(exp.file.getId());
    }

    private void setProperty(final File file, final String key, final String value) {
        List<Property> properties = file.getProperties();
        Property property = null;
        if (properties != null) {
            for (final Property aux : properties) {
                if (key.equals(aux.getKey())) {
                    property = aux;
                    break;
                }
            }
        } else {
            properties = new ArrayList<>();
        }
        if (property == null) {
            property = new Property();
            property.setKey(key);
            property.setVisibility("PRIVATE");
            properties.add(property);
        }
        property.setValue(value);
        file.setProperties(properties);
    }

    static class RemoteExperiment {

        public String experimentId;

        public long version;

        public boolean archived;

        public File file;

        @Override
        public String toString() {
            return "RemoteExperiment{" +
                    "experimentId='" + experimentId + '\'' +
                    ", version=" + version +
                    ", archived=" + archived +
                    ", file=" + file.getId() +
                    '}';
        }
    }


}
