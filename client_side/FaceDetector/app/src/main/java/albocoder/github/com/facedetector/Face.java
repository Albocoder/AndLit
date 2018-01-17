package albocoder.github.com.facedetector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static android.view.View.X;

public class Face {
    private Rect boundingBox;
    private Mat faceContent;
    private long personID;

    Face(Rect bb,Mat content){
        boundingBox = bb;
        faceContent = content;
        personID = -1;
    }

    public Rect getBoundingBox(){return boundingBox;}
    public Mat getRGBContent(){return faceContent;}
    public Mat getgscaleContent(){
        Mat dst = new Mat();
        Imgproc.cvtColor(faceContent,dst,Imgproc.COLOR_RGB2GRAY);
        return dst;
    }
    public void setID(long id){if(personID == -1){personID = id;}}

    // image processing
    public Mat performHistEqualization(){
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
//    private Mat tanTriggsPreproc(){
//        double alpha = 0.1d, tau = 10.0d, gamma = 0.2d;
//        int sigma0 = 1, sigma1 = 2;
//        Mat toReturn = new Mat();
//        faceContent.copyTo(toReturn);
//        toReturn.convertTo(toReturn,CvType.CV_32FC1);
//        Core.pow(toReturn,gamma,toReturn);
//        {
//            Mat gaussian0 = new Mat(), gaussian1 = new Mat();
//            int kernel_sz0 = (3*sigma0);
//            int kernel_sz1 = (3*sigma1);
//            kernel_sz0 += ((kernel_sz0 % 2) == 0) ? 1 : 0;
//            kernel_sz1 += ((kernel_sz1 % 2) == 0) ? 1 : 0;
//            Imgproc.GaussianBlur(toReturn,gaussian0,new Size(kernel_sz0,kernel_sz0), sigma0, sigma0, Core.BORDER_REPLICATE);
//            Imgproc.GaussianBlur(toReturn,gaussian1,new Size(kernel_sz1,kernel_sz1), sigma1, sigma1, Core.BORDER_REPLICATE);
//            Core.subtract(gaussian0,gaussian1,toReturn);
//        }
//        {
//            double meanI = 0.0;
//            {
//                Mat tmp = new Mat();
//                Mat tmp2 = new Mat();
//                Core.absdiff(toReturn,Mat.zeros(toReturn.size(),toReturn.type()),tmp2);
//                Core.pow(tmp2,alpha,tmp);
//                meanI = Core.mean(tmp).val[0];
//            }
//            Core.divide(Math.pow(meanI, 1.0/alpha),toReturn,toReturn);
//        }
//
//        {
//            double meanI = 0.0;
//            {
//                Mat tmp = new Mat();
//                Mat tmp2 = new Mat();
//                Mat mined = new Mat();
//                Core.absdiff(toReturn,Mat.zeros(toReturn.size(),toReturn.type()),tmp2);
//                // TODO: Core.min(tmp2,tau);
//                Core.pow(tmp2,alpha,tmp);
//                meanI = Core.mean(tmp).val[0];
//            }
//            Core.divide(Math.pow(meanI, 1.0/alpha),toReturn,toReturn);
//        }
//        {
//            Mat exp_x = new Mat(), exp_negx =  new Mat();
//            Mat a = new Mat();
//            Mat minus_a = new Mat();
//            Core.divide(tau,toReturn,a);
//            Core.divide(-tau,toReturn,minus_a);
//            Core.exp(a,exp_x);
//            Core.exp(minus_a,exp_negx);
//            Core.subtract(exp_x,exp_negx,a);
//            Core.add(exp_x,exp_negx,minus_a);
//            Core.divide(a,minus_a,toReturn);
//            Core.multiply(toReturn,new Scalar(tau),toReturn);
//        }
//        return toReturn;
//    }

}

