package com.lenefice.cafii;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class AppNotification extends Application {
    public static final String CHANNEL_ID = "Cafii";

    @Override
    public void onCreate() {
        super.onCreate();

        createMyNotification();
    }

    private void createMyNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cafii",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }

    }
}
