package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class SunShineWidget extends AppWidgetProvider {



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {

            int widgetId = appWidgetIds[i];

            Temperature todayWeather = DetailActivity.getMaListe().get(0);
            int weatherId = todayWeather.weatherId;

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.sun_shine_widget);

            remoteViews.setImageViewResource(R.id.widget_item_icon, Utility.getArtResourceForWeatherCondition(weatherId));

            long dateAujourdhui = todayWeather.date;
            String friendlyDateText = Utility.getDayName(context, dateAujourdhui);
            String dateText = Utility.getFormattedMonthDay(context, dateAujourdhui);

            remoteViews.setTextViewText(R.id.widget_item_date_textview, friendlyDateText);

            String description = Utility.getShortDescriptionForWeatherCondition(context, weatherId);
            remoteViews.setTextViewText(R.id.widget_item_forecast_textview, description);

            String highString = Utility.formatTemperature(context, todayWeather.high);
            remoteViews.setTextViewText(R.id.widget_item_high_textview, highString );

            String lowString = Utility.formatTemperature(context, todayWeather.low);
            remoteViews.setTextViewText(R.id.widget_item_low_textview,lowString);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

