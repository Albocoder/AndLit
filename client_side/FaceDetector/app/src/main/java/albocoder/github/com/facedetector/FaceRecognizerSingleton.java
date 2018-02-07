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
    public static final int TOP_PREDICTIONS = 5;
    public static final double INSTANCES_DETECTION_RATIO = 3.0;
    public static final double RELATIVE_RATIO = 0.25;
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
        if (classifierMetadata == null)
            classifierMetadata = AppDatabase.getDatabase(c).classifierDao().getClassifier();

        if (trainedModel != null)
            return;
        else if(mustTrainConditions())
            trainModel();
        else
            loadTrainedModel();
    }

    public synchronized void trainModel() { // todo test this with data and stuff
        AppDatabase db = AppDatabase.getDatabase(c);
        int trainingInstances = db.trainingFaceDao().getNumberOfTrainingInstances();
        List<Integer> allLabels = db.trainingFaceDao().getAllPossibleRecognitions();

        trainedModel = createLBPHFaceRecognizer(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);

        Mat labels = new Mat(trainingInstances, 1);
        IntBuffer labelsBuffer = labels.createBuffer();
        opencv_core.MatVector facePhotos = new opencv_core.MatVector(trainingInstances);
        int index = 0;
        for (int i: allLabels) {
            List<training_face> facesOfLabel = db.trainingFaceDao().getInstancesOfLabel(i);
            for(training_face tf: facesOfLabel) {
                Face tmp = FaceOperator.loadFaceFromDatabase(tf);
                facePhotos.put(index,tmp.getBGRContent());
                labelsBuffer.put(index,i);
                index++;
            }
        }
        trainedModel.train(facePhotos,labels);
        Log.d(TAG,"Model is trained");
        saveTrainedModel(allLabels.size(),trainingInstances);
    }

    public RecognizedFace recognize(Face face) {
        if (face == null)
            return null;
        int [] foundLabels = new int[TOP_PREDICTIONS];        // top 5 predictions
        double [] confidence = new double[TOP_PREDICTIONS];   // top 5 prediction confidences
        opencv_core.UMat faceUmat = new opencv_core.UMat(face.getBGRContent());
        trainedModel.predict(faceUmat,foundLabels,confidence);
        return new RecognizedFace(face,foundLabels,confidence);
    }

    // private inner functions for modularity and nice stuff
    private boolean mustTrainConditions() {
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
            if (((double)numberOfInstancesPossible/(double)numberOfPossibleDetections) <
                    ((double)numberOfInstancesUsed/(double)numberOfCurrentDetections + RELATIVE_RATIO))
                return false;
        }
        else {
            // if in average there is less than 3 photos per detection don't train
            if ( ((double)newInstances/(double)newDetections) < INSTANCES_DETECTION_RATIO )
                return false;
        }

        // else train
        return true;
    }
    private synchronized void loadTrainedModel() {
        String path = CLASSIFIER_NAME;
        if (classifierMetadata != null)
            path = classifierMetadata.path;
        File classifierFile = new File(c.getFilesDir(),path);
        if (classifierFile.exists())
            trainedModel.load(classifierFile.getAbsolutePath());
        else
            trainModel(); // todo get it from the cloud. The damn user has deleted the shit
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
            String md5 = FaceOperator.MD5toHexString(digest);
            dis.close();

            Long tsLong = System.currentTimeMillis()/1000;
            classifierMetadata = new Classifier(classifierFile.getAbsolutePath(),md5,
                tsLong,numberOfDetections,numInstTrained,THRESHOLD);
            AppDatabase.getDatabase(c).classifierDao().deleteClassifier();
            AppDatabase.getDatabase(c).classifierDao().insertClassifier(classifierMetadata);
        } catch (IOException|NoSuchAlgorithmException e) {}
    }
}
