package com.example.neginsharif.mdemo;


import android.app.Notification;
import android.content.Intent;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neginsharif.mdemo.notification.PopUpNotification;


public class CollisionDetectionActivity extends AppCompatActivity {
    private double secondPeakRatio;
    private PopUpNotification notification;
    private TextView tvSecondPeak;
    private Button finishDetection;
    private MDoppler calibratedDoppler;

    private TextView mTextField;
    private int counter = 0;

    private boolean gesturesOn = false;
    boolean pause = false;
    private int fileCounter = 0;

   // ActivateProximity activateProximity;
    SensorManager mySensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collision_detection);
        setTitle("Collision Detection Mode");

        tvSecondPeak = (TextView) findViewById(R.id.second_peak_ratio);
        finishDetection = (Button) findViewById(R.id.finishDetection);

        final Intent intent = getIntent();
        secondPeakRatio = intent.getDoubleExtra("secondPeakRatio", 1);
        tvSecondPeak.setText("Threshold is set to : " + secondPeakRatio);
        calibratedDoppler = new MDoppler(secondPeakRatio);

     //   activateProximity = new ActivateProximity(this);


        mTextField = (TextView) findViewById(R.id.mTextField);
        activateCollisionDetection();

        startGraph();
        renderGraph();


        finishDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibratedDoppler.pause();
                finish();

            }
        });

    }


 // ref:   https://github.com/jasper-lu/Doppler-Android-Demo/blob/master/app/src/main/java/com/jasperlu/test/GraphFragment.java
    private void renderGraph() {
   /*     mSeries = new XYSeries("Vols/FreqBin");
        div10 = new XYSeries("div10");
        mSeries.add(3, 4);
        mRenderer = new XYSeriesRenderer();
        div10Renderer = new XYSeriesRenderer();
        div10Renderer.setColor(getResources().getColor(R.color.red));
        dataset.addSeries(mSeries);
        dataset.addSeries(div10);
        renderer.addSeriesRenderer(mRenderer);
        renderer.addSeriesRenderer(div10Renderer);
        renderer.setPanEnabled(false);
        renderer.setZoomEnabled(false);
        mChart = ChartFactory.getLineChartView(CollisionDetectionActivity.this, dataset, renderer);

        ((LinearLayout) findViewById(R.id.fftchart)).addView(mChart);*/
    }



    private void activateCollisionDetection() {
    //    activateProximity.activate();
        activateProximitySensor();
        calibratedDoppler.start();
        //calibratedDoppler = TheDoppler.getDoppler();
      /*  if (flag == 0) {
            activateBtn.setText(COLLISION_DETECTION_MODE);
            activateBtn.setEnabled(false);
            activateBtn.setText(CALIBRATION_DISABLED);
            calibratedDoppler.start();
            flag = 1;
        } else if (flag == 1){
            calibratedDoppler.pause();
            calibratedDoppler.removeGestureListener();
            calibratedDoppler.removeReadCallback();
            activateBtn.setText("Activate");
            flag = 1;
        }*/

        calibratedDoppler.setOnGestureListener(new MDoppler.OnGestureListener() {
            @Override
            public void onPush() {
                counter++;
               // if (Integer.valueOf(mTextField.getText().toString())== 0) {

                if (counter==2) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                    notification = new PopUpNotification(CollisionDetectionActivity.this);
                    notification.sendNotification();
                }else  notifLowApi();


                   counter = 0;
               // }
                    /*new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            mTextField.setText("done!");
                        }
                    }.start();*/
                }
            }

            @Override
            public void onPull() {

            }

            @Override
            public void onTap() {

            }

            @Override
            public void onDoubleTap() {

            }

            @Override
            public void onNothing() {

            }
        });
    }

    private void notifLowApi() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setContentText("hi")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("title")
                .setPriority(NotificationCompat.PRIORITY_MAX).build();

        builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);


        NotificationManagerCompat notificationManagerCompat =  NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1, builder.build());
    }

    @Override
    public void onBackPressed() {
        calibratedDoppler.pause();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        calibratedDoppler.pause();
        super.onDestroy();

    }


    @Override
    protected void onPause() {
     //   activateProximity.pause();
        mySensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }




    public void startGraph() {
        calibratedDoppler.setOnReadCallback(new MDoppler.OnReadCallback() {
            @Override
            public void onBandwidthRead(int leftBandwidth, int rightBandwidth) {

            }

            @Override
            public void onBinsRead(double[] bins) {
              /* mSeries.clear();
                div10.clear();
                double frac = bins[929] * calibratedDoppler.maxVolRatio;*/
               /* for (int i = 1650; i < 1900; ++i) {
                    mSeries.add(i, bins[i]);
                    div10.add(i, frac);
                }
                mChart.repaint();*/


            }


        });

    }

    public void activateProximitySensor() {
        mySensorManager = (SensorManager) getSystemService(
                SENSOR_SERVICE);
        sensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[0] < sensor.getMaximumRange()) {
                    Toast.makeText(getApplicationContext(), "Proximiy sensor maximum value: " + event.values[0], Toast.LENGTH_SHORT).show();
                 //   getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                    calibratedDoppler.pause();
                    pause = true;
                }else {
                    if (pause==true) {
                        pause = false;
                        calibratedDoppler.start();
                    //    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                    }
                    //   Toast.makeText(context, "Proximiy sensor value: " + event.values[0], Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        mySensorManager.registerListener(sensorEventListener,sensor,2*1000*1000);




    }


}

