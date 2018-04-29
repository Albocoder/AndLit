package com.andlit.face;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.detected_face;
import com.andlit.database.entities.training_face;
import com.andlit.utils.StorageHelper;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Random;



import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_objdetect.CASCADE_SCALE_IMAGE;

public class FaceOperator {
    // class constants
    private static final int FACE_MIN_SQUARE_EDGE = 40; // start detection from 40x40 square
    private static final float FACE_MIN_SQUARE_RELATIVE_EDGE = 0.2f;

    // shared fields
    private static CascadeClassifier frontDetector;
    private static CascadeClassifier profileDetector;
    private static final String TAG = "Controller:FaceOperator";
    private static final String DETECTIONS_PATH = "detections";
    private static final String TRAINING_PATH = "training";

    // unique fields
    private float mRelativeFaceSize;
    private int mAbsoluteFaceSize;
    private Mat scene;
    private final Context context;
    private FaceRecognizerSingleton frs;

    // calculated fields
    private Face[] foundFaces;
    private RecognizedFace[] recognizedFaces;

    // constructors
    public FaceOperator(Context c, Mat f, int abs, float rel,FaceRecognizerSingleton frs){
        super();
        context = c;
        scene = f.clone();
        mAbsoluteFaceSize = abs;
        mRelativeFaceSize = rel;
        this.frs = frs;
        foundFaces = null;
        recognizedFaces = null;
    }
    public FaceOperator(Context c, Mat f,FaceRecognizerSingleton frs){this (c,f,FACE_MIN_SQUARE_EDGE,FACE_MIN_SQUARE_RELATIVE_EDGE,frs);}
    public FaceOperator(Context c, Mat f,float rel,FaceRecognizerSingleton frs){this (c,f,0,rel,frs);}
    public FaceOperator(Context c, Mat f,int abs,FaceRecognizerSingleton frs){this (c,f,abs,FACE_MIN_SQUARE_RELATIVE_EDGE,frs);}

    public Face[] getFaces() {
        if (foundFaces != null)
            return foundFaces;
        initializeDetector();
        Mat mGray = scene.clone();

        cvtColor(scene,mGray, CV_RGB2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0)
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        }

        RectVector facesVector = new RectVector();
        if (frontDetector != null)
            frontDetector.detectMultiScale(mGray, facesVector, 1.1, 5, CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Face[] facesArray = new Face[(int) facesVector.size()];
        for (int i = 0; i < facesVector.size();i++) {
            Rect faceRect = facesVector.get(i);
            Mat content = scene.apply(faceRect);
            facesArray[i] = new Face(faceRect,content);
        }
        foundFaces = facesArray;
        mGray.release();
        System.gc();
        Runtime.getRuntime().gc(); // DON'T DELETE THIS FFS!!!
        return facesArray;
    }
    public Mat getScene() { return scene; }
    private synchronized void initializeDetector() {
        if (frontDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface_improved.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                frontDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                frontDetector.load(mCascadeFile.getAbsolutePath());
                if (frontDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    frontDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                cascadeDir.delete();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            }
        }
        if (profileDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_profileface);
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_profileface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                profileDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                profileDetector.load(mCascadeFile.getAbsolutePath());
                if (profileDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier2");
                    profileDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier2 from " + mCascadeFile.getAbsolutePath());
                cascadeDir.delete();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade2. Exception thrown: " + e);
            }
        }
    }
    public RecognizedFace[] recognizeFaces() {
        if(recognizedFaces != null)
            return recognizedFaces;
        if(frs == null)
            return null;
        Face [] ff = this.getFaces();
        recognizedFaces = new RecognizedFace[ff.length];
        for(int i = 0; i < recognizedFaces.length; i++) {
            recognizedFaces[i] = frs.recognize(ff[i]);
            recognizedFaces[i].setBestMatch(context);
        }
        return recognizedFaces;
    }
    public RecognizedFace recognizeFace(int index) {
        if(frs == null)
            return null;
        Face [] ff = this.getFaces();
        if(index>=ff.length)
            return null;
        if(recognizedFaces != null) {
            if(recognizedFaces[index] != null)
                return recognizedFaces[index];
        }
        else
            recognizedFaces = new RecognizedFace[ff.length];
        recognizedFaces[index] = frs.recognize(ff[index]);
        recognizedFaces[index].setBestMatch(context);
        return recognizedFaces[index];
    }

    // Utility functions
    public void destroy() {
        for(Face f:foundFaces)
            f.destroy();
        scene.release();
        foundFaces = null;
        System.gc();
        Runtime.getRuntime().gc();
    }
    public void storeAllFaces() {
        Face [] ff = this.getFaces();
        for(Face f: ff)
            try {
                if(f.getID() == -1)
                    saveDetectedFaceToDatabase(context,f);
                else
                    saveTrainingFaceToDatabase(context,f);
            } catch (IOException|NoSuchAlgorithmException e) {
                Log.e(TAG,"Couldn't save a face! Error encountered: "+e.getMessage());
            }
    }
    public void storeUnlabeledFaces() {
        Face [] ff = this.getFaces();
        for(Face f: ff)
            try {
                if(f.getID() == -1)
                    saveDetectedFaceToDatabase(context,f);
            } catch (IOException|NoSuchAlgorithmException e) {
                Log.e(TAG,"Couldn't save a face! Error encountered: "+e.getMessage());
            }
    }

    // static utility functions
    // referring: https://stackoverflow.com/questions/35469726/creating-directory-in-internal-storage
    public static detected_face saveDetectedFaceToDatabase(Context c, RecognizedFace f) throws IOException, NoSuchAlgorithmException {
        return saveDetectedFaceToDatabase(c,f,true);
    }
    public static training_face saveTrainingFaceToDatabase(Context c, RecognizedFace f) throws IOException, NoSuchAlgorithmException {
        return saveTrainingFaceToDatabase(c, f, true);
    }
    public static detected_face saveDetectedFaceToDatabase(Context c, RecognizedFace f, boolean privateData) throws IOException, NoSuchAlgorithmException {
        long timeInMillis = System.currentTimeMillis();
        Random r = new Random();
        detected_face detection;
        // if we can write to external storage we do
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED && !privateData) {
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File savingDir = new File(Environment.getExternalStorageDirectory(), DETECTIONS_PATH);

            // check if directory exists
            if (!savingDir.exists())
                savingDir.mkdirs();
            File imageFile = new File(Environment.getExternalStorageDirectory(),
                    DETECTIONS_PATH + "/" + filename);
            if (imageFile.exists())
                imageFile.delete();

            // check if written
            boolean saved = imwrite(imageFile.getAbsolutePath(), f.getFace().getgscaleContent());
            if (!saved)
                return null;

            // get MD5 of the file we just wrote
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(imageFile.getAbsolutePath()));

            detection = new detected_face(f.getFace().getID(), "EXTERNAL@"+filename
                    , md5, timeInMillis,f.getLabels()[0]);
            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.detectedFacesDao().insertFace(detection);
            } catch (RuntimeException e) {
                try{
                    db.detectedFacesDao().updateRowData(detection);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    imageFile.delete();
                    return null;
                }
            }
        } else {
            // write to internal storage
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File mFileTemp = new File(c.getFilesDir() + File.separator + DETECTIONS_PATH, filename);
            mFileTemp.getParentFile().mkdirs();

            // check if written
            boolean written = imwrite(mFileTemp.getAbsolutePath(), f.getFace().getgscaleContent());
            if (!written)
                return null;

            // get MD5
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(mFileTemp.getAbsolutePath()));

            // create new entry
            detection = new detected_face(f.getFace().getID(), mFileTemp.getAbsolutePath()
                    , md5, timeInMillis,f.getLabels()[0]);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.detectedFacesDao().insertFace(detection);
            } catch (SQLiteConstraintException e) {
                try{
                    db.detectedFacesDao().updateRowData(detection);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    mFileTemp.delete();
                    return null;
                }
            }
        }
        return detection;
    }
    public static training_face saveTrainingFaceToDatabase(Context c, RecognizedFace f, boolean privateData) throws IOException, NoSuchAlgorithmException {
        if(f.getFace().getID() == -1)
            return null;
        long timeInMillis = System.currentTimeMillis();
        Random r = new Random();
        training_face trainingInstance;
        // if we can write to external storage we do
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED && !privateData) {
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File savingDir = new File(Environment.getExternalStorageDirectory(), TRAINING_PATH);

            // check if directory exists
            if (!savingDir.exists())
                savingDir.mkdirs();
            File imageFile = new File(Environment.getExternalStorageDirectory(),
                    TRAINING_PATH + "/" + filename);
            if (imageFile.exists())
                imageFile.delete();

            // check if written
            boolean saved = imwrite(imageFile.getAbsolutePath(), f.getFace().getgscaleContent());
            if (!saved)
                return null;

            // get MD5 of the file we just wrote
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(imageFile.getAbsolutePath()));

            trainingInstance = new training_face(f.getFace().getID(),
                    "EXTERNAL@"+filename, md5);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.trainingFaceDao().insertTrainingFace(trainingInstance);
            } catch (RuntimeException e) {
                try{
                    db.trainingFaceDao().insertTrainingFace(trainingInstance);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    imageFile.delete();
                    return null;
                }
            }
        } else {
            // write to internal storage
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File mFileTemp = new File(c.getFilesDir() + File.separator + TRAINING_PATH, filename);
            mFileTemp.getParentFile().mkdirs();

            // check if written
            boolean written = imwrite(mFileTemp.getAbsolutePath(), f.getFace().getgscaleContent());
            if (!written)
                return null;

            // get MD5
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(mFileTemp.getAbsolutePath()));

            // create new entry
            trainingInstance = new training_face(f.getFace().getID(),
                    "INTERNAL@"+filename, md5);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.trainingFaceDao().insertTrainingFace(trainingInstance);
            } catch (SQLiteConstraintException e) {
                try{
                    db.trainingFaceDao().insertTrainingFace(trainingInstance);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    mFileTemp.delete();
                    return null;
                }
            }
        }
        return trainingInstance;
    }
    public static detected_face saveDetectedFaceToDatabase(Context c, Face f) throws IOException, NoSuchAlgorithmException {
        return saveDetectedFaceToDatabase(c,f,true);
    }
    public static training_face saveTrainingFaceToDatabase(Context c, Face f) throws IOException, NoSuchAlgorithmException {
        return saveTrainingFaceToDatabase(c, f, true);
    }
    public static detected_face saveDetectedFaceToDatabase(Context c, Face f, boolean privateData) throws IOException, NoSuchAlgorithmException {
        long timeInMillis = System.currentTimeMillis();
        Random r = new Random();
        detected_face detection;
        // if we can write to external storage we do
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED && !privateData) {
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File savingDir = new File(Environment.getExternalStorageDirectory(), TRAINING_PATH);

            // check if directory exists
            if (!savingDir.exists())
                savingDir.mkdirs();
            File imageFile = new File(Environment.getExternalStorageDirectory(),
                    DETECTIONS_PATH + "/" + filename);
            if (imageFile.exists())
                imageFile.delete();

            // check if written
            boolean saved = imwrite(imageFile.getAbsolutePath(), f.getgscaleContent());
            if (!saved)
                return null;

            // get MD5 of the file we just wrote
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(imageFile.getAbsolutePath()));

            detection = new detected_face(f.getID(), "EXTERNAL@"+filename
                    , md5, timeInMillis,-1);
            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.detectedFacesDao().insertFace(detection);
            } catch (RuntimeException e) {
                try{
                    db.detectedFacesDao().updateRowData(detection);
                    Log.e(TAG, "First Try failed: "+e.getStackTrace().toString());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getStackTrace().toString());
                    imageFile.delete();
                    return null;
                }
            }
        } else {
            // write to internal storage
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File mFileTemp = new File(c.getFilesDir() + File.separator + DETECTIONS_PATH, filename);
            mFileTemp.getParentFile().mkdirs();

            // check if written
            boolean written = imwrite(mFileTemp.getAbsolutePath(), f.getgscaleContent());
            if (!written)
                return null;

            // get MD5
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(mFileTemp.getAbsolutePath()));

            // create new entry
            detection = new detected_face(f.getID(), "INTERNAL@"+filename
                    , md5, timeInMillis,-1);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.detectedFacesDao().insertFace(detection);
            } catch (SQLiteConstraintException e) {
                try{
                    db.detectedFacesDao().updateRowData(detection);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    mFileTemp.delete();
                    return null;
                }
            }
        }
        return detection;
    }
    public static training_face saveTrainingFaceToDatabase(Context c, Face f, boolean privateData) throws IOException, NoSuchAlgorithmException {
        if(f.getID() == -1)
            return null;
        long timeInMillis = System.currentTimeMillis();
        Random r = new Random();
        training_face trainingInstance;
        // if we can write to external storage we do
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED && !privateData) {
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File savingDir = new File(Environment.getExternalStorageDirectory(), TRAINING_PATH);

            // check if directory exists
            if (!savingDir.exists())
                savingDir.mkdirs();
            File imageFile = new File(Environment.getExternalStorageDirectory(),
                    TRAINING_PATH + "/" + filename);
            if (imageFile.exists())
                imageFile.delete();

            // check if written
            boolean saved = imwrite(imageFile.getAbsolutePath(), f.getgscaleContent());
            if (!saved)
                return null;

            // get MD5 of the file we just wrote
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(imageFile.getAbsolutePath()));

            trainingInstance = new training_face(f.getID(),"EXTERNAL@"+filename, md5);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.trainingFaceDao().insertTrainingFace(trainingInstance);
            } catch (RuntimeException e) {
                try{
                    db.trainingFaceDao().insertTrainingFace(trainingInstance);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (RuntimeException e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    imageFile.delete();
                    return null;
                }
            }
        } else {
            // write to internal storage
            String filename = "face_" + timeInMillis + "_" + r.nextInt() + ".png";
            File mFileTemp = new File(c.getFilesDir() + File.separator + TRAINING_PATH, filename);
            mFileTemp.getParentFile().mkdirs();

            // check if written
            boolean written = imwrite(mFileTemp.getAbsolutePath(), f.getgscaleContent());
            if (!written)
                return null;

            // get MD5
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(mFileTemp.getAbsolutePath()));

            // create new entry
            trainingInstance = new training_face(f.getID(),
                    "INTERNAL@"+filename, md5);

            // insert and return new entry
            AppDatabase db = AppDatabase.getDatabase(c);
            try {
                db.trainingFaceDao().insertTrainingFace(trainingInstance);
            } catch (SQLiteConstraintException e) {
                try{
                    db.trainingFaceDao().insertTrainingFace(trainingInstance);
                    Log.e(TAG, "First Try failed: "+e.getLocalizedMessage());
                }catch (Exception e2) {
                    Log.e(TAG, "Second Try failed: "+e2.getLocalizedMessage());
                    mFileTemp.delete();
                    return null;
                }
            }
        }
        return trainingInstance;
    }

    public static boolean deleteDetectionInstance(Context c, detected_face d){
        File toDelete = new File(getAbsolutePath(c,d));
        AppDatabase db = AppDatabase.getDatabase(c);
        try{
            db.detectedFacesDao().deleteDetectionForFace(d);
        }catch (RuntimeException e){
            return false;
        }
        if (toDelete.exists())
            toDelete.delete();
        return true;
    }
    public static boolean deleteTrainingInstance(Context c, training_face t){
        File toDelete = new File(getAbsolutePath(c,t));
        AppDatabase db = AppDatabase.getDatabase(c);
        try{
            db.trainingFaceDao().deleteEntry(t);
        }catch (RuntimeException e){ return false; }
        if (toDelete.exists())
            toDelete.delete();
        return true;
    }
    public static training_face moveDetectionToTraining(Context c, detected_face d) {
        // checks for sanity
        if (d == null)
            return null;
        // if id != -1 (just to make less accesses to database)
        if (d.id == -1)
            return null;
        File faceImg = new File(getAbsolutePath(c,d));
        // if face exists
        if(!faceImg.exists())
            return null;

        // read matrix from image
        Face tmp = loadFaceFromDatabase(c,d);
        if (tmp == null)
            return null;
        tmp.setID(d.id);

        try {
            training_face tf = saveTrainingFaceToDatabase(c,tmp);
            if (tf == null) // person is not known in the database
                return null;
            if(deleteDetectionInstance(c,d))
                return tf;
            else{
                // sanitizing database to use least possible space
                deleteTrainingInstance(c,tf);
                return null;
            }
        } catch (IOException|NoSuchAlgorithmException e) {
            Log.e(TAG,"Error trying to move face to training: "+e.getLocalizedMessage());
            return null;
        }
    }
    public static Face loadFaceFromDatabase(Context c,detected_face f) {
        if( f == null)
            return null;
        Mat image = imread(getAbsolutePath(c,f));
        if(image == null)
            return null;
        Face toReturn = new Face(image);
        toReturn.setID(f.id);
        return  toReturn;
    }
    public static Face loadFaceFromDatabase(Context c,training_face f) {
        if( f == null)
            return null;
        Mat image = imread(getAbsolutePath(c,f));
        if(image == null)
            return null;
        Face toReturn = new Face(image);
        toReturn.setID(f.label);
        return toReturn;
    }
    public static RecognizedFace loadRecognizedFaceFromDatabase(Context c,detected_face f) {
        if( f == null)
            return null;
        Mat image = imread(getAbsolutePath(c,f));
        if(image == null)
            return null;
        RecognizedFace toReturn = new RecognizedFace(new Face(image),new int[]{f.predictedlabel},new double[]{-1.0});
        toReturn.getFace().setID(f.id);
        return  toReturn;
    }
    public static String getAbsolutePath(Context c,String path,boolean isTraining) {
        String storagePlace = path.substring(0,path.indexOf("@"));
        String filename = path.substring(path.indexOf("@")+1);
        String folder = isTraining?TRAINING_PATH:DETECTIONS_PATH;
        if(storagePlace.equals("EXTERNAL"))
            return new File(Environment.getExternalStorageDirectory(),
                    folder + "/" + filename).getAbsolutePath();
        return new File(c.getFilesDir() + File.separator + folder, filename).getAbsolutePath();
    }
    public static String getAbsolutePath(Context c,training_face tf) {
        return getAbsolutePath(c,tf.path,true);
    }
    public static String getAbsolutePath(Context c,detected_face df) {
        return getAbsolutePath(c,df.path,false);
    }
}