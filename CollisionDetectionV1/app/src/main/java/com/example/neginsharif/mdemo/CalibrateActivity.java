package com.example.neginsharif.mdemo;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.neginsharif.mdemo.datamanagement.SeperatePeakProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CalibrateActivity extends AppCompatActivity {

    private MDoppler mDoppler;
    public List<double[]> allBins;
    // public List<GraphFragment.Bins> allBinss = new ArrayList<>();
    private SeperatePeakProcess seperatePeakProcess;
    double threshold = 1;
    private double cFactor = 0.35;
    Button randomButton;
    RelativeLayout relativeLayout;
    View corner;
    int height;
    int width;
    Random n;
    ObjectAnimator animationX;
    ObjectAnimator animationY;
  //  boolean sleep;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        setTitle("Calibration Mode");
        randomButton = findViewById(R.id.button);
        relativeLayout = findViewById(R.id.card_layout);
        corner = findViewById(R.id.viewAlaki);


        mDoppler = new MDoppler();
        mDoppler.start();
        game();

    //    sleep= false;

      //  mThread progressthread = new mThread(); // The activity finishes after 30 seconds and passes the threshold to main activity
       // progressthread.start();

        calibrate();


    }

    private void game() {

        n = new Random();

        ViewTreeObserver vto = corner.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = (int) (corner.getX() - randomButton.getWidth());
                height = (int) (corner.getY() - randomButton.getHeight());
            }
        });


        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int newHeight = n.nextInt(height);
                final int newWidth = n.nextInt(width);

                animationX = ObjectAnimator.ofFloat(randomButton, "translationX", newWidth);
                animationY = ObjectAnimator.ofFloat(randomButton, "translationY", newHeight);
                animationX.setDuration(0);
                animationY.setDuration(0);
                animationX.start();
                animationY.start();
            }
        });
    }


    private void calibrate() {
        Handler handler;

        allBins = new ArrayList<>();

        seperatePeakProcess = new SeperatePeakProcess();

        //       activateProximity.activate();

        mDoppler.setOnReadCallback(new MDoppler.OnReadCallback() {
            @Override
            public void onBandwidthRead(int leftBandwidth, int rightBandwidth) {
                //nothing
            }

            @Override
            public void onBinsRead(double[] bins) {
       //         if (sleep)
                allBins.add(bins);
                //allBinss.add(new GraphFragment.Bins(bins));
            }
        });


        handler=new Handler();
        Runnable r=new Runnable() {
            public void run() {
                //what ever you do here will be done after 5 seconds delay.

                mDoppler.pause();
                threshold = seperatePeakProcess.getThreshold(allBins, cFactor);
                //  seperatePeakProcess.save2(allBinss);
                // Log.d("threshold", " threshold is : " + threshold);
                //Toast.makeText(CalibrateActivity.this, "saved", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("threshold", threshold);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        };
        handler.postDelayed(r, 30000);


    }

    @Override
    protected void onPause() {
        //       activateProximity.pause();
        super.onPause();
    }



}
