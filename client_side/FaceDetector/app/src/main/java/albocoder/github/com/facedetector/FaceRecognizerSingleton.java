package albocoder.github.com.facedetector;

import android.content.Context;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.database.entities.Classifier;
import albocoder.github.com.facedetector.utils.StorageHelper;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class FaceRecognizerSingleton {
    // constants
    public static final int SEARCH_RADIUS = 1;
    public static final int NEIGHBORS = 5;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 8;
    public static final double THRESHOLD = 130.0;
    private static final String TAG = "FaceRecognizerSingleton";
    private static final String CLASSIFIER_PATH = "lbphClassifier.yml"; // Default path for the classifier if it doesn't exist

    // statics
    private static LBPHFaceRecognizer trainedModel;
    private static Classifier classifierMetadata;
    // fields
    private Context c;

    public FaceRecognizerSingleton(Context c){
        this.c = c;
        // Get metadata from the database
        if (classifierMetadata == null)     // If no entry we get null
            classifierMetadata = AppDatabase.getDatabase(c).classifierDao().getClassifier();
        trainModel();
    }

    private synchronized void trainModel() {
        loadTrainedModel();
        if (trainedModel != null)
            return;
        int numClasses = 1; // todo make this training dynamic
        trainedModel = createLBPHFaceRecognizer(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);
        MatVector imgs = new MatVector(8);
        for (int i = 1; i < 9; i++) {
            try {
                String fileLocation = StorageHelper.getFilePathFromAssets(c, "trainingdata/features/face"+i+".png", "face.png");
                Mat totest = new Face(imread(fileLocation)).performHistEqualization();  // Must be taken from Face class
                new File(fileLocation).delete();   // delete the file
                imgs.put(i,totest);
            } catch (IOException e) {}
        }
        Mat labels = Mat.ones(1,((int)imgs.size()),0).asMat();
        Log.i(TAG,"Number of images loaded is: "+imgs.size());
        trainedModel.train(imgs,labels);
        Log.i(TAG,"Model is trained");
        saveTrainedModel(numClasses);

        // todo: remove this
        Face toPredict = null;
        try {
            int[] l = new int[1];
            double[] conf = new double[1];
            String fileLocation = StorageHelper.getFilePathFromAssets(c, "erinface/face9.png", "face.png");
            toPredict = new Face(imread(fileLocation));
            new File(fileLocation).delete();
        }catch (IOException e){}

        predict(toPredict);
    }
    public Object predict(Face face) { // todo decide return type
        if (face == null)
            return null;
        int [] foundLabels = new int[5];        // top 5 predictions
        double [] confidence = new double[5];   // top 5 prediction confidences
        opencv_core.UMat faceUmat = new opencv_core.UMat(face.performHistEqualization());
        trainedModel.predict(faceUmat,foundLabels,confidence);
        Log.i(TAG,"Predicted: \nLabels:"+foundLabels.toString()
                +"\nConfidences:"+confidence.toString());
        return null;
    }
    private synchronized void loadTrainedModel() {
        if (trainedModel != null)
            return;
        String path = CLASSIFIER_PATH;
        if (classifierMetadata != null)
            path = classifierMetadata.path;
        File classifierFile = new File(c.getFilesDir(),path);
        if (classifierFile.exists())
            trainedModel.load(classifierFile.getAbsolutePath());
    }
    private synchronized void saveTrainedModel(int numberOfDetections){
        String path = CLASSIFIER_PATH;
        if (classifierMetadata != null)
            path = classifierMetadata.path;
        classifierMetadata = new Classifier(classifierMetadata);
        File classifierFile = new File(c.getFilesDir(),path);
        trainedModel.save(classifierFile.getAbsolutePath());
        try {
            FileInputStream fis = new FileInputStream(classifierFile);
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
            classifierMetadata.hash = md5;
            Long tsLong = System.currentTimeMillis()/1000;
            classifierMetadata.last_update = tsLong;
            classifierMetadata.num_recogn = numberOfDetections;
            classifierMetadata.path = path;
            AppDatabase.getDatabase(c).classifierDao().deleteClassifier();
            AppDatabase.getDatabase(c).classifierDao().insertClassifier(classifierMetadata);
        } catch (IOException e) {}
    }
}
