package albocoder.github.com.facedetector.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import albocoder.github.com.facedetector.R;

@Deprecated
public class LocalDBInterface{
    public static final String DATABASE_NAME = "localData.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TAG = "Interface:LocalDB";

    private final Context c;

    public LocalDBInterface(Context context) {
        c = context;
        Log.i(TAG,"instantiated !");
    }

    public void initDatabase(SQLiteDatabase db) {
        Log.d(TAG,"onCreate called!");
        InputStream is = c.getResources().openRawResource(R.raw.db_init);
        byte [] buffer = new byte[512];
        int bytesRead;
        StringBuffer sqlQueries = new StringBuffer();
        try {
            while ((bytesRead = is.read(buffer)) != -1){
                byte [] readBytes = Arrays.copyOfRange(buffer,0,bytesRead);
                sqlQueries.append(new String(readBytes,"UTF-8"));
            }
            db.execSQL(sqlQueries.toString());
        } catch (IOException e) {
            String query = "CREATE TABLE IF NOT EXISTS API_keys (\n" +
                    "  fb       integer(10), \n" +
                    "  twitter  integer(10), \n" +
                    "  insta    integer(10), \n" +
                    "  linkedin integer(10));\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS Classifier (\n" +
                    "  path        varchar(50) NOT NULL, \n" +
                    "  hash        varchar(32) NOT NULL, \n" +
                    "  last_update date NOT NULL, \n" +
                    "  num_recogn  integer(10) NOT NULL);\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS detectedfaces (\n" +
                    "  id         integer(10) NOT NULL, \n" +
                    "  path       varchar(50) NOT NULL, \n" +
                    "  hash       varchar(32) NOT NULL, \n" +
                    "  date_taken date NOT NULL, \n" +
                    "  PRIMARY KEY (id), \n" +
                    "  FOREIGN KEY(id) REFERENCES KnownPPL(id));\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS KnownPPL (\n" +
                    "  id      integer(10) NOT NULL, \n" +
                    "  name    varchar(30), \n" +
                    "  sname   varchar(30), \n" +
                    "  dob     date, \n" +
                    "  age     smallint(5), \n" +
                    "  address varchar(60), \n" +
                    "  PRIMARY KEY (id));\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS misc_info (\n" +
                    "  id        integer(10) NOT NULL,\n" +
                    "  key       varchar(30) NOT NULL,\n" +
                    "  desc      varchar(100) NOT NULL,\n" +
                    "  PRIMARY KEY (id), \n" +
                    "  FOREIGN KEY(id) REFERENCES KnownPPL(id));\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS UserLogin (\n" +
                    "  id           INTEGER NOT NULL PRIMARY KEY, \n" +
                    "  username     varchar(30) NOT NULL, \n" +
                    "  access_token varchar(32) NOT NULL);";
            db.execSQL(query);
        }
    }
}
