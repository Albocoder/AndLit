package albocoder.github.com.facedetector;

import org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class Face {
    // class constants
    private static final int WIDTH = 80;
    private static final int HEIGHT = 80;

    // class fields
    private Rect boundingBox;
    private Mat faceContent;        // must always be 60x60

    // calculated-later fields
    private int local_id;
    private Mat histEqualizedFace;
    private Mat gScaledFace;

    // constructors
    public Face(Rect bb,Mat content){
        boundingBox = new Rect(bb);
        faceContent = new Mat();
        resize(content,faceContent,new Size(WIDTH,HEIGHT));
        cvtColor(faceContent, faceContent, CV_RGBA2RGB);
        local_id = -1;

        histEqualizedFace = null;
        gScaledFace = null;
    }
    public Face(Mat content) { this (null,content); }

    // useful functions
    public Rect getBoundingBox(){return boundingBox;}
    public Mat getRGBContent(){return faceContent;}
    public Mat getgscaleContent(){
        Mat dst = new Mat();
        cvtColor(faceContent,dst,CV_RGB2GRAY);
        return dst;
    }
    public Mat performHistEqualization() {
        if(histEqualizedFace != null)
            return histEqualizedFace;
        Mat histEqualizedFace = new Mat();
        equalizeHist(faceContent,histEqualizedFace);
        return histEqualizedFace;
    }
    public boolean setID(int id){
        if(local_id == -1){
            if(id <= -1)
                return false;   // can't set negative value id
            local_id = id;
            return true;
        }
        else
            return false; // id is already set!
    }
    public int getID(){ return local_id; }
    public void destroy() {faceContent.release();}
    public void saveToDatabase(){
//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        String filename = "barry.png";
//        File file = new File(path, filename);
//
//        Boolean bool = null;
//        filename = file.toString();
//        bool = Highgui.imwrite(filename, mIntermediateMat);
//
//        if (bool == true)
//            Log.d(TAG, "SUCCESS writing image to external storage");
//        else
//            Log.d(TAG, "Fail writing image to external storage");
    }
}
