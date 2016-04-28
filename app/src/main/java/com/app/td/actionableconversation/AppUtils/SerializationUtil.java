package com.app.td.actionableconversation.AppUtils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.app.td.actionableconversation.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtil {

    // deserialize to Object from given file
    public static Object deserialize(String fileName) {

        try {
            Log.i("debug", "start deserialization with : " + fileName);
            File inFile = new File(Environment.getExternalStorageDirectory(), fileName);
            Log.i("debug", "opened file");
            FileInputStream fis = new FileInputStream(inFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            Log.i("debug", "deserialization successful with : " + fileName);
            return obj;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i("debug", "deserialize failed");
            e.printStackTrace();
        }

        return null;
    }

    public static Object deserialize(InputStream myInput) {

        try {

            ObjectInputStream ois = new ObjectInputStream(myInput);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("ERROR1");
            e.printStackTrace();
        }

        return null;
    }

    // serialize the given object and save it to file
    public static void serialize(Object obj, String fileName, boolean isDestroy){
        try{
            Log.i("debug", "start serialization");
            File outFile = new File(Environment.getExternalStorageDirectory(), fileName);
            Log.i("debug", "opened file");
            FileOutputStream fos = new FileOutputStream(outFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(obj);
            fos.close();
            Log.i("debug", "serialization successful");
        }catch(Exception e){
            Log.i("debug", "serialization failed");
        }

        if(isDestroy){
            if(fileName == "db.ser"){
                MainActivity.dbSaved = true;
            }else if(fileName == "classifier.ser"){
                MainActivity.pstSaved = true;
            }
        }

    }
}