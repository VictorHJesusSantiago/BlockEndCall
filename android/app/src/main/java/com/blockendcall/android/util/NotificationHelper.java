package com.blockendcall.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.blockendcall.android.R;
import com.blockendcall.android.ui.CheckNumberActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHelper {

    public static final String CHANNEL_BLOCKED = "blocked_calls";
    public static final String CHANNEL_STATS   = "stats_updates";

    private static final AtomicInteger idCounter = new AtomicInteger(1000);

    public static void createChannels(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);

        NotificationChannel blocked = new NotificationChannel(
                CHANNEL_BLOCKED,
                "Chamadas Bloqueadas",
                NotificationManager.IMPORTANCE_HIGH);
        blocked.setDescription("Notificação quando uma chamada spam é bloqueada automaticamente");
        blocked.enableVibration(true);

        NotificationChannel stats = new NotificationChannel(
                CHANNEL_STATS,
                "Estatísticas",
                NotificationManager.IMPORTANCE_LOW);
        stats.setDescription("Resumo periódico de chamadas bloqueadas");

        nm.createNotificationChannel(blocked);
        nm.createNotificationChannel(stats);
    }

    public static void notifyBlockedCall(Context context, String phoneNumber, String category) {
        String categoryLabel = getCategoryLabel(category);
        String title = "🚫 Chamada Spam Bloqueada";
        String text = phoneNumber + " · " + categoryLabel;

        Intent intent = new Intent(context, CheckNumberActivity.class);
        intent.putExtra("prefill_number", phoneNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_BLOCKED)
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Número " + phoneNumber + " identificado como " + categoryLabel
                                + " e bloqueado automaticamente pela comunidade BlockEndCall."))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFFC62828)
                .build();

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.notify(idCounter.getAndIncrement(), notification);

        BlockedCallLog.save(context, phoneNumber, category);
    }

    private static String getCategoryLabel(String category) {
        if (category == null) return "Spam";
        switch (category) {
            case "TELEMARKETING":  return "Telemarketing";
            case "SCAM":           return "Golpe/Scam";
            case "ROBOCALL":       return "Robocall";
            case "DEBT_COLLECTOR": return "Cobrança";
            case "PHISHING":       return "Phishing";
            default:               return "Spam";
        }
    }
}
