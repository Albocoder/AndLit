package com.andlit.session;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private static final int NOT_RUN_YET = -1;
    private static final int RESULT_SUCCESFUL = 0;
    private static final int RESULT_ERROR = 1;

    // logging constants
    private static String TAG = "Session";
    // FAILURE
    private static final String PICTURE_UNAVAILABLE = "Session has no picture available!";
    private static final String PICTURE_EXISTS = "Picture already exists. Please restart session!";
    // SUCCESS
    private static final String PICTURE_SUCCESS = "Picture was taken successfully!";
    private static final String ANALYSIS_SUCCESS = "Face detection terminated successfully!";
    private static final String RECOGNITION_SUCCESS = "Face recognition terminated successfully!";
    // INFO
    private static final String IMG_DESC_START = "Getting image description from the server!";

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
    public void destroySession(){
        restartSession();
        speaker.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getLayoutId() != 0)
            setContentView(getLayoutId());
        // setting up control variables
        TAG += ""+new Random().nextInt();
        randPictureName = ""+new Random().nextLong()+".png";
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        isVoiceSession = sharedPref.getBoolean(SettingsDefinedKeys.AUDIO_FEEDBACK,false);
        // running arbitrary code from child
        onCreateChild();
        // start
        restartSession();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // running arbitrary code from the child
        onActivityResultChild(requestCode,resultCode,data);
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
                    Log.d(TAG,"Picture taken successfully!");
                    audioFeedback(PICTURE_SUCCESS);
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        restartSession();
        onRestartChild();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroySession();
        onDestroyChild();
    }


    // *************************** OVERRIDABLE FUNCTIONS ************************ //
    protected int getLayoutId(){return 0;}
    protected void onActivityResultChild(int requestCode, int resultCode, Intent data){}
    protected void onDestroyChild(){}
    protected void onCreateChild(){}
    protected void onRestartChild(){}

    // ***************************** ACTION FUNCTIONS *************************** //
    public void getPicture() {
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

    public boolean describePicture(){
        if(vis == null)
            return false;
        if(vis.getImgFile() == null) {
            return false;
        }
        if(d == null){
            try {
                d = vis.getDescriptionOfImageBinary();
            } catch (IOException e) {
                return false;
            }
        }
        audioFeedback(d.toString());
        return true;
    }

    public boolean describePictureAsync() {
        if(descriptionResult == RESULT_SUCCESFUL)
            return true;
        else if(descriptionResult == NOT_RUN_YET) {
            new DescribeImageAsync().execute();
            return true;
        }
        else
            return false;
    }



//    public void getText

    // **************************** ASYNC CLASSES ******************************* //
    @SuppressLint("StaticFieldLeak")
    private class DescribeImageAsync extends AsyncTask<Void,Void,Integer> {

        @Override
        protected void onPreExecute() {
            audioFeedback(IMG_DESC_START);
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
            descriptionResult = ret;
        }
    }


    // ************************* ACCESSORS FUNCTIONS **************************** //

    public FaceOperator getFop() { return fop; }

    public Description getDescription() { return d; }

    public List<Text> getText() { return t; }

    public int getDescriptionResult() { return descriptionResult; }

    public int getTextResult() { return textResult; }


    // **************************** INNER FUNCTIONS ***************************** //
    private void restartSession() {
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
        descriptionResult = -1;
        vis = null; fop = null; d = null; t = null;
    }
    private void audioFeedback(String msg) {
        if(isVoiceSession)
            speaker.speak(msg);
    }
}
