package com.example.lenovocom.ultrasoundcollisionsense;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RecentMagnitudeData {
    private int windowSize;
    List<Short> shortValues;
    List<List<Short>> allWindows;

    public RecentMagnitudeData(int windowSize){
        shortValues = new ArrayList<Short>();
        allWindows = new ArrayList<>();
        this.windowSize = windowSize;
        initiateWindow();

    }

    public void initiateWindow(){

        /*if(this.windowSize < shortValues.size()){
            int endloop = shortValues.size() - this.windowSize;
            for(int i=0 ; i < endloop ; i++){
                shortValues.remove(0);
            }
        }
        else {

            int endloop = this.windowSize - shortValues.size();
            short[] temp = new short[endloop];
            for (int i = 0; i < endloop; i++) {
                temp[i] = 0;
                shortValues.add(i,temp);

            }*/
        shortValues.clear();

        //}
    }

    public void addToQueue(Short[] newItem){
        List<Short> itemList = new ArrayList<Short>(Arrays.asList(newItem));
        //itemList = Arrays.asList(newItem);

        if(shortValues.size() + itemList.size() > windowSize) {
            int freeWindow = windowSize - shortValues.size();
            shortValues.addAll(itemList.subList(0,freeWindow));
            /*for(int j=0; j<freeWindow; j++){
                itemList.remove(0);
            }*/
            itemList.removeAll(itemList.subList(0,freeWindow));
            List<Short> currentWindow = new ArrayList<Short>();
            currentWindow.addAll(shortValues.subList(0,shortValues.size()));
            allWindows.add(currentWindow);
            shortValues.clear();
            int remainSize = itemList.size();
            int loopCounter = (remainSize / windowSize) + 1;
            for(int i=0; i<loopCounter; i++) {
                if(i!=(loopCounter - 1)) {
                    if(itemList.size()==0)
                        continue;
                    shortValues.addAll(itemList.subList(0,windowSize));
                    itemList.removeAll(itemList.subList(0,windowSize));
                    List<Short> currentWindow1 = new ArrayList<Short>();
                    currentWindow1.addAll(shortValues.subList(0,shortValues.size()));
                    allWindows.add(currentWindow1);
                    /*for(int j=0; j<windowSize; j++){
                        itemList.remove(0);
                    }*/
                    shortValues.clear();
                } else
                    shortValues.addAll(itemList);
            }
        } else {
            if(shortValues.size() + itemList.size() == windowSize) {
                List<Short> currentWindow = new ArrayList<Short>();
                currentWindow.addAll(shortValues.subList(0,shortValues.size()));
                currentWindow.addAll(itemList);
                allWindows.add(currentWindow);
                shortValues.clear();
            }
            else
                shortValues.addAll(itemList);
        }

    }

    /*public short[] getRecentWindow(){
        List<short[]> shortArray = new List<short[]>() ;
        for(int i = 0; i < windowSize ; i++)
            shortArray[i] = shortValues.get(i);
        return shortArray;
    }*/

    public List<Double[]> recentWindow (){
        List<Double[]> windowsList = new ArrayList<>();
        for (int i =0; i< allWindows.size(); i++){
            if(i==allWindows.size()-1)
                break;

            Double[] window = new Double[allWindows.get(i).size()];
            for(int j=0; j<windowSize; j++)
                try {
                    window[j] = Double.valueOf(allWindows.get(i).get(j));
                }catch (Exception e)
                {
                    return windowsList;
                }
            windowsList.add(window);
        }
        return  windowsList;

    }




}
