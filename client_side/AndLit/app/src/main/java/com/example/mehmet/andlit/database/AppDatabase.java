package com.example.mehmet.andlit.database;

import android.arch.persistence.room.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.example.mehmet.andlit.database.daos.api_keys_dao;
import com.example.mehmet.andlit.database.daos.classifier_dao;
import com.example.mehmet.andlit.database.daos.detected_faces_dao;
import com.example.mehmet.andlit.database.daos.known_ppl_dao;
import com.example.mehmet.andlit.database.daos.misc_info_dao;
import com.example.mehmet.andlit.database.daos.settings_dao;
import com.example.mehmet.andlit.database.daos.training_face_dao;
import com.example.mehmet.andlit.database.daos.user_login_dao;
import com.example.mehmet.andlit.database.entities.API_key;
import com.example.mehmet.andlit.database.entities.Classifier;
import com.example.mehmet.andlit.database.entities.KnownPPL;
import com.example.mehmet.andlit.database.entities.Setting;
import com.example.mehmet.andlit.database.entities.UserLogin;
import com.example.mehmet.andlit.database.entities.detected_face;
import com.example.mehmet.andlit.database.entities.misc_info;
import com.example.mehmet.andlit.database.entities.training_face;


@Database(entities = {API_key.class, Classifier.class, detected_face.class, KnownPPL.class,
        misc_info.class, UserLogin.class, training_face.class, Setting.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract api_keys_dao apiKeysDao();
    public abstract classifier_dao classifierDao();
    public abstract detected_faces_dao detectedFacesDao();
    public abstract known_ppl_dao knownPplDao();
    public abstract misc_info_dao miscInfoDao();
    public abstract user_login_dao userLoginDao();
    public abstract training_face_dao trainingFaceDao();
    public abstract settings_dao settingsDao();

    @VisibleForTesting
    public static final String DATABASE_NAME = "local-andlit-database";

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class,DATABASE_NAME)
                            // Don't uncomment this!
                            .allowMainThreadQueries()

                            // MAYBE? -> recreate the database if necessary
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        if(INSTANCE != null && INSTANCE.isOpen())
            INSTANCE.close();
        INSTANCE = null;
    }
}
