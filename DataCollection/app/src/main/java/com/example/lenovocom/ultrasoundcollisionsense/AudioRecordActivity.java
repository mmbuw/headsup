package com.example.lenovocom.ultrasoundcollisionsense;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.lenovocom.ultrasoundcollisionsense.database.FFTValuesDataAccess;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.TransformType;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import uk.me.berndporr.kiss_fft.KISSFastFourierTransformer;

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    KISSFastFourierTransformer kissFastFourierTransformer;
    private RecordingThread mRecordingThread;
    GraphView graphView;
    LineGraphSeries<DataPoint> series;
    int windowsSize = 1024;
    short[] audioData;
    List<Double[]> freqCounts;
    List<short[]> listOfAudioData;
    RecentMagnitudeData recentMagnitudeData;
    Button record, stop;
    FFTAsynctask mfftAsyncTask;
    //    FFTValuesDataAccess fftValuesDataAccess = new FFTValuesDataAccess(this);
    List<Double> loggedData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        init();
    }

    private void init() {

        graphView = (GraphView) findViewById(R.id.graphView);
        series = new LineGraphSeries<DataPoint>();
        audioData = new short[windowsSize];
        randomFill(audioData);
        recentMagnitudeData = new RecentMagnitudeData(windowsSize);
        record = (Button) findViewById(R.id.record);
        stop = (Button) findViewById(R.id.stop);
        freqCounts = new ArrayList<>();
        kissFastFourierTransformer = new KISSFastFourierTransformer();
        record.setOnClickListener(this);
        stop.setOnClickListener(this);

        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] audioData) {

                Short[] audioDataS = new Short[audioData.length];
                for (int i = 0; i < audioDataS.length; i++) {
                    audioDataS[i] = audioData[i];
                }
                recentMagnitudeData.addToQueue(audioDataS);
                List<Double[]> windowsList = recentMagnitudeData.recentWindow();
                int start = freqCounts.size() == 0 ? 0 : freqCounts.size() - 1;
                for (int i = start; i < windowsList.size(); i++) {
                    mfftAsyncTask = new FFTAsynctask(windowsSize);
                    mfftAsyncTask.execute(windowsList.get(i));
                }
            }
        });

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == record.getId())
            mRecordingThread.startRecording();
        else if (v.getId() == stop.getId()) {
            mfftAsyncTask.cancel(true);
            mRecordingThread.stopRecording();
            try {
                writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mfftAsyncTask.isCancelled())
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
        }
    }


    public void writeToFile() throws IOException {
//        ref: http://www.java2s.com/Code/Java/File-Input-Output/WritedoubletoafileusingDataOutputStream.htm
        String strFilePath = "/data/data/com.example.lenovocom.ultrasoundcollisionsense/Double.txt";
        FileOutputStream fos = new FileOutputStream(strFilePath, true);
        DataOutputStream dos = new DataOutputStream(fos);

        int n = 0;

        for (int i = 0; i < freqCounts.size(); i++) {
            for (int j = 0; j < windowsSize; j++)

                loggedData.add(freqCounts.get(i)[j]);
        }
        for (int i = 0; i < loggedData.size(); i++) {
            dos.writeChars(String.valueOf(loggedData.get(i).doubleValue()) + ", ");
            n++;
            if (n == 1023) {
                n = 0;
                dos.writeChars("\n");
            }

        }
        dos.close();

    }

    /**
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
     */

    private class FFTAsynctask extends AsyncTask<Double[], Void, Double[]> {

        private int wsize; //window size must be power of 2

        // constructor to set window size
        FFTAsynctask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected Double[] doInBackground(Double[]... values) {


            Double[] realPart = values[0].clone(); // actual values
            Double[] imagPart = new Double[wsize]; // init empty
            Arrays.fill(imagPart, 0.0d);

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
//            FFT fft = new FFT(wsize);
//            fft.fft(realPart, imagPart);


            fftKissFunc(realPart, imagPart);
            //init new double array for magnitude (e.g. frequency count)
            Double[] magnitude = new Double[wsize];


            //fill array with magnitude values of the distribution
            for (int i = 0; wsize > i; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }
            magnitude[0] = magnitude[1];

            return magnitude;

        }

        private void fftKissFunc(Double[] realPart, Double[] imagPart) {

            Complex[] indata = new Complex[realPart.length];

            for (int i = 0; i < realPart.length; i++) {
                indata[i] = new Complex(realPart[i], imagPart[i]);

            }
            Complex[] outdata = kissFastFourierTransformer.transform(indata, TransformType.FORWARD);

            for (int i = 0; i < realPart.length; i++) {
                realPart[i] = outdata[i].getReal();
                imagPart[i] = outdata[i].getImaginary();
            }

        }

        @Override
        protected void onPostExecute(Double[] values) {
            //hand over values to global variable after background task is finished
            freqCounts.add(values);
            syncTaskDone();
        }
    }


    public void syncTaskDone() {
        addEntry();
        // store(freqCounts);

    }


    // store on DB
//    private void store(List<Double[]> freqCounts) {
//        String stringFreqCounts = convDoubleArrayToString(freqCounts);
//        //stringFreqCounts into db
//
//        fftValuesDataAccess.open();
//        fftValuesDataAccess.Create(stringFreqCounts);
//        fftValuesDataAccess.close();
//    }
//
//    private String convDoubleArrayToString(List<Double[]> freqCounts) {
//        String stringFreqCounts = null;
//        for (int i = 0; i < freqCounts.size(); i++) {
//            for (int j = 0; j < windowsSize; j++) {
//                stringFreqCounts = stringFreqCounts + String.valueOf((double) freqCounts.get(i)[j]) + ", ";
//            }
//        }
//        return stringFreqCounts;
//    }


    /**
     * little helper function to fill example with random double values
     */
    public void randomFill(short[] array) {
        Random rand = new Random();
        for (int i = 0; array.length > i; i++) {
            array[i] = (short) rand.nextInt(Short.MAX_VALUE + 1);  //https://stackoverflow.com/questions/10189016/generate-short-random-number-in-java
        }
    }


    //  https://github.com/jjoe64/GraphView
    private void addEntry() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//        graphView.getViewport().setScalable(true);
//        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(1024);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(20000);

        for (int i = 0; i < windowsSize; i++) {
            try {
                series.appendData(new DataPoint(i, (freqCounts.get(freqCounts.size() - 1)[i]) / 1000), true, windowsSize);
            } catch (Exception e) {
                series.appendData(new DataPoint(i, 0), true, windowsSize);
            }

        }
        graphView.removeAllSeries();
        graphView.addSeries(series);


    }



}
