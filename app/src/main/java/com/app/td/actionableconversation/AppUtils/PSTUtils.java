package com.app.td.actionableconversation.AppUtils;

import android.util.Log;

import com.app.td.actionableconversation.Algorithm.PSTMultiClassClassifier;
import com.app.td.actionableconversation.DB.Location;
import com.app.td.actionableconversation.PhoneCallHandlerTrans;

/**
 * Created by user on 27/04/2016.
 */
public class PSTUtils {

    static String filePath;

    public PSTUtils(String path) {
        this.filePath = path;
    }

    public static void savePST(PSTMultiClassClassifier classifier) {
        SerializationUtil.serialize(classifier, filePath);
    }

    public PSTMultiClassClassifier loadPST() {
        return (PSTMultiClassClassifier)SerializationUtil.deserialize(filePath);
    }

    public static char predictInputOnClassifier(String theCall,
                                                Location location,int[] time){
        int clock = time[0];
        int day = time[1];

        Log.i("debug","predictInputOnClassifier");
        Log.i("debug","the Call : " + theCall);
        Log.i("debug","the location : " + location.getLonge() + " " + location.getLat());
        Log.i("debug","time : " + time[0] + " " + time[1]);

        Double[] callRep = RepresentationUtils.mapData(theCall, location.getLat(), location.getLonge(), clock, day);
        if(PhoneCallHandlerTrans.classifier == null){
            int size = callRep.length;
            char[] labelSet = {'1' , '2' , '3' , '4' , '5' , '6'};
            PhoneCallHandlerTrans.classifier =  new PSTMultiClassClassifier(size,labelSet);

        }

        return PhoneCallHandlerTrans.classifier.predict(callRep);
    }
}

