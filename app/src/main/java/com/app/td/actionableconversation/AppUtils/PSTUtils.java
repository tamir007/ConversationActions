package com.app.td.actionableconversation.AppUtils;

import android.util.Log;

import com.app.td.actionableconversation.Algorithm.PSTMultiClassClassifier;
import com.app.td.actionableconversation.DB.ourLocation;
import com.app.td.actionableconversation.PhoneCallHandlerTrans;

/**
 * Created by user on 27/04/2016.
 */
public class PSTUtils {

    static String filePath;

    public PSTUtils(String path) {
        this.filePath = path;
    }

    public static void savePST(PSTMultiClassClassifier classifier, boolean isDestroy) {
        SerializationUtil.serialize(classifier, filePath, isDestroy);
    }

    public PSTMultiClassClassifier loadPST() {
        return (PSTMultiClassClassifier)SerializationUtil.deserialize(filePath);
    }

    public static char predictInputOnClassifier(String theCall,
                                                ourLocation ourLocation,int[] time){
        int clock = time[0];
        int day = time[1];

        Log.i("debug","predictInputOnClassifier");
        Log.i("debug","the Call : " + theCall);
        Log.i("debug","the ourLocation : " + ourLocation.getLonge() + " " + ourLocation.getLat());
        Log.i("debug","time : " + time[0] + " " + time[1]);

        Double[] callRep = RepresentationUtils.mapData(theCall, ourLocation.getLat(), ourLocation.getLonge(), clock, day);
        if(PhoneCallHandlerTrans.classifier == null){
            int size = callRep.length;
            char[] labelSet = {'1' , '2' , '3' , '4' , '5' , '6'};
            PhoneCallHandlerTrans.classifier =  new PSTMultiClassClassifier(size,labelSet);

        }
        char prediction = PhoneCallHandlerTrans.classifier.predict(callRep);
        Log.i("debug","Prediction : " + prediction);
        return prediction;
    }
}

