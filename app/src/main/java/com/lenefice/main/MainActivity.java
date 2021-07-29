package com.lenefice.main;

import android.app.ActivityManager;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "main";

    private Button button2Min, button5Min,
            button10Min, button30Min, buttonNever, cancelButton;

    private TextView textView;

    private boolean success,onePlus,asus,vivo,colme,samsung,
            gock,miui,aosp,others;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "activity created ");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        toMapComponents();
        askPermission();
        detectDevice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        buttonsAreEnabled();
        Log.d(TAG, "Activity started");
        button2Min.setOnClickListener(this);
        button5Min.setOnClickListener(this);
        button10Min.setOnClickListener(this);
        button30Min.setOnClickListener(this);
        buttonNever.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        if(isMyServiceRunning(CafiiService.class)) {
            EventBus.getDefault().register(this);
            Log.d(TAG, "check for service running");
            myService = new Intent(this, CafiiService.class);
            buttonsAreDisabled();
        }
        else {

        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch(view.getId()) {
            case R.id.button2Min:
                sendDataStartService(120000);
                break;

            case R.id.button5Min:
                sendDataStartService(300000);
                break;

            case R.id.button10Min:
                sendDataStartService(600000);
                break;

            case R.id.button30Min:
                sendDataStartService(1800000);
                break;
            case R.id.buttonNever:
                sendDataStartService(Integer.MAX_VALUE);
                break;
            case R.id.cancelButton:
                EventBus.getDefault().post(new OnCancelEvent(true));
                buttonsAreEnabled();
                EventBus.getDefault().unregister(this);
                break;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(isMyServiceRunning(CafiiService.class))
        EventBus.getDefault().unregister(this);
    }

    void toMapComponents() {
        Log.d(TAG, "toMapComponents: ");
        textView = findViewById(R.id.textView);
        button2Min = findViewById(R.id.button2Min);
        button5Min = findViewById(R.id.button5Min);
        button10Min = findViewById(R.id.button10Min);
        button30Min = findViewById(R.id.button30Min);
        buttonNever = findViewById(R.id.buttonNever);
        cancelButton = findViewById(R.id.cancelButton);
    }

    void askPermission() {
        Log.d(TAG, "askPermission: ");
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
        } else {
            Toast.makeText(MainActivity.this, R.string.ALLOW_PERM, Toast.LENGTH_LONG).show();
            finish();
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
            else {
                aosp = true;
            }
        }
        else {
            others = true;
        }
    }

    void buttonsAreDisabled() {
        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button10Min.setEnabled(false);
        button30Min.setEnabled(false);
        buttonNever.setEnabled(false);
        cancelButton.setEnabled(true);
    }

    void showOptionsAsPerDevice() {
        if(onePlus || colme || vivo) {
            buttonNever.setEnabled(false);
        }
        else if(asus || samsung || others) {
            buttonNever.setEnabled(false);
            button30Min.setEnabled(false);
        }
        else if(miui) {
            button30Min.setEnabled(false);
        }
        else if(gock || aosp) {
            buttonNever.setEnabled(true);
            button30Min.setEnabled(true);
        }
    }

    void buttonsAreEnabled() {
        textView.setText("Click the desired preset :- ");
        cancelButton.setEnabled(false);
        button2Min.setEnabled(true);
        button5Min.setEnabled(true);
        button10Min.setEnabled(true);
        button30Min.setEnabled(true);
        buttonNever.setEnabled(true);
        showOptionsAsPerDevice();
    }

    void sendDataStartService(int time) {
        Log.d(TAG, "sendDataStartService: ");
        sharedPreferences = getSharedPreferences("Timer Presets",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        myService = new Intent(this, CafiiService.class);
        editor.putInt("timer",time);
        editor.commit();
        EventBus.getDefault().register(this);
        startService(myService);
        buttonsAreDisabled();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
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
}