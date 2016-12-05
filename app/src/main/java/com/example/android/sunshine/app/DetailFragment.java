/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;
    public static final int COL_WEATHER_CITY = 11;

    private ImageView mIconView;
    private TextView mCityView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private RelativeLayout graphic;
    private List<TextView> dates = new ArrayList<>();

    private List<TextView> degresLow = new ArrayList<>();
    private List<TextView> degresHigh = new ArrayList<>();

    private List<Temperature> maListe;
    private List<Temperature> listGraph;

    private long dateAujourdhui;

    private TextView legendeMin;
    private TextView legendeMax;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mCityView = (TextView) rootView.findViewById(R.id.detail_city_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        maListe = DetailActivity.getMaListe();
        listGraph = new ArrayList<>(maListe);

        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date1));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date2));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date3));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date4));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date5));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date6));
        dates.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_date7));

        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre1_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre2_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre3_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre4_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre5_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre6_low));
        degresLow.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre7_low));

        for (int i = 0; i < degresLow.size(); i++) {
            degresLow.get(i).setText(String.format("%.1f",listGraph.get(i).low) + "°");
        }

        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre1_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre2_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre3_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre4_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre5_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre6_high));
        degresHigh.add((TextView) rootView.findViewById(R.id.fragment_detail_textView_degre7_high));

        for (int i = 0; i < degresHigh.size(); i++) {
            degresHigh.get(i).setText(String.format("%.1f",listGraph.get(i).high) + "°");
        }

        legendeMin = (TextView) rootView.findViewById(R.id.fragment_detail_textView_minLegende);
        legendeMax = (TextView) rootView.findViewById(R.id.fragment_detail_textView_maxLegende);

        graphic = (RelativeLayout) rootView.findViewById(R.id.fragment_detail_relativeLayout_graphic);
        graphic.addView(new Rectangle(getActivity()));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        MenuItem mShare = menu.findItem(R.id.detail_action_share);

        mShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //mShareActionProvider.setShareIntent(createShareForecastIntent());
                createShareForecastIntent();
                return true;
            }
        });
    }

    private Intent createShareForecastIntent() {

        View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = getScreenShot(rootView);
        File file = store(bitmap, "screenshot.png");

        return shareImage(file);
    }

    private Intent shareImage(File file){

        Uri uri = Uri.fromFile(file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
        return shareIntent;
    }

    private Bitmap getScreenShot(View rootView) {
        View screenView = rootView.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static File store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged(String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            // Use weather art image
            mIconView.setBackgroundResource(Utility.getArtResourceForWeatherCondition(weatherId));
            AnimationDrawable animation = (AnimationDrawable) mIconView.getBackground();
            animation.start();

            String cityName = data.getString(COL_WEATHER_CITY);
            mCityView.setText(cityName);
            // Read date from cursor and update views for day of week and date
            dateAujourdhui = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), dateAujourdhui);
            String dateText = Utility.getFormattedMonthDay(getActivity(), dateAujourdhui);
            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            // Read description from cursor and update view
            String description = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);

            // For accessibility, add a content description to the icon field
            mIconView.setContentDescription(description);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high);
            mHighTempView.setText(highString);

            // Read low temperature from cursor and update view
            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low);
            mLowTempView.setText(lowString);

            // Read humidity from cursor and update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view
            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            // Read pressure from cursor and update view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            // We still need this for the share intent
            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private class Rectangle extends View{
        Paint paint = new Paint();

        final Paint paintLow = new Paint();
        final Paint paintHigh = new Paint();
        final Paint paintAjourdhui = new Paint();

        private List<Temperature> temperatures;

        public Rectangle(Context context) {
            super(context);

            maListe = DetailActivity.getMaListe();
            temperatures = new ArrayList<>(maListe);

            paintLow.setColor(Color.BLUE);
            paintLow.setAlpha(255);
            paintLow.setStrokeWidth(5.0f);

            paintHigh.setColor(Color.RED);
            paintHigh.setAlpha(255);
            paintHigh.setStrokeWidth(5.0f);
        }

        @Override
        public void onDraw(Canvas canvas) {
            int graphicHeight = graphic.getHeight();
            int graphicWidth = graphic.getWidth();
            setTextViewDate();
            dessinerSeparateurs(canvas, graphicWidth, graphicHeight);
            dessinerBoite(canvas, graphicWidth, graphicHeight);

            //calcule des points sur le graphic
            double tempMinY = getMinLow(temperatures);
            double tempMaxY = getMaxHigh(temperatures);

            if (tempMinY < 0) {
                double difference = Math.abs(tempMinY);

                for (Temperature temperature : temperatures) {
                    temperature.low += difference;
                    temperature.high += difference;
                }

                tempMaxY += difference;
            } else if (tempMinY > 0) {
                double difference = tempMinY;

                for (Temperature temperature : temperatures) {
                    temperature.low -= difference;
                    temperature.high -= difference;
                }

                tempMaxY -= difference;
            }

            float milieuX = graphicWidth / 14;

            int padding = 150;
            int viewPort = graphicHeight - padding;

            for (int i = 0; i < temperatures.size(); i++) {
                double posYLow = getYPos(temperatures.get(i).low, tempMaxY, viewPort, padding);
                double posYHigh = getYPos(temperatures.get(i).high, tempMaxY, viewPort, padding);

                canvas.drawCircle(milieuX, (float) posYLow, 10, paintLow);
                canvas.drawCircle(milieuX, (float) posYHigh, 10, paintHigh);

                if (i != 0) {
                    float posYLowPrecedent = (float)getYPos(temperatures.get(i - 1).low, tempMaxY, viewPort, padding);
                    float posYHighPrecedent = (float)getYPos(temperatures.get(i - 1).high, tempMaxY, viewPort, padding);

                    float xPrecedent = milieuX - (graphicWidth / 7);
                    canvas.drawLine(xPrecedent, posYLowPrecedent, milieuX, (float)posYLow, paintLow);
                    canvas.drawLine(xPrecedent, posYHighPrecedent, milieuX, (float)posYHigh, paintHigh);
                } else {
                    setTextViewDegres(tempMaxY, viewPort, padding);
                }

                milieuX += graphicWidth / 7;
            }
        }

        private void dessinerSeparateurs(Canvas canvas, int width, int height) {
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(5f);

            //lignes du graphic
            float xLine = width / 7;

            for (int i = 0; i < temperatures.size(); i++) {
                if (temperatures.get(i).date == dateAujourdhui) {
                    paintAjourdhui.setColor(Color.YELLOW);
                    paintAjourdhui.setAlpha(75);
                    paintAjourdhui.setStrokeWidth(width / 7);

                    double line;
                    if (i == 0) {
                        line = (width / 14) * (i + 1);
                    } else {
                        line = ((width / 7) * (i + 1)) - (width / 14) + 2.5;
                    }
                    canvas.drawLine((float)line, 0, (float)line, height - 5, paintAjourdhui);
                }

                canvas.drawLine(xLine, 0, xLine, height - 5, paint);
                xLine += width / 7;
            }
        }

        private void dessinerBoite(Canvas canvas, int width, int height) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10f);

            //lignes noires autour du graphic
            canvas.drawLine(1, 0, 1, height, paint);
            canvas.drawLine(0, height, width, height, paint);

            //légende des couleurs
            paintLow.setStrokeWidth(30f);
            paintHigh.setStrokeWidth(30f);
            canvas.drawLine(width - (legendeMin.getWidth() * 4.3f), height - (legendeMin.getHeight() / 1.5f), width - (legendeMin.getWidth() * 4.3f), height - 10, paintLow);
            canvas.drawLine(width - (legendeMax.getWidth() * 1.8f), height - (legendeMax.getHeight() / 1.5f), width - (legendeMax.getWidth() * 1.8f), height - 10, paintHigh);
            paintLow.setStrokeWidth(5.0f);
            paintHigh.setStrokeWidth(5.0f);
        }

        /**
         * Calcule la position d'un point relative à un viewport.
         * @param temp Le point à calculer.
         * @param maxScaleY La hauteur maximale pour les proportions.
         * @param viewHeight La hauteur de l'affichage.
         * @param padding L'espacement haut et bas (globalisé) à respecter.
         * @return La position Y du point.
         */
        private double getYPos(double temp, double maxScaleY, double viewHeight, double padding) {
            double yPos = temp / maxScaleY * viewHeight;
            yPos = Math.abs(yPos - viewHeight); // Flip the graphic.
            yPos += (padding / 2);
            return yPos;
        }

        private double getMinLow(List<Temperature> liste) {
            double minValue = liste.get(0).low;

            for (int i = 1; i < liste.size(); i++) {
                if (minValue > liste.get(i).low) {
                    minValue = liste.get(i).low;
                }
            }
            return minValue;
        }

        private double getMaxHigh(List<Temperature> liste) {
            double maxValue = liste.get(0).high;

            for (int i = 1; i < liste.size(); i++) {
                if (maxValue < liste.get(i).high) {
                    maxValue = liste.get(i).high;
                }
            }
            return maxValue;
        }

        private void setTextViewDate() {
            DateFormat df = getShortDateInstanceWithoutYears(Locale.getDefault());

            int graphicWidth = graphic.getWidth();

            int x = graphicWidth / 35;
            int y = 0;
            for (int i = 0; i < temperatures.size(); i++) {
                Date date = new Date(temperatures.get(i).date);
                dates.get(i).setText(df.format(date));

                dates.get(i).setX(x);
                dates.get(i).setY(y);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
                lp.setMargins(x, y, 0, 0);

                dates.get(i).setLayoutParams(lp);

                x += graphicWidth / 7;
            }
        }

        public DateFormat getShortDateInstanceWithoutYears(Locale locale) {
            SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
            sdf.applyPattern(sdf.toPattern().replaceAll("[^\\p{Alpha}]*y+[^\\p{Alpha}]*", ""));
            return sdf;
        }

        private void setTextViewDegres(double tempMaxY, int viewPort, int padding) {
            int graphicWidth = graphic.getWidth();

            int x = graphicWidth / 21;
            int y;

            for (int i = 0; i < degresLow.size(); i++) {
                y = ((int) getYPos(temperatures.get(i).low, tempMaxY, viewPort, padding)) + 10;
                degresLow.get(i).setX(x);
                degresLow.get(i).setY(y);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
                lp.setMargins(x, y, 0, 0);

                degresLow.get(i).setLayoutParams(lp);

                x += graphicWidth / 7;
            }

            x = graphicWidth / 21;

            for (int i = 0; i < degresHigh.size(); i++) {
                y = ((int) getYPos(temperatures.get(i).high, tempMaxY, viewPort, padding)) + 10;

                degresHigh.get(i).setX(x);
                degresHigh.get(i).setY(y);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
                lp.setMargins(x, y, 0, 0);

                degresHigh.get(i).setLayoutParams(lp);

                x += graphicWidth / 7;
            }
        }
    }
}