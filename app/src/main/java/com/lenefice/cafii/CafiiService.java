package com.lenefice.cafii;

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
import static com.lenefice.cafii.AppNotification.CHANNEL_ID;
import org.greenrobot.eventbus.EventBus;

import es.dmoral.toasty.Toasty;

public class CafiiService extends Service {

    private int defaultTimeOut, newScreenTimeOut;

    private CountDownTimer countDownTimer, coolDownTimer;

    private String timeLeft, toastNotify;

    private boolean isCountDTRunning, isCoolDTRunning;

    @Override
    public void onCreate() {

        super.onCreate();
        getDefaultTimeOut();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.PRESETS), Context.MODE_PRIVATE);
        newScreenTimeOut = sharedPreferences.getInt(getString(R.string.TIMER),0);

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
        Toasty.custom(this, getString(R.string.TIMER_STOP),
                R.drawable.toast_icon_wrong, R.color.toastcolorred,
                Toast.LENGTH_SHORT, true, true).show();

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
                toastNotify = getString(R.string.MIN2RUN);
                break;

            case 5 :
                toastNotify = getString(R.string.MIN5RUN);
                break;

            case 10 :
                toastNotify = getString(R.string.MIN10RUN);
                break;

            case 30 :
                toastNotify = getString(R.string.MIN30RUN);
                break;

            case Integer.MAX_VALUE :
                toastNotify = getString(R.string.INFIRUN);
                break;

        }

        Toasty.custom(this, toastNotify, R.drawable.toast_icon,
                R.color.toastcolor, Toast.LENGTH_SHORT, true,
                true).show();

    }

    private void createNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification foregroundNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.SERVICE))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(5)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(toastNotify + getString(R.string.REMOVE_RECENTS)))
                .build();

        startForeground(1, foregroundNotification);

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

                if(newScreenTimeOut != Integer.MAX_VALUE) {
                    int minutes = (int) (secondsTicking / 1000) / 60;
                    int seconds = (int) (secondsTicking / 1000) % 60;

                    timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                }
                else {
                    timeLeft = getResources().getString(R.string.INFINITY);
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
                        EventBus.getDefault().post(new EventTimer(getResources().getString(R.string.COOL_DOWN)));
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
