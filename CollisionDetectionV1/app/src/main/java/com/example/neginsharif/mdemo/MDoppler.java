package com.example.neginsharif.mdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import static java.lang.Math.signum;

import com.example.neginsharif.mdemo.datamanagement.OutlierDetector;
import com.jasperlu.doppler.Calibrator;
import com.jasperlu.doppler.FFT.FFT;
import com.jasperlu.doppler.FrequencyPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 3/11/2015.
 * <p>
 * To find frequency, check:
 * http://stackoverflow.com/questions/18652000/record-audio-in-java-and-determine-real-time-if-a-tone-of-x-frequency-was-played
 */
// reference: https://github.com/jasper-lu/Doppler-Android-Demo
public class MDoppler {
    public interface OnReadCallback {
        //bandwidths are the number to the left/to the right from the pilot tone the shift was
        public void onBandwidthRead(int leftBandwidth, int rightBandwidth);

        //for testing/graphing as well
        public void onBinsRead(double[] bins);
    }

    //base gestures. can extend to have more
    public interface OnGestureListener {
        //swipe towards
        public void onPush();

        //swipe away
        public void onPull();

        //self-explanatory
        public void onTap();

        public void onDoubleTap();

        public void onNothing();

    }

    //prelimiary frequency stuff
    public static final float PRELIM_FREQ = 20000;
    public static final int PRELIM_FREQ_INDEX = 20000;
    public static final int MIN_FREQ = 18000;
    public static final int MAX_FREQ = 21000;

    public static final int RELEVANT_FREQ_WINDOW = 50;
    public static final int DEFAULT_SAMPLE_RATE = 48000;

    //modded from the soundwave paper. frequency bins are scanned until the amp drops below
    // 1% of the primary tone peak
    private static final double MAX_VOL_RATIO_DEFAULT = 0.1;
    // private static final double SECOND_PEAK_RATIO = 0.0019;
    public static double SECOND_PEAK_RATIO = 1;
    public static double maxVolRatio = MAX_VOL_RATIO_DEFAULT;

    //for bandwidth positions in array
    private static final int LEFT_BANDWIDTH = 0;
    private static final int RIGHT_BANDWIDTH = 1;

    //I want to add smoothing
    private static final float SMOOTHING_TIME_CONSTANT = 0.5f;

    /**
     * utility variables for reading and parsing through audio data
     **/
    private AudioRecord microphone;
    private FrequencyPlayer frequencyPlayer;
    private int SAMPLE_RATE = DEFAULT_SAMPLE_RATE;

    private float frequency;
    public int freqIndex;

    private short[] buffer;
    private float[] fftRealArray;
    //holds the freqs of the previous iteration
    private float[] oldFreqs;
    private int bufferSize = 2048;

    private Handler mHandler;
    private boolean repeat;

    FFT fft;
    //to calibrate or not
    private boolean calibrate = true;
    Calibrator calibrator;
    /**
     * end utility variables for parsing through audio data
     **/

    //callbacks
    private boolean isGestureListenerAttached = false;
    private OnGestureListener gestureListener;
    private boolean isReadCallbackOn = false;
    private OnReadCallback readCallback;

    /**
     * variables for gesture detection
     **/
    private int previousDirection = 0;
    private int directionChanges;
    private int cyclesLeftToRead = -1;
    //wait this many before starting to read again
    private int cyclesToRefresh;
    private int directionSame;
    private final int cyclesToRead = 5;

    public double cpNormalizedVolume = 0;

    List<Double> consecutivepeaks = new ArrayList<>();


    public MDoppler(double SECOND_PEAK_RATIO) {
        //write a check to see if stereo is supported
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        buffer = new short[bufferSize];
        frequency = PRELIM_FREQ;
        freqIndex = PRELIM_FREQ_INDEX;
        frequencyPlayer = new FrequencyPlayer(PRELIM_FREQ);
        microphone = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        mHandler = new Handler();
        calibrator = new Calibrator();
        this.SECOND_PEAK_RATIO = SECOND_PEAK_RATIO;
    }

    public MDoppler() {
        //write a check to see if stereo is supported
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        buffer = new short[bufferSize];
        frequency = PRELIM_FREQ;
        freqIndex = PRELIM_FREQ_INDEX;
        frequencyPlayer = new FrequencyPlayer(PRELIM_FREQ);
        microphone = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        mHandler = new Handler();
        calibrator = new Calibrator();
    }


    private void setFrequency(float frequency) {
        this.frequency = frequency;
        this.freqIndex = fft.freqToIndex(frequency);
    }

    public boolean start() {
        frequencyPlayer.play();
        boolean startedRecording = false;
        try {
            //you might get an error here if another app hasn't released the microphone
            microphone.startRecording();
            repeat = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    optimizeFrequency(MIN_FREQ, MAX_FREQ);
                    //assuming fft.forward was already called;
                    readMic();
                }
            }, 1000);

            startedRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DOPPLER", "start recording error");
            return false;
        }
        if (startedRecording) {
            int bufferReadResult = microphone.read(buffer, 0, bufferSize);
            bufferReadResult = getHigherP2(bufferReadResult);
            //get higher p2 because buffer needs to be "filled out" for FFT
            fftRealArray = new float[getHigherP2(bufferReadResult)];
            fft = new FFT(getHigherP2(bufferReadResult), SAMPLE_RATE);
        }
        return true;
    }

    public int[] getBandwidth() {
        readAndFFT();

        //rename this
        int primaryTone = freqIndex;

        double normalizedVolume = 0;

        double primaryVolume = fft.getBand(primaryTone);

        int leftBandwidth = 0;

        do {
            leftBandwidth++;
            double volume = fft.getBand(primaryTone - leftBandwidth);
            normalizedVolume = volume / primaryVolume;
        } while (normalizedVolume > maxVolRatio && leftBandwidth < RELEVANT_FREQ_WINDOW);


        //secondary bandwidths are for looking past the first minima to search for "split off" peaks, as per the paper
        int secondScanFlag = 0;
        int secondaryLeftBandwidth = leftBandwidth;

        //second scan
        do {
            secondaryLeftBandwidth++;
            double volume = fft.getBand(primaryTone - secondaryLeftBandwidth);
            normalizedVolume = volume / primaryVolume;

            if (normalizedVolume > SECOND_PEAK_RATIO) {
                secondScanFlag = 1;
            }

            if (secondScanFlag == 1 && normalizedVolume < maxVolRatio) {
                break;
            }
        } while (secondaryLeftBandwidth < RELEVANT_FREQ_WINDOW);

        if (secondScanFlag == 1) {
            leftBandwidth = secondaryLeftBandwidth;
        }

        int rightBandwidth = 0;

        do {
            rightBandwidth++;
            double volume = fft.getBand(primaryTone + rightBandwidth);
            normalizedVolume = volume / primaryVolume;
        } while (normalizedVolume > maxVolRatio && rightBandwidth < RELEVANT_FREQ_WINDOW);


        int secondaryRightBandwidth = rightBandwidth - 1;
        do {
            secondaryRightBandwidth++;
            double volume = fft.getBand(primaryTone + secondaryRightBandwidth);
            normalizedVolume = volume / primaryVolume;

            if (normalizedVolume > SECOND_PEAK_RATIO) {
                secondScanFlag = 1;
                cpNormalizedVolume = normalizedVolume;
            }

            if (secondScanFlag == 1 && normalizedVolume < maxVolRatio) {
                break;
            }
        } while (secondaryRightBandwidth < RELEVANT_FREQ_WINDOW);

        if (secondScanFlag == 1) {
            rightBandwidth = secondaryRightBandwidth;
        }

        return new int[]{leftBandwidth, rightBandwidth};

    }


    public void getRightProperty() {
        readAndFFT();

        //rename this
        int primaryTone = freqIndex;

        double normalizedVolume = 0;

        double primaryVolume = fft.getBand(primaryTone);

        int rightInit = 3;
        int peaksCounter = 0;

       // int flag = 0;

        do {
            rightInit++;
            double volume = fft.getBand(primaryTone + rightInit);
            normalizedVolume = volume / primaryVolume;
            consecutivepeaks.add(normalizedVolume);
            if (consecutivepeaks.size()==12){
                for (Double proportion : consecutivepeaks){

                    if (proportion >= SECOND_PEAK_RATIO)
                        peaksCounter++;
                }
                if (peaksCounter>=3)
                    gestureListener.onPush();
                peaksCounter = 0;
                consecutivepeaks= new ArrayList<>();

            }

           /* if (normalizedVolume >= SECOND_PEAK_RATIO) {
                gestureListener.onPush();
            }*/
         //   flag = 0;
        } while (rightInit < RELEVANT_FREQ_WINDOW);

    }


    public void readMic() {
        int[] bandwidths = getBandwidth();
        int leftBandwidth = bandwidths[LEFT_BANDWIDTH];
        int rightBandwidth = bandwidths[RIGHT_BANDWIDTH];

        if (isReadCallbackOn) {
            callReadCallback(leftBandwidth, rightBandwidth);
            getRightProperty();
        }

        if (isGestureListenerAttached) {
            // callGestureCallback(leftBandwidth, rightBandwidth);
            getRightProperty();
        }

        if (calibrate) {
            maxVolRatio = calibrator.calibrate(maxVolRatio, leftBandwidth, rightBandwidth);
        }

        if (repeat) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    readMic();
                }
            });
        }
    }

    public void setOnGestureListener(OnGestureListener listener) {
        gestureListener = listener;
        isGestureListenerAttached = true;
    }

    public void removeGestureListener() {
        gestureListener = null;
        isGestureListenerAttached = false;
    }

    public void callGestureCallback(int leftBandwidth, int rightBandwidth) {
        //early escape if need to refresh
        if (cyclesToRefresh > 0) {
            cyclesToRefresh--;
            return;
        }


        if (leftBandwidth > 4 || rightBandwidth > 4) {
            //Log.d("GESTURE CALLBACK", "Start of if statement");
            //implement gesture logic
            int difference = leftBandwidth - rightBandwidth;
            int direction = (int) signum(difference);

            //Log.d("GESTURE CALLBACK", "DIRECTION IS " + direction);
            if (direction == 1) {
                Log.d("DIRECTION", "POS");
            } else if (direction == -1) {
                Log.d("Direction", "NEG");
            } else {
                Log.d("DIrection", "none");
            }

            if (direction != 0 && direction != previousDirection) {
                //scan a 4 frame window to wait for taps or double taps
                //Log.d("GESTURE CALLBACK", "previous direction is diff than current");
                cyclesLeftToRead = cyclesToRead;
                //Log.d("GESTURE CALLBACK", "setting prev direction");
                previousDirection = direction;
                directionChanges++;
            }
        }

        cyclesLeftToRead--;


        if (cyclesLeftToRead == 0) {
            //Log.d("GESTURE CALLBACK", "No more cycles to read. finding appropriate lsitener");

            if (directionChanges == 1) {

                if (previousDirection == -1) {
                    gestureListener.onPush();
                } else {
                    gestureListener.onPull();
                }
            } else if (directionChanges == 2) {
                gestureListener.onTap();
            } else {
                gestureListener.onDoubleTap();
            }
            previousDirection = 0;
            directionChanges = 0;
            cyclesToRefresh = cyclesToRead;
        } else {
            gestureListener.onNothing();
        }
    }

    public void setOnReadCallback(OnReadCallback callback) {
        readCallback = callback;
        isReadCallbackOn = true;
    }

    public void removeReadCallback() {
        readCallback = null;
        isReadCallbackOn = false;
    }

    public void callReadCallback(int leftBandwidth, int rightBandwidth) {
        double[] array = new double[fft.specSize()];
        for (int i = 0; i < fft.specSize(); ++i) {
            array[i] = fft.getBand(i);
        }

        readCallback.onBandwidthRead(leftBandwidth, rightBandwidth);
        readCallback.onBinsRead(array);
    }

    public boolean setCalibrate(boolean bool) {
        calibrate = bool;
        return calibrate;
    }

    public void smoothOutFreqs() {
        for (int i = 0; i < fft.specSize(); ++i) {
            float smoothedOutMag = SMOOTHING_TIME_CONSTANT * fft.getBand(i) + (1 - SMOOTHING_TIME_CONSTANT) * oldFreqs[i];
            fft.setBand(i, smoothedOutMag);
        }
    }

    public boolean pause() {
        try {
            microphone.stop();
            frequencyPlayer.pause();
            repeat = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void optimizeFrequency(int minFreq, int maxFreq) {
        readAndFFT();
        int minInd = fft.freqToIndex(minFreq);
        int maxInd = fft.freqToIndex(maxFreq);

        int primaryInd = freqIndex;
        for (int i = minInd; i <= maxInd; ++i) {
            if (fft.getBand(i) > fft.getBand(primaryInd)) {
                primaryInd = i;
            }
        }
        setFrequency(fft.indexToFreq(primaryInd));
        Log.d("NEW PRIMARY IND", fft.indexToFreq(primaryInd) + "");
    }

    //reads the buffer into fftrealarray, applies windowing, then fft and smoothing
    public void readAndFFT() {
        //copy into old freqs array
        if (fft.specSize() != 0 && oldFreqs == null) {
            oldFreqs = new float[fft.specSize()];
        }
        for (int i = 0; i < fft.specSize(); ++i) {
            oldFreqs[i] = fft.getBand(i);
        }

        int bufferReadResult = microphone.read(buffer, 0, bufferSize);

        for (int i = 0; i < bufferReadResult; i++) {
            fftRealArray[i] = (float) buffer[i] / Short.MAX_VALUE; //32768.0
        }

        //apply windowing
        for (int i = 0; i < bufferReadResult / 2; ++i) {
            // Calculate & apply window symmetrically around center point
            // Hanning (raised cosine) window
            float winval = (float) (0.5 + 0.5 * Math.cos(Math.PI * (float) i / (float) (bufferReadResult / 2)));
            if (i > bufferReadResult / 2) winval = 0;
            fftRealArray[bufferReadResult / 2 + i] *= winval;
            fftRealArray[bufferReadResult / 2 - i] *= winval;
        }

        // zero out first point (not touched by odd-length window)
        //fftRealArray[0] = 0;

        fft.forward(fftRealArray);

        //apply smoothing
        smoothOutFreqs();
    }

    // compute nearest higher power of two
    // see: graphics.stanford.edu/~seander/bithacks.html
    int getHigherP2(int val) {
        val--;
        val |= val >> 1;
        val |= val >> 2;
        val |= val >> 8;
        val |= val >> 16;
        val++;
        return (val);
    }
}
