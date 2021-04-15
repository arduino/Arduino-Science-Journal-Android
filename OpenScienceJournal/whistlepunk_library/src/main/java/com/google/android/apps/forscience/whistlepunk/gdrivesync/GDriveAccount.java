package com.google.android.apps.forscience.whistlepunk.gdrivesync;

public class GDriveAccount {

    public String accountId;

    public String email;

    public String token;

    public String folderId;

    public String folderPath;

    public GDriveAccount() {
    }

    public GDriveAccount(String accountId, String email, String token, String folderId, String folderPath) {
        this.accountId = accountId;
        this.email = email;
        this.token = token;
        this.folderId = folderId;
        this.folderPath = folderPath;
    }
}
