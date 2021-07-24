package com.lenefice.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button activateButton, button2Min, button5Min,
            button10Min, button30Min, buttonNever, cancelButton;
    private TextView textView;
    private CountDownTimer countDownTimer;
    private boolean success, activateStatus, b;
    private int defaultTimeOut;

    private boolean onePlus,asus,vivo,colme,samsung,gock,miui,aosp,others,checkNever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toMapComponents();
        askPermission();
        justStarted();
        detectDevice();

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateDeactivate();
            }
        });

        button2Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerOf(120000);
            }
        });

        button5Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerOf(300000);
            }
        });

        button10Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerOf(600000);
            }
        });

        button30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerOf(1800000);
            }
        });

        buttonNever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNever=true;
                startTimerOf(Integer.MAX_VALUE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTimer();
            }
        });

    }

    void toMapComponents() {
        textView = findViewById(R.id.textView);
        activateButton = findViewById(R.id.activateButton);
        button2Min = findViewById(R.id.button2Min);
        button5Min = findViewById(R.id.button5Min);
        button10Min = findViewById(R.id.button10Min);
        button30Min = findViewById(R.id.button30Min);
        buttonNever = findViewById(R.id.buttonNever);
        cancelButton = findViewById(R.id.cancelButton);
    }

    void askPermission() {
        boolean value;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            value = Settings.System.canWrite(getApplicationContext());

            if (value) {
                success = true;
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        } else {
            success = true;
        }

        if (success) {
            activateButton.setEnabled(true);
        } else {
            activateButton.setEnabled(false);
            Toast.makeText(MainActivity.this, "Please allow permission & launch the app again", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    void justStarted() {
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button10Min.setEnabled(false);
        button30Min.setEnabled(false);
        buttonNever.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    void detectDevice() {

        final String ONEPLUSCLOCK = "com.oneplus.deskclock";
        final String ASUS = "com.asus.deskclock";
        final String VIVO = "com.android.bbkclock";
        final String COLME = "com.coloros.alarmclock";
        final String SAMSUNG = "com.sec.android.app.clockpackage";
        final String GOCK = "com.google.android.deskclock";
        final String MIAO = "com.android.deskclock";
        final String MIUI = "com.miui.gallery";

        Context context = getApplicationContext();
        PackageManager packageManager = context.getPackageManager();

        if(appInstalledOrNot(context, ONEPLUSCLOCK) && isAppInSystemPartition(packageManager, ONEPLUSCLOCK)) {
            onePlus=true;
        }
        else if(appInstalledOrNot(context, ASUS) && isAppInSystemPartition(packageManager, ASUS)) {
            asus=true;
        }
        else if(appInstalledOrNot(context, VIVO) && isAppInSystemPartition(packageManager, VIVO)) {
            vivo=true;
        }
        else if(appInstalledOrNot(context, COLME) && isAppInSystemPartition(packageManager, COLME)) {
            colme=true;
        }
        else if(appInstalledOrNot(context, SAMSUNG) && isAppInSystemPartition(packageManager, SAMSUNG)) {
            samsung=true;
        }
        else if(appInstalledOrNot(context, GOCK) && isAppInSystemPartition(packageManager, GOCK)) {
            gock=true;
        }
        else if(appInstalledOrNot(context, MIAO) && isAppInSystemPartition(packageManager, MIAO)) {
            if(appInstalledOrNot(context, MIUI) && isAppInSystemPartition(packageManager, MIUI)) {
                miui=true;
            }
            else {
                aosp=true;
            }
        }
        else {
            others=true;
        }
    }

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        if (packageInfoList != null) {
            for (PackageInfo packageInfo : packageInfoList) {
                String packageName = packageInfo.packageName;
                if (packageName != null && packageName.equals(uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAppInSystemPartition(PackageManager pm, String packageName) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        }catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    void getDefaultTimeOut() {
        try {
            defaultTimeOut = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    void activateDeactivate() {
        if(activateStatus) {
            toDeactivate();
        }
        else {
            toActivate();
        }
    }

    private void setScreenTimeout(int milliseconds) {
        Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);
    }

    void showOptionsAsPerDevice() {
        if(onePlus||colme||vivo) {
            buttonNever.setEnabled(false);
        }
        else if(asus||samsung||others) {
            buttonNever.setEnabled(false);
            button30Min.setEnabled(false);
        }
        else if(miui) {
            button30Min.setEnabled(false);
        }
    }

    void toDeactivate() {
        activateButton.setEnabled(true);
        activateButton.setText("Activate");
        justStarted();
        setScreenTimeout(defaultTimeOut);
        textView.setText("Not Active");
        activateStatus=false;
        checkNever=false;
    }
    void toActivate() {
        getDefaultTimeOut();
        activateButton.setEnabled(true);
        activateButton.setText("Deactivate");
        setScreenTimeout(30000);
        button2Min.setEnabled(true);
        button5Min.setEnabled(true);
        button10Min.setEnabled(true);
        button30Min.setEnabled(true);
        buttonNever.setEnabled(true);
        cancelButton.setEnabled(false);
        showOptionsAsPerDevice();
        textView.setText("Choose Your Profile :-");
        activateStatus=true;
    }

    private void startTimer(int time) {
        countDownTimer = new CountDownTimer(time,1000) {
            @Override
            public void onTick(long l) {
                int minutes = (int) (l / 1000) / 60;
                int seconds = (int) (l / 1000) % 60;

                String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                if(checkNever) {
                    textView.setText("Infinite");
                }
                else {
                    textView.setText(timeLeft);
                }
            }

            @Override
            public void onFinish() {
                Settings.System.putInt(
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                textView.setText("Cool Down Timer of 35 seconds Please Wait");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toDeactivate();
                    }
                }, 35000);
            }
        }.start();
    }

    void asTimerStarted() {
        activateButton.setEnabled(false);
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button10Min.setEnabled(false);
        button30Min.setEnabled(false);
        buttonNever.setEnabled(false);
        cancelButton.setEnabled(true);
    }

    void startTimerOf(int time) {
        setScreenTimeout(time);
        startTimer(time);
        if(success) {
            asTimerStarted();
        }
        else {
            justStarted();
        }
    }

    void cancelTimer() {
        countDownTimer.cancel();
        justStarted();
        toDeactivate();
    }
}