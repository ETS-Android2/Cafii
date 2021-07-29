package com.lenefice.main;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

import static com.lenefice.main.AppNotification.CHANNEL_ID;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CafiiService extends Service {

    private static final String TAG = "service";
    private int defaultTimeOut;

    private SharedPreferences sharedPreferences;

    private CountDownTimer countDownTimer, coolDownTimer;

    private String timeLeft;

    private boolean isCountDTRunning, isCoolDTRunning, isCancelled;

    private int newScreenTimeOut;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service created");
        getDefaultTimeOut();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service started");
        createNotification();
        sharedPreferences = getSharedPreferences("Timer Presets", Context.MODE_PRIVATE);
        newScreenTimeOut = sharedPreferences.getInt("timer",0);
        setTimeOut(newScreenTimeOut);
        setTimer(newScreenTimeOut);
        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    void setTimeOut(int milliseconds) {
        Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);
    }

    void setTimer(int milliseconds) {

        isCountDTRunning = true;
        countDownTimer = new CountDownTimer(milliseconds,1000) {
            @Override
            public void onTick(long secondsTicking) {
                Log.d(TAG, "timer"+secondsTicking);
                if(newScreenTimeOut!=Integer.MAX_VALUE) {
                    int minutes = (int) (secondsTicking / 1000) / 60;
                    int seconds = (int) (secondsTicking / 1000) % 60;

                    timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                }
                else {
                    timeLeft = "infinity";
                }

                EventBus.getDefault().post(new EventTimer(timeLeft));
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
                        EventBus.getDefault().post(new EventTimer("Cool Down Timer of 35 seconds is running Please wait..."));
                    }
                    @Override
                    public void onFinish() {
                        isCoolDTRunning = false;
                        EventBus.getDefault().post(new AutoKilled(true));
                        endOfService();
                    }
                }.start();
            }
        }.start();
    }

    void endOfService() {
        setTimeOut(defaultTimeOut);
        stopForeground(true);
        EventBus.getDefault().unregister(this);
        stopSelf();
    }

    @Subscribe
    public void killService(OnCancelEvent event) {
        isCancelled=event.getValue();
        if(isCancelled) {
            if (isCountDTRunning) {
                countDownTimer.cancel();
            }
            if (isCoolDTRunning) {
                coolDownTimer.cancel();
            }
            endOfService();
        }
    }

}
