package com.example.neginsharif.mdemo;

/**
 * Created by neginsharif on 29.01.19.
 */

public class TheDoppler {
    private static MDoppler doppler;

    public static MDoppler getDoppler() {
        if (doppler == null) {
            doppler = new MDoppler();
        }
        return doppler;
    }
}
