package com.app.td.actionableconversation;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.app.td.actionableconversation.Algorithm.PSTMultiClassClassifier;
import com.app.td.actionableconversation.AppUtils.PSTUtils;
import com.app.td.actionableconversation.AppUtils.TimeUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.app.td.actionableconversation.AppUtils.SerializationUtil.serialize;

//google shit

/**
 * This Class will handle all Tele-Phone actions.
 */
public class PhoneCallHandlerTrans extends PhonecallReceiver{

    static boolean isInstalled = false;
    String debugTag = "debug";
    static boolean running = false;
    static PredictionCycle speech;
    static PSTMultiClassClassifier classifer;
    static Context myContext;
    static String callAddress;
    static String myPhoneContacts;
    // private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    HashMap<String, Double> newCall;
    double signedResult;

    public static final  String MENTIONED_NAMES_EXTRA = "Relevant names";
    public static final String PHONE_NUMBERS_EXTRA = "Relevant numbers";
    private Location mLastLocation;
    private boolean isRelevant;
    static double latitude;
    static double longitude;
    // Google client to interact with Google API
    static GoogleApiClient mGoogleApiClient;

    public static final String BROADCAST = "PACKAGE_NAME.android.action.broadcast";
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        recordMic();
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        recordMic();
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }



    public static void setLocation(String address){
        callAddress = address;
    }


    private void recordMic() {
        Log.i(debugTag, "record mic");
        mGoogleApiClient.connect();
        if(speech == null){
            speech = new PredictionCycle();
            speech.initialize();
        }
        speech.run();

    }

    private void stopRecordMic() {
        if (speech != null) speech.stop();
    }

    public class PredictionCycle {
        RecognitionListener listener;
        SpeechRecognizer recognizer;
        Intent intent;
        boolean shouldStop;
        String theText;
        public void feedback(char tag){
            if(classifer != null){
                classifer.feedback(tag);
                Log.i(debugTag, "feedback successful in PredictionCycle.feedback()");
            }else{
                Log.i(debugTag,"classifer null in PredictionCycle.feedback()");
            }

        }


        public String findContact(String phoneNumber) {
            Log.i(debugTag, "finding contact");
            ContentResolver cr = myContext.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cur = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            // add checks to the results of the cursor
            if (cur == null) {
                Log.i(debugTag, "cursor null");
                return null;
            }
            String contactName = null;
            if (cur.moveToFirst()) {
                contactName = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.i(debugTag, "name is : " + contactName);
            }
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            return contactName;
        }


        /**
         * Initialize PredictionCycle
         */
        protected void initialize() {
            Log.d(debugTag, "initialize PredictionCycle");
            intent = createRecognitionIntent();
            recognizer = SpeechRecognizer.createSpeechRecognizer(myContext);
            Log.d(debugTag, "after recognizer init");
        }

        public void run() {
            // mute sounds
            muteSounds();
            // The Listeners
            Log.d(debugTag, "get new listener");
            listener = createRecognitionListener();
            // Set Listeners to SpeechRecognizer
            Log.d(debugTag, "before bind - recognizer and listener");
            recognizer.setRecognitionListener(listener);
            //run first recognizer
            runSpeech(recognizer, intent);
            Log.d(debugTag, "after run speech");
        }

        public void stop() {
            shouldStop = true;
            Log.d(debugTag, "stop call");
            recognizer.stopListening();
            Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
            return;
        }

        private void runSpeech(SpeechRecognizer n_recognizer, Intent n_intent) {

            n_recognizer.startListening(n_intent);
        }

        public RecognitionListener createRecognitionListener() {

            return new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {
                    Log.d(debugTag, "onResults");
                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults != null) {
                        theText += voiceResults.get(0) + "\n";
                    }
                    Log.d(debugTag, "Before should stop");
                    // if should stop and not continue the listener cycles
                    if (!shouldStop) {
                        Log.d(debugTag, "called reRunListener");
                        reRunListener(0);
                        Log.d(debugTag, "returned reRunListener");
                    }else{
                        Log.d(debugTag, "File Saved");
                        recognizer.cancel();
                        recognizer.destroy();
                        Log.d(debugTag, "destroyed recognizer");
                        unMuteSounds();
                        // start PST
                        predict();
                        Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(debugTag, "Ready for speech");
                }

                private void reRunListener(int error) {

                    recognizer.cancel();
                    Log.i(debugTag,"Got error : " + error + " on reRunListener");
                    if(recognizer != null){
                        recognizer.destroy();
                    }
                    recognizer = SpeechRecognizer.createSpeechRecognizer(myContext);
                    recognizer.setRecognitionListener(listener);

                    runSpeech(recognizer, intent);
                }

                @Override
                protected void finalize() throws Throwable {
                    super.finalize();
                }

                @Override
                public void onError(int error) {
                    Log.d(debugTag, "onError : " + error);
                    if(!shouldStop){
                        Log.d(debugTag, "continue");
                        reRunListener(error);
                    }else{
                        Log.d(debugTag, "Stopping in error");
                        recognizer.cancel();
                        recognizer.destroy();
                        Log.d(debugTag, "destroyed recognizer");
                        unMuteSounds();
                        // START PST
                        predict();
                        Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBeginningOfSpeech() {

                    Log.d(debugTag, "onBeginingOfSpeech");
                }

                private void predict(){
                    int[] time = getTime();
                    char prediction = PSTUtils.predictInputOnClassifier(classifer,theText,getLocation(),time);
                    onPredictionAction(prediction);
                }

                private void onPredictionAction(char prediction){

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d(debugTag, "onBufferRecieved");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(debugTag, "onEndOfSpeech");

                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d(debugTag, "onEevent");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // TODO Auto-generated method stub
                    Log.d(debugTag, "onPartialResults");
                }
                @Override
                public void onRmsChanged(float rmsdB) {
                    // TODO Auto-generated method stub
                }
            };
        }

        private Intent createRecognitionIntent() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "com.app.td.actionableconversation");
            //intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            //intent.putExtra("android.speech.extra.DICTATION_MODE", true);
            return intent;
        }

        private void muteSounds() {
            Log.d(debugTag, "muteSound");
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            aManager.setStreamMute(AudioManager.STREAM_RING, true);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }

        private void unMuteSounds() {
            Log.d(debugTag, "unMuteSounds");
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            aManager.setStreamMute(AudioManager.STREAM_RING, false);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        /**
         * Method to display the location on UI
         * */
        private com.app.td.actionableconversation.DB.Location getLocation() {
            Log.i(debugTag, "get Location");
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(PhoneCallHandlerTrans.mGoogleApiClient);
            com.app.td.actionableconversation.DB.Location location = null;
            if (mLastLocation != null) {

                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                location = new com.app.td.actionableconversation.DB.Location(latitude,longitude);
                Log.i(debugTag, "Location : latitude = " + latitude);
                Log.i(debugTag, "Location : longitude = " + longitude);


            } else {
                Log.i(debugTag, "last location is null");
            }

            return location;
        }

        private int[] getTime() {
            long time = System.currentTimeMillis();
            int clock = TimeUtil.getClockFromLong(time);
            int day = TimeUtil.getDayFromLong(time);
            int[] timeArray = new int[2];
            timeArray[0] = clock;
            timeArray[1] = day;

            return timeArray;
        }
    }
};