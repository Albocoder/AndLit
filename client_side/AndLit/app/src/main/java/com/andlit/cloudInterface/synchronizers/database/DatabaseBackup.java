package com.andlit.cloudInterface.synchronizers.database;

import android.content.Context;

import com.andlit.cloudInterface.synchronizers.database.model.DatabaseStats;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonArray;
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

public class DatabaseBackup {

    // constants
    private static final String TAG = "DatabaseBackup";

    // variables
    private DatabaseBackupAPI a;
    private UserLogin ul;
    private File dbFile;
    private Context c;

    public DatabaseBackup(Context c) throws Exception {
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.c = c;
        a = api.create(DatabaseBackupAPI.class);
        dbFile = c.getDatabasePath(AppDatabase.DATABASE_NAME);
        if(!dbFile.exists())
            throw new Exception("Database doesn't exist!");
        AppDatabase db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
    }

    public boolean saveDatabase() throws Throwable {
        AppDatabase.destroyInstance();
        RequestBody file = RequestBody.create(MediaType.parse("application/x-sqlite3"),dbFile);
        MultipartBody.Part fileFromElement =
                MultipartBody.Part.createFormData("uploaded_file", dbFile.getName(), file);
        Call<JsonObject> call = a.uploadDatabase("Token "+ul.access_token,fileFromElement);
        Response<JsonObject> resp;
        try {
            resp = call.execute();
        } catch (IOException e) {
            AppDatabase.getDatabase(c);
            throw e.fillInStackTrace();
        }
        int code = resp.code();
        AppDatabase.getDatabase(c);
        return code >= 200 && code < 300;
    }

    public DatabaseStats getInfoAboutUploadedDB() throws IOException {
        Call<JsonArray> call = a.showDatabaseStats("Token "+ul.access_token);
        Response<JsonArray> resp = call.execute();
        int code = resp.code();
        if (code >= 200 && code < 300) {
            JsonArray arr = resp.body();
            if(arr == null || arr.size() == 0)
                return null;
            JsonObject o = arr.get(0).getAsJsonObject();
            // can be extended here if the API gives more info
            long fileSize = o.get("file_size").getAsLong();
            return new DatabaseStats(fileSize);
        }
        return null;
    }

    public boolean loadDatabase() throws IOException {
        Call<ResponseBody> call = a.downloadDatabase("Token "+ul.access_token);
        Response<ResponseBody> resp = call.execute();
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
                fos = new FileOutputStream(dbFile,false);
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

    public boolean backupDatabase( DatabaseStats inCloud ) throws Throwable {
        long localSize = dbFile.length();
        long remoteSize = inCloud.getSize();
        if( localSize != remoteSize )
            return saveDatabase();
        return true;
    }

    public boolean restoreDatabase( DatabaseStats inCloud ) throws IOException {
        long localSize = dbFile.length();
        long remoteSize = inCloud.getSize();
        if( localSize != remoteSize )
            return loadDatabase();
        return true;
    }
}
