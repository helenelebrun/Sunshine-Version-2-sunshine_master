package com.example.android.sunshine.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by TheFrenchOne on 12/6/2016.
 */
public class LocationDialogPreference extends DialogPreference {


    private EditText txtLongitude;
    private EditText txtLatitude;

    private double longitude;
    private double latitude;


    public LocationDialogPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        setPersistent(false);

        setDialogLayoutResource(R.layout.location_dialog_preference);
    }

    @Override
    protected View onCreateDialogView() {

        View view = super.onCreateDialogView();

        txtLongitude = (EditText) view.findViewById(R.id.location_pref_txt_longitude);
        txtLatitude = (EditText) view.findViewById(R.id.location_pref_txt_latitude);

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        longitude = Double.parseDouble(txtLongitude.getText().toString());
        latitude =  Double.parseDouble(txtLatitude.getText().toString());
        super.onDismiss(dialog);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SharedPreferences.Editor editor = getEditor();
            Resources res = getContext().getResources();
            editor.putString(res.getString(R.string.pref_longitude_key), txtLongitude.getText().toString());
            editor.putString(res.getString(R.string.pref_latitude_key), txtLatitude.getText().toString());
            editor.commit();
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Resources res = getContext().getResources();
        txtLongitude.setText(getSharedPreferences().getString(res.getString(R.string.pref_longitude_key).toString(), res.getString(R.string.pref_longitude_default).toString()));
        txtLatitude.setText(getSharedPreferences().getString(res.getString(R.string.pref_latitude_key).toString(), res.getString(R.string.pref_latitude_default).toString()));

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return Double.parseDouble(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            Resources res = getContext().getResources();
            txtLatitude.setText(res.getString(R.string.pref_latitude_default).toString());
            txtLongitude.setText(res.getString(R.string.pref_longitude_default).toString());
        } else {

        }
    }




}
