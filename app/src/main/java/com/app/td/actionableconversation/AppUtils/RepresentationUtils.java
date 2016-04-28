package com.app.td.actionableconversation.AppUtils;
import android.util.Log;

import com.app.td.actionableconversation.MainActivity;
import com.app.td.actionableconversation.PhoneCallHandlerTrans;
import com.app.td.actionableconversation.R;

import java.util.HashMap;


/**
 * Created by user on 15/02/2016.
 */
public class RepresentationUtils {
    // ==================== REAL DATA ========================

    private final static int numOfLocations = 4;

//    // jerusalem location
//    private final static double jerusLat = 31.768319; // 20
//    private final static double jerusLong = 35.21370999999999;// 20
//
//    // tel aviv location
//    private final static double telAvivLat = 32.0852999;// 20
//    private final static double telAvivLong = 32.0852999;// 60
//
//    // Haifa location
//    private final static double haifaLat = 32.7940463; //60
//    private final static double haifaLong = 34.98957100000007; //20
//
//    // beersheva location
//    private final static double beershevaLat = 31.252973; //60
//    private final static double beershevaLong = 34.791462000000024; //60

    private final static double jerusLat = 20; // 20
    private final static double jerusLong = 20;// 20

    // tel aviv location
    private final static double telAvivLat = 20;// 20
    private final static double telAvivLong = 60;// 60

    // Haifa location
    private final static double haifaLat = 60; //60
    private final static double haifaLong = 20; //20

    // beersheva location
    private final static double beershevaLat = 60; //60
    private final static double beershevaLong = 60; //60


    private static HashMap<String,Integer> myDict ;
    private static HashMap<String,Integer> myPlaces;

    /**
     *
     * @param call - String of call transcript
     * @param myLat - latitude of user
     * @param myLong - longitude of user
     * @param clock - 0-47 int where 0 =  00:00 , 47 = 23:30, representing time arround clock
     * @param day - 0-6 int representing day , 0 = sunday , 6 = saturday
     * @return double[] representing one call
     */
    public static Double[] mapData(String call, double myLat, double myLong, int clock, int day){
        Log.i("debug", "mapData");
        double clockWeight = 1.0;
        double dayWeight = 1.0;
        double locWeight = 0.0;
        double docWeight = 1.0;

        myDict = (HashMap<String, Integer>)
                SerializationUtil.deserialize(
                        PhoneCallHandlerTrans.myContext.getResources().openRawResource(R.raw.dict));

        myPlaces = (HashMap<String, Integer>)
                SerializationUtil.deserialize(
                        PhoneCallHandlerTrans.myContext.getResources().openRawResource(R.raw.places));
        Double[] resultVec;
        double[] clockVec = getClockVec(clock, clockWeight);
        double[] dayVec = getDayVec(day, dayWeight);
        double[] locVec = getLocationVec(myLat, myLong, locWeight);
        double[] wordRep = parseDocument(call, docWeight);
        double[] vector = appendArrays(wordRep,clockVec);
        vector = appendArrays(vector,dayVec);
        vector = appendArrays(vector,locVec);
        resultVec = new Double[vector.length];
        for(int j = 0 ; j < resultVec.length ; j++){
            resultVec[j] = new Double(vector[j]);
        }
        Log.i("debug", "mapData success");
        return resultVec;
    }

    private static double[] appendArrays(double[] aArray, double[] bArray){
        int aLen = aArray.length;
        int bLen = bArray.length;
        double[] newList = new double[aLen + bLen];

        for(int i = 0 ; i < aLen ; i++){
            newList[i] = aArray[i];
        }

        for(int i = 0 ; i < bLen ; i++){
            newList[i + aLen] = bArray[i];
        }

        return newList;
    }

    private static double[] parseDocument(String theCall, double weight){

        theCall = theCall.toLowerCase();
        int dictShift = myDict.size();
        double[] vec = new double[dictShift];
        Integer slot = null;
        // check for words
        String[] words = theCall.split(" ");

        for(String str : words){
            try{
                if( (slot = myDict.get(str)) != null){
                    vec[slot] += weight;
                }
            }catch (Exception e){
                Log.i("debug", "failed with dictionary");
            }

        }
        return vec;
    }

    private static double calcEuclideanDist(double aLat, double aLong, double bLat, double bLong){

//        return Math.sqrt(Math.pow(aLat-bLat,2) + Math.pow(aLong-bLong, 2));
        return (Math.pow(aLat-bLat,2) + Math.pow(aLong-bLong, 2));
    }


    private static double[] getLocationVec(double myLat, double myLong, double weight){

        // convert coordinations to distance from locations

        double[] locVec = new double[numOfLocations];
        locVec[0] = calcEuclideanDist(myLat, myLong, jerusLat, jerusLong)*weight;
        locVec[1] = calcEuclideanDist(myLat, myLong, telAvivLat, telAvivLong)*weight;
        locVec[2] = calcEuclideanDist(myLat, myLong, haifaLat, haifaLong)*weight;
        locVec[3] = calcEuclideanDist(myLat, myLong, beershevaLat, beershevaLong)*weight;

        return locVec;
    }

    private static double[] getDayVec(int day, double weight){
        // convert day to triangle around time area ( cyclic solution)

        int weekSize = 7;

        double[] dayVec = new double[weekSize];
        dayVec[(int)Math.floor((day-1)%weekSize)] = 0.5*weight;
        dayVec[day] = 1.0*weight;
        dayVec[(int)Math.floor((day+1)%weekSize)] = 0.5*weight;

        return dayVec;
    }

    private static double[] getClockVec(int clock, double weight){
        // convert clock to triangle around time area ( cyclic solution)

        int clockSize = 48;
        double[] clockVec = new double[clockSize];
        clockVec[(int)Math.floor((clock-2)%clockSize)] = 0.5*weight;
        clockVec[(int)Math.floor((clock-1)%clockSize)] = 1.0*weight;
        clockVec[clock] = 2.0*weight;
        clockVec[(int)Math.floor((clock+1)%clockSize)] = 1.0*weight;
        clockVec[(int)Math.floor((clock+2)%clockSize)] = 0.5*weight;
        return clockVec;
    }


}