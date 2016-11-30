package com.example.android.sunshine.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Helene on 2016-11-29.
 */
public class Temperature implements Parcelable {

    public int weatherId = 0;

    public long date = 0;

    public String description = "";

    public double high = 0;

    public double low = 0;

    protected Temperature(Parcel in) {
        weatherId = in.readInt();
        date = in.readLong();
        description = in.readString();
        high = in.readDouble();
        low = in.readDouble();
    }

    public static final Creator<Temperature> CREATOR = new Creator<Temperature>() {
        @Override
        public Temperature createFromParcel(Parcel in) {
            return new Temperature(in);
        }

        @Override
        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };

    public Temperature() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(weatherId);
        dest.writeLong(date);
        dest.writeString(description);
        dest.writeDouble(high);
        dest.writeDouble(low);
    }
}
