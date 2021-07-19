package com.google.android.apps.forscience.whistlepunk;

/** Activity request codes used in calls to startActivityForResult. */
public final class ActivityRequestCodes {
  public static final int REQUEST_ONBOARDING_ACTIVITY = 2;
    public static final int REQUEST_SIGN_IN_ACTIVITY = 3;
  public static final int REQUEST_ACCOUNT_SWITCHER = 4;
  public static final int REQUEST_OLD_USER_OPTION_PROMPT_ACTIVITY = 5;

  public static final int REQUEST_TAKE_PHOTO = 6;
  public static final int REQUEST_SELECT_PHOTO = 7;

  public static final int REQUEST_ARDUINO_SIGN_IN = 9;

  public static final int REQUEST_NEW_TERMS_ACTIVITY = 10;

  private ActivityRequestCodes() {}
}
