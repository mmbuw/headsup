package com.example.neginsharif.mdemo.datamanagement;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// since the speed of filling the arraylist is much more than the speed of writing on file and storing, we use BlockingQueue.
// ref (as an example): https://www.journaldev.com/1034/java-blockingqueue-example

public class InternalStorage {
    List<Double> bins = new ArrayList<>();
    BlockingQueue<Double> binsQueue;
    int counter = 1;
    BlockingQueue<Long> timeStamp;


    public InternalStorage() {
        timeStamp = new ArrayBlockingQueue<>(1000);
        binsQueue = new ArrayBlockingQueue<Double>(1000 * 4096);
        Consumer consumer = new Consumer(binsQueue, timeStamp);
        new Thread(consumer).start();

    }

    public void store(String tList, String name) {

        String filePath = "/data/data/com.example.neginsharif.mdemo/" + name + ".txt";
        FileOutputStream foss = null;
        try {
            foss = new FileOutputStream(filePath, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DataOutputStream doss = new DataOutputStream(foss);

        try {
            doss.writeChars(tList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            doss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeAllBins(final List<Double> tList, final String name, long time) {


        try {
            timeStamp.put(time);
            for (Double item : tList) {

                binsQueue.put(item);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //counter++;
        //bins.addAll(tList);
        //if (counter == 10) {
        //   counter = 0;
        //   binsQueue.addAll(bins);


        //Producer producer = new Producer();
        //Consumer consumer = new Consumer(name);
        //new Thread(producer).start();
        //new Thread(consumer).start();

        // bins.clear();
 /*           new Thread(new Runnable() {
                @Override
                public void run() {


                    String filePath = "/data/data/com.example.neginsharif.mdemo/" + name + ".txt";
                    FileOutputStream foss = null;
                    try {
                        foss = new FileOutputStream(filePath, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    DataOutputStream doss = new DataOutputStream(foss);

                    try {
                        doss.writeChars(String.valueOf(bins));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        doss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bins.clear();
                }
            }).start();*/


        //}
    }


    public void write(int sampleNo, int windowSize, List<Double> tempBins) {

        List<String> filePath = new ArrayList<>();
        List<FileOutputStream> foss = new ArrayList<>();
        List<DataOutputStream> doss = new ArrayList<>();

        for (int n = 0; n < sampleNo; n++) {

            filePath.add("/data/data/com.example.neginsharif.mdemo/Float" + (n + 1) + ".txt");


        }
        try {
            for (int n = 0; n < sampleNo; n++) {
                foss.add(new FileOutputStream(filePath.get(n), true));

            }
            for (int n = 0; n < sampleNo; n++) {
                doss.add(new DataOutputStream(foss.get(n)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        for (int n = 1; n <= sampleNo; n++) {

            int i = 0;
            for (i = (n - 1) * windowSize + i; i < n * windowSize; i++) {

                try {
                    doss.get(n - 1).writeChars(String.valueOf(tempBins.get(i)) + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            try {
                doss.get(n - 1).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveBins(int fileNo, double[] bins, long time) {
        String filePath = "/data/data/com.example.neginsharif.mdemo/" + fileNo + /*"," + time +*/ ".txt";
        String filePath2 = "/data/data/com.example.neginsharif.mdemo/time.txt";
        FileOutputStream foss = null;
        FileOutputStream foss2 = null;
        try {
            foss = new FileOutputStream(filePath, true);
            DataOutputStream doss = new DataOutputStream(foss);
            foss2 = new FileOutputStream(filePath2, true);
            DataOutputStream doss2 = new DataOutputStream(foss2);
            for (double bin:bins){
                doss.writeChars(bin+"\n");
            }
// this is the time that the bins are read, the number of figures in this file equals to the number of text files, and they are respectively showing the time that bins in file are read
            doss2.writeChars(time + ", ");
            doss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


   /* public class Producer implements Runnable {

        private BlockingQueue<Double> queue;
        // private List<Double> binsList;

        public Producer() {
            //this.queue = q;
            //        this.binsList = binsList;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    queue.put(binsQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }*/

    public class Consumer implements Runnable {

        private BlockingQueue<Double> queue;
        private int fileCounter;
        private int binCounter;
        private BlockingQueue<Long> time;

        public Consumer(BlockingQueue<Double> q, BlockingQueue<Long> time) {
            queue = q;
            fileCounter = 1;
            binCounter = 0;
            this.time = time;
        }

        @Override
        public void run() {
            String filePath = "/data/data/com.example.neginsharif.mdemo/" + fileCounter + ".txt";
            FileOutputStream foss = null;
            try {
                foss = new FileOutputStream(filePath, true);
                DataOutputStream doss = new DataOutputStream(foss);

                while (true) {
                    binCounter++;
                    if (binCounter % 4096 == 0) {
                        fileCounter++;
                        filePath = "/data/data/com.example.neginsharif.mdemo/" + fileCounter + ".txt";
                        doss.close();
                        foss = new FileOutputStream(filePath, true);
                        doss = new DataOutputStream(foss);
                        doss.writeChars(time.take() + "\n");
                    }

                    doss.writeChars(String.valueOf(queue.take()) + ",");

                }

                //doss.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }


        }
    }
}
