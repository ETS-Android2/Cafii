package com.lenefice.cafii;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button2Min, button5Min,
            button10Min, button30Min, buttonNever, cancelButton, buttonInfo;

    private TextView textView;
    private long pressedTime;

    private boolean success, onePlus, asus, vivo, colme, samsung,
            gock, miui, aosp, huawei, others;

    private Intent myService;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toMapComponents();
        askPermission();
        detectDevice();

    }

    @Override
    protected void onStart() {

        super.onStart();

        buttonsAreEnabled();
        button2Min.setOnClickListener(this);
        button5Min.setOnClickListener(this);
        button10Min.setOnClickListener(this);
        button30Min.setOnClickListener(this);
        buttonNever.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        buttonInfo.setOnClickListener(this);

        if(isMyServiceRunning()) {

            EventBus.getDefault().register(this);
            myService = new Intent(this, CafiiService.class);
            buttonsAreDisabled();

        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch(view.getId()) {

            case R.id.button2Min:
                sendDataStartService(2);
                break;

            case R.id.button5Min:
                sendDataStartService(5);
                break;

            case R.id.button10Min:
                sendDataStartService(10);
                break;

            case R.id.button30Min:
                sendDataStartService(30);
                break;

            case R.id.buttonNever:
                sendDataStartService(Integer.MAX_VALUE);
                break;

            case R.id.cancelButton:
                cancelTriggered();
                buttonsAreEnabled();
                break;

            case R.id.buttonInfo:
                showNoticeDialog();
                break;

        }

    }

    @Override
    protected void onStop() {

        super.onStop();

        if(isMyServiceRunning()) {
            Toast.makeText(getApplicationContext(), "Please do not remove from recents", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().unregister(this);
        }

    }

    @Override
    public void onBackPressed() {

        if(isMyServiceRunning()) {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                cancelTriggered();
                Toast.makeText(MainActivity.this, R.string.TIMER_STOP, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), R.string.PRESS_BACK, Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onBackPressed();
            finish();
        }
        pressedTime = System.currentTimeMillis();

    }

    private void toMapComponents() {

        textView = findViewById(R.id.textView);
        button2Min = findViewById(R.id.button2Min);
        button5Min = findViewById(R.id.button5Min);
        button10Min = findViewById(R.id.button10Min);
        button30Min = findViewById(R.id.button30Min);
        buttonNever = findViewById(R.id.buttonNever);
        cancelButton = findViewById(R.id.cancelButton);
        buttonInfo = findViewById(R.id.buttonInfo);

    }

    private void askPermission() {

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
        }
        else {
            success = true;
        }

        if(!success) {
            Toast.makeText(MainActivity.this, R.string.ALLOW_PERM, Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private static boolean appInstalledOrNot(Context context, String uri) {

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

    private static boolean isAppInSystemPartition(PackageManager pm, String packageName) {

        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }

    private void detectDevice() {

        final String ONEPLUSCLOCK = "com.oneplus.deskclock",
                ASUS = "com.asus.deskclock",
                VIVO = "com.android.BBKClock",
                COLME = "com.coloros.alarmclock",
                SAMSUNG = "com.sec.android.app.clockpackage",
                GOCK = "com.google.android.deskclock",
                MIAO = "com.android.deskclock",
                MIUI = "com.miui.gallery",
                HUAWEI = "com.huawei.appmarket";

        Context context = getApplicationContext();
        PackageManager packageManager = context.getPackageManager();

        if(appInstalledOrNot(context, ONEPLUSCLOCK) && isAppInSystemPartition(packageManager, ONEPLUSCLOCK)) {
            onePlus = true;
        }
        else if(appInstalledOrNot(context, ASUS) && isAppInSystemPartition(packageManager, ASUS)) {
            asus = true;
        }
        else if(appInstalledOrNot(context, VIVO) && isAppInSystemPartition(packageManager, VIVO)) {
            vivo = true;
        }
        else if(appInstalledOrNot(context, COLME) && isAppInSystemPartition(packageManager, COLME)) {
            colme = true;
        }
        else if(appInstalledOrNot(context, SAMSUNG) && isAppInSystemPartition(packageManager, SAMSUNG)) {
            samsung = true;
        }
        else if(appInstalledOrNot(context, GOCK) && isAppInSystemPartition(packageManager, GOCK)) {
            gock = true;
        }
        else if(appInstalledOrNot(context, MIAO) && isAppInSystemPartition(packageManager, MIAO)) {
            if(appInstalledOrNot(context, MIUI) && isAppInSystemPartition(packageManager, MIUI)) {
                miui = true;
            }
            else if(appInstalledOrNot(context, HUAWEI) && isAppInSystemPartition(packageManager, HUAWEI)) {
                huawei = true;
            }
            else {
                aosp = true;
            }
        }
        else {
            others = true;
        }

    }

    private void buttonsAreDisabled() {

        button2Min.setEnabled(false);
        button2Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        button5Min.setEnabled(false);
        button5Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        button10Min.setEnabled(false);
        button10Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        button30Min.setEnabled(false);
        button30Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        buttonNever.setEnabled(false);
        buttonNever.setTextColor(getApplication().getResources().getColor(R.color.grey));
        cancelButton.setVisibility(View.VISIBLE);

    }

    private void showOptionsAsPerDevice() {

        if(onePlus || colme || vivo) {
            buttonNever.setEnabled(false);
            buttonNever.setTextColor(getApplication().getResources().getColor(R.color.grey));
        }
        else if(asus || samsung || huawei || others) {
            buttonNever.setEnabled(false);
            buttonNever.setTextColor(getApplication().getResources().getColor(R.color.grey));
            button30Min.setEnabled(false);
            button30Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        }
        else if(miui) {
            button30Min.setEnabled(false);
            button30Min.setTextColor(getApplication().getResources().getColor(R.color.grey));
        }
        else if(gock || aosp) {
            buttonNever.setEnabled(true);
            buttonNever.setTextColor(getApplication().getResources().getColor(R.color.white));
            button30Min.setEnabled(true);
            button30Min.setTextColor(getApplication().getResources().getColor(R.color.white));
        }

    }

    private void buttonsAreEnabled() {

        textView.setText(R.string.SELECT_PROFILE);
        cancelButton.setVisibility(View.INVISIBLE);

        button2Min.setEnabled(true);
        button2Min.setTextColor(getApplication().getResources().getColor(R.color.white));

        button5Min.setEnabled(true);
        button5Min.setTextColor(getApplication().getResources().getColor(R.color.white));

        button10Min.setEnabled(true);
        button10Min.setTextColor(getApplication().getResources().getColor(R.color.white));

        button30Min.setEnabled(true);
        button30Min.setTextColor(getApplication().getResources().getColor(R.color.white));

        buttonNever.setEnabled(true);
        buttonNever.setTextColor(getApplication().getResources().getColor(R.color.white));

        showOptionsAsPerDevice();

    }

    private void sendDataStartService(int time) {

        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = getSharedPreferences("Timer Presets",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        myService = new Intent(this, CafiiService.class);
        editor.putInt("timer", time);
        editor.apply();

        EventBus.getDefault().register(this);
        startService(myService);
        buttonsAreDisabled();

    }

    private boolean isMyServiceRunning() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CafiiService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;

    }

    @Subscribe
    public void onEventStart(EventTimer object) {

        textView.setText(object.getCurrentTime());

    }

    @Subscribe
    public void onAutoKilled(AutoKilled killed) {

        if(killed.getKilled()) {
            buttonsAreEnabled();
        }

    }

    private void showNoticeDialog() {

        AlertDialog info = new AlertDialog.Builder(this)
                .setTitle(R.string.INFOTITLE)
                .setMessage(R.string.INFO)
                .create();
        info.show();

    }

    private void cancelTriggered() {

        EventBus.getDefault().unregister(this);
        stopService(myService);

    }

}