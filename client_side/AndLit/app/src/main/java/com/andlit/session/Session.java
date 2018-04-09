package com.andlit.session;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.andlit.RequestCodes;
import com.andlit.camera.BitmapWrapper;
import com.andlit.camera.CameraActivity;
import com.andlit.cloudInterface.Vision.VisionEndpoint;
import com.andlit.cloudInterface.Vision.models.Description;
import com.andlit.cloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.face.Face;
import com.andlit.face.FaceOperator;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.face.RecognizedFace;
import com.andlit.settings.SettingsDefinedKeys;
import com.andlit.ui.IntermediateCameraActivity;
import com.andlit.voice.VoiceGenerator;
import com.andlit.voice.VoiceToCommandWrapper;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.andlit.utils.StorageHelper.writePNGToInternalMemory;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public abstract class Session extends Activity {
    // FLAG CONSTANTS
    private static final int NOT_RUN_YET = -2;
    private static final int RUNNING = -1;
    private static final int RESULT_SUCCESFUL = 0;
    private static final int RESULT_ERROR = 1;

    // logging constants
    private static String TAG = "Session";
    // FAILURE
    private static final String PICTURE_UNAVAILABLE = "Session has no picture available!";
    private static final String PICTURE_EXISTS = "Picture already exists. Please restart session!";
    private static final String NETWORK_ERROR = "No internet access, server is down.";
    private static final String PICTURE_LARGE = "Picture too large";
    private static final String TEXT_ERROR_RERUNNING = "Last text recognition job failed. Rerunning.";
    private static final String DESC_ERROR_RERUNNING = "Last image description job failed. Rerunning.";
    // SUCCESS
    private static final String PICTURE_SUCCESS = "Picture was taken successfully!";
    private static final String ANALYSIS_SUCCESS = "Face detection terminated successfully!";
    private static final String RECOGNITION_SUCCESS = "Face recognition terminated successfully!";
    private static final String DESC_SUCCESS = "Description obtained successfully";
    private static final String TEXT_SUCCESS = "Text recognition results obtained successfully";
    // INFO
    private static final String IMG_DESC_START = "Getting image description result from the server!";
    private static final String DESC_STILL_RUNNING = "Image description is still running. Please wait.";
    private static final String TEXT_START = "Getting text recognition result from the server!";
    private static final String TEXT_STILL_RUNNING = "Text recognition is still running. Please wait.";
    private static final String NO_TEXTS_FOUND = "No text blocks found.";
    private static final String ONE_TEXT_FOUND = "One text block found.";
    private static final String TEXT_FOUND_PROMPT = " text blocks found.";

    // session control variables
    protected boolean isVoiceSession;
    private boolean saveOnExit;
    protected String randPictureName;

    // initialized when session starts
    private VoiceGenerator speaker;
    private AppDatabase db;
    private FaceRecognizerSingleton frs;
    private VoiceToCommandWrapper vc;

    // calculated in session
    private VisionEndpoint vis;     // has the image file inside
    private FaceOperator fop;
    private Description d;
    private List<Text> t;
    private int descriptionResult;
    private int textResult;



    // ************************* SESSION ACTIVITY FUNCTIONS ************************ //
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(null);
        if(getLayoutId() != 0)
            setContentView(getLayoutId());
        // setting up control variables
        int TAGSERIALNO = new Random().nextInt();
        if(TAGSERIALNO < 0)
            TAGSERIALNO = -TAGSERIALNO;
        TAG += ""+TAGSERIALNO;
        randPictureName = "capture_"+TAGSERIALNO+".png";
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        // start
        restartSession();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // running arbitrary code from the child
        // proceed with own code
        if(requestCode == RequestCodes.CAMERA_ACTIVITY_RC) {
            if( resultCode == Activity.RESULT_OK){
                try {
                    if(randPictureName == null)
                        randPictureName = ""+new Random().nextLong()+".png";

                    String path = writePNGToInternalMemory(this,BitmapWrapper.bitmap,"tmp",randPictureName);
                    if(vis != null)
                        vis.destroy();
                    vis = new VisionEndpoint(this,new File(path));
                    audioFeedback(PICTURE_SUCCESS);
                } catch (IOException ignored) {}
            }
        }
        else if(requestCode == RequestCodes.AUDIO_FEEDBACK_RC){
            // todo add google voice recognition callback
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartSession();
        //onRestartChild();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        destroySession();
        onDestroyChild();
    }

    // ************************* SESSION SANITY FUNCTIONS ************************ //
    public final void destroySession(){ restartSession(); }

    public final void restartSession() {
        speaker = new VoiceGenerator(this);
        db = AppDatabase.getDatabase(this);
        frs = new FaceRecognizerSingleton(this);
        vc = new VoiceToCommandWrapper(this);
        if(vis != null)
            vis.destroy();
        if(fop != null) {
            if(saveOnExit)
                fop.storeAllFaces();
            fop.destroy();
        }
        descriptionResult = NOT_RUN_YET;
        textResult = NOT_RUN_YET;
        vis = null; fop = null; d = null; t = null;
    }

    // *************************** OVERRIDABLE FUNCTIONS ************************ //
    protected int getLayoutId(){return 0;}

    protected void onDestroyChild(){}

    // ***************************** ACTION FUNCTIONS *************************** //

    public void takePicture() {
        if(vis != null) {
            audioFeedback(PICTURE_EXISTS);
            return;
        }
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i,RequestCodes.CAMERA_ACTIVITY_RC);
    }

    public boolean detectFaces() {
        if (vis == null) {
            audioFeedback(PICTURE_UNAVAILABLE);
            return false;
        }
        if(vis.getImgFile() == null) {
            audioFeedback(PICTURE_UNAVAILABLE);
            return false;
        }
        if(fop == null) {
            opencv_core.Mat toAnalyze = imread(vis.getImgFile().getAbsolutePath());
            fop = new FaceOperator(this, toAnalyze);
        }
        fop.getFaces();
        audioFeedback(ANALYSIS_SUCCESS);
        return true;
    }

    public boolean recognizeFaces() {
        if(!detectFaces())
            return false;
        fop.recognizeFaces();
        audioFeedback(RECOGNITION_SUCCESS);
        return true;
    }

    public RecognizedFace recognizeFace(int index) {
        if(!detectFaces())
            return null;
        Face[] f = fop.getFaces();
        try{
            return frs.recognize(f[index]);
        }catch (IndexOutOfBoundsException e){ return null; }
    }

    public boolean describePictureAsync(AsyncJobCallback cb) {
        if(descriptionResult == RESULT_SUCCESFUL) {
            audioFeedback(d.toString());
            return true;    // means result already exists
        }
        else if(descriptionResult == NOT_RUN_YET)
            new DescribeImageAsync(cb).execute();
        else if(descriptionResult == RUNNING)
            audioFeedback(DESC_STILL_RUNNING);
        else {
            audioFeedback(DESC_ERROR_RERUNNING);
            new DescribeImageAsync(cb).execute();
        }
        audioPause(500);
        return false;   // means result doesn't exist
    }

    public boolean recognizeTextAsync(AsyncJobCallback cb) {
        if(textResult == RESULT_SUCCESFUL) {
            if(t.size() == 0)
                audioFeedback(NO_TEXTS_FOUND);
            else if(t.size() == 1)
                audioFeedback(ONE_TEXT_FOUND);
            else
                audioFeedback(t.size()+TEXT_FOUND_PROMPT);
            return true;    // means result already exists
        }
        else if(textResult == NOT_RUN_YET)
            new TextRecognitionAsync(cb).execute();
        else if(textResult == RUNNING)
            audioFeedback(TEXT_STILL_RUNNING);
        else {
            audioFeedback(TEXT_ERROR_RERUNNING);
            new TextRecognitionAsync(cb).execute();
        }
        audioPause(500);
        return false;   // means result doesn't exist
    }

    public boolean describePictureAsync(){ return describePictureAsync(null); }

    public boolean recognizeTextAsync(){ return recognizeTextAsync(null); }

    // ***************************** VOICE COMMAND FUNCTIONS *************************** //


    // ID: 0 (master)
    public final void runFunction(int id){
        switch (id) {
            case(1):    functionOne();      break;
            case(2):    functionTwo();      break;
            case(3):    functionThree();    break;
            case(4):    functionFour();     break;
            case(5):    functionFive();     break;
            case(6):    functionSix();      break;
            case(7):    functionSeven();    break;
            case(8):    functionEight();    break;
            case(9):    functionNine();     break;
            case(10):   functionTen();      break;
        }
    }
    // ID: 1    ()
    public void functionOne(){

    }
    // ID: 2    ()
    public void functionTwo(){}
    // ID: 3    ()
    public void functionThree(){}
    // ID: 4    ()
    public void functionFour(){}
    // ID: 5    ()
    public void functionFive(){}
    // ID: 6    ()
    public void functionSix(){}
    // ID: 7    ()
    public void functionSeven(){}
    // ID: 8    ()
    public void functionEight(){}
    // ID: 9    ()
    public void functionNine(){}
    // ID: 10   ()
    public void functionTen(){}

    // **************************** ASYNC CLASSES ******************************* //
    @SuppressLint("StaticFieldLeak")
    private class DescribeImageAsync extends AsyncTask<Void,Void,Integer> {

        private AsyncJobCallback cb;

        public DescribeImageAsync(AsyncJobCallback cb){ this.cb = cb; }

        @Override
        protected void onPreExecute() {
            audioFeedback(IMG_DESC_START);
            descriptionResult = RUNNING;
        }

        @Override
        protected Integer doInBackground(Void... paramsObj) {
            if (vis == null)
                return 1;
            try {
                d = vis.getDescriptionOfImageBinary();
                if(d == null)
                    return 2;
            } catch (IOException e) {
                return 3;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret) {
            switch (ret) {
                case(1):
                    audioFeedback(PICTURE_UNAVAILABLE);
                    descriptionResult = RESULT_ERROR;
                    break;
                case (2):
                    audioFeedback(NETWORK_ERROR);
                    descriptionResult = RESULT_ERROR;
                    break;
                case(3):
                    audioFeedback(PICTURE_LARGE);
                    descriptionResult = RESULT_ERROR;
                    break;
                default:
                    audioFeedback(DESC_SUCCESS);
                    descriptionResult = RESULT_SUCCESFUL;
                    break;
            }
            if(cb != null)
                cb.run(ret);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class TextRecognitionAsync extends AsyncTask<Void,Void,Integer> {

        private AsyncJobCallback cb;

        public TextRecognitionAsync(AsyncJobCallback cb){ this.cb = cb; }

        @Override
        protected void onPreExecute() {
            audioFeedback(TEXT_START);
            textResult = RUNNING;
        }

        @Override
        protected Integer doInBackground(Void... paramsObj) {
            if (vis == null)
                return 1;
            try {
                t = vis.getTextFromImageBinary();
                if(t == null)
                    return 2;
            } catch (IOException e) {
                return 3;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret) {
            switch (ret) {
                case(1):
                    audioFeedback(PICTURE_UNAVAILABLE);
                    textResult = RESULT_ERROR;
                    break;
                case (2):
                    audioFeedback(NETWORK_ERROR);
                    textResult = RESULT_ERROR;
                    break;
                case(3):
                    audioFeedback(PICTURE_LARGE);
                    textResult = RESULT_ERROR;
                    break;
                default:
                    audioFeedback(TEXT_SUCCESS);
                    textResult = RESULT_SUCCESFUL;
                    break;
            }
            if(cb != null)
                cb.run(ret);
        }

    }

    // ************************* ACCESSORS FUNCTIONS **************************** //

    public FaceOperator getFop() { return fop; }

    public Description getDescription() { return d; }

    public List<Text> getText() { return t; }

    public int getDescriptionResult() { return descriptionResult; }

    public int getTextResult() { return textResult; }

    // **************************** INNER FUNCTIONS ***************************** //
    private void audioFeedback(String msg) {
        if(isVoiceSession)
            speaker.speak(msg);
    }
    private void audioPause(int durationInMs) {
        if(isVoiceSession)
            speaker.pause(durationInMs);
    }
}
