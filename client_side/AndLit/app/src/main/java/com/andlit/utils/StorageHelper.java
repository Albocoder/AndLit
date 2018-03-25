package com.andlit.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

public class StorageHelper {
    final static String TAG = "StorageHelper";

    public static CascadeClassifier loadClassifierCascade(Context context, int resId) {
        FileOutputStream fos = null;
        InputStream inputStream;

        inputStream = context.getResources().openRawResource(resId);
        File xmlDir = context.getDir("xml", Context.MODE_PRIVATE);
        File cascadeFile = new File(xmlDir, "temp.xml");
        try {
            fos = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.d(TAG, "Can\'t load the cascade file");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        CascadeClassifier detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (detector.isNull()) {
            Log.e(TAG, "Failed to load cascade classifier");
            detector = null;
        } else {
            Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
        }
        // delete the temporary directory
        cascadeFile.delete();

        return detector;
    }
    public static File loadXmlFromRes2File(Context context, int resId, String filename) {
        FileOutputStream fos = null;
        InputStream inputStream;

        inputStream = context.getResources().openRawResource(resId);
        File trainDir = context.getDir("xml", Context.MODE_PRIVATE);
        File trainFile = new File(trainDir, filename);
        try {
            fos = new FileOutputStream(trainFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.d(TAG, "Can\'t load the train file");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return trainFile;
    }
    @NonNull
    public static String getFilePathFromAssets(Context c, String path,String newFilePath, String newFileName) throws IOException {
        InputStream is = c.getAssets().open(path);
        File cascadeDir = c.getDir(newFilePath, Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, newFileName);
        FileOutputStream os = new FileOutputStream(mCascadeFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1)
            os.write(buffer, 0, bytesRead);
        is.close();
        os.close();
        return  mCascadeFile.getAbsolutePath();
    }
    public static File saveMat2AssetsFile(Mat mat, String filePath, String fileName) {
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdir();
        }
        File file = new File(path, fileName);
        Mat mat2Save = new Mat();
        cvtColor(mat, mat2Save, COLOR_RGB2BGR);
        boolean result = imwrite(file.toString(), mat2Save);
        mat2Save.release();
        if (result)
            return file;
        else
            return null;
    }
    public static String MD5toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase();
    }
    public static byte[] getMD5OfFile(String filepath) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(filepath);
        return getMD5OfFile(fis);
    }
    public static byte[] getMD5OfFile(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(is,md);
        byte[] buffer = new byte[4096];
        try {
            while (dis.read(buffer) != -1);
        } finally { dis.close(); }
        byte[] digest = md.digest();
        return digest;
    }
    public static String writeMat(Context c,Mat m,String path) throws IOException {
        File f = new File(path);
        if(!f.exists())
            f.createNewFile();
        if(imwrite(f.getAbsolutePath(),m))
            return f.getAbsolutePath();
        else
            return null;
    }
    public static String writeMat(Context c,Mat m) throws IOException {
        long l = System.currentTimeMillis();
        int random = new Random().nextInt();
        File defaultPlace = new File(c.getFilesDir() + File.separator+l+".png");
        if(defaultPlace.exists())
            defaultPlace.delete();
        return writeMat(c,m,defaultPlace.getAbsolutePath());
    }
    public static Mat readMat(String path){
        return imread(path);
    }
}
