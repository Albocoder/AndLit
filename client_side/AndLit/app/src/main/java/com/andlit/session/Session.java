package com.andlit.session;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import com.andlit.RequestCodes;
import com.andlit.camera.BitmapWrapper;
import com.andlit.camera.CameraActivity;
import com.andlit.cloudInterface.vision.VisionEndpoint;
import com.andlit.cloudInterface.vision.model.Description;
import com.andlit.cloudInterface.vision.model.Text;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.andlit.utils.StorageHelper.writePNGToInternalMemory;
import static org.bytedeco.javacpp.opencv_core.ROTATE_90_CLOCKWISE;
import static org.bytedeco.javacpp.opencv_core.rotate;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public abstract class Session extends Activity {
    // FLAG CONSTANTS
    private static final int NOT_RUN_YET = -2;
    private static final int RUNNING = -1;
    private static final int RESULT_SUCCESSFUL = 0;
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
    private static final String NOT_SUPPORTED = "Action not supported.";
    // SUCCESS
    private static final String PICTURE_SUCCESS = "Picture was taken successfully!";
    private static final String PICTURE_STORED_SUCCESS = "Picture was stored and ready to query";
    private static final String ANALYSIS_SUCCESS = "Face detection terminated successfully!";
    private static final String RECOGNITION_SUCCESS = "Face recognition terminated successfully!";
    private static final String DESC_SUCCESS = "Description obtained successfully";
    private static final String TEXT_SUCCESS = "Text recognition results obtained successfully";
    // INFO
    private static final String IMG_DESC_START = "Getting image description result from the server!";
    private static final String DESC_STILL_RUNNING = "Image description is still running. Please wait.";
    private static final String TEXT_START = "Getting text recognition result from the server!";
    private static final String TEXT_STILL_RUNNING = "Text recognition is still running. Please wait.";
    private static final String TRAIN_START = "Training the classifier. This might take a while.";
    private static final String TRAIN_STILL_RUNNING = "Training is still running. Please wait.";
    private static final String TRAIN_FINISH = "Training has finished!";
    private static final String RETRAIN = "Training recently finished. Please repeat the same command to retrain.";
    private static final String RECOGNITION_START = "Started face recognition.";
    private static final String ANALYSIS_START = "Started face detection.";
    private static final String CLEARED_SESSION = "Session is now clear.";
    private static final String NO_TEXTS_FOUND = "No text blocks found.";
    private static final String ONE_TEXT_FOUND = "One text block found.";
    private static final String TEXT_FOUND_PROMPT = " text blocks found.";
    private static final String INFO_FUNCTION_NONE = "Sorry. I didn't understand that.";
    private static final String INFO_FUNCTION_1_1 = "Taking a picture. Please stay still.";
    private static final String INFO_FUNCTION_2_1 = " faces found in the image.";
    private static final String INFO_FUNCTION_2_2 = "Found one face in the image.";
    private static final String INFO_FUNCTION_2_3 = "Found no faces in the image.";
    private static final String QUERY_TXT_1 = "Text block ";
    private static final String QUERY_TXT_2 = " says.";
    private static final String QUERY_REC_1 = "Face ";
    private static final String QUERY_REC_2 = " is identified to be of ";

    // session control variables
    protected boolean isVoiceSession;
    protected boolean saveOnExit;
    protected String randPictureName;
    protected boolean debugMode;

    // initialized when session starts
    protected VoiceGenerator speaker;
    protected AppDatabase db;
    protected FaceRecognizerSingleton frs;
    protected VoiceToCommandWrapper vc;

    // calculated in session
    private VisionEndpoint vis;     // has the image file inside
    private FaceOperator fop;
    private Description d;
    private List<Text> t;
    private int descriptionResult;
    private int textResult;
    private int trainResult;

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
        debugMode = true;
        randPictureName = "capture_"+TAGSERIALNO+".png";
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        // start
        restartSession();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RequestCodes.CAMERA_ACTIVITY_RC) {
            if( resultCode == Activity.RESULT_OK){
                try {
                    if(randPictureName == null) {
                        randPictureName = ""+new Random().nextLong()+".png";
                    }
                    audioFeedback(PICTURE_SUCCESS);

                    String path = writePNGToInternalMemory(this,BitmapWrapper.bitmap,"tmp",randPictureName);
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        opencv_core.Mat res = imread(path);
                        opencv_core.Mat newMat = new opencv_core.Mat();
                        rotate(res,newMat,ROTATE_90_CLOCKWISE);
                        imwrite(path,newMat);
                        newMat.release(); res.release();
                    }

                    if(vis != null)
                        vis.destroy();
                    vis = new VisionEndpoint(this,new File(path));
                    audioFeedback(PICTURE_STORED_SUCCESS);
                } catch (IOException ignored) {}
            }
        }
        else if(requestCode == RequestCodes.SPEECH_INPUT_RC){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(this,result.get(0),Toast.LENGTH_SHORT).show();
            runFunction(vc.decide(result.get(0)));
        }
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
        trainResult = NOT_RUN_YET;
        vis = null; fop = null; d = null; t = null;
    }

    // *************************** OVERRIDABLE FUNCTIONS ************************ //
    protected int getLayoutId(){return 0;}

    protected void onDestroyChild(){}

    // ***************************** ACTION FUNCTIONS *************************** //

    public final void takePicture() {
        if(vis != null) {
            audioFeedback(PICTURE_EXISTS);
            return;
        }
        audioFeedback(INFO_FUNCTION_1_1);
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i,RequestCodes.CAMERA_ACTIVITY_RC);
    }

    public final boolean detectFaces() {
        if (vis == null) {
            audioFeedback(PICTURE_UNAVAILABLE);
            return false;
        }
        if(vis.getImgFile() == null) {
            audioFeedback(PICTURE_UNAVAILABLE);
            return false;
        }
        if(fop == null) {
            audioFeedback(ANALYSIS_START);
            opencv_core.Mat toAnalyze = imread(vis.getImgFile().getAbsolutePath());
            fop = new FaceOperator(this, toAnalyze);
        }
        fop.getFaces();
        audioFeedback(ANALYSIS_SUCCESS);
        return true;
    }

    public final boolean recognizeFaces() {
        if(!detectFaces())
            return false;
        audioFeedback(RECOGNITION_START);
        fop.recognizeFaces();
        audioFeedback(RECOGNITION_SUCCESS);
        return true;
    }

    public final RecognizedFace recognizeFace(int index) {
        if(!detectFaces())
            return null;
        Face[] f = fop.getFaces();
        try{
            return frs.recognize(f[index]);
        }catch (IndexOutOfBoundsException e){ return null; }
    }

    public boolean describePictureAsync(AsyncJobCallback cb) {
        if(descriptionResult == RESULT_SUCCESSFUL) {
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
        if(textResult == RESULT_SUCCESSFUL) {
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

    public boolean trainClassifierAsync(AsyncJobCallback cb) {
        if(trainResult == RESULT_SUCCESSFUL) {
            audioFeedback(RETRAIN);
            trainResult = NOT_RUN_YET;
            return true;    // means training has recently finished
        }
        else if(trainResult == NOT_RUN_YET)
            new TrainerAsync(cb).execute();
        else if(trainResult == RUNNING)
            audioFeedback(TRAIN_STILL_RUNNING);
        audioPause(500);
        return false;       // means training hasn't finished yet
    }

    public boolean describePictureAsync(){ return describePictureAsync(null); }

    public boolean recognizeTextAsync(){ return recognizeTextAsync(null); }

    public boolean trainClassifierAsync() { return trainClassifierAsync(null); }

    // ***************************** VOICE COMMAND FUNCTIONS *************************** //

    public void startVoiceCapture() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, RequestCodes.SPEECH_INPUT_RC);
        }
        catch (ActivityNotFoundException a) {
            audioFeedback(NOT_SUPPORTED);
            Toast.makeText(getApplicationContext(),NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
        }
    }

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
            case(11):   functionEleven();   break;
            default:    functionNone();     break;
        }
    }
    // ID: -1
    public void functionNone(){ audioFeedback(INFO_FUNCTION_NONE); }
    // ID: 1    (take a picture)
    public void functionOne() { takePicture(); }
    // ID: 2    (face detection)
    public void functionTwo() {
        if(!detectFaces())
            return;
        Face[] faces = fop.getFaces();
        if (faces.length > 1)
            audioFeedback(faces.length + INFO_FUNCTION_2_1);
        else if (faces.length == 1)
            audioFeedback(INFO_FUNCTION_2_2);
        else
            audioFeedback(INFO_FUNCTION_2_3);
    }
    // ID: 3    (scene description)
    public void functionThree() {
        describePictureAsync(new AsyncJobCallback((Object) null) {
            @Override
            public Object run(Object result) {
                if((Integer)result == 0){
                    audioFeedback(d.toString());
                }
                return null;
            }
        }
        );
    }
    // ID: 4    (text recognition)
    public void functionFour() {
        recognizeTextAsync(new AsyncJobCallback() {
            @Override
            public Object run(Object result) {
                if((Integer)result == 0) {
                    recognizeTextAsync();
                }
                return null;
            }
        });
    }
    // ID: 5    (train classifier)
    public void functionFive(){ trainClassifierAsync(); }
    // ID: 6    (synchronization)
    public void functionSix(){ /* todo: write cloud interface for this */ }
    // ID: 7    (face recognition)
    public void functionSeven(){ recognizeFaces(); }
    // ID: 8    (restart session)
    public void functionEight(){
        restartSession();
        audioFeedback(CLEARED_SESSION);
    }
    // ID: 9    (query how many faces)
    public void functionNine(){ functionTwo(); }
    // ID: 10   (query read all text blocks)
    public void functionTen(){
        if(!recognizeTextAsync())
            return;
        int i = 1;
        for(Text tmp: t){
            audioFeedback(QUERY_TXT_1+i+QUERY_TXT_2);
            audioPause(200);
            audioFeedback(tmp.getText());
            i++;
        }
    }
    // ID: 11   (query face identities)
    public void functionEleven(){
        if(!recognizeTextAsync())
            return;
        RecognizedFace[] faces = fop.recognizeFaces();
        int i = 1;
        for (RecognizedFace f : faces) {
            f.setBestMatch(this);
            audioFeedback(QUERY_REC_1+i+ QUERY_REC_2+f.getBestMatch().name
                    +" "+f.getBestMatch().sname);
            audioPause(200);
        }
    }

    // **************************** ASYNC CLASSES ******************************* //
    @SuppressLint("StaticFieldLeak")
    public class DescribeImageAsync extends AsyncTask<Void,Void,Integer> {

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
                    descriptionResult = RESULT_SUCCESSFUL;
                    break;
            }
            if(cb != null)
                cb.run(ret);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class TextRecognitionAsync extends AsyncTask<Void,Void,Integer> {

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
                    textResult = RESULT_SUCCESSFUL;
                    break;
            }
            if(cb != null)
                cb.run(ret);
        }

    }

    @SuppressLint("StaticFieldLeak")
    public class TrainerAsync extends AsyncTask<Void,Void,Integer> {

        private AsyncJobCallback cb;

        public TrainerAsync(AsyncJobCallback cb){ this.cb = cb; }

        @Override
        protected void onPreExecute() {
            audioFeedback(TRAIN_START);
            trainResult = RUNNING;
        }

        @Override
        protected Integer doInBackground(Void... paramsObj) {
            new FaceRecognizerSingleton(Session.this).trainModel();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret) {
            audioFeedback(TRAIN_FINISH);
            trainResult = RESULT_SUCCESSFUL;
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
    public final void audioFeedback(String msg) {
        if(isVoiceSession)
            speaker.speak(msg);
        if(debugMode)
            Log.d(TAG,msg);
    }
    public final void audioPause(int durationInMs) {
        if(isVoiceSession)
            speaker.pause(durationInMs);
    }
}