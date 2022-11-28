package com.example.neginsharif.mdemo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.neginsharif.mdemo.datamanagement.InternalStorage;

public class NegativeReceiver extends BroadcastReceiver {
    private InternalStorage internalStorage = new InternalStorage();
    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean response = intent.getBooleanExtra("response", false);
        Message msg = new Message(response);
        Toast.makeText(context,
                msg.getTimestamp() + " negative broadcast " + msg.getResponse(),
                Toast.LENGTH_SHORT)
                .show();
        internalStorage.store( msg.getTimestamp() + "negative " + "\n" ,"NotificationResponse");


   }
}
