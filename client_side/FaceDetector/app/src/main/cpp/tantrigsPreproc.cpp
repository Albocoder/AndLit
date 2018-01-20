#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <iostream>

#include <fstream>
#include <sstream>

using namespace cv;
using namespace std;

Mat norm_0_255(const Mat& src) {
    Mat dst;
    switch(src.channels()) {
        case 1:
            cv::normalize(src, dst, 0, 255, NORM_MINMAX, CV_8UC1);
            break;
        case 3:
            cv::normalize(src, dst, 0, 255, NORM_MINMAX, CV_8UC3);
            break;
        default:
            src.copyTo(dst);
            break;
    }
    return dst;
}

Mat tan_triggs_preprocessing(InputArray src,
                             float alpha = 0.1, float tau = 10.0, float gamma = 0.2, int sigma0 = 1,
                             int sigma1 = 2) {

    // Convert to floating point:
    Mat X = src.getMat();
    X.convertTo(X, CV_32FC1);
    // Start preprocessing:
    Mat I;
    pow(X, gamma, I);
    // Calculate the DOG Image:
    {
        Mat gaussian0, gaussian1;
        // Kernel Size:
        int kernel_sz0 = (3*sigma0);
        int kernel_sz1 = (3*sigma1);
        // Make them odd for OpenCV:
        kernel_sz0 += ((kernel_sz0 % 2) == 0) ? 1 : 0;
        kernel_sz1 += ((kernel_sz1 % 2) == 0) ? 1 : 0;
        GaussianBlur(I, gaussian0, Size(kernel_sz0,kernel_sz0), sigma0, sigma0, BORDER_REPLICATE);
        GaussianBlur(I, gaussian1, Size(kernel_sz1,kernel_sz1), sigma1, sigma1, BORDER_REPLICATE);
        subtract(gaussian0, gaussian1, I);
    }

    {
        double meanI = 0.0;
        {
            Mat tmp;
            pow(abs(I), alpha, tmp);
            meanI = mean(tmp).val[0];

        }
        I = I / pow(meanI, 1.0/alpha);
    }

    {
        double meanI = 0.0;
        {
            Mat tmp;
            pow(min(abs(I), tau), alpha, tmp);
            meanI = mean(tmp).val[0];
        }
        I = I / pow(meanI, 1.0/alpha);
    }

    // Squash into the tanh:
    {
        Mat exp_x, exp_negx;
        exp( I / tau, exp_x );
        exp( -I / tau, exp_negx );
        divide( exp_x - exp_negx, exp_x + exp_negx, I );
        I = tau * I;
    }
    return I;
}
//int main(int argc, const char *argv[]) {
//    // Get filename to the source image:
//    if (argc != 2) {
//        cout << "usage: " << argv[0] << " <image.ext>" << endl;
//        exit(1);
//    }
//    // Load image & get skin proportions:
//    Mat image = imread(argv[1], CV_LOAD_IMAGE_GRAYSCALE);
//    // Calculate the TanTriggs Preprocessed image with default parameters:
//    Mat preprocessed = tan_triggs_preprocessing(image);
//    // Draw it on screen:
//    imshow("Original Image", image);
//    imshow("TanTriggs Preprocessed Image", norm_0_255(preprocessed));
//    // Show the images:
//    waitKey(0);
//    // Success!
//    return 0;
//}