package com.blockendcall.android.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "blockendcall_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, long userId, String name, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public void saveUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, null); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, null); }
    public boolean isLoggedIn() { return getToken() != null; }
}
