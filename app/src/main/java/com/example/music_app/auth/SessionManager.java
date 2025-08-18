package com.example.music_app.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ACTIVE_GROUP_ID = "active_group_id";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLoginSession(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USERNAME);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void setActiveGroupId(Integer groupId) {
        if (groupId == null) {
            editor.remove(KEY_ACTIVE_GROUP_ID);
        } else {
            editor.putInt(KEY_ACTIVE_GROUP_ID, groupId);
        }
        editor.apply();
    }

    public Integer getActiveGroupId() {
        if (!prefs.contains(KEY_ACTIVE_GROUP_ID)) return null;
        return prefs.getInt(KEY_ACTIVE_GROUP_ID, -1);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}

