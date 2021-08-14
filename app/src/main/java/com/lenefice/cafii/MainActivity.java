/**
 * <h1>CAfii - The Run Time Genii</h1>
 * This is the default and only Activity
 * for Cafii where user will interact.
 * Every UI component will be shown here.
 *
 * @author  Ishaan Kaushal & Himank Bose
 * @version 5.9.5
 * @since   2021-07-22
 */

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
import android.os.Build;
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
import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button2Min, button5Min,
            button10Min, button30Min, buttonNever, cancelButton, buttonInfo;

    private TextView textView;
    private long pressedTime;

    private boolean success, onePlus, asus, vivo, colme, samsung,
            gock, miui, aosp, huawei, others, above10;

    private Intent myService;

    /**
     * This method creates MainActivity from activity_main.xml.
     * This method is executed just after the app starts.
     * This will fully set the UI portion of App with all permission & detection.
     * Override annotation will override this method in AppCompatActivity.
     * @param savedInstanceState The savedInstanceState is a reference to a Bundle object.
     */

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toMapComponents();
        detectAndroidVersion();
        askPermission();
        detectDevice();

    }

    /**
     * After onCreate method or onStop method this will be executed.
     * This will show UI status according to service running or not.
     * It will also register the EventBus events coming from service.
     * Override annotation will override this method in AppCompatActivity.
     */

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

    /**
     * This method will be executed on tap of any Clickable component.
     * As soon as any clickable component is tapped its id will be
     * selected according to its switch and will execute particular case.
     * Override annotation will override this method in View.OnClickListener.
     */

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

    /**
     * This method will be executed after minimizing the App or pressing back.
     * This will check if service is running it will show appropriate Toast
     * and will unregister the EventBus for time being to prevent memory leak.
     * Override annotation will override this method in AppCompatActivity.
     */

    @Override
    protected void onStop() {

        super.onStop();

        if(isMyServiceRunning()) {
            if(above10) {
                Toast.makeText(this, R.string.RECENTS, Toast.LENGTH_SHORT).show();
            }
            else {
                Toasty.custom(this, getString(R.string.RECENTS),
                        R.drawable.toast_info, R.color.toastblue,
                        Toast.LENGTH_SHORT, true, true).show();
                EventBus.getDefault().unregister(this);
            }
        }

    }

    /**
     * This method will be executed if back button is pressed.
     * This will check if service is running it will show appropriate Toast
     * and will stop service and exit if back button is pressed twice
     * Override annotation will override this method in AppCompatActivity.
     */

    @Override
    public void onBackPressed() {

        if(isMyServiceRunning()) {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                cancelTriggered();


                if(above10) {
                    Toast.makeText(this, R.string.TIMER_STOP, Toast.LENGTH_SHORT).show();
                }
                else {
                        Toasty.custom(this, getString(R.string.TIMER_STOP),
                                R.drawable.toast_icon_wrong, R.color.toastcolorred,
                                Toast.LENGTH_SHORT, true, true).show();
                    }

            } else {
                if(above10) {
                    Toast.makeText(this, R.string.PRESS_BACK, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toasty.custom(this, getString(R.string.PRESS_BACK),
                            R.drawable.toast_info, R.color.toastblue,
                            Toast.LENGTH_SHORT, true, true).show();
                }
            }
        }
        else {
            super.onBackPressed();
            finish();
        }
        pressedTime = System.currentTimeMillis();

    }

    /**
     * This method will map the components in activity_main.xml to
     * MainActivity.java
     */

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

    /**
     * This method will map the components in activity_main.xml to
     * MainActivity.java
     */

    private void askPermission() {

        boolean value;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            value = Settings.System.canWrite(getApplicationContext());

            if (value) {
                success = true;
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse(getString(R.string.PACKAGE) + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }
        else {
            success = true;
        }

        if(!success) {
            if(above10) {
                    Toast.makeText(this, R.string.ALLOW_PERM, Toast.LENGTH_SHORT).show();
                }
            else {
                Toasty.custom(this, getString(R.string.ALLOW_PERM),
                        R.drawable.toast_info, R.color.toastblue,
                        Toast.LENGTH_SHORT, true, true).show();
            }
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

        final String ONEPLUSCLOCK = getString(R.string.ONEPLUS),
                ASUS = getString(R.string.ASUS),
                VIVO = getString(R.string.VIVO),
                COLME = getString(R.string.COLME),
                SAMSUNG = getString(R.string.SAMSUNG),
                GOCK = getString(R.string.GOCK),
                MIAO = getString(R.string.MIAO),
                MIUI = getString(R.string.MIUI),
                HUAWEI = getString(R.string.HUAWEI);

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

    private void detectAndroidVersion() {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            above10 = true;
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
        sharedPreferences = getSharedPreferences(getString(R.string.PRESETS),Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        myService = new Intent(this, CafiiService.class);
        editor.putInt(getString(R.string.TIMER), time);
        editor.putBoolean(getString(R.string.ABOVE10), above10);
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

    public void onClickLogo(View v) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                (getString(R.string.LENEFICE)));
        startActivity(browserIntent);

    }

}