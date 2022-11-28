package com.example.neginsharif.mdemo.datamanagement;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExternalStorage {

    private String state;
   Context context;

    public ExternalStorage(Context context) {

        this.context = context;
    }
//create constructor

    public void store(String data) {
// reference : https://www.youtube.com/watch?v=kerqarY7_wQ

        state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //File Root = Environment.getExternalStorageDirectory();
            //File Root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File Dir = new File(Environment.getExternalStorageDirectory() + "/myAppFile");
            if (!Dir.exists()) {
                Dir.mkdir();
            }

            File file = new File(Dir, "SpToFp.txt");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                fileOutputStream.write(data.getBytes());
                fileOutputStream.close();
                Toast.makeText(context, "file saved.", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else
            Toast.makeText(context, "SD card not available.", Toast.LENGTH_SHORT).show();
    }


    public void write(int sampleNo, int windowSize, List<Double> tempBins) {


        state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //File Root = Environment.getExternalStorageDirectory();
            File Root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File Dir = new File(Root.getAbsolutePath() + "/myAppFile");
            List<File> files = new ArrayList<>();
            List<FileOutputStream> fileOutputStreams = new ArrayList<>();


            if (!Dir.exists()) {
                Dir.mkdir();
            }


            for (int n = 0; n < sampleNo; n++) {

                files.add(new File(Dir, "Float" + (n + 1) + ".txt"));

            }
            try {
                //FileOutputStream fileOutputStream = new FileOutputStream(file);

                for (int n = 0; n < sampleNo; n++) {
                    fileOutputStreams.add(new FileOutputStream(files.get(n)));
                }


                for (int n = 1; n <= sampleNo; n++) {

                    int i = 0;
                    for (i = (n - 1) * windowSize + i; i < n * windowSize; i++) {


                        fileOutputStreams.get(n - 1).write((String.valueOf(tempBins.get(i)) + "\n").getBytes());

                    }

                    fileOutputStreams.get(n - 1).close();

                }

           //     Toast.makeText(context, "file saved.", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else{}
         //   Toast.makeText(context, "SD card not available.", Toast.LENGTH_SHORT).show();


    }


    public String read() {
        //File Root = Environment.getExternalStorageDirectory();
        File Root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ;
        File Dir = new File(Root.getAbsolutePath() + "/myAppFile");
        File file = new File(Dir, "Myfile.txt");
        String message;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            while ((message = bufferedReader.readLine()) != null) {
                stringBuffer.append(message + "\n");
            }
            return stringBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
