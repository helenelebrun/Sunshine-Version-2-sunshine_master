package com.example.android.sunshine.app.SplashScreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView img = (ImageView) findViewById(R.id.activity_splash_imageView_splash);

        AnimationDrawable animation = (AnimationDrawable) img.getDrawable();
        animation.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent openMainActivity =  new Intent(Splash.this, MainActivity.class);
                startActivity(openMainActivity);
                finish();
            }
        }, 3000);
    }
}
