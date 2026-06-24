package com.blockendcall.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BlockedCallLog {

    private static final String PREF_NAME = "blocked_call_log";
    private static final String KEY_CALLS = "calls_json";
    private static final int MAX_ENTRIES = 100;
    private static final Gson GSON = new Gson();

    public static class Entry {
        public String phoneNumber;
        public String category;
        public String timestamp;

        public Entry(String phoneNumber, String category, String timestamp) {
            this.phoneNumber = phoneNumber;
            this.category = category;
            this.timestamp = timestamp;
        }
    }

    public static void save(Context context, String phoneNumber, String category) {
        List<Entry> calls = getAll(context);
        String ts = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        calls.add(0, new Entry(phoneNumber, category, ts));

        if (calls.size() > MAX_ENTRIES) {
            calls = calls.subList(0, MAX_ENTRIES);
        }

        prefs(context).edit()
                .putString(KEY_CALLS, GSON.toJson(calls))
                .apply();
    }

    public static List<Entry> getAll(Context context) {
        String json = prefs(context).getString(KEY_CALLS, null);
        if (json == null) return new ArrayList<>();
        List<Entry> result = GSON.fromJson(json, new TypeToken<List<Entry>>() {}.getType());
        return result != null ? result : new ArrayList<>();
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_CALLS).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
