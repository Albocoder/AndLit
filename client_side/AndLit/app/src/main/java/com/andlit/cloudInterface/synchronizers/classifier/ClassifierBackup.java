package com.andlit.cloudInterface.synchronizers.classifier;

import android.content.Context;

import com.andlit.cloudInterface.synchronizers.classifier.model.ClassifierStats;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.Classifier;
import com.andlit.database.entities.UserLogin;
import com.andlit.face.FaceRecognizerSingleton;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// todo: test this
public class ClassifierBackup {
    // constants
    private static final String TAG = "ClassifierBackup";

    // variables
    private ClassifierBackupAPI a;
    private UserLogin ul;
    private File classifierFile;
    private Context c;

    public ClassifierBackup(Context c) {
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        a = api.create(ClassifierBackupAPI.class);
        this.c = c;
        AppDatabase db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
        Classifier cls = db.classifierDao().getClassifier();
        classifierFile = new File(c.getFilesDir(),cls.path);
    }

    public boolean saveClassifier() throws IOException {
        RequestBody file = RequestBody.create(MediaType.parse("application/x-sqlite3"),classifierFile);
        MultipartBody.Part fileFromElement =
                MultipartBody.Part.createFormData("uploaded_file", classifierFile.getName(), file);
        Call<JsonObject> call = a.uploadClassifier("Token "+ul.access_token,fileFromElement);
        Response<JsonObject> resp;
        resp = call.execute();
        int code = resp.code();
        return code >= 200 && code < 300;
    }

    public ClassifierStats getInfoAboutUploadedCls() throws IOException {
        Call<JsonObject> call = a.showClassifierStats("Token "+ul.access_token);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if (code >= 200 && code < 300) {
            JsonObject o = resp.body();
            if (o == null)
                return null;
            // can be extended here if the API gives more info
            long fileSize = o.get("file_size").getAsLong();
            return new ClassifierStats(fileSize);
        }
        return null;
    }

    public boolean loadClassifier()  {
        Call<ResponseBody> call = a.downloadClassifier("Token "+ul.access_token);
        Response<ResponseBody> resp = null;
        try {
            resp = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(resp == null)
            return false;
        int code = resp.code();
        if (code >= 200 && code < 300) {
            ResponseBody body = resp.body();
            if(body == null)
                return false;
            InputStream is = body.byteStream();
            if(is == null)
                return false;
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(classifierFile,false);
            } catch (FileNotFoundException ignored) { return false; }

            byte [] buffer = new byte[1024];
            while (true) {
                int read = 0;

                try {
                    read = is.read(buffer);
                } catch (IOException ignored){}

                if (read == -1)
                    break;

                try {
                    fos.write(buffer, 0, read);
                } catch (IOException ignored){}
            }

            try {
                fos.flush();
            } catch (IOException ignored) { return false; }
            try {
                fos.close();
                is.close();
            } catch (IOException ignored) { }
            return true;
        }
        return false;
    }

}
