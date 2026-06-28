package com.blockendcall.android.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class ScheduledBlockingReceiver extends BroadcastReceiver {
    public static final String ACTION_ENABLE = "com.blockendcall.ACTION_ENABLE_BLOCK";
    public static final String ACTION_DISABLE = "com.blockendcall.ACTION_DISABLE_BLOCK";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        SharedPreferences prefs = ctx.getSharedPreferences("blockendcall_settings", Context.MODE_PRIVATE);
        if (ACTION_ENABLE.equals(intent.getAction())) {
            prefs.edit().putBoolean("scheduled_block_active", true).apply();
        } else if (ACTION_DISABLE.equals(intent.getAction())) {
            prefs.edit().putBoolean("scheduled_block_active", false).apply();
        }
    }

    public static void schedule(Context ctx, int startHour, int endHour) {
        cancel(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Calendar enable = Calendar.getInstance();
        enable.set(Calendar.HOUR_OF_DAY, startHour);
        enable.set(Calendar.MINUTE, 0);
        enable.set(Calendar.SECOND, 0);
        if (enable.before(Calendar.getInstance())) enable.add(Calendar.DAY_OF_YEAR, 1);

        Calendar disable = Calendar.getInstance();
        disable.set(Calendar.HOUR_OF_DAY, endHour);
        disable.set(Calendar.MINUTE, 0);
        disable.set(Calendar.SECOND, 0);
        if (disable.before(Calendar.getInstance())) disable.add(Calendar.DAY_OF_YEAR, 1);

        Intent enableIntent = new Intent(ctx, ScheduledBlockingReceiver.class).setAction(ACTION_ENABLE);
        Intent disableIntent = new Intent(ctx, ScheduledBlockingReceiver.class).setAction(ACTION_DISABLE);
        PendingIntent enablePi = PendingIntent.getBroadcast(ctx, 1001, enableIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent disablePi = PendingIntent.getBroadcast(ctx, 1002, disableIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC_WAKEUP, enable.getTimeInMillis(), AlarmManager.INTERVAL_DAY, enablePi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, disable.getTimeInMillis(), AlarmManager.INTERVAL_DAY, disablePi);
    }

    public static void cancel(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent enableIntent = new Intent(ctx, ScheduledBlockingReceiver.class).setAction(ACTION_ENABLE);
        Intent disableIntent = new Intent(ctx, ScheduledBlockingReceiver.class).setAction(ACTION_DISABLE);
        PendingIntent enablePi = PendingIntent.getBroadcast(ctx, 1001, enableIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent disablePi = PendingIntent.getBroadcast(ctx, 1002, disableIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(enablePi);
        am.cancel(disablePi);
    }
}
