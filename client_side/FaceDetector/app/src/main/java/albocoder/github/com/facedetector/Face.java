package albocoder.github.com.facedetector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static android.view.View.X;

public class Face {
    // constants
    private int NORMAL_SIZE = 60;

    // fields
    private Rect boundingBox;
    private Mat faceContent;

    // calculated fields
    private long personID;
    private Mat histEqualization;

    // constructor
    Face(Rect bb,Mat content){
        // obtained
        if(!checkSanity(bb,content))
            throw new RuntimeException("Bounding box and content must not be null");
        boundingBox = bb;
        faceContent = maintainSanity(content);
        // calculated
        personID = -1;
        histEqualization = null;
    }

    // sanity maintaining functions
    private boolean checkSanity(Rect bb,Mat content){
        if(bb == null)
            return false;
        if(content == null)
            return false;
        return true;
    }
    private Mat maintainSanity(Mat content){
        Mat toReturn = new Mat();
        content.copyTo(toReturn);
        // TODO: check the structure of Mat content (row,cols,RGB)
        Imgproc.resize(content,toReturn,new Size(NORMAL_SIZE,NORMAL_SIZE));
        return toReturn;
    }

    // accessors
    public Rect getBoundingBox(){return boundingBox;}
    public Mat getRGBContent(){return faceContent;}
    public Mat getgscaleContent(){
        Mat dst = new Mat();
        Imgproc.cvtColor(faceContent,dst,Imgproc.COLOR_RGB2GRAY);
        return dst;
    }
    //public void setID(long id){if(personID == -1){personID = id;}}
    public long getID(){return personID;}

    // image processing - mutators
    public Mat performHistEqualization(){
        if (histEqualization != null)
            return histEqualization;
        Mat equalized = new Mat();
        faceContent.copyTo(equalized);
        //Imgproc.blur(equalized,equalized,new Size(3,3));

        List<Mat> channels = new ArrayList<Mat>();
        Core.split(equalized, channels);

        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Imgproc.equalizeHist(channels.get(1), channels.get(1));
        Imgproc.equalizeHist(channels.get(2), channels.get(2));

        Core.merge(channels, equalized);
        return equalized;
    }
    public long recognize(){
        if (personID != -1)
            return personID;
        performHistEqualization();
        FaceRecognizerSingleton f = new FaceRecognizerSingleton();
        f.trainTheModel();
        personID = f.predict(histEqualization);
        return personID;
    }
}