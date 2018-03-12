package com.example.mehmet.andlit.helperUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import com.example.mehmet.andlit.R;
import com.example.mehmet.andlit.Settings.SettingsDefinedKeys;
import com.example.mehmet.andlit.database.AppDatabase;
import com.example.mehmet.andlit.database.entities.*;
import com.example.mehmet.andlit.face.*;
import com.example.mehmet.andlit.helperUI.listRelated.PersonDataAdapter;
import com.example.mehmet.andlit.helperUI.listRelated.PotentialPeopleAdapter;
import com.example.mehmet.andlit.helperUI.listRelated.TwoStringDataHolder;
import com.example.mehmet.andlit.utils.StorageHelper;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.example.mehmet.andlit.Settings.SettingsDefinedKeys.*;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class IntermediateCameraActivity extends Activity {

    // constants
    private static final String TAG = "IntermediateCamActivity";
    public static final String ARGUMENT_KEY = "filename";
    public static final int REQUEST_IMG_ANALYSIS = 1336;

    // fields
    private File imageLocation;
    private FaceRecognizerSingleton frs;
    private FaceOperator fop;
    private AppDatabase db;
    private List<KnownPPL> allKnownPpl;
    private Boolean saveOnExit;

    // view fields
    private ImageView analyzed;
    private ProgressDialog progress;
    int SCREEN_HEIGHT,SCREEN_WIDTH;
    double widthRatio,heightRatio;

    // ********************* ACTIVITY FUNCTIONS ********************* //
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_processed);
        // Setting up UI things
        Button takeImage = findViewById(R.id.BtnCpt);
        analyzed = findViewById(R.id.AnalyzedImg);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading!");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        // instantiating database connection
        db = AppDatabase.getDatabase(this);
        // initializing more variables
        fop = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_HEIGHT = displayMetrics.heightPixels;
        SCREEN_WIDTH = displayMetrics.widthPixels;
        allKnownPpl = db.knownPplDao().getAllRecords();
        // todo remove train() and just // instantiate FaceRecognizerSingleton
        train(); frs = new FaceRecognizerSingleton(this);
        // setting up file that holds the image from camera
        setupImageHoldingFile();
        // setting up settings to operate on
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT, false);
        // setting up listeners
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
                Intent i = new Intent(IntermediateCameraActivity.this
                        ,ImgGrabber.class);
                i.putExtra(ARGUMENT_KEY,imageLocation.getAbsolutePath());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(i,REQUEST_IMG_ANALYSIS);
            }
        });
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
                                    opencv_core.Point(((int)event.getX()),((int)event.getY())))){
                                RecognizedFace rf = fop.recognizeFace(i);
                                showPopUpForFace(rf);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMG_ANALYSIS) {
            if (imageLocation.length() == 0)
                Toast.makeText(this, "Error in taking the image!", Toast.LENGTH_SHORT).show();
            else {
                // using settings to check if user wants the detections to be saved in device
                if(fop != null) {
                    if(saveOnExit)
                        fop.storeUnlabeledFaces();
                    fop.destroy();
                }
                fop = process();
                Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
                analyzed.setImageBitmap(result);
            }
            if(imageLocation!= null)
                imageLocation.delete();
            progress.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLocation!= null)
            imageLocation.delete();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(imageLocation == null)
            return;
        Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
        widthRatio = (double) SCREEN_WIDTH/(double)result.getWidth();
        heightRatio = (double) SCREEN_HEIGHT/(double) result.getHeight();
        if(fop == null)
            return;
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
        }
        analyzed.setImageBitmap(result);
    }

    // helper function to cleanly exit the activity
    private void exitActivity(int code){
        if(imageLocation!= null)
            imageLocation.delete();
        setResult(code);
        finish();
    }


    // ********************************** UI related functions ***********************************//
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
        if(bestPrediction == -1) { // todo find what label the classifier returns as instance not found
            Toast.makeText(this,"Person not recognized!",Toast.LENGTH_SHORT).show();
            title = "Unknown Person";
            String defaultPhoto = null;
            try {
                defaultPhoto = StorageHelper.getFilePathFromAssets
                        (this, "default_profile.jpg","tmp", "default_profile.jpg");
            } catch (IOException e) {}
            photo = BitmapFactory.decodeFile(defaultPhoto);
            new File(defaultPhoto).delete();
        }
        else {
            db = AppDatabase.getDatabase(this);
            KnownPPL p = db.knownPplDao().getPersonWithID(bestPrediction);
            title =  p.name+" "+p.sname;
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
        } catch (IOException e) {}
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
                        FaceOperator.saveTrainingFaceToDatabase(view.getContext(),rf);
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


    //*********************************** HELPER FUNCTIONS ***************************************//
    // gets a single face from the database for that person of given ID
    private Bitmap getPhotoForDetection(int id) {
        if (id == -1)
            return null;
        Bitmap photo;
        List<String> filePaths = db.trainingFaceDao().getPathsOfLabel(id);
        String filePath = null;
        if(filePaths.size() > 0)
            filePath = filePaths.get(0);
        else
            try {
                filePath = StorageHelper.getFilePathFromAssets
                        (this, "default_profile.jpg","tmp", "default_profile.jpg");
                photo = BitmapFactory.decodeFile(filePath);
                new File(filePath).delete();
            } catch (IOException e) {}
        photo = BitmapFactory.decodeFile(filePath);
        return photo;
    }
    // an adapter for known people list in manual labeling
    private PersonDataAdapter getKnownDataForKnownPerson(KnownPPL p){
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
            Toast.makeText(this,Html.fromHtml("Couldn't save this person!<br />Error:"+e.getLocalizedMessage()),Toast.LENGTH_SHORT);
            Log.e(TAG,e.getStackTrace().toString());
        }
    }
    // this is called to process the image taken from camera
    public FaceOperator process() {
        opencv_core.Mat toAnalyze = imread(imageLocation.getAbsolutePath());
        Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
        widthRatio = (double) SCREEN_WIDTH/(double)result.getWidth();
        heightRatio = (double) SCREEN_HEIGHT/(double) result.getHeight();

        FaceOperator fop = new FaceOperator(this,toAnalyze);
        Face[] faces = fop.getFaces();
        if (faces == null)
            return null;
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();

            // this is used to reset the rect to the screen size
            aFacesArray.getBoundingBoxWithRatio(widthRatio,heightRatio);
            rectangle(toAnalyze,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    , opencv_core.Scalar.GREEN,2, LINE_8,0);
        }
        imwrite(imageLocation.getAbsolutePath(),toAnalyze);
        return fop;
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
            return;
        }
    }
    // TODO: DELETE THIS IN PRODUCTION! USER WILL START WITH NO KNOWN INSTANCES!
    private void train() {
        if (frs != null)
            return;
        List<training_face> recs =  db.trainingFaceDao().getAllRecords();
        if(recs.size() > 0)
            return;
        // testing classifier
        db.knownPplDao().insertEntry(
                new KnownPPL(1,"Erin","Avllazagaj",new Date(1996,3,13), 21,"Dorm #76 Bilkent") );
        db.knownPplDao().insertEntry(
                new KnownPPL(2,"Barack","Obama",new Date(1961,8,4), 56,"USA") );
        db.knownPplDao().insertEntry(
                new KnownPPL(3,"Argert","Boja",new Date(2016,2,2), 2,"107") );
        // random generator for md5 (NEVER DO THIS IRL) use: StorageHelper.getMD5OfFile()
        Random r = new Random();

        // delete all instances
        recs =  db.trainingFaceDao().getAllRecords();
        if(recs.size() != 0)
            return;
//        for (training_face tf : recs)
//            FaceOperator.deleteTrainingInstance(this,tf);

        List<Integer> erinIndex  = new ArrayList<Integer>();
        erinIndex.add(1);erinIndex.add(2);erinIndex.add(3);erinIndex.add(4);erinIndex.add(5);
        List<Integer> obamaIndex  = new ArrayList<>();
        obamaIndex.add(6);obamaIndex.add(7);obamaIndex.add(8);obamaIndex.add(9);obamaIndex.add(10);obamaIndex.add(11);
        List<Integer> argertIndex  = new ArrayList<>();
        argertIndex.add(13);argertIndex.add(14);argertIndex.add(16);
        List<Integer> testing = new ArrayList<>();
        testing.add(15);testing.add(12);
        Face detected = null;
        for(int i = 1; i < 17; i++) {
            String filepath;
            try {
                filepath = StorageHelper.getFilePathFromAssets(this,"training/face"+i+".png","training","face"+i+".png");
            } catch (IOException e) { continue; }
            String md5 = ""+r.nextLong()+r.nextInt();
            try {
                md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(filepath));
            } catch (IOException|NoSuchAlgorithmException e) {}

            opencv_core.Mat m = imread(filepath);
            if (m==null)
                continue;
            detected = new Face(m);
            if(erinIndex.contains(i)) {
                detected.setID(1);
            }
            else if(obamaIndex.contains(i)){
                detected.setID(2);
            }
            else if(argertIndex.contains(i)){
                detected.setID(3);
            }else{
                continue;
            }
            try{
                training_face f = FaceOperator.moveDetectionToTraining(this,
                        FaceOperator.saveDetectedFaceToDatabase(this,detected));
                Log.d(TAG,f.toString());
            }catch (NoSuchAlgorithmException|IOException e) {
                e.printStackTrace();
            }
            new File(filepath).delete();
        }
    }
}