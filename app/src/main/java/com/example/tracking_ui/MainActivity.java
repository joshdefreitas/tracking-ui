package com.example.tracking_ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity<MyActivity> extends AppCompatActivity {

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove action bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);
        //change orientation ot landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        startButton = findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlgorithm();
            }
        });

    }

    //Onclick method for start button
    public void startAlgorithm() {
        Intent intent = new Intent(
                MainActivity.this,
                ShowWebChartActivity.class);
        intent.putExtra("NUM1", 20);
        intent.putExtra("NUM2", 20);
        intent.putExtra("NUM3", 20);
        intent.putExtra("NUM4", 20);
        intent.putExtra("NUM5", 20);

        startActivity(intent);

    }
}
