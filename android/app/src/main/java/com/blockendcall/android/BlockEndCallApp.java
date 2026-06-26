package com.blockendcall.android;

import android.app.Application;
import com.blockendcall.android.util.NotificationHelper;

public class BlockEndCallApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
    }
}
