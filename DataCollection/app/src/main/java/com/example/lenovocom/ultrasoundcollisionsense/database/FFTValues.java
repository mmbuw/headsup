package com.example.lenovocom.ultrasoundcollisionsense.database;

public class FFTValues {
    private int id;
    private String fftValues;

    public FFTValues(int id, String fftValues) {
        this.id = id;
        this.fftValues = fftValues;
    }

    public FFTValues() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFftValues() {
        return fftValues;
    }

    public void setFftValues(String fftValues) {
        this.fftValues = fftValues;
    }
}
