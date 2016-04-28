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
import android.widget.TableLayout;
import android.widget.TableRow;

import com.app.td.actionableconversation.AppUtils.PSTUtils;
import com.google.gson.Gson;

public class SuggestActivity extends AppCompatActivity {

    public final String debugTag = "debug";
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(debugTag, "on create (SUGGEST)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        Intent intent = getIntent();
        String contact = intent.getStringExtra(PhoneCallHandlerTrans.MENTIONED_NAMES_EXTRA);
        gson = new Gson();
        // create the layout params that will be used to define how your
        // button will be displayed
        if(contact != "other"){
            makeButtons(contact);
        }

    }

    private void makeButtons (final String suggested) {
        Log.d("debug", "in make buttons");
        TableLayout myTable = (TableLayout) findViewById(R.id.tableForButtons);


            TableRow tableRow = new TableRow(this);
            myTable.addView(tableRow);
            Button myButton = new Button(this);
            myButton.setText(suggested);
            myButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(debugTag , "clicked");
                    // call number of contact name
                    Button b = (Button)v;
                    String number = b.getText().toString();
                    if(!number.matches("\\+[0-9]+") && !number.matches("[0-9]+")){
                        return;
                    }
                    Log.d("debug", "call : " + number);
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                    Log.d("debug", "number to call : " + number);

                }
            });
            tableRow.addView(myButton);

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
