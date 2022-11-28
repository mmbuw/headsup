package com.example.neginsharif.mdemo;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neginsharif.mdemo.notification.PopUpNotification;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //private MDoppler doppler;
    private Button calibrateBtn, activateBtn;
    private TextView thresholdTextView;
    public double secondPeakRatio = 1;
    PopUpNotification notification;
    private int flag;
    private String CALIBRATION_DISABLED = "Calibration disabled";
    private String CALIBRATION_ENABLED = "Calibrate";
    private String ACTIVATE_COLLISION_DETECTION = "Activate";
    private String COLLISION_DETECTION_MODE = "Collision detection mode";
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //doppler = TheDoppler.getDoppler();

        activateBtn = (Button) findViewById(R.id.activate);
        calibrateBtn = (Button) findViewById(R.id.init_calibrate);
        thresholdTextView = (TextView) findViewById(R.id.threshold);

        secondPeakRatio = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString("calibrated", "1"));
        if (secondPeakRatio == 1)
             thresholdTextView.setText("Not calibrated");
        else
            thresholdTextView.setText("Already calibrated");
            //thresholdTextView.setText("threshold is set to: " + secondPeakRatio);

        requestAudioPermissions();
        flag = 0;

        //  doppler.start();




        calibrateBtn.setOnClickListener(this);
        activateBtn.setOnClickListener(this);


    }


    @Override
    protected void onResume() {
        super.onResume();
        //  doppler.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.init_calibrate) {
            Intent intent = new Intent(MainActivity.this, CalibrateActivity.class);
            startActivityForResult(intent, 1);

        } else if (v.getId() == R.id.activate){
            Intent intent = new Intent(MainActivity.this, CollisionDetectionActivity.class);
            intent.putExtra("secondPeakRatio", secondPeakRatio);
            startActivity(intent);
        }
           // activateCollisionDetection();


    }

    private void activateCollisionDetection() {
        MDoppler calibratedDoppler = new MDoppler(secondPeakRatio);
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
                notification = new PopUpNotification(MainActivity.this);
                notification.sendNotification();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                secondPeakRatio = data.getDoubleExtra("threshold", 1);
                thresholdTextView.setText("new threshold is " + secondPeakRatio);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("calibrated", String.valueOf(secondPeakRatio)).apply();

            }//else if (requestCode == RESULT_CANCELED)
            //Toast.makeText(this, "Please calibrate and press finish", Toast.LENGTH_LONG).show();

        }

    }







//ref: https://stackoverflow.com/questions/48762146/record-audio-permission-is-not-displayed-in-my-application-on-starting-the-appli
    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
          //  recordAudio();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    //recordAudio();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
