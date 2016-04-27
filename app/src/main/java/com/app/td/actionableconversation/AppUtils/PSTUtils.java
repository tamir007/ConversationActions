package com.app.td.actionableconversation.AppUtils;

import android.util.Log;

import com.app.td.actionableconversation.Algorithm.PSTMultiClassClassifier;
import com.app.td.actionableconversation.DB.Location;
import com.app.td.actionableconversation.MainActivity;
import com.app.td.actionableconversation.PhoneCallHandlerTrans;
import com.google.android.gms.location.LocationServices;

import static com.app.td.actionableconversation.AppUtils.SerializationUtil.serialize;

/**
 * Created by user on 27/04/2016.
 */
public class PSTUtils {

    String filePath;

    public PSTUtils(String path) {
        this.filePath = path;
    }

    public void savePST(PSTMultiClassClassifier classifer) {
        serialize(classifer, filePath);
    }

    public PSTMultiClassClassifier loadPST() {
        return (PSTMultiClassClassifier)SerializationUtil.deserialize(filePath);
    }

    public static char predictInputOnClassifier(PSTMultiClassClassifier classifier , String theCall,
                                                Location location,int[] time){
        int clock = time[0];
        int day = time[1];

        Double[] callVec = RepresentationUtils.mapData(theCall, location.getLat(), location.getLonge(), clock, day);
        return classifier.predict(callVec);
    }
}
