package com.app.td.actionableconversation;

import com.app.td.actionableconversation.AppUtils.CallLogInfo;
import com.app.td.actionableconversation.AppUtils.PSTUtils;
import com.app.td.actionableconversation.AppUtils.SerializationUtil;
import com.app.td.actionableconversation.DB.DB;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.LocationServices;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    String debugTag = "debug";
    PSTUtils pstUtils = new PSTUtils("/data/classifier.ser");
    String dbPath = "/data/db.ser";
    static DB commonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        PhoneCallHandlerTrans.classifier = pstUtils.loadPST();
        loadDB();
    }

    public void saveDB(){
        SerializationUtil.serialize(commonData, dbPath);
    }

    public static HashMap<Character,String> getCharToContactMap(){
        return commonData.getCToS();
    }

    public static HashMap<String,Character> getContactToCharMap(){
        return commonData.getSToC();
    }

    public void loadDB(){
        commonData = (DB)SerializationUtil.deserialize(dbPath);
        if(commonData == null) {
            commonData = new DB();
            String[] fiveMostCalled = CallLogInfo.getMostCalled(5, this);
            commonData.addUsers(fiveMostCalled);
            saveDB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(debugTag, "building google client");
        PhoneCallHandlerTrans.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(debugTag, "google: on connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(debugTag, "google: on connection failed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(debugTag, "google: on connected");
    }
}
