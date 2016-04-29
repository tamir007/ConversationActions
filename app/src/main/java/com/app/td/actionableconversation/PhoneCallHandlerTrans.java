package com.app.td.actionableconversation;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import com.app.td.actionableconversation.Algorithm.PSTMultiClassClassifier;
import com.app.td.actionableconversation.AppUtils.CallLogInfo;
import com.app.td.actionableconversation.AppUtils.PSTUtils;
import com.app.td.actionableconversation.AppUtils.TimeUtil;
import com.app.td.actionableconversation.DB.ourLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PhoneCallHandlerTrans extends PhonecallReceiver{

    String debugTag = "debug";
    static PredictionCycle speech;
    public static PSTMultiClassClassifier classifier;
    public static Context myContext,currentContext;
    static String callAddress;
    public static final  String MENTIONED_NAMES_EXTRA = "Relevant names";
    public static final  String LONGITUDE = "longitude";
    public static final  String LATITUDE = "latitude";
    public static final  String CONTACT_NAME = "contact_name";

    private Location mLastLocation;
    static GoogleApiClient mGoogleApiClient;

    public static final String BROADCAST = "PACKAGE_NAME.android.action.broadcast";
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        if(myContext == null){
            return;
        }
        feedbackAndSave(number);
        recordMic();
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        if(myContext == null){
            return;
        }
       feedbackAndSave(number);
        recordMic();
    }


    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        if(myContext == null){
            return;
        }
        stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        if(myContext == null){
            return;
        }
        stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }


    private void feedbackAndSave(String number){
        Log.i(debugTag, " feedbackAndSave");
        if(classifier != null){
            HashMap<String,Character> map = MainActivity.getContactToCharMap();
            Log.i(debugTag, " number is : " + number);
            Character c = map.get(number);
            if(c == null){
                Log.i(debugTag, "Character is null");
                c = new Character('6');
            }
            Log.i(debugTag, " after get character in feedbackandSave " + c.charValue());
            classifier.feedback(c.charValue());
        }else{
            Log.i(debugTag, " classifier is null");
        }
    }

    private void recordMic() {
        Log.i(debugTag, "record mic");
        mGoogleApiClient.connect();
        speech = new PredictionCycle();
        speech.initialize();
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
        String theText = "";
        public void feedback(char tag){
            if(classifier != null){
                classifier.feedback(tag);
                Log.i(debugTag, "feedback successful in PredictionCycle.feedback()");
            }else{
                Log.i(debugTag, "classifier null in PredictionCycle.feedback()");
            }

        }

        /**
         * Initialize PredictionCycle
         */
        protected void initialize() {
            Log.d(debugTag, "initialize PredictionCycle");
            intent = createRecognitionIntent();
            Log.d(debugTag, "after createRecognitionIntent");
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
                    char prediction = PSTUtils.predictInputOnClassifier(theText,getLocation(),time);
                    onPredictionAction(prediction);
                }

                private void onPredictionAction(char prediction){
                    // does nothing for now
                    Log.i(debugTag, "onPredictionAction");
                    HashMap<Character,String> map = MainActivity.commonData.getCToS();
                    Log.i(debugTag, "contact predicted : " + prediction);
                    if(prediction == '?'){
                        prediction = '6';
                        Log.i(debugTag, "contact predicted change to : " + prediction);
                    }
                    // Get contact Number
                    String number = map.get(new Character(prediction));
                    Intent intent = new Intent(myContext.getApplicationContext() , SuggestActivity.class);
                    intent.putExtra(MENTIONED_NAMES_EXTRA, number);
                    Log.i(debugTag, "Send to suggest activity : " + mLastLocation.getLongitude() +
                            " " + mLastLocation.getLatitude() + " " + number);
                    intent.putExtra(LONGITUDE, mLastLocation.getLongitude());
                    intent.putExtra(LATITUDE, mLastLocation.getLatitude());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.i(debugTag, "Getting contact name of number : " + number);
                    String contactName = CallLogInfo.getNameByNumber(myContext,number);
                    intent.putExtra(CONTACT_NAME,contactName);
                    Log.i(debugTag, "Starting Activity");
                    myContext.getApplicationContext().startActivity(intent);
                    Log.i(debugTag, "Activity started");
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
            Log.i(debugTag,"createRecognitionIntent");
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            Log.i(debugTag,"RecognizerIntent ACTION_RECOGNIZE_SPEECH");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            Log.i(debugTag, "putExtra LANGUAGE_MODEL_FREE_FORM");
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "com.app.td.actionableconversation");
            Log.i(debugTag, "putExtra EXTRA_CALLING_PACKAGE");
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
        private ourLocation getLocation() {
            Log.i(debugTag, "get ourLocation");
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(PhoneCallHandlerTrans.mGoogleApiClient);
            ourLocation ourLocation = null;
            if (mLastLocation != null) {

                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                ourLocation = new ourLocation(latitude,longitude);
                Log.i(debugTag, "ourLocation : latitude = " + latitude);
                Log.i(debugTag, "ourLocation : longitude = " + longitude);


            } else {
                Log.i(debugTag, "last ourLocation is null");
            }

            return ourLocation;
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