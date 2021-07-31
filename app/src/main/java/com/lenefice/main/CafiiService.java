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
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.Locale;
import static com.lenefice.main.AppNotification.CHANNEL_ID;
import org.greenrobot.eventbus.EventBus;

public class CafiiService extends Service {

    private int defaultTimeOut, newScreenTimeOut;

    private SharedPreferences sharedPreferences;

    private CountDownTimer countDownTimer, coolDownTimer;

    private String timeLeft, toastNotify;;

    private boolean isCountDTRunning, isCoolDTRunning;

    @Override
    public void onCreate() {

        super.onCreate();
        getDefaultTimeOut();

        sharedPreferences = getSharedPreferences("Timer Presets", Context.MODE_PRIVATE);
        newScreenTimeOut = sharedPreferences.getInt("timer",0);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        customNotificationText();
        createNotification();

        if(newScreenTimeOut != Integer.MAX_VALUE)
        newScreenTimeOut *= 60000;

        setTimeOut(newScreenTimeOut);
        setTimer(newScreenTimeOut);

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {

        if (isCountDTRunning) {
            countDownTimer.cancel();
        }

        if (isCoolDTRunning) {
            coolDownTimer.cancel();
        }

        setTimeOut(defaultTimeOut);
        stopForeground(true);

        Toast.makeText(getApplicationContext(), "Timer Ended", Toast.LENGTH_SHORT).show();

        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDefaultTimeOut() {

        try {
            defaultTimeOut = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void customNotificationText() {

        switch(newScreenTimeOut) {

            case 2 :
                toastNotify = "2 minutes timer is running...";
                break;

            case 5 :
                toastNotify = "5 minutes timer is running...";
                break;

            case 10 :
                toastNotify = "10 minutes timer is running...";
                break;

            case 30 :
                toastNotify = "30 minutes timer is running...";
                break;

            case Integer.MAX_VALUE :
                toastNotify = "Infinity timer is running...";
                break;

        }

        Toast.makeText(getApplicationContext(), toastNotify, Toast.LENGTH_SHORT).show();

    }

    private void createNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        Notification foregroundNotification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Cafii Service")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(5)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(toastNotify + " Please do not force stop or remove from recents"))
                .build();

        startForeground(1,foregroundNotification);

    }

    private void setTimeOut(int milliseconds) {

        Settings.System.putInt(
                getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, milliseconds);

    }

    private void setTimer(int milliseconds) {

        isCountDTRunning = true;
        countDownTimer = new CountDownTimer(milliseconds,1000) {
            @Override
            public void onTick(long secondsTicking) {

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
                        stopSelf();
                    }
                }.start();
            }
        }.start();
    }

}
