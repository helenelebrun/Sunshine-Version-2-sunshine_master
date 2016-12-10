package com.example.android.sunshine.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Implementation of App Widget functionality.
 */
public class SunShineWidget extends AppWidgetProvider {

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_CITY_NAME = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int count = appWidgetIds.length;




        for (int i = 0; i < count; i++) {

            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.sun_shine_widget);

            updateData(context, remoteViews);
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

    private void updateData(Context context, RemoteViews remoteViews)
    {

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        String locationSetting = Utility.getPreferredLocation(context);

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cursor = context.getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);


        if(cursor.getCount() > 0){
            cursor.moveToFirst();

            int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);


            String cityName = cursor.getString(COL_CITY_NAME);

            remoteViews.setTextViewText(R.id.widget_item_city_textview, cityName);
            remoteViews.setImageViewResource(R.id.widget_item_image, Utility.getArtResourceForWeatherCondition(weatherId));

            long dateAujourdhui = cursor.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getFriendlyDayString(context, dateAujourdhui);

            remoteViews.setTextViewText(R.id.widget_item_date_textview, friendlyDateText);

            String description = Utility.getShortDescriptionForWeatherCondition(context, weatherId);
            remoteViews.setTextViewText(R.id.widget_item_forecast_textview, description);

            double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(context, high);
            remoteViews.setTextViewText(R.id.widget_item_high_textview, highString);

            double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(context, low);
            remoteViews.setTextViewText(R.id.widget_item_low_textview, lowString);
        }



    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(
                AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.sun_shine_widget);
            watchWidget = new ComponentName(context, SunShineWidget.class);

            updateData(context, remoteViews);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        }
    }
}

