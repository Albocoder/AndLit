package albocoder.github.com.facedetector;

import org.opencv.core.Mat;
<<<<<<< HEAD

public class FaceRecognizerSingleton {
    private static LBPHFaceRecognizer trainedModel;
    public FaceRecognizerSingleton(){ trainModel(); }

    private void trainModel() {

    }
    public long predict(Face face){

    }
}
=======
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;

public class FaceRecognizerSingleton {
    private static FaceRecognizer trainedModel;

    public FaceRecognizerSingleton(){
        if(trainedModel == null)
            trainTheModel();
    }
    public void trainTheModel(){
        // read the trainedModel serialized from database
        int RADIUS = 1, NEIGHBORS = 8, GRID_X = 8, GRID_Y = 8;
        double THRESHOLD = 130d;
        trainedModel = LBPHFaceRecognizer.create(RADIUS, NEIGHBORS, GRID_X, GRID_Y, THRESHOLD);
        // get images and labels from the database
        //trainedModel.train(Mat img,Mat labels_aka_IDs);
    }
    // // TODO: Implement this function
    public long predict(Mat toPredict){
        return -1;
    }
}
>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
