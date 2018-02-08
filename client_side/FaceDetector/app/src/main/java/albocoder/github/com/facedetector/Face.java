package albocoder.github.com.facedetector;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2RGB;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGRA2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class Face {
    // class constants
    private static final int WIDTH = 80;
    private static final int HEIGHT = 80;

    // class fields
    private Rect boundingBox;
    private Mat faceContent;        // must always be 80x80

    // calculated-later fields
    private int local_id;
    private Mat histEqualizedFace;
    private Mat gScaledFace;

    // constructors
    public Face(Rect bb,Mat content){
        boundingBox = bb;
        faceContent = content;
        resize(content,faceContent,new Size(WIDTH,HEIGHT));
        cvtColor(faceContent, faceContent, CV_BGRA2BGR);
        local_id = -1;

        histEqualizedFace = null;
        gScaledFace = null;
    }
    public Face(Mat content) { this (null,content); }
    // useful functions
    public Rect getBoundingBox(){return boundingBox;}
    public Mat getBGRContent(){return faceContent;}
    public Mat getRGBContent(){
        Mat toReturn = faceContent.clone();
        cvtColor(toReturn, toReturn, CV_BGR2RGB);
        return toReturn;
    }
    public Mat getgscaleContent(){
        Mat dst = faceContent.clone();
        cvtColor(faceContent,dst,CV_RGB2GRAY);
        return dst;
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
}
