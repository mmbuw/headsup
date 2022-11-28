package com.example.lenovocom.ultrasoundcollisionsense.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FFTValuesDataAccess {

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public static final String TABLE_NAME = "fft_values";
    public static final String COLUMN_ID = "id";
    public static final String LOGGED_DATA = "loggedData";


    private String[] allColumns = {COLUMN_ID, LOGGED_DATA};

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LOGGED_DATA + " TEXT"
                    + ")";

    public FFTValuesDataAccess(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public FFTValues Create(String val) {
        ContentValues values = new ContentValues();
        values.put(LOGGED_DATA, val);
        long insertId = database.insert(TABLE_NAME, null, values);
        Cursor cursor = database.query(TABLE_NAME, allColumns, COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        FFTValues fftValues = cursorToFFTValues(cursor);
        cursor.close();
        return fftValues;
    }

    public void delete(FFTValues fftValues) {
        int id = fftValues.getId();
        System.out.println("FFTValue deleted with id: " + id);
        database.delete(TABLE_NAME, COLUMN_ID
                + " = " + id, null);
    }

    public List<FFTValues> getAll() {
        List<FFTValues> fftValues = new ArrayList<FFTValues>();

        Cursor cursor = database.query(TABLE_NAME,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FFTValues comment = cursorToFFTValues(cursor);
            fftValues.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return fftValues;
    }

    public void update(FFTValues fftValues) {
        int id = fftValues.getId();
        ContentValues values = new ContentValues();
        values.put(LOGGED_DATA, fftValues.getFftValues());
        database.update(TABLE_NAME, values, COLUMN_ID + " = " + id, null);
    }

    private FFTValues cursorToFFTValues(Cursor cursor) {
        FFTValues fftValues = new FFTValues();
        fftValues.setFftValues(cursor.getString(0));

        return fftValues;
    }


}
