package com.google.android.apps.forscience.whistlepunk.gdrivesync.api;

public class GDriveFile {

    public String id;

    public String name;

    public String mimeType;

    public String iconLink;

    public boolean isFolder;

    @Override
    public String toString() {
        return "GDriveFile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", iconLink='" + iconLink + '\'' +
                ", isFolder=" + isFolder +
                '}';
    }

}
