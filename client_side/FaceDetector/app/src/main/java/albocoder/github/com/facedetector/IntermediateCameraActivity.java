package albocoder.github.com.facedetector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.database.entities.KnownPPL;
import albocoder.github.com.facedetector.database.entities.training_face;
import albocoder.github.com.facedetector.face.Face;
import albocoder.github.com.facedetector.face.FaceOperator;
import albocoder.github.com.facedetector.face.FaceRecognizerSingleton;
import albocoder.github.com.facedetector.face.RecognizedFace;
import albocoder.github.com.facedetector.utils.StorageHelper;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class IntermediateCameraActivity extends Activity {

    private static final String TAG = "IntermediateCamActivity";
    public static final String ARGUMENT_KEY = "filename";
    public static final int REQUEST_IMG_ANALYSIS = 1336;

    // fields
    private File imageLocation;
    FaceRecognizerSingleton frs;
    private AppDatabase db;

    // view fields
    private ImageView analyzed;
    private Button takeImage;
    ProgressDialog progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_processed);
        takeImage = (Button) findViewById(R.id.BtnCpt);
        analyzed = (ImageView) findViewById(R.id.AnalyzedImg);

        db = AppDatabase.getDatabase(this);
        train(); frs = new FaceRecognizerSingleton(this);

        File root = new File(getFilesDir(), ImgGrabber.CAPTURED_DIR);
        if (!root.exists())
            root.mkdirs();
        imageLocation = new File(root,"capture_"+System.currentTimeMillis()+".png");
        if(imageLocation.exists())
            imageLocation.delete();
        try {
            imageLocation.createNewFile();
        } catch (IOException e) {
            Log.d(TAG,"Couldn't open file to save the image!");
            exitActivity(-1);
            return;
        }
        progress = new ProgressDialog(this);
        progress.setTitle("Analyzing!");
        progress.setMessage("Please wait while we process the image...");
        progress.setCancelable(false);

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(IntermediateCameraActivity.this
                        ,ImgGrabber.class);
                i.putExtra(ARGUMENT_KEY,imageLocation.getAbsolutePath());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(i,REQUEST_IMG_ANALYSIS);
                progress.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMG_ANALYSIS) {
            if (imageLocation.length() == 0)
                Toast.makeText(this, "Error in taking the image!", Toast.LENGTH_SHORT);
            else {
                imwrite(imageLocation.getAbsolutePath(),process());
                Bitmap result = BitmapFactory.decodeFile(imageLocation.getAbsolutePath());
                analyzed.setImageBitmap(result);
            }
            progress.dismiss();
        }
    }

    private void exitActivity(int code){
        setResult(code);
        finish();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLocation!= null)
            imageLocation.delete();
    }
    opencv_core.Mat process() {
        opencv_core.Mat toAnalyze = imread(imageLocation.getAbsolutePath());
//        resize(toAnalyze,toAnalyze,new opencv_core.Size(900,1000)); // todo remove this line and test
        FaceOperator fop = new FaceOperator(this,toAnalyze);
        Face[] faces = fop.getFaces();
        if (faces == null)
            return null;
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();

            RecognizedFace rf = frs.recognize(aFacesArray);
            int [] labels = rf.getLabels();
            //double [] conf = rf.getConfidences();

            KnownPPL p = db.knownPplDao().getEntryWithID(labels[0]);
            if (p!= null)
                putText(toAnalyze,p.name+" "+p.sname,new opencv_core.Point(x, y),CV_FONT_HERSHEY_PLAIN
                        ,6, opencv_core.Scalar.MAGENTA,4,1,false);
            rectangle(toAnalyze,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    , opencv_core.Scalar.GREEN,2, LINE_8,0);
        }
        fop.destroy();
        return toAnalyze;
    }
    void train(){
        if (frs != null)
            return;
        // testing classifier
        db.knownPplDao().insertEntry(
                new KnownPPL(1,"Erin","Avllazagaj",new Date(1996,3,13), 21,"Dorm #76 Bilkent") );
        db.knownPplDao().insertEntry(
                new KnownPPL(2,"Barack","Obama",new Date(1961,8,4), 56,"USA") );
        db.knownPplDao().insertEntry(
                new KnownPPL(3,"Argert","Boja",new Date(2016,2,2), 2,"107") );
        // random generator for md5 (NEVER DO THIS IRL) use: StorageHelper.getMD5OfFile()
        Random r = new Random();

        // delete all instances
        List<training_face> recs =  db.trainingFaceDao().getAllRecords();
        if(recs.size() != 0)
            return;
//        for (training_face tf : recs)
//            FaceOperator.deleteTrainingInstance(this,tf);

        List<Integer> erinIndex  = new ArrayList<Integer>();
        erinIndex.add(1);erinIndex.add(2);erinIndex.add(3);erinIndex.add(4);erinIndex.add(5);
        List<Integer> obamaIndex  = new ArrayList<>();
        obamaIndex.add(6);obamaIndex.add(7);obamaIndex.add(8);obamaIndex.add(9);obamaIndex.add(10);obamaIndex.add(11);
        List<Integer> argertIndex  = new ArrayList<>();
        argertIndex.add(13);argertIndex.add(14);argertIndex.add(16);
        List<Integer> testing = new ArrayList<>();
        testing.add(15);testing.add(12);
        Face detected = null;
        for(int i = 1; i < 17; i++) {
            String filepath;
            try {
                filepath = StorageHelper.getFilePathFromAssets(this,"training/face"+i+".png","training","face"+i+".png");
            } catch (IOException e) { continue; }
            String md5 = ""+r.nextLong()+r.nextInt();
            try {
                md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(filepath));
            } catch (IOException|NoSuchAlgorithmException e) {}

            opencv_core.Mat m = imread(filepath);
            if (m==null)
                continue;
            detected = new Face(m);
            if(erinIndex.contains(i)) {
                detected.setID(1);
            }
            else if(obamaIndex.contains(i)){
                detected.setID(2);
            }
            else if(argertIndex.contains(i)){
                detected.setID(3);
            }else{
//                frs = new FaceRecognizerSingleton(this);
//                RecognizedFace rf = frs.recognize(detected);
//                Log.d(TAG,"Face"+i+" prediction: "+rf.toString());
                continue;
            }
            try{
                training_face f = FaceOperator.moveDetectionToTraining(this,
                        FaceOperator.saveDetectedFaceToDatabase(this,detected));
                Log.d(TAG,f.toString());
            }catch (NoSuchAlgorithmException|IOException e) {
                e.printStackTrace();
            }
            new File(filepath).delete();
        }
    }
}
