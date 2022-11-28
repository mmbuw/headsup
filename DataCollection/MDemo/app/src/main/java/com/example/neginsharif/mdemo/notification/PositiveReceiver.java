package com.example.neginsharif.mdemo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.neginsharif.mdemo.datamanagement.ExternalStorage;
import com.example.neginsharif.mdemo.datamanagement.InternalStorage;

public class PositiveReceiver extends BroadcastReceiver {

    private InternalStorage internalStorage= new InternalStorage();
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean response = intent.getBooleanExtra("response", true);
        Message msg = new Message(response);
        Toast.makeText(context,
                msg.getTimestamp() + " positive broadcast " + msg.getResponse(),
                Toast.LENGTH_SHORT)
                .show();
        internalStorage.store( msg.getTimestamp() + "positive " + "\n" ,"NotificationResponse");
    }
}
