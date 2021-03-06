package com.andlit.database;

import android.arch.persistence.room.Database;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.andlit.database.daos.andlit_device_connection_dao;
import com.andlit.database.daos.andlit_device_dao;
import com.andlit.database.daos.classifier_dao;
import com.andlit.database.daos.detected_faces_dao;
import com.andlit.database.daos.known_ppl_dao;
import com.andlit.database.daos.misc_info_dao;
import com.andlit.database.daos.pools_dao;
import com.andlit.database.daos.training_face_dao;
import com.andlit.database.daos.user_login_dao;
import com.andlit.database.daos.wifi_credentials_dao;
import com.andlit.database.entities.API_key;
import com.andlit.database.entities.AndLitDevice;
import com.andlit.database.entities.AndLitDeviceConnection;
import com.andlit.database.entities.Classifier;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.Pool;
import com.andlit.database.entities.UserLogin;
import com.andlit.database.entities.WifiCredentials;
import com.andlit.database.entities.detected_face;
import com.andlit.database.entities.misc_info;
import com.andlit.database.entities.training_face;


@Database(entities = {API_key.class, Classifier.class, detected_face.class, KnownPPL.class,
        misc_info.class, UserLogin.class, training_face.class, Pool.class, AndLitDevice.class,
        AndLitDeviceConnection.class, WifiCredentials.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract classifier_dao classifierDao();
    public abstract detected_faces_dao detectedFacesDao();
    public abstract known_ppl_dao knownPplDao();
    public abstract misc_info_dao miscInfoDao();
    public abstract user_login_dao userLoginDao();
    public abstract training_face_dao trainingFaceDao();
    public abstract pools_dao poolsDao();
    public abstract andlit_device_connection_dao andlitDeviceConnection();
    public abstract andlit_device_dao andlitDeviceDao();
    public abstract wifi_credentials_dao wifiCredentialsDao();

    public static final String DATABASE_NAME = "local-andlit-database";

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class,DATABASE_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        if(INSTANCE != null)
            INSTANCE.close();
        INSTANCE = null;
    }
}
