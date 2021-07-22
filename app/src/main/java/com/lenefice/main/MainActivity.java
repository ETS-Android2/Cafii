package com.lenefice.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button activateButton, button2Min, button5Min, button15Min, button30Min, button1Hour, cancelButton;
    private TextView textView;

    private int testValue;
    private boolean activateStatus;
    boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        activateButton = findViewById(R.id.activateButton);
        button2Min = findViewById(R.id.button2Min);
        button5Min = findViewById(R.id.button5Min);
        button15Min = findViewById(R.id.button15Min);
        button30Min = findViewById(R.id.button30Min);
        button1Hour = findViewById(R.id.button1Hour);
        cancelButton = findViewById(R.id.cancelButton);

        try {
            testValue = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if(testValue != 60000) {
            activateButton.setEnabled(true);
            button2Min.setEnabled(false);
            button5Min.setEnabled(false);
            button15Min.setEnabled(false);
            button30Min.setEnabled(false);
            button1Hour.setEnabled(false);
            cancelButton.setEnabled(false);
            activateStatus=false;
            activateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setScreenTimeout(60000);
                    Toast.makeText(MainActivity.this, "30 Minutes", Toast.LENGTH_SHORT).show();
                    activateButton.setEnabled(false);
                    recreate();
                    activateButton.setText("Deactivate");
                    button2Min.setEnabled(true);
                    button5Min.setEnabled(true);
                    button15Min.setEnabled(true);
                    button30Min.setEnabled(true);
                    button1Hour.setEnabled(true);
                    cancelButton.setEnabled(false);
                }
            });
        }
        else {
            activateButton.setEnabled(false);
            activateButton.setText("Deactivate");
            button2Min.setEnabled(true);
            button5Min.setEnabled(true);
            button15Min.setEnabled(true);
            button30Min.setEnabled(true);
            button1Hour.setEnabled(true);
            cancelButton.setEnabled(false);
        }

        button2Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(120000);
                if(success) {
                    activateButton.setEnabled(false);
                    button2Min.setEnabled(false);
                    button5Min.setEnabled(false);
                    button15Min.setEnabled(false);
                    button30Min.setEnabled(false);
                    button1Hour.setEnabled(false);
                    cancelButton.setEnabled(true);
                }
            }
        });
    }

    private void setScreenTimeout(int milliseconds) {

        boolean value;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            value = Settings.System.canWrite(getApplicationContext());

            if (value) {
                Settings.System.putInt(
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);
                success = true;
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        } else {
            Settings.System.putInt(
                    getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);
            success = true;
        }
    }

    private void startTimer(int time) {
        CountDownTimer countDownTimer = new CountDownTimer(time,1000) {
            @Override
            public void onTick(long l) {
                int minutes = (int) (l / 1000) / 60;
                int seconds = (int) (l / 1000) % 60;

                String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                textView.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                Settings.System.putInt(
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
            }
        }.start();
    }
}