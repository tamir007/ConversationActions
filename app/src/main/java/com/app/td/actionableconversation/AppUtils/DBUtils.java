package com.app.td.actionableconversation.AppUtils;

import android.util.Log;

import com.app.td.actionableconversation.DB.*;

/**
 * Created by user on 14/02/2016.
 */
public class DBUtils {

    private static String DBPath = "DB PATH";

    public static void saveDB(DB theDB, boolean isDestroy){
        SerializationUtil.serialize(theDB,DBPath,isDestroy);
        Log.i("debug", "after DB save");

    }

    public static DB loadDB(){
        DB newDB = (DB)SerializationUtil.deserialize(DBPath);
        Log.i("debug", "after DB load");

        return newDB;
    }
}
