package com.example.neginsharif.mdemo;

public class Bins {
    public long time;
    public double[] bins;

    public Bins(double[] bins) {
        this.time = System.currentTimeMillis();
        this.bins = bins;
    }
}
