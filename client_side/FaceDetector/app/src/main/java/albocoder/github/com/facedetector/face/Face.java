package albocoder.github.com.facedetector.face;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2RGB;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGRA2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class Face {
    // class constants
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;

    // class fields
    private Rect boundingBox;
    private Mat faceContent;        // must always be 80x80

    // calculated-later fields
    private int local_id;
    private Mat gScaledFace;

    // constructors
    public Face(Rect bb,Mat content){
        boundingBox = bb;
        faceContent = content;
        resize(content,faceContent,new Size(WIDTH,HEIGHT));
        if(faceContent.channels() == 1) {
            gScaledFace = content.clone();
            equalizeHist(content,gScaledFace);
            faceContent = null;
        }
        else {
            cvtColor(faceContent, faceContent, CV_BGRA2BGR);
            gScaledFace = null;
        }
        local_id = -1;
    }
    public Face(Mat content) { this (null,content); }
    // useful functions
    public Rect getBoundingBox(){return boundingBox;}
    public Mat getBGRContent(){return faceContent;}
    public Mat getRGBContent() {
        if(faceContent == null)
            return null;
        Mat toReturn = faceContent.clone();
        cvtColor(toReturn, toReturn, CV_BGR2RGB);
        return toReturn;
    }
    public Mat getgscaleContent(){
        if(gScaledFace!=null)
            return gScaledFace;
        Mat dst = faceContent.clone();
        cvtColor(faceContent,dst,CV_RGB2GRAY);
        gScaledFace = dst.clone();
        equalizeHist(dst,gScaledFace);
        dst.release();
        return gScaledFace;
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
