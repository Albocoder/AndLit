package albocoder.github.com.facedetector;

import android.Manifest;
import android.support.v4.content.ContextCompat;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Face {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 60;

    private Rect boundingBox;
    private Mat faceContent;        // must always be 60x60
    private Mat histEqualizedFace;

    public Face(Rect bb,Mat content){
        boundingBox = bb.clone();
        faceContent = new Mat();
        histEqualizedFace = null;
        Imgproc.resize(content,faceContent,new Size(WIDTH,HEIGHT));
    }

    public Rect getBoundingBox(){return boundingBox;}
    public Mat getRGBContent(){return faceContent;}
    public Mat getgscaleContent(){
        Mat dst = new Mat();
        Imgproc.cvtColor(faceContent,dst,Imgproc.COLOR_RGB2GRAY);
        return dst;
    }
    public Mat histEqualization() {
        if(histEqualizedFace != null)
            return histEqualizedFace;
        Mat histEqualizedFace = new Mat();
        Imgproc.equalizeHist(faceContent,histEqualizedFace);
        return histEqualizedFace;
    }
    public void recognize(){

    }
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
