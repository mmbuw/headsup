package com.example.neginsharif.mdemo.datamanagement;

import android.os.Handler;
import android.util.Log;

import com.example.neginsharif.mdemo.MDoppler;

import java.util.ArrayList;
import java.util.List;

public class SeperatePeakProcess {
    protected int sampleNo;
    //MDoppler doppler;
    //  public List<Double> allBins = new ArrayList<>();
    protected int windowSize = 4096;
    InternalStorage internalStorage;

    Handler handler;

    OutlierDetector outlierDetector;
    List<Double> localMaxRatios;

    public SeperatePeakProcess() {
       // this.doppler = doppler;
        internalStorage = new InternalStorage();
    }


    public int getBinNumber(int frequency) {
        return windowSize * frequency / MDoppler.DEFAULT_SAMPLE_RATE;
    }


    public int[] getInRagebins(int rightShiftBinNo, int leftShiftBinNo) {
        int[] inRangeData = new int[2];
        inRangeData[0] = getBinNumber(MDoppler.MIN_FREQ) - leftShiftBinNo;
        inRangeData[1] = getBinNumber(MDoppler.MAX_FREQ) + rightShiftBinNo;
        return inRangeData;
    }


   /* public void save2(List<GraphFragment.Bins> allBinss) {

        int allBinsIndex = 1;
        for (GraphFragment.Bins bin : allBinss) {

            internalStorage.saveBins(allBinsIndex, bin.bins, bin.time);
            Log.d("window size", "bins size: " + bin.bins.length);
            int indxMax1 = 0, indxMax2 = 0, indxLocalMax = 0;
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

            internalStorage.store(secondPeakRatio + ", ", "SecondPeakRatio");
            internalStorage.store(localMaxRatio + ", ", "LocalMaxRatio");
            internalStorage.store(indxLocalMax - indxMax1 + ", ", "DistToPilotIndex");
            allBinsIndex++;

        }
    }*/

    private void save(List<double[]> allBins) {

        localMaxRatios = new ArrayList<>();
        for (double[] bin : allBins) {

            int indxLocalMax =0, indxMax1 = 0;
            double localMax= 0.0 , max1 = 0.0;
            int[] inRangeBin = getInRagebins(40, 0);

            for (int i = inRangeBin[0]; i <= inRangeBin[1]; i++)
                if (bin[i] > max1) {
                    indxMax1 = i;
                    max1 = bin[i];
                }
           // int pilotIndx = MDoppler.freqIndex;
           // double pilotMagnitude = bin[pilotIndx];

            //finding the local maximum which is not in the bandwidth of the normal pilot to, but in the range of the related frequency
            for (int i = indxMax1 + 4; i <= inRangeBin[1]; i++)
                if (bin[i] > localMax) {
                    indxLocalMax = i;
                    localMax = bin[i];
                }


            double localMaxRatio = localMax / max1;

            localMaxRatios.add(localMaxRatio);
            Log.d("LocalMaxRatios", " localmaxratios :  " + localMaxRatios);
        }

    }


    public double getThreshold(List<double[]> allBins, double factor) {
        save(allBins);
        outlierDetector = new OutlierDetector(localMaxRatios);
        return outlierDetector.getThreshold(factor);

    }


}
