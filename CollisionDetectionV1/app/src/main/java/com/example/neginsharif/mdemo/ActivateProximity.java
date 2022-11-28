package com.example.neginsharif.mdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class ActivateProximity {


    SensorManager mySensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    Activity context;

    public ActivateProximity(Context context) {
        this.context = (Activity)context;
    }

    public void activate() {
        mySensorManager = (SensorManager) context.getSystemService(
                context.SENSOR_SERVICE);
        sensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[0] < sensor.getMaximumRange()) {
                    Toast.makeText(context, "Proximiy sensor maximum value: " + event.values[0], Toast.LENGTH_SHORT).show();
                    context.getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                }else {
                 //   Toast.makeText(context, "Proximiy sensor value: " + event.values[0], Toast.LENGTH_SHORT).show();
                    context.getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        mySensorManager.registerListener(sensorEventListener,sensor,2*1000*1000);




    }


    public void pause() {
        mySensorManager.unregisterListener(sensorEventListener);
    }
}
