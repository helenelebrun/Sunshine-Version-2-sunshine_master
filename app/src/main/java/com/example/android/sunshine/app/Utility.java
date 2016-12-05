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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utility {

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        double longitude = Double.parseDouble(
                prefs.getString(context.getString(R.string.pref_longitude_key),
                        context.getString(R.string.pref_longitude_default)));

        double latitude =  Double.parseDouble(
                prefs.getString(context.getString(R.string.pref_latitude_key),
                        context.getString(R.string.pref_latitude_default)));

        Location location = new Location("");
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> address = null;
        String locationQuery = "";

        if (geocoder != null) {


            try {
                address = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (address != null && address.size() > 0) {
                locationQuery = address.get(0).getPostalCode();
            }
        }


        return locationQuery;
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                     in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            //return R.drawable.art_storm;
            return R.drawable.xml_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            //return R.drawable.art_light_rain;
            return R.drawable.xml_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            //return R.drawable.art_rain;
            return R.drawable.xml_rain;
        } else if (weatherId == 511) {
            //return R.drawable.art_snow;
            return R.drawable.xml_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            //return R.drawable.art_rain;
            return R.drawable.xml_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            //return R.drawable.art_snow;
            return R.drawable.xml_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            //return R.drawable.art_fog;
            return R.drawable.xml_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            //return R.drawable.art_storm;
            return R.drawable.xml_storm;
        } else if (weatherId == 800) {
            //return R.drawable.art_clear;
            return R.drawable.xml_sun;
        } else if (weatherId == 801) {
            //return R.drawable.art_light_clouds;
            return R.drawable.xml_sun_cloud;
        } else if (weatherId >= 802 && weatherId <= 804) {
            //return R.drawable.art_clouds;
            return R.drawable.xml_cloud;
        }
        return -1;
    }

    public static String getShortDescriptionForWeatherCondition(Context context, int weatherId)
    {
        if (weatherId >= 200 && weatherId <= 232) {
            //return R.drawable.art_storm;
            return context.getString(R.string.short_desc_snow);
        } else if (weatherId >= 300 && weatherId <= 321) {
            //return R.drawable.art_light_rain;
            return context.getString(R.string.short_desc_light_rain);
        } else if (weatherId >= 500 && weatherId <= 504) {
            //return R.drawable.art_rain;
            return context.getString(R.string.short_desc_rain);
        } else if (weatherId == 511) {
            //return R.drawable.art_snow;
            return context.getString(R.string.short_desc_snow);
        } else if (weatherId >= 520 && weatherId <= 531) {
            //return R.drawable.art_rain;
            return  context.getString(R.string.short_desc_rain);
        } else if (weatherId >= 600 && weatherId <= 622) {
            //return R.drawable.art_snow;
            return  context.getString(R.string.short_desc_snow);
        } else if (weatherId >= 701 && weatherId <= 761) {
            //return R.drawable.art_fog;
            return  context.getString(R.string.short_desc_fog);
        } else if (weatherId == 761 || weatherId == 781) {
            //return R.drawable.art_storm;
            return  context.getString(R.string.short_desc_storm);
        } else if (weatherId == 800) {
            //return R.drawable.art_clear;
            return  context.getString(R.string.short_desc_sun);
        } else if (weatherId == 801) {
            //return R.drawable.art_light_clouds;
            return  context.getString(R.string.short_desc_partly_cloudy);
        } else if (weatherId >= 802 && weatherId <= 804) {
            //return R.drawable.art_clouds;
            return  context.getString(R.string.short_desc_cloud);
        }
        return "";
    }

    public static int getBackgroundColorForWeatherCondition(int weatherId) {

        if (weatherId >= 200 && weatherId <= 232) {
            return R.color.weather_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.color.weather_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.color.weather_rain;
        } else if (weatherId == 511) {
            return R.color.weather_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.color.weather_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.color.weather_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.color.weather_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.color.weather_storm;
        } else if (weatherId == 800) {
            return R.color.weather_clear;
        } else if (weatherId == 801) {
            return R.color.weather_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.color.weather_clouds;
        }
        return -1;
    }

    public static int getBackgroundColor(int compteur) {

        int id_color = 0;

        if (compteur == 0) {
            id_color = R.color.color3;
        } else if (compteur == 1) {
            id_color = R.color.color1;
        } else if (compteur == 2) {
            id_color = R.color.color2;
        } else if (compteur == 3) {
            id_color = R.color.color0;
        }
        return id_color;
    }
}