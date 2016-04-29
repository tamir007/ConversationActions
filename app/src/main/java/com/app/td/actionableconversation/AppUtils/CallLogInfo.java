package com.app.td.actionableconversation.AppUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daniel zateikin on 16/02/2016.
 */
public class CallLogInfo {

    public static String[] getMostCalled(int numOfContacts, Activity activity){
        Uri allCalls = Uri.parse("content://call_log/calls");
        Log.i("debug", "getMostCalled");
        Cursor c = activity.managedQuery(allCalls, null, null, null, null);
        HashMap<String,Integer> phoneNumIndices = new HashMap<>();
        HashMap<Integer, String> indicesToPhone = new HashMap<>();

        ArrayList<Integer> scores = new ArrayList<Integer>();

        int number = c.getColumnIndex(CallLog.Calls.NUMBER);
        int index = 0;
        int occurrences;
        int mostRecent = 200;
        while (c.moveToNext() && mostRecent > 0 ) {
            mostRecent--;
            String phNumber = c.getString(number);

            if(phoneNumIndices.get(phNumber) != null){
                occurrences = scores.get(phoneNumIndices.get(phNumber));
                scores.set(phoneNumIndices.get(phNumber), occurrences + 1);
            }
            else{

                scores.add(index, 1);
                phoneNumIndices.put(phNumber, index);
                indicesToPhone.put(index, phNumber);
                index++;

            }
        }

        String[] fiveContacts = new String[5];

        for (int j = 0; j < 5; j++) {

            int maxNumber = 0;
            int maxIndex = 0;
            for (int i = 0; i < scores.size(); i++) {
                if (scores.get(i) > maxNumber) {
                    maxNumber = scores.get(i);
                    maxIndex = i;
                }
            }
            fiveContacts[j] = indicesToPhone.get(maxIndex);
            scores.add(maxIndex, -1);

        }

        return fiveContacts;
    }


    public static String getNameByNumber(Context context , String number){

        Log.i("debug","start getNameByNumber");
        ContentResolver resolver = context.getContentResolver();
        Log.i("debug","got ContentResolver");
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        Cursor cursor = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        Log.i("debug","Caught cursor");
        String contactName = "";
        if(cursor == null){
            Log.i("debug", "name not found, cursor is null");
            return contactName;
        }

        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            Log.i("debug","found name");
        }
        Log.i("debug","returning :" + contactName + " from getNameByNumber");
        return contactName;
    }


}
