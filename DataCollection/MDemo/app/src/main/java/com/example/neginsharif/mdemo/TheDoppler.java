package com.example.neginsharif.mdemo;

import com.jasperlu.doppler.Doppler;

/**
 * Created by neginsharif on 29.01.19.
 */

public class TheDoppler {
    private static Doppler doppler;

    public static Doppler getDoppler() {
        if (doppler == null) {
            doppler = new Doppler();
        }
        return doppler;
    }
}
