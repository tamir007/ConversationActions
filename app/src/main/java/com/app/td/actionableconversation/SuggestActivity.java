package com.app.td.actionableconversation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class SuggestActivity extends AppCompatActivity {

    public final String debugTag = "debug";
    Gson gson;
    double longi;
    double lat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(debugTag, "on create (SUGGEST)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        Intent intent = getIntent();
        String number = intent.getStringExtra(PhoneCallHandlerTrans.MENTIONED_NAMES_EXTRA);
        Log.i(debugTag, "Contact : " + number);
        longi = intent.getDoubleExtra(PhoneCallHandlerTrans.LONGITUDE, 0.0);
        Log.i(debugTag, "longitude : " + longi);
        lat = intent.getDoubleExtra(PhoneCallHandlerTrans.LATITUDE, 0.0);
        Log.i(debugTag, "latitude : " + lat);
        String contactName = "";
        contactName = intent.getStringExtra(PhoneCallHandlerTrans.CONTACT_NAME);
        gson = new Gson();
        // create the layout params that will be used to define how your
        // button will be displayed
        if(number != "other"){
            setUpContactSuggestion(number, contactName);
        }
        Log.i(debugTag, "creating map");


    }

    @Override
    public void onStart() {
    super.onStart();
            GoogleMap googleMap;
            try{
                Log.i(debugTag, "FragmentManager created");
                SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                Log.i(debugTag, "mapFragment created");
                googleMap = mapFrag.getMap();
                Log.i(debugTag, "map created");
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Log.i(debugTag, "chang map type to MAP_TYPE_NORMAL");
                CameraUpdate center=
                        CameraUpdateFactory.newLatLng(new LatLng(lat, longi));
                Log.i(debugTag, "chang map type to newLatLng");
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
                Log.i(debugTag, "CameraUpdateFactory zoom 15");
                googleMap.moveCamera(center);
                Log.i(debugTag, "moveCamera in map");
                googleMap.animateCamera(zoom,2500,null);
                Log.i(debugTag, "animateCamera in map");
            }catch(Exception e){
                Log.i(debugTag, "Exception : " + e);
            }


    }




    private void setUpContactSuggestion(final String number, String contactName) {
        Log.d("debug", "in make buttons");
        ImageButton myButton = (ImageButton) findViewById(R.id.callButton);
        TextView contactText = (TextView) findViewById(R.id.contactText);
        contactText.setText("Contact " + contactName);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(debugTag, "clicked");
                // call number of contact name
                if (!number.matches("\\+[0-9]+") && !number.matches("[0-9]+")) {
                    return;
                }
                Log.d("debug", "call : " + number);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                startActivity(callIntent);
                Log.d("debug", "number to call : " + number);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_suggest, menu);
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


    @Override
    protected void onStop() {
        Log.i(debugTag, "onStop (SUGGEST)");
//        PSTUtils.savePST(PhoneCallHandlerTrans.classifier,false);
        SharedPreferences.Editor prefsEditor = MainActivity.mPrefs.edit();
        String json = gson.toJson(MainActivity.commonData);
        Log.i(debugTag, "Saving DB");
        prefsEditor.putString("db", json);
        json = gson.toJson(PhoneCallHandlerTrans.classifier);
        Log.i(debugTag, "Saving classifier");
        prefsEditor.putString("classifier",json);
        prefsEditor.commit();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        Log.i(debugTag , "onDestroy (SUGGEST)");
        SharedPreferences.Editor prefsEditor = MainActivity.mPrefs.edit();
        String json = gson.toJson(MainActivity.commonData);
        Log.i(debugTag, "Saving DB");
        prefsEditor.putString("db", json);
        json = gson.toJson(PhoneCallHandlerTrans.classifier);
        Log.i(debugTag, "Saving classifier");
        prefsEditor.putString("classifier",json);
        prefsEditor.commit();
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        Log.i(debugTag, "onPause (SUGGEST)");
//        PSTUtils.savePST(PhoneCallHandlerTrans.classifier,false);
        SharedPreferences.Editor prefsEditor = MainActivity.mPrefs.edit();
        String json = gson.toJson(MainActivity.commonData);
        Log.i(debugTag, "Saving DB");
        prefsEditor.putString("db", json);
        json = gson.toJson(PhoneCallHandlerTrans.classifier);
        Log.i(debugTag, "Saving classifier");
        prefsEditor.putString("classifier",json);
        prefsEditor.commit();
        super.onPause();
    }
}
