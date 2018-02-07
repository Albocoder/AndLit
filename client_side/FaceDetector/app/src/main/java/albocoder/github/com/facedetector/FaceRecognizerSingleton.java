package albocoder.github.com.facedetector;

import android.content.Context;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.database.entities.Classifier;
import albocoder.github.com.facedetector.database.entities.training_face;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class FaceRecognizerSingleton {
    // constants
    public static final int SEARCH_RADIUS = 1;
    public static final int NEIGHBORS = 5;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 8;
    public static final double THRESHOLD = 130.0;
    private static final String TAG = "FaceRecognizerSingleton";
    private static final String CLASSIFIER_NAME = "lbphClassifier.yml"; // Default path for the classifier if it doesn't exist

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

        if (trainedModel != null)
            return;
        else if(mustTrainConditions())
            trainModel();
        else
            loadTrainedModel();
    }

    public synchronized void trainModel() {
        // get the training data labels and paths
        // load the data in list and train from that
        // then save
        AppDatabase db = AppDatabase.getDatabase(c);
        int trainingInstances = db.trainingFaceDao().getNumberOfTrainingInstances();
        List<Integer> numClasses = db.trainingFaceDao().getAllPossibleRecognitions();

        trainedModel = createLBPHFaceRecognizer(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);

        Mat labels = new Mat(trainingInstances, 1);
        IntBuffer rotulosBuffer = labels.createBuffer();
        opencv_core.MatVector facePhotos = new opencv_core.MatVector(trainingInstances);
        int index = 0;
        for (int i: numClasses) {
            List<training_face> facesOfLabel = db.trainingFaceDao().getInstancesOfLabel(i);
            for(training_face tf: facesOfLabel) {
                Face tmp = FaceOperator.loadFaceFromDatabase(tf);
                Mat totest = tmp.getBGRContent();  // Must be taken from Face class
                rotulosBuffer.put(index);
                index ++;//todo
            }
        }
//        Mat labels = Mat.ones(1,((int)imgs.size()),0).asMat();
//        Log.i(TAG,"Number of images loaded is: "+imgs.size());
//        trainedModel.train(imgs,labels);
//        Log.i(TAG,"Model is trained");
//        saveTrainedModel(numClasses,);
    }

    private boolean mustTrainConditions() {
        AppDatabase db = AppDatabase.getDatabase(c);
        int numberOfPossibleDetections = db.trainingFaceDao().getNumberOfPossibleRecognitions();
        int numberOfCurrentDetections = 0;
        if(classifierMetadata != null)
            numberOfCurrentDetections = classifierMetadata.num_recogn;
        else
            return true;
        return true;//todo
    }

    public Object predict(Face face) { // todo decide return type
        if (face == null)
            return null;
        int [] foundLabels = new int[5];        // top 5 predictions
        double [] confidence = new double[5];   // top 5 prediction confidences
        opencv_core.UMat faceUmat = new opencv_core.UMat(face.getBGRContent());
        trainedModel.predict(faceUmat,foundLabels,confidence);
        Log.i(TAG,"Predicted: \nLabels:"+foundLabels.toString()
                +"\nConfidences:"+confidence.toString());
        return null;
    }
    private synchronized void loadTrainedModel() {
        String path = CLASSIFIER_NAME;
        if (classifierMetadata != null)
            path = classifierMetadata.path;
        File classifierFile = new File(c.getFilesDir(),path);
        if (classifierFile.exists())
            trainedModel.load(classifierFile.getAbsolutePath());
    }
    private synchronized void saveTrainedModel(int numberOfDetections, int numInstTrained){
        File classifierFile;
        if (classifierMetadata != null)
            classifierFile = new File(classifierMetadata.path);
        else
            classifierFile = new File(c.getFilesDir(),CLASSIFIER_NAME);
        trainedModel.save(classifierFile.getAbsolutePath());
        try {
            FileInputStream fis = new FileInputStream(classifierFile);

            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(fis,md);
            byte[] digest = md.digest();
            String md5 = new String(digest);
            dis.close();

            Long tsLong = System.currentTimeMillis()/1000;
            classifierMetadata = new Classifier(classifierFile.getAbsolutePath(),md5,
                tsLong,numberOfDetections,numInstTrained);
            AppDatabase.getDatabase(c).classifierDao().deleteClassifier();
            AppDatabase.getDatabase(c).classifierDao().insertClassifier(classifierMetadata);
        } catch (IOException|NoSuchAlgorithmException e) {}
    }
}
