package com.blockendcall.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.blockendcall.android.R;
import com.blockendcall.android.ui.CheckNumberActivity;

public class BlockEndCallWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int id : widgetIds) {
            updateWidget(context, manager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_block_end_call);
        views.setTextViewText(R.id.widget_title, "BlockEndCall");
        views.setTextViewText(R.id.widget_status, "Toque para verificar");

        Intent checkIntent = new Intent(context, CheckNumberActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, checkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_check_btn, pendingIntent);

        manager.updateAppWidget(widgetId, views);
    }
}
