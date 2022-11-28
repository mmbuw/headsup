package com.example.neginsharif.mdemo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "channel1";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
//reference : https://codinginflow.com/tutorials/android/notifications-notification-channels/part-1-notification-channels
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel
                    (CHANNEL_ID, "channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel 1 description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
}
