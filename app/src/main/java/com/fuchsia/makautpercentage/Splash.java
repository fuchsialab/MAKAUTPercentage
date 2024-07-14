package com.fuchsia.makautpercentage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        hideNavigationbar();

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                Intent i = new Intent(Splash.this, MainActivity.class); startActivity(i);
                finish(); } }, 500);
    }
    private void hideNavigationbar(){
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        );
    }

}