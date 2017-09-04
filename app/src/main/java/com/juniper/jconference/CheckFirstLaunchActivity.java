package com.juniper.jconference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CheckFirstLaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings=getSharedPreferences("prefs",0);
        boolean firstRun=settings.getBoolean("firstRun",false);
        if(firstRun==false)//if running for first time
        //Splash will load for first time
        {
            SharedPreferences.Editor editor=settings.edit();
            editor.putBoolean("firstRun",true);
            editor.commit();
            Intent i=new Intent(CheckFirstLaunchActivity.this,SplashScreenActivity.class);
            startActivity(i);
            finish();
        }
        else
        {

            Intent a=new Intent(CheckFirstLaunchActivity.this,JdialerStartupActivity.class);
            startActivity(a);  // Launch next activity
            finish();
        }
    }
}
