package com.example.note1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;


public class CollectionWidget extends AppWidgetProvider {

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget);

        Intent adapter = new Intent(context, WidgetService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.widget_list, adapter);

        // Нажатие на "Заметки"
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setAction(Intent.ACTION_MAIN);
        mainIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pMainIntent = PendingIntent.getActivity(context, appWidgetId,
                mainIntent, 0);
        views.setOnClickPendingIntent(R.id.textView1, pMainIntent);

        // Нажатие на "+"
        Intent newNoteIntent = new Intent(context, EditNoteActivity.class);
        newNoteIntent.putExtra (EditNoteActivity.EXTRA_NOTE_ID, "");
        PendingIntent pnewNoteIntent = PendingIntent.getActivity(context, appWidgetId,
                newNoteIntent, 0);
        views.setOnClickPendingIntent(R.id.textView2, pnewNoteIntent);

        // Нажатие на элемент из списка
        Intent listClickIntent = new Intent(context, EditNoteActivity.class);
        PendingIntent listClickPIntent = PendingIntent.getActivity (context, 0,
                listClickIntent, 0);
        views.setPendingIntentTemplate(R.id.widget_list, listClickPIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled (context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled (context);
    }

    // Метод для обновления списка ListView из активностей MainActivity и EditNoteActivity
    public static void updateNote1Widget(Context context) {
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CollectionWidget.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
}
