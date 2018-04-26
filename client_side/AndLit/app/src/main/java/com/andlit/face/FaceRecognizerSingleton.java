package com.andlit.face;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.andlit.cloudInterface.synchronizers.classifier.ClassifierBackup;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.Classifier;
import com.andlit.database.entities.training_face;
import com.andlit.utils.StorageHelper;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class FaceRecognizerSingleton {

    // constants
    private static final int SEARCH_RADIUS = 1;
    private static final int NEIGHBORS = 8;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 8;
    private static final double THRESHOLD = 120;
    private static final int TOP_PREDICTIONS = 1;
    private static final double INSTANCES_DETECTION_RATIO = 3.0;
    private static final double RELATIVE_RATIO = 0.25;
    private static final String TAG = "FaceRecognizerSingleton";
    public static final String CLASSIFIER_NAME = "lbphClassifier.yml"; // Default path for the classifier if it doesn't exist

    // statics
    private static LBPHFaceRecognizer trainedModel;
    private static Classifier classifierMetadata;

    // fields
    private Context c;

    public FaceRecognizerSingleton(Context c){
        this.c = c;
        // Get metadata from the database
        if (classifierMetadata == null)
            classifierMetadata = AppDatabase.getDatabase(c).classifierDao().getClassifier();

        if (trainedModel == null)
            loadTrainedModel();
    }

    public boolean isReady(){ return trainedModel != null; }

    public synchronized void trainModel() {
        AppDatabase db = AppDatabase.getDatabase(c);
        int trainingInstances = db.trainingFaceDao().getNumberOfTrainingInstances();
        if(trainingInstances == 0) {
            try{
                Toast.makeText(c, "No training instances found! Synchronize or add faces.", Toast.LENGTH_SHORT).show();
            }catch (RuntimeException ignored){}
            return;
        }
        List<Integer> allLabels = db.trainingFaceDao().getAllPossibleRecognitions();
        if(trainedModel == null)
            trainedModel = createLBPHFaceRecognizer(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);

        Mat labels = new Mat( trainingInstances,1,CV_32SC1 );
        IntBuffer labelsBuffer = labels.createBuffer();
        opencv_core.MatVector facePhotos = new opencv_core.MatVector(trainingInstances);
        int index = 0;
        for (int i: allLabels) {
            List<training_face> facesOfLabel = db.trainingFaceDao().getInstancesOfLabel(i);
            for (training_face tf: facesOfLabel) {
                Face tmp = FaceOperator.loadFaceFromDatabase(c,tf);
                facePhotos.put(index,tmp.getgscaleContent());
                labelsBuffer.put(index,i);
                index++;
            }
        }
        trainedModel.train(facePhotos,labels);
        Log.d(TAG,"Model finished training!");
        saveTrainedModel(allLabels.size(),trainingInstances);
    }

    public RecognizedFace recognize(Face face) {
        if (face == null)
            return null;
        if (classifierMetadata == null)
            return null;
        if(!isReady())
            return new RecognizedFace(face,new int[]{-1},new double[]{-1});
        // JVM data holders
        int minimumLabels = Math.min(TOP_PREDICTIONS,classifierMetadata.num_recogn);
        int [] foundLabels = new int[minimumLabels];        // top 5 predictions
        double [] confidence = new double[minimumLabels];   // top 5 prediction confidences

        // native data holders
        IntPointer f = new IntPointer(minimumLabels);
        DoublePointer c = new DoublePointer(minimumLabels);

        trainedModel.predict(face.getgscaleContent(),f,c);
        f.get(foundLabels);
        c.get(confidence);
        return new RecognizedFace(face,foundLabels,confidence);
    }

    public boolean mustTrainConditions() {
        if(classifierMetadata == null)
            return true;
        AppDatabase db = AppDatabase.getDatabase(c);
        int numberOfPossibleDetections = db.trainingFaceDao().getNumberOfPossibleRecognitions();
        int numberOfInstancesPossible = db.trainingFaceDao().getNumberOfTrainingInstances();
        int numberOfCurrentDetections = classifierMetadata.num_recogn;
        int numberOfInstancesUsed = classifierMetadata.num_inst_trained;

        int newDetections = numberOfPossibleDetections-numberOfCurrentDetections;
        int newInstances = numberOfInstancesPossible-numberOfInstancesUsed;
        if (newDetections < 0)
            return false;

        if (newDetections == 0){
            // if same number of detections but new average instances haven't increased enough
            return !(((double) numberOfInstancesPossible / (double) numberOfPossibleDetections) <
                    ((double) numberOfInstancesUsed / (double) numberOfCurrentDetections + RELATIVE_RATIO));
        }
        else {
            // if in average there is less than 3 photos per detection don't train
            return !(((double) newInstances / (double) newDetections) < INSTANCES_DETECTION_RATIO);
        }
    }

    private synchronized void loadTrainedModel() {
        File classifierFile = null;
        if (classifierMetadata != null)
            classifierFile = new File(c.getFilesDir(),classifierMetadata.path);
        if(classifierFile==null)
            classifierFile = new File(c.getFilesDir(),CLASSIFIER_NAME);
        if(!classifierFile.exists()) {
            try{
                ClassifierBackup cb = new ClassifierBackup(c);
                cb.loadClassifier();
                if(classifierFile.exists()){
                    if(trainedModel == null)
                        trainedModel  = createLBPHFaceRecognizer();
                    trainedModel.load(classifierFile.getAbsolutePath());
                }else {
                    trainModel();
                }
            }catch(Exception e){trainModel();}
        }
        else {
            if(classifierFile.length() == 0) {
                trainModel();
                return;
            }
            if(trainedModel == null)
                trainedModel  = createLBPHFaceRecognizer();
            trainedModel.load(classifierFile.getAbsolutePath());
        }
    }

    private synchronized void saveTrainedModel(int numberOfDetections, int numInstTrained){
        File classifierFile;
        if (classifierMetadata != null)
            classifierFile = new File(c.getFilesDir(),classifierMetadata.path);
        else
            classifierFile = new File(c.getFilesDir(),CLASSIFIER_NAME);
        trainedModel.save(classifierFile.getAbsolutePath());
        try {
            String md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(classifierFile.getAbsolutePath()));
            Long tsLong = System.currentTimeMillis()/1000;
            classifierMetadata = new Classifier(classifierFile.getName(),md5,
                tsLong,numberOfDetections,numInstTrained,THRESHOLD);
            AppDatabase.getDatabase(c).classifierDao().deleteClassifier();
            AppDatabase.getDatabase(c).classifierDao().insertClassifier(classifierMetadata);
        } catch (IOException|NoSuchAlgorithmException ignored) {}
    }

    public static synchronized void destroyInstance() { trainedModel = null; }
}
