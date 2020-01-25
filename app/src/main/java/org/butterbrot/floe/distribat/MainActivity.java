package org.butterbrot.floe.distribat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.util.Arrays;

import uk.me.berndporr.kiss_fft.KISSFastFourierTransformer;
import org.apache.commons.math3.complex.Complex;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "DistriBat";

    // frequency resolution is ~  5.86 Hz with 8192
    // frequency resolution is ~ 46.87 Hz with 1024
    // processing takes ~ 15 ms
    public static int samplerate = 48000;
    public static int fftwindowsize = 1024; //8192

    public RecordAudioTask ra;
    public KISSFastFourierTransformer fft;
    public AudioRecord audioRecord;

    SoundPool soundPool;
    int[] pings;
    public int count = 0;

    short[] rawbuffer;
    double[] input;
    double[] prev;
    double[] hann;
    double[] scratch;
    double[] masterbuf;
    boolean doRecord = false;
    int master_offset = 0;

    // visualization stuff
    public static int canvas_size = 512;
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


    // https://stackoverflow.com/questions/5774104/android-audio-fft-to-retrieve-specific-frequency-magnitude-using-audiorecord
    private double ComputeFrequency(int arrayIndex) {
        return ((1.0 * samplerate) / (1.0 * fftwindowsize)) * arrayIndex;
    }

    private int ComputeIndex(int frequency) {
        return (int)((((double)fftwindowsize) / ((double)samplerate)) * (double)frequency);
    }

    // Hann window is generally considered the best all-purpose window function, see also ...
    // https://dsp.stackexchange.com/questions/22175/how-should-i-select-window-size-and-overlap
    private double[] hann_window(int size) {
        double[] hann = new double[size];
        for (int i = 0; i < size; i++) {
            hann[i] = 0.5 * (1.0 - Math.cos(2.0*i*Math.PI/(double)(size-1)));
        }
        return hann;
    }

    double[] fft_with_hann(double[] input, int offset) {
        for (int i = 0; i < scratch.length; i++) scratch[i] = hann[i] * input[i+offset];
        Complex[] tmp = fft.transformRealOptimisedForward(scratch);
        for (int i = 0; i < tmp.length; i++) scratch[i] = tmp[i].abs();
        return scratch;
    }

    int[] freq_offsets = {
            19000, //3243,
            21000, //3584
    };

    // FIXME: needs to be dynamic for each frequency
    double freq_threshold = 20.0;

    // detect "interesting" frequencies in FFT result
    private int detect_freq(double[] data) {
        // first run -> convert frequencies to FFT bins
        if (freq_offsets[0] > 10000)
            for (int i = 0; i < freq_offsets.length; i++) {
            freq_offsets[i] = ComputeIndex(freq_offsets[i]);
            Log.d(TAG,"mapping index "+freq_offsets[i]);
        }
        int freq_count = 0;
        for (int f: freq_offsets) {
            if (data[f] > freq_threshold) {
                //Log.d(TAG, " " + ComputeFrequency(f) + "detected");
                freq_count += 1;
            }
        }
        return freq_count;
    }

    // Requesting permission to RECORD_AUDIO (from https://developer.android.com/guide/topics/media/mediarecorder#java)
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
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

        // TODO: setAudioAttributes()?
        soundPool = new SoundPool.Builder().build();
        pings = new int[6];
        pings[0] = soundPool.load(this,R.raw.sine19500,0);
        pings[1] = soundPool.load(this,R.raw.sine19700,0);
        pings[2] = soundPool.load(this,R.raw.sine19900,0);
        pings[3] = soundPool.load(this,R.raw.sine20100,0);
        pings[4] = soundPool.load(this,R.raw.sine20300,0);
        pings[5] = soundPool.load(this,R.raw.sine20500,0);

        //pings[1] = soundPool.load(this,R.raw.sine19600,0);
        //pings[3] = soundPool.load(this,R.raw.sine19800,0);
        //pings[5] = soundPool.load(this,R.raw.sine20000,0);
        //pings[7] = soundPool.load(this,R.raw.sine20200,0);
        //pings[9] = soundPool.load(this,R.raw.sine20400,0);

        fft = new KISSFastFourierTransformer();

        int bufferSize = AudioRecord.getMinBufferSize( samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT );
        Log.d(TAG,"minBufferSize = "+bufferSize);
        audioRecord = new AudioRecord( MediaRecorder.AudioSource.UNPROCESSED, samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize*4 );

        rawbuffer = new short[fftwindowsize];
        input = new double[fftwindowsize];
        prev = new double[fftwindowsize];
        hann = hann_window(fftwindowsize);
        scratch = new double[fftwindowsize];

        masterbuf = new double[samplerate]; // room for one second of data

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doRecord) { doRecord = false; ((FloatingActionButton)view).setImageResource(android.R.drawable.ic_media_play); }
                else { ra = (RecordAudioTask) new RecordAudioTask().execute(); ((FloatingActionButton)view).setImageResource(android.R.drawable.ic_media_pause); }
            }
        });

        imageView = (ImageView) this.findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(canvas_size, canvas_size, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
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
    private class RecordAudioTask extends AsyncTask<Void, double[], Void> {

        @Override protected Void doInBackground(Void... params) { try {

            audioRecord.startRecording();
            Log.d(TAG,"start recording");
            doRecord = true;

            while (doRecord) {

                // FIXME: ugly hack, when exactly should playback happen?
                if (count++ % 20 == 0) soundPool.play(pings[(int)(Math.random()*pings.length)],1.0f,1.0f,0,0,1.0f );

                int result = audioRecord.read(rawbuffer, 0, rawbuffer.length, AudioRecord.READ_BLOCKING);
                //Log.d(TAG, "got audio data: numsamples = " + result);

                long time1 = System.currentTimeMillis();
                double[] tmpb = prev; prev = input; input = tmpb;
                // FIXME: magic scale factor 100.0
                for (int i = 0; i < input.length; i++) input[i] = 100.0 * (rawbuffer[i] / (double)Short.MAX_VALUE);
                double[] output = fft_with_hann(input,0);
                //long time2 = System.currentTimeMillis();

                //Log.d(TAG,"timediff = ms: "+(time2-time1));

                /*double max = 0.0;
                int maxpos = 0;
                for (int i = 1; i < output.length; i++)
                    if (output[i].abs() > max) {
                        max = output[i].abs();
                        maxpos = i;
                    }
                Log.v(TAG,"max freq = "+ComputeFrequency(maxpos)+" index "+maxpos+" value "+output[maxpos].abs());*/

                publishProgress(output);

                int freq_count = detect_freq(output);
                if (freq_count == 0) { master_offset = 0; continue; }

                // we found at least one, but not yet all required frequencies,
                // so we now need to start filling the master buffer
                if (freq_count < freq_offsets.length) {

                    if (master_offset == 0) {
                        Log.v(TAG,"initial frequency detected, filling master buffer");
                        System.arraycopy( prev, 0, masterbuf, 0, prev.length );
                        master_offset += prev.length;
                    }

                    Log.v(TAG,"appending data to master buffer");
                    System.arraycopy( input, 0, masterbuf, master_offset, input.length );
                    master_offset += input.length;

                    // sanity check
                    if (master_offset >= masterbuf.length) {
                        Log.d(TAG,"master buffer overrun, resetting");
                        master_offset = 0;
                        continue;
                    }

                } else {

                    if (master_offset == 0) continue;
                    Log.v(TAG,"all required frequencies detected, starting offset calculation on master buffer bytes: "+master_offset);
                    int stepsize = 48; // corresponds to 1 ms at 48 kHz
                    int prev_offset = master_offset-fftwindowsize-stepsize;
                    for (int i = prev_offset; i > 0; i -= stepsize) {
                        output = fft_with_hann(masterbuf,i);
                        int res = detect_freq(output);
                        if (res == 2) prev_offset = i; // freq_offsets.length
                        if (res == 0) {
                            int diff = prev_offset - i; // - stepsize ?
                            Log.d(TAG,"frequency start offset (ms): "+((1000.0 * diff) / (double)samplerate));
                            break;
                        }
                    }
                    // reset
                    master_offset = 0;
                }
            }

            audioRecord.stop();
            Log.d(TAG,"stop recording");

        } catch(Exception e) { } return null; }

        // https://stackoverflow.com/questions/5511250/capturing-sound-for-analysis-and-visualizing-frequencies-in-android
        @Override protected void onProgressUpdate(double[]... data) {
            canvas.drawColor(Color.BLACK);
            for (int x = 0; x < canvas_size; x++) {
                // visualize only the uppermost part of the spectrum
                int startbin = (data[0].length/2) - canvas_size;
                int y1 = (int) (canvas_size - (data[0][startbin+x] * 10));
                int y2 = canvas_size;
                canvas.drawLine(x, y1, x, y2, paint);
            }
            imageView.invalidate();
        }
    }
}
