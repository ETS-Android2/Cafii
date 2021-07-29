package com.lenefice.main;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

import static com.lenefice.main.AppNotification.CHANNEL_ID;

public class CafiiService extends Service {

    private static final String TAG = "service";
    private int defaultTimeOut;

    private SharedPreferences sharedPreferences;

    private CountDownTimer countDownTimer, coolDownTimer;

    private String timeLeft;

    private boolean isCountDTRunning, isCoolDTRunning;

    private final IBinder cafiiBinder = new CafiiBinder();

    public class CafiiBinder extends Binder {

        CafiiService getService() {
            return CafiiService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cafiiBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service created");
        getDefaultTimeOut();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service started");
        createNotification();
        sharedPreferences = getSharedPreferences("Timer Presets", Context.MODE_PRIVATE);
        int newScreenTimeOut = sharedPreferences.getInt("timer",0);
        setTimeOutAndTimer(newScreenTimeOut);
        return START_STICKY;

    }

    void getDefaultTimeOut() {
        try {
            defaultTimeOut = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    void createNotification() {
        Log.d(TAG, "createNotification: ");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        Notification foregroundNotification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Cafii Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,foregroundNotification);

    }

    void setTimeOutAndTimer(int milliseconds) {
        Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);

        isCountDTRunning = true;
        countDownTimer = new CountDownTimer(milliseconds,1000) {
            @Override
            public void onTick(long secondsTicking) {
                Log.d(TAG, "timer"+secondsTicking);
                int minutes = (int) (secondsTicking / 1000) / 60;
                int seconds = (int) (secondsTicking / 1000) % 60;

                timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            }

            @Override
            public void onFinish() {
                isCountDTRunning = false;
                Settings.System.putInt(
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                isCoolDTRunning = true;
                coolDownTimer = new CountDownTimer(35000,1000) {
                    @Override
                    public void onTick(long coolDownTicks) {
                    }

                    @Override
                    public void onFinish() {
                        isCoolDTRunning = false;
                    }
                }.start();
            }
        }.start();
    }

    public String getCTD() {
        return timeLeft;
    }

}
