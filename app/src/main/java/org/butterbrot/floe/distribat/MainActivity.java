package org.butterbrot.floe.distribat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import uk.me.berndporr.kiss_fft.KISSFastFourierTransformer;
import org.apache.commons.math3.complex.Complex;

public class MainActivity extends AppCompatActivity {

    public KISSFastFourierTransformer fft;
    public AudioRecord audioRecord;
    public static int samplerate = 48000;
    public static int fftwindowsize = 8192;
    public static String TAG = "DistriBat";
    short[] rawbuffer;
    double[] input;

    // https://stackoverflow.com/questions/5774104/android-audio-fft-to-retrieve-specific-frequency-magnitude-using-audiorecord
    private double ComputeFrequency(int arrayIndex) {
        return ((1.0 * samplerate) / (1.0 * fftwindowsize)) * arrayIndex;
    }

    // Requesting permission to RECORD_AUDIO (from https://developer.android.com/guide/topics/media/mediarecorder#java)
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        fft = new KISSFastFourierTransformer();

        int bufferSize = AudioRecord.getMinBufferSize( samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT );
        Log.d(TAG,"minBufferSize = "+bufferSize);
        audioRecord = new AudioRecord( MediaRecorder.AudioSource.UNPROCESSED, samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize );

        rawbuffer = new short[fftwindowsize];
        input = new double[fftwindowsize];

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                if ((audioRecord != null) && (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)) {
                    // this throws an exception with some combinations of samplerate and bufferSize
                    try { audioRecord.startRecording(); }
                    catch (Exception e) { audioRecord = null; }
                }
                int result = audioRecord.read(rawbuffer,0, rawbuffer.length, AudioRecord.READ_BLOCKING);
                Log.d(TAG,"got audio data: numsamples = "+result);

                for (int i = 0; i < input.length; i++) input[i] = 100.0 * (rawbuffer[i] / 32768.0);

                Complex[] output = fft.transformRealOptimisedForward(input);

                double max = 0.0;
                int maxpos = 0;
                for (int i = 1; i < output.length; i++)
                    if (output[i].abs() > max) {
                        max = output[i].abs();
                        maxpos = i;
                    }

                Log.d(TAG,"max freq = "+ComputeFrequency(maxpos));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // https://www.androidcookbook.info/android-media/visualizing-frequencies.html
    /*private class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override protected Void doInBackground(Void... params) { try {

            audioRecord.startRecording();

            while (true) {

                int result = audioRecord.read( rawbuffer, 0, fftwindowsize, AudioRecord.READ_BLOCKING );

            for (int i = 0; i < rawbuffer.length && i < bufferReadResult; i++) input[i] = (double) rawbuffer[i] / (double) Short.MAX_VALUE;

                        transformer.ft(toTransform);

                Calling publishProgress calls onProgressUpdate.

                publishProgress(toTransform);

                Log.e("AudioRecord", "Recording Failed");

                return null;
     */
}
