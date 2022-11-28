package com.example.lenovocom.ultrasoundcollisionsense;

// reference: https://github.com/newventuresoftware/WaveformControl

public interface AudioDataReceivedListener {

    void onAudioDataReceived(short[] data);
}
