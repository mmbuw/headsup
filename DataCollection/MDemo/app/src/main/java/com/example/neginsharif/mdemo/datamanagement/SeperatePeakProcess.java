package com.example.neginsharif.mdemo.datamanagement;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.neginsharif.mdemo.Bins;
import com.jasperlu.doppler.Doppler;

import java.util.List;

public class SeperatePeakProcess {
    protected int sampleNo;
    Doppler doppler;
    //  public List<Double> allBins = new ArrayList<>();
    protected int windowSize = 4096;
    Context context;
    ExternalStorage externalStorage;
    InternalStorage internalStorage;

    Handler handler;

    public SeperatePeakProcess(Doppler doppler, Context context) {
        this.doppler = doppler;
        this.context = context;
        externalStorage = new ExternalStorage(context);
        internalStorage = new InternalStorage();
    }


    public int getBinNumber(int frequency) {
        return windowSize * frequency / doppler.DEFAULT_SAMPLE_RATE;
    }


    public int[] getInRagebins(int rightShiftBinNo, int leftShiftBinNo) {
        int[] inRangeData = new int[2];
        inRangeData[0] = getBinNumber(doppler.MIN_FREQ) - leftShiftBinNo;
        inRangeData[1] = getBinNumber(doppler.MAX_FREQ) + rightShiftBinNo;
        return inRangeData;
    }


    public void save(List<Bins> allBins) {

        int allBinsIndex = 1;
        for (Bins bin : allBins) {

            internalStorage.saveBins(allBinsIndex, bin.bins, bin.time);
            Log.d("window size", "bins size: " + bin.bins.length);
            int indxMax1 = 0, indxMax2=0, indxLocalMax = 0;
            double max1 = 0.0, max2 = 0.0, localMax = 0.0;
            int[] inRangeBin = getInRagebins(40, 0);

            for (int i = inRangeBin[0]; i <= inRangeBin[1]; i++)
                if (bin.bins[i] > max1) {
                    indxMax1 = i;
                    max1 = bin.bins[i];
                }

            for (int i = indxMax1 + 1; i <= inRangeBin[1]; i++)
                if (bin.bins[i] > max2) {
                    indxMax2 = i;
                    max2 = bin.bins[i];
                }
       //finding the local maximum which is not in the bandwidth of the normal pilot to, but in the range of the related frequency
            for (int i = indxMax1 + 5; i <= inRangeBin[1]; i++)
                if (bin.bins[i] > localMax) {
                    indxLocalMax = i;
                    localMax = bin.bins[i];
                }


            double secondPeakRatio = max2 / max1;
            double localMaxRatio = localMax / max1;

            internalStorage.store( secondPeakRatio + ", ", "SecondPeakRatio");
            internalStorage.store(localMaxRatio + ", ", "LocalMaxRatio");
            internalStorage.store(indxLocalMax - indxMax1 + ", ", "DistToPilotIndex");
            allBinsIndex++;

        }
    }


}
