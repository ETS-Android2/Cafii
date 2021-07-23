package com.lenefice.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button activateButton, button2Min, button5Min, button15Min, button30Min, button1Hour, cancelButton;
    private TextView textView;
    private CountDownTimer countDownTimer;

    private int defaultTimeOut;
    private boolean success, activateStatus=false;

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
            defaultTimeOut = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        activateButton.setEnabled(true);
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button15Min.setEnabled(false);
        button30Min.setEnabled(false);
        button1Hour.setEnabled(false);
        cancelButton.setEnabled(false);

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activateStatus) {
                    activateOnClick();
                }
                else {
                    activateButton.setEnabled(true);
                    activateButton.setText("Deactivate");
                    setScreenTimeout(30001);

                    int tempCheck = 0;
                    try {
                       tempCheck = Settings.System.getInt(getContentResolver(),
                                Settings.System.SCREEN_OFF_TIMEOUT);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(tempCheck!=30001) {
                        setScreenTimeout(30001);
                        recreate();
                    }

                    button2Min.setEnabled(true);
                    button5Min.setEnabled(true);
                    button15Min.setEnabled(true);
                    button30Min.setEnabled(true);
                    button1Hour.setEnabled(true);
                    cancelButton.setEnabled(false);
                    textView.setText("Choose Your Profile :-");
                    activateStatus=true;
                }
            }
        });

        button2Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(120000);
                if(success) {
                    presetTimer();
                }
                else {
                    noSuccess();
                }
            }
        });

        button5Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(300000);
                if(success) {
                    presetTimer();
                }
                else {
                    noSuccess();
                }
            }
        });

        button15Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(900000);
                if(success) {
                    presetTimer();
                }
                else {
                    noSuccess();
                }
            }
        });

        button30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(1800000);
                if(success) {
                    presetTimer();
                }
                else {
                    noSuccess();
                }
            }
        });

        button1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScreenTimeout(7200000);
                startTimer(3600000);
                if(success) {
                    presetTimer();
                }
                else {
                    noSuccess();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();
                textView.setText("Not Active");
                resetScreenTimeOut();
                activateButton.setEnabled(true);
                activateButton.setText("Activate");
                button2Min.setEnabled(false);
                button5Min.setEnabled(false);
                button15Min.setEnabled(false);
                button30Min.setEnabled(false);
                button1Hour.setEnabled(false);
                cancelButton.setEnabled(false);
                activateStatus=false;
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
        countDownTimer = new CountDownTimer(time,1000) {
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
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30001);
                textView.setText("Cool Down Timer of 35 seconds Please Wait");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activateOnClick();
                    }
                }, 35000);
            }
        }.start();
    }

    void presetTimer() {
        activateButton.setEnabled(false);
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button15Min.setEnabled(false);
        button30Min.setEnabled(false);
        button1Hour.setEnabled(false);
        cancelButton.setEnabled(true);
    }
    void noSuccess() {
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button15Min.setEnabled(false);
        button30Min.setEnabled(false);
        button1Hour.setEnabled(false);
        cancelButton.setEnabled(false);
    }
    void resetScreenTimeOut() {
        Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defaultTimeOut);
    }
    void activateOnClick() {
        activateButton.setEnabled(true);
        activateButton.setText("Activate");
        setScreenTimeout(defaultTimeOut);
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button15Min.setEnabled(false);
        button30Min.setEnabled(false);
        button1Hour.setEnabled(false);
        cancelButton.setEnabled(false);
        textView.setText("Not Active");
        activateStatus=false;
    }
}