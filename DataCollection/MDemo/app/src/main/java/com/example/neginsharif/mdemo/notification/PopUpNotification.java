package com.example.neginsharif.mdemo.notification;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.neginsharif.mdemo.MainActivity;
import com.example.neginsharif.mdemo.R;

import static com.example.neginsharif.mdemo.App.CHANNEL_ID;

public class PopUpNotification {

    NotificationManagerCompat notificationManager;
    Context context;

    public PopUpNotification(Context context) {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
    }


    public void sendNotification() {
//reference : https://codinginflow.com/tutorials/android/notifications-notification-channels/part-1-notification-channels
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);


        Intent posIntent = new Intent(context, PositiveReceiver.class);
        posIntent.putExtra("response", true);
        PendingIntent pendingPosIntent = PendingIntent.getBroadcast(
                context,
                0,
                posIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Intent negIntent = new Intent(context, NegativeReceiver.class);
        posIntent.putExtra("response", false);
        PendingIntent pendingNegIntent = PendingIntent.getBroadcast(
                context,
                1,
                negIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("OBJECT")
                .setContentText("Is an object closing to you?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                //.setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "yes", pendingPosIntent)
                .addAction(R.mipmap.ic_launcher, "no", pendingNegIntent)
                .build();

        notificationManager.notify(1, notification);

    }

}
