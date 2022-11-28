//package com.example.lenovocom.ultrasoundcollisionsense;
//
//import android.media.AudioManager;
//import android.media.ToneGenerator;
//import android.os.AsyncTask;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;
//
//import java.util.Random;
//
//public class ToneGenActivity extends AppCompatActivity {
//
//    Button beep;
//    Button stop;
//    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//    LineGraphSeries<DataPoint> series;
//    GraphView graphView;
//
//    int windowsSize = 64;
//    int sampleRate = 0;
//    double[] mgdouble;
//    double[] freqCounts;
//    RecentMagnitudeData recentMagnitudeData;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tone_gen);
//        beep = (Button) findViewById(R.id.beep);
//        stop = (Button) findViewById(R.id.stop);
//        graphView = (GraphView) findViewById(R.id.graphView);
//        series = new LineGraphSeries<DataPoint>();
//
//        mgdouble = new double[windowsSize];
//        randomFill(mgdouble);
//        recentMagnitudeData = new RecentMagnitudeData(windowsSize);
//
//
//beep.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        toneGen1.startTone(ToneGenerator.TONE_DTMF_1); //DTMF tone for key 1: 1209Hz, 697Hz, continuous
//    }
//});
//
//stop.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        toneGen1.stopTone();
//    }
//});
//
//    }
//
//
//
//
//
//    /**
//     * Implements the fft functionality as an async task
//     * FFT(int n): constructor with fft length
//     * fft(double[] x, double[] y)
//     */
//
//    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {
//
//        private int wsize; //window size must be power of 2
//
//        // constructor to set window size
//        FFTAsynctask(int wsize) {
//            this.wsize = wsize;
//        }
//
//        @Override
//        protected double[] doInBackground(double[]... values) {
//
//
//            Double[] realPart = values[0].clone(); // actual acceleration values
//            double[] imagPart = new double[wsize]; // init empty
//
//            /**
//             * Init the FFT class with given window size and run it with your input.
//             * The fft() function overrides the realPart and imagPart arrays!
//             */
//            FFT fft = new FFT(wsize);
//            fft.fft(realPart, imagPart);
//            //init new double array for magnitude (e.g. frequency count)
//            double[] magnitude = new double[wsize];
//
//
//            //fill array with magnitude values of the distribution
//            for (int i = 0; wsize > i; i++) {
//                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
//            }
//            magnitude[0] = magnitude[1];
//
//            return magnitude;
//
//        }
//
//        @Override
//        protected void onPostExecute(double[] values) {
//            //hand over values to global variable after background task is finished
//            freqCounts = values;
//            syncTaskDone();
//        }
//    }
//
//    public void syncTaskDone() {
//        addEntry();
//    }
//
//
//    //  https://github.com/jjoe64/GraphView
//    private void addEntry() {
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//        for (int i = 0; i < windowsSize; i++) {
//            try {
//                series.appendData(new DataPoint(i, freqCounts[i]), true, windowsSize);
//            } catch (Exception e) {
//                series.appendData(new DataPoint(i, 0), true, windowsSize);
//            }
//
//        }
//        graphView.removeAllSeries();
//        graphView.addSeries(series);
//
//
//    }
//
//
//
//    /**
//     * little helper function to fill example with random double values
//     */
//    public void randomFill(double[] array) {
//        Random rand = new Random();
//        for (int i = 0; array.length > i; i++) {
//            array[i] = rand.nextDouble();
//        }
//    }
//
//}
