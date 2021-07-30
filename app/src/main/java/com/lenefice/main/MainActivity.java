package com.lenefice.main;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button2Min, button5Min,
            button10Min, button30Min, buttonNever, cancelButton;

    private FloatingActionButton buttonInfo;

    private TextView textView;

    private boolean success,onePlus,asus,vivo,colme,samsung,
            gock,miui,aosp,others;

    private Intent myService;

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

            case R.id.buttonInfo:
                showNoticeDialog();
                break;

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        if(isMyServiceRunning())
        EventBus.getDefault().unregister(this);

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
        }catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }

    private void detectDevice() {

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

    private void buttonsAreDisabled() {

        button2Min.setEnabled(false);
        button5Min.setEnabled(false);
        button10Min.setEnabled(false);
        button30Min.setEnabled(false);
        buttonNever.setEnabled(false);
        cancelButton.setEnabled(true);

    }

    private void showOptionsAsPerDevice() {

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

    private void buttonsAreEnabled() {

        textView.setText("Click the desired preset :- ");
        cancelButton.setEnabled(false);
        button2Min.setEnabled(true);
        button5Min.setEnabled(true);
        button10Min.setEnabled(true);
        button30Min.setEnabled(true);
        buttonNever.setEnabled(true);
        showOptionsAsPerDevice();

    }

    private void sendDataStartService(int time) {

        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = getSharedPreferences("Timer Presets",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        myService = new Intent(this, CafiiService.class);
        editor.putInt("timer",time);
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
                .setTitle("Must Read")
                .setMessage("Himank")
                .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss()).create();
        info.show();

    }

}