package com.andlit.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andlit.R;
import com.andlit.ui.helperUI.ImgGrabber;
import com.andlit.cloudInterface.vision.VisionEndpoint;
import com.andlit.cloudInterface.vision.model.Description;
import com.andlit.cloudInterface.vision.model.Text;
import com.andlit.settings.SettingsDefinedKeys;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.*;
import com.andlit.face.*;
import com.andlit.ui.helperUI.listRelated.PersonDataAdapter;
import com.andlit.ui.helperUI.listRelated.PotentialPeopleAdapter;
import com.andlit.ui.helperUI.listRelated.TwoStringDataHolder;
import com.andlit.utils.StorageHelper;
import com.andlit.voice.VoiceGenerator;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

// TODO: this will be reworked for better modularity and using session framework
public class IntermediateCameraActivity extends Activity {

    // constants
    private static final String TAG = "IntermediateCamActivity";
    public static final String ARGUMENT_KEY = "filename";
    public static final int REQUEST_IMG_ANALYSIS = 1336;

    // internal fields
    private File imageLocation;
    private FaceOperator fop;
    private AppDatabase db;
    private FaceRecognizerSingleton frs;
    private List<KnownPPL> allKnownPpl;
    private VisionEndpoint vis;
    private VoiceGenerator speaker;

    // external fields
    private Description d;
    private List<Text> t;
    private Boolean saveOnExit,audioFeedback;

    // view fields
    private ImageView analyzed;
    private int SCREEN_HEIGHT,SCREEN_WIDTH;

    // ********************* ACTIVITY FUNCTIONS ********************* //
    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_processed);
        d = null;
        t = null;
        vis = null;
        // instantiating voice generator
        speaker = new VoiceGenerator(this.getApplicationContext());

        // instantiating database connection
        db = AppDatabase.getDatabase(this);

        // initializing more variables
        fop = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_HEIGHT = displayMetrics.heightPixels;
        SCREEN_WIDTH = displayMetrics.widthPixels;
        allKnownPpl = db.knownPplDao().getAllRecords();

        new InstantiateRecognizer().execute();

        // setting up settings to operate on
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        audioFeedback = sharedPref.getBoolean(SettingsDefinedKeys.AUDIO_FEEDBACK, false);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT, true);

        // setting up listeners
        Button takeImage = findViewById(R.id.BtnCpt);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraActivity();
            }
        });

        // imageview is the background
        analyzed = findViewById(R.id.AnalyzedImg);
        analyzed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(fop == null)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Face[] faces = fop.getFaces();
                        for(int i = 0; i < faces.length;i++)
                            if(faces[i].getBoundingBoxWithRatio().contains(new
                                    opencv_core.Point(((int)event.getX()),((int)event.getY()))))
                                showPopUpForFace(fop.recognizeFace(i));
                        if(t == null)
                            break;
                        for(Text t1:t) {
                            if(t1.getRatioedLoc().contains(new
                                    opencv_core.Point(((int)event.getX()),((int)event.getY()))))
                                showTextOfClickedArea(t1);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        // description button setup
        Button describeImg = findViewById(R.id.imgDescription);
        describeImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (d == null) {
                    new DescribeImageAsync().execute();
                    return;
                }
                // show popup for description
                showImageDescription();
            }
        });

        // text recognition button setup
        Button textRecognize = findViewById(R.id.textRecognition);
        textRecognize.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (t == null)
                    new RecognizeTextAsync().execute();
            }
        });

        // setting up file that holds the image from camera
        if(this.getIntent().hasExtra("filepath")) {
            String filelocation = this.getIntent().getStringExtra("filepath");
            imageLocation = new File(filelocation);
            if(vis != null)
                vis.destroy();
            if(fop != null) {
                if(saveOnExit)
                    fop.storeUnlabeledFaces();
                fop.destroy();
            }
            new ProcessInputAsync().execute(imageLocation,analyzed,this.getApplicationContext());
        }
        else
            setupImageHoldingFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMG_ANALYSIS) {

            if (imageLocation.length() == 0)
                Toast.makeText(this, "Error in taking the image!", Toast.LENGTH_SHORT).show();
            else {
                if(vis != null)
                    vis.destroy();
                if(fop != null) {
                    if(saveOnExit)
                        fop.storeUnlabeledFaces();
                    fop.destroy();
                }
                new ProcessInputAsync().execute(imageLocation,analyzed,this.getApplicationContext());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(fop != null) {
            if(saveOnExit)
                fop.storeUnlabeledFaces();
            fop.destroy();
        }
        if(imageLocation!= null)
            imageLocation.delete();
        if(speaker != null)
            speaker.destroy();
        exitActivity(0);
    }

    // helper function to cleanly exit the activity
    private void exitActivity(int code){
        if(imageLocation!= null)
            imageLocation.delete();
        setResult(code);
        finish();
    }

    // ********************************** UI related functions ***********************************//
    @SuppressLint("StaticFieldLeak")
    private class DescribeImageAsync extends AsyncTask<Void,Void,Integer> {

        private ProgressDialog progressDialog = new ProgressDialog(
                IntermediateCameraActivity.this, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Analyzing...");
            progressDialog.setMessage("Please wait!");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            progressDialog.dismiss();
            switch (ret){
                case (1):
                    Snackbar.make(IntermediateCameraActivity.this.analyzed
                            , "Picture not taken yet!", Snackbar.LENGTH_SHORT).show();
                    break;
                case (2):
                    Snackbar.make(IntermediateCameraActivity.this.analyzed
                            , "Image too large!", Snackbar.LENGTH_SHORT).show();
                    break;
                case(3):
                    Snackbar.make(IntermediateCameraActivity.this.analyzed
                            , "No internet, server down or image too large!", Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    showImageDescription();
                    break;
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class RecognizeTextAsync extends AsyncTask<Void,Void,Integer> {

        private ProgressDialog progressDialog = new ProgressDialog(
                IntermediateCameraActivity.this, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Analyzing...");
            progressDialog.setMessage("Please wait!");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            progressDialog.dismiss();
            switch (ret){
                case (1):
                    Snackbar.make(IntermediateCameraActivity.this.analyzed
                            , "Picture not taken yet!", Snackbar.LENGTH_SHORT).show();
                    break;
                case (2):
                    Snackbar.make(IntermediateCameraActivity.this.analyzed
                            , "No internet, server down or image too large!", Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    markFoundText();
                    break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ProcessInputAsync extends AsyncTask<Object,Void,Void> {

        private ProgressDialog progressDialog = new ProgressDialog(
                IntermediateCameraActivity.this, R.style.AppTheme_Dark_Dialog);
        private ImageView analyzed;
        private File imageLocation;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Analyzing capture...");
            progressDialog.setMessage("Please wait!");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... args) {
            imageLocation = (File) args[0];
            analyzed = (ImageView) args[1];
            Context c = (Context) args[2];
            Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
            process();
            vis = new VisionEndpoint(c,result);
            d = null;
            t = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            if(audioFeedback)
                speaker.speak("Image was processed and ready to query.");
            Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
            analyzed.setImageBitmap(result);
            imageLocation.delete();

            Face[] faces = fop.getFaces();
            if(audioFeedback)
                if (faces.length > 1)
                    speaker.speak("Found "+faces.length+" faces in the image.");
                else if (faces.length == 1)
                    speaker.speak("Found one face in the image.");
                else
                    speaker.speak("Found no faces in the image.");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InstantiateRecognizer extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            frs = new FaceRecognizerSingleton(IntermediateCameraActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void res){
            if(frs.isReady())
                Toast.makeText(IntermediateCameraActivity.this,
                        "Face recognizer loaded successfully",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(IntermediateCameraActivity.this,
                        "Error in loading face recognizer",Toast.LENGTH_SHORT).show();
        }
    }

    // ********************************** UI related functions ***********************************//
    public void startCameraActivity() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, 1337);
        }
        else {
            Intent i = new Intent(IntermediateCameraActivity.this
                    , ImgGrabber.class);
            i.putExtra(ARGUMENT_KEY, imageLocation.getAbsolutePath());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(i, REQUEST_IMG_ANALYSIS);
        }
    }


    private void showPopUpForTrueFace( RecognizedFace rf ) {
        // initializing
        if(rf.getFace().getID() <=0 )
            return;
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.user_face_truly_recognized_dialogue);
        db = AppDatabase.getDatabase(this);

        // getting the data to show
        int id = rf.getFace().getID();
        KnownPPL p = db.knownPplDao().getPersonWithID(id);
        if(p == null)
            return;
        String name = p.name+" "+p.sname;
        Bitmap photo = getPhotoForDetection(p.id);
        PersonDataAdapter pdAdapter = getKnownDataForKnownPerson(p);

        // showing data
        ImageView userPic = dialog.findViewById(R.id.userPic);
        userPic.setImageBitmap(photo);
        TextView userName = dialog.findViewById(R.id.userName);
        userName.setText(name);
        ListView userData = dialog.findViewById(R.id.userData);
        userData.setAdapter(pdAdapter);
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @SuppressLint("SetTextI18n")
    private void showPopUpForFace(final RecognizedFace rf) {
        // checking if true recognition is achieved!
        if(rf.getFace().getID() != -1) {
            showPopUpForTrueFace(rf);
            return;
        }
        // its not so lets continue with this
        int bestPrediction = rf.getLabels()[0];
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.user_face_profile_dialogue);
        String title;
        Bitmap photo;
        PersonDataAdapter pdAdapter = null;
        if(bestPrediction == -1) {
            if(audioFeedback)
                speaker.speak("Face not recognized.");
            title = "Unknown Person";
            String defaultPhoto = null;
            try {
                defaultPhoto = StorageHelper.getFilePathFromAssets
                        (this, "default_profile.jpg","tmp", "default_profile.jpg");
            } catch (IOException ignored) {}
            photo = BitmapFactory.decodeFile(defaultPhoto);
            if(defaultPhoto != null)
                new File(defaultPhoto).delete();
        }
        else {
            db = AppDatabase.getDatabase(this);
            KnownPPL p = db.knownPplDao().getPersonWithID(bestPrediction);
            title =  p.name+" "+p.sname;
            if(audioFeedback)
                speaker.speak("Face predicted to be of "+title+".");
            photo = getPhotoForDetection(p.id);
            pdAdapter = getKnownDataForKnownPerson(p);
        }
        dialog.setTitle(title);
        ImageView profilePhoto = dialog.findViewById(R.id.profilePhoto);
        profilePhoto.setImageBitmap(photo);
        TextView name = dialog.findViewById(R.id.profileName);
        name.setText(title);

        // IF NOT SET AS TRAINING INSTANCE ENCOURAGE THE USER TO DO IT
        ListView allKnown = dialog.findViewById(R.id.potentialCandidates);
        allKnown.setAdapter(new PotentialPeopleAdapter(this, allKnownPpl));
        allKnown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView personName = view.findViewById(R.id.name);
                final TextView personID = view.findViewById(R.id.description);
                String idString = personID.getText().toString();
                int id = Integer.parseInt(idString.substring(3));
                if(audioFeedback)
                    speaker.speak("You are about to correct the prediction.");
                alertSettingIDForFace(rf,personName.getText().toString(),id,dialog);
            }
        });

        ListView userData = dialog.findViewById(R.id.profileInfo);
        userData.setAdapter(pdAdapter);
        Button addNew = dialog.findViewById(R.id.addNew);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                addNewFace(rf);
            }
        });
        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void addNewFace(final RecognizedFace rf ) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_new_person_layout);
        ImageView profileImage = dialog.findViewById(R.id.profileImage);
        try {
            String tmp = StorageHelper.writeMat(this,rf.getFace().getBGRContent());
            Bitmap tmpb = BitmapFactory.decodeFile(tmp);
            profileImage.setImageBitmap(tmpb);
            new File(tmp).delete();
        } catch (IOException ignored) {}
        final TextView name = dialog.findViewById(R.id.newName);
        final TextView sname = dialog.findViewById(R.id.newSname);
        Button save = dialog.findViewById(R.id.newPersonButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString();
                String s = sname.getText().toString();
                if(n.length() <= 0 || s.length() <= 0)
                    Toast.makeText(view.getContext(),
                            "Name and Surname fields must be completed!", Toast.LENGTH_SHORT).show();
                else {
                    KnownPPL newPerson = new KnownPPL(-1,n,s,0,0,"");
                    AppDatabase db = AppDatabase.getDatabase(view.getContext());
                    long id = db.knownPplDao().insertEntry(newPerson);
                    rf.getFace().setID((int)id);
                    try {
                        training_face f = FaceOperator.saveTrainingFaceToDatabase(view.getContext(),rf);
                        if( f == null )
                            Snackbar.make(view,"Couldn't save instance to database!",Snackbar.LENGTH_SHORT);
                        else
                            allKnownPpl.add(newPerson);
                        dialog.dismiss();
                    } catch (IOException|NoSuchAlgorithmException e) {
                        Toast.makeText(view.getContext(),
                                "Couldn't save the face for the new person!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        dialog.show();
    }


    private void showImageDescription() {
        if(audioFeedback)
            speaker.speak(d.toString());
        new AlertDialog.Builder(IntermediateCameraActivity.this)
                .setTitle("Description")
                .setMessage(d.toString())
                .setIcon(android.R.drawable.ic_dialog_info)
                /*.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        rf.getFace().setID(i);
                        saveRecognizedFace(rf);
                        if(parent != null)
                            parent.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null)*/.show();
    }


    private void markFoundText() {
        if(t == null)
            return;
        Bitmap result = BitmapFactory.decodeFile(vis.getImgFile().getAbsolutePath());
        double widthRatio = (double) SCREEN_WIDTH / (double) result.getWidth();
        double heightRatio = (double) SCREEN_HEIGHT / (double) result.getHeight();
        opencv_core.Mat toAnalyze = imread(vis.getImgFile().getAbsolutePath());

        for (Text tmp:t){
            tmp.getRatioedLoc(widthRatio,heightRatio);
            opencv_core.Rect loc = tmp.getLoc();
            int x = loc.x();
            int y = loc.y();
            int w = loc.width();
            int h = loc.height();
            rectangle(toAnalyze,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    , opencv_core.Scalar.CYAN,2, LINE_8,0);
        }
        Face[] faces = fop.getFaces();
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();

            // this is used to reset the rect to the screen size
            aFacesArray.getBoundingBoxWithRatio(widthRatio, heightRatio);
            rectangle(toAnalyze,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    , opencv_core.Scalar.GREEN,2, LINE_8,0);
        }
        imwrite(vis.getImgFile().getAbsolutePath(),toAnalyze);
        result = BitmapFactory.decodeFile(vis.getImgFile().getAbsolutePath());
        analyzed.setImageBitmap(result);
        if(audioFeedback)
            if (t.size() > 1)
                speaker.speak("Found "+t.size()+" text blocks.");
            else if (t.size() == 1)
                speaker.speak("Found one text block.");
            else
                speaker.speak("Found no text block.");
    }


    private void showTextOfClickedArea(Text t1) {
        if(audioFeedback)
            speaker.speak(t1.getText());
        new AlertDialog.Builder(IntermediateCameraActivity.this)
                .setTitle("Text")
                .setMessage(t1.getText())
                .setIcon(android.R.drawable.ic_dialog_info)
                /*.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        rf.getFace().setID(i);
                        saveRecognizedFace(rf);
                        if(parent != null)
                            parent.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null)*/.show();
    }

    //*********************************** HELPER FUNCTIONS ***************************************//
    // gets a single face from the database for that person of given ID
    private Bitmap getPhotoForDetection(int id) {
        if (id == -1)
            return null;
        Bitmap photo;
        List<String> filePaths = db.trainingFaceDao().getPathsOfLabel(id);
        String filePath = null;
        if(filePaths.size() > 0)
            filePath = FaceOperator.getAbsolutePath(this,filePaths.get(0),true) ;
        else
            try {
                filePath = StorageHelper.getFilePathFromAssets
                        (this, "default_profile.jpg","tmp", "default_profile.jpg");
                photo = BitmapFactory.decodeFile(filePath);
                new File(filePath).delete();
                return photo;
            } catch (IOException ignored) {}
        photo = BitmapFactory.decodeFile(filePath);
        return photo;
    }


    // an adapter for known people list in manual labeling
    private PersonDataAdapter getKnownDataForKnownPerson(KnownPPL p) {
        if (p.id <= 0)
            return null;
        db = AppDatabase.getDatabase(this);
        PersonDataAdapter toReturn= new PersonDataAdapter(this,new ArrayList<TwoStringDataHolder>());
        toReturn.add(new TwoStringDataHolder("Full name", p.name+" "+p.sname));
        if(p.dob != null)
            toReturn.add(new TwoStringDataHolder("Date of birth",new Date(p.dob).toString()));
        if(p.age != null)
            toReturn.add(new TwoStringDataHolder("Age",""+p.age));
        List<misc_info> infos = db.miscInfoDao().getInfosForID(p.id);
        for(misc_info i: infos)
            toReturn.add(new TwoStringDataHolder(i.key,i.desc));
        return toReturn;
    }


    // saves recognized face or shows UI error
    private void saveRecognizedFace(RecognizedFace rf){
        try {
            FaceOperator.saveTrainingFaceToDatabase(this,rf);
        } catch (IOException|NoSuchAlgorithmException e) {
            Toast.makeText(this,Html.fromHtml("Couldn't save this person!<br />Error:"+e.getLocalizedMessage()),Toast.LENGTH_SHORT).show();
            Log.e(TAG,e.getLocalizedMessage());
        }
    }


    // this is called to process the image taken from camera
    public void process() {
        if(audioFeedback)
            speaker.speak("Processing image.");
        opencv_core.Mat toAnalyze = imread(imageLocation.getAbsolutePath());
        Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
        double widthRatio = (double) SCREEN_WIDTH / (double) result.getWidth();
        double heightRatio = (double) SCREEN_HEIGHT / (double) result.getHeight();
        if(!frs.isReady())
            return;
        fop = new FaceOperator(this,toAnalyze,frs);
        Face[] faces = fop.getFaces();

        if (faces == null)
            return;
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();

            // this is used to reset the rect to the screen size
            aFacesArray.getBoundingBoxWithRatio(widthRatio, heightRatio);
            rectangle(toAnalyze,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    ,opencv_core.Scalar.GREEN,2, LINE_8,0);
        }
        imwrite(imageLocation.getAbsolutePath(),toAnalyze);
    }


    // WARNING: Do not call this without user interaction!!! You might f*** up the classifier
    private void alertSettingIDForFace(final RecognizedFace rf, String newPerson, final int i, final Dialog parent){
        db = AppDatabase.getDatabase(this);
        new AlertDialog.Builder(this)
                .setTitle("Was it really wrong?")
                .setMessage(Html.fromHtml("Saving this face as <b>"+newPerson
                        +"</b>?<br/><b>WARNING</b>: <i>Wrong person might compromise the accuracy.</i>"))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        rf.getFace().setID(i);
                        saveRecognizedFace(rf);
                        if(parent != null)
                            parent.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    // this is called from onCreate to set up the file which will hold the image taken from camera
    private void setupImageHoldingFile() {
        File root = new File(getFilesDir(), ImgGrabber.CAPTURED_DIR);
        if (!root.exists())
            root.mkdirs();
        imageLocation = new File(root,"capture_"+System.currentTimeMillis()+".png");
        if(imageLocation.exists())
            imageLocation.delete();
        try {
            imageLocation.createNewFile();
        } catch (IOException e) {
            Log.d(TAG,"Couldn't open file to save the image!");
            exitActivity(-1);
        }
    }
}