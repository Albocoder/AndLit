package com.andlit.session;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import com.andlit.R;
import com.andlit.UI.IntermediateCameraActivity;
import com.andlit.cloudInterface.Vision.VisionEndpoint;
import com.andlit.cloudInterface.Vision.models.Description;
import com.andlit.cloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
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

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class Session {
    // FLAG CONSTANTS
    private static final int NOT_RUN_YET = -1;
    private static final int SUCCESFUL = 0;
    private static final int ERROR = 1;



    // constants
    private static final String TAG = "VoiceSession";
    private static final String PICTURE_UNAVAILABLE = "Session has no picture available!";

    private static final String PICTURE_SUCCESS = "Picture was taken successfully!";
    private static final String ANALYSIS_SUCCESS = "Face detection terminated successfully!";
    private static final String RECOGNITION_SUCCESS = "Face recognition terminated successfully!";
    private static final String IMG_DESC_START = "Getting image description from the server!";

    // session control variables
    private boolean isVoiceSession;
    private boolean saveOnExit;

    // initialized when session starts
    private VoiceGenerator speaker;
    private AppDatabase db;
    private FaceRecognizerSingleton frs;
    private VoiceToCommandWrapper vc;
    private Context c;
    //todo: private Camerathingy cam

    // calculated in session
    private VisionEndpoint vis;     // has the image file inside
    private FaceOperator fop;
    private Description d;
    private List<Text> t;
    private int descriptionResult;
    private int textResult;



    // ************************* SESSION SETUP FUNCTIONS ************************ //
    public Session(Context c) {
        // setting up control variables
        this.c = c;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        isVoiceSession = sharedPref.getBoolean(SettingsDefinedKeys.AUDIO_FEEDBACK,false);
        // start
        restartSession();
    }
    public Session(Context c,boolean v) {
        // setting up control variables
        this.c = c;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        isVoiceSession = v;
        // start
        restartSession();
    }

    public void destroySession(){ restartSession(); }




    // ***************************** ACTION FUNCTIONS *************************** //
    public boolean getPicture() {
        //todo do this!
        audioFeedback(PICTURE_SUCCESS);
        return true;
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
            fop = new FaceOperator(c, toAnalyze);
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
        if(descriptionResult == SUCCESFUL)
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
        speaker = new VoiceGenerator(c);
        db = AppDatabase.getDatabase(c);
        frs = new FaceRecognizerSingleton(c);
        vc = new VoiceToCommandWrapper(c);
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
