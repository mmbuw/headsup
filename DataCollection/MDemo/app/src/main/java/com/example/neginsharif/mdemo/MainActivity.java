package com.example.neginsharif.mdemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.neginsharif.mdemo.datamanagement.ExternalStorage;
import com.example.neginsharif.mdemo.datamanagement.InternalStorage;
import com.example.neginsharif.mdemo.datamanagement.SeperatePeakProcess;
import com.example.neginsharif.mdemo.notification.PopUpNotification;
import com.jasperlu.doppler.Doppler;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

//Inspired by MDemo; reference: https://github.com/jasper-lu/Doppler-Android-Demo

public class MainActivity extends AppCompatActivity {

    Doppler doppler;

    private XYSeries mSeries;
    private XYSeries div10;
    private GraphicalView mChart;
    private final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    private XYSeriesRenderer mRenderer;
    private XYSeriesRenderer div10Renderer;
    private final XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    private boolean gesturesOn = false;
    boolean pause = false;
    private int fileCounter = 0;
    SeperatePeakProcess seperatePeakProcess;

    protected int windowSize = 4096;
    Button save;
    InternalStorage internalStorage = new InternalStorage();
    ExternalStorage externalStorage;
    PopUpNotification notification;
    public List<Bins> allBins=new ArrayList<>();
    LinearLayout chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save = (Button) findViewById(R.id.save);
        chart = (LinearLayout) findViewById(R.id.chart);
        externalStorage = new ExternalStorage(this);

        doppler = TheDoppler.getDoppler();
        doppler.start();

        renderGraph();

        startGraph();
        seperatePeakProcess = new SeperatePeakProcess(doppler, this);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doppler.pause();
                seperatePeakProcess.save(allBins);
            }
        });


    }



    private void renderGraph() {
        mSeries = new XYSeries("Vols/FreqBin");
        div10 = new XYSeries("div10");
        mSeries.add(3, 4);
        mRenderer = new XYSeriesRenderer();
        div10Renderer = new XYSeriesRenderer();
        div10Renderer.setColor(getResources().getColor(R.color.red));
        dataset.addSeries(mSeries);
        dataset.addSeries(div10);
        renderer.addSeriesRenderer(mRenderer);
        renderer.addSeriesRenderer(div10Renderer);
        renderer.setPanEnabled(false);
        renderer.setZoomEnabled(false);
        mChart = ChartFactory.getLineChartView(this, dataset, renderer);

        chart.addView(mChart);
    }




    public void startGraph() {
        doppler.setOnReadCallback(new Doppler.OnReadCallback() {
            @Override
            public void onBandwidthRead(int leftBandwidth, int rightBandwidth) {

            }

            @Override
            public void onBinsRead(double[] bins) {
                mSeries.clear();
                div10.clear();
                double frac = bins[929] * doppler.maxVolRatio;
                //Log.d("PRIMARY VOL", bins[929] + "");
                allBins.add(new Bins(bins));

                for (int i = 0; i < windowSize / 2; ++i) {
                    mSeries.add(i, bins[i]);
                    div10.add(i, frac);
                }
                mChart.repaint();


            }

        });

    }




}
