package com.example.neginsharif.mdemo.datamanagement;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutlierDetector {

    List<Double> dataSet = new ArrayList<>();


    public OutlierDetector(List<Double> dataSet) {
        this.dataSet = dataSet;
    }

    private List<Double> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<Double> dataSet) {
        this.dataSet = dataSet;
    }


    public List<Double> getUpperOutliers(){
        List<Double> upperOutliers = new ArrayList<>();
        double upperBound = getBounds()[1];
        for (int i = 0; i< getDataSet().size(); i++){
            if(getDataSet().get(i)> upperBound)
                upperOutliers.add(getDataSet().get(i));
        }
        return upperOutliers;
    }


    private double[] getBounds() {

        int k = 3;
        double indx = medianIndxFinder(getDataSet());
        double[] q = getQuartiles(getDataSet(), indx);
        double q1 = q[0];
        double q3 = q[1];
        double interquartile = q3-q1;
        return new double[] {(q1- interquartile*k), (q3+ interquartile*k) };

    }

    // the upper bound plus some percentage is taken as threshold for SECOND_PEAK_RATIO
    public double getThreshold (double factor){
        return getBounds()[1] * (factor + 1);
    }




    private double[] getQuartiles(List<Double> exList, double medianIndx) {
        List<Double> part1 = new ArrayList();
        List<Double> part2 = new ArrayList();
        double q1, q3;

        if (exList.size() % 2 == 0) {
            part1.addAll(exList.subList(0, (int) medianIndx + 1));
            part2.addAll(exList.subList((int) medianIndx + 1, exList.size()));
        } else {
            part1.addAll(exList.subList(0, (int) medianIndx));
            part2.addAll(exList.subList((int) medianIndx + 1, exList.size()));
        }

        q1 = getMedian(part1);
        Log.d("median", "part1 median:" + q1);
        q3 = getMedian(part2);
        Log.d("median", "part2 median:" +  q3);


        return new double[]{q1, q3};
    }

    private double medianIndxFinder(List<Double> exList) {
        double medianIndx;
        Collections.sort(exList);
        if (exList.size() % 2 == 0)
            medianIndx = Math.floor((double) ((exList.size() + 1) / 2) - 1);
        else
            medianIndx = (double) ((exList.size() + 1) / 2) - 1;

        return medianIndx;
    }

    private double getMedian(List<Double> exList) {
        double median;
        if (exList.size() % 2 == 0)
            median = ((double) exList.get(exList.size() / 2) + (double) exList.get(exList.size() / 2 - 1)) / 2;
        else {
            median = (double) exList.get((int) Math.floor(exList.size() / 2));

            // median = (double) exList.get((int) (exList.size() / 2));
        }
        return median;

    }



}
