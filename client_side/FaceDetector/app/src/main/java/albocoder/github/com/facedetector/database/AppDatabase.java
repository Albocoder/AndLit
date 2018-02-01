package albocoder.github.com.facedetector.database;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import albocoder.github.com.facedetector.database.daos.*;
import albocoder.github.com.facedetector.database.entities.*;

@Database(entities = {API_key.class, Classifier.class, detected_faces.class, KnownPPL.class,
        misc_info.class, UserLogin.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract api_keys_dao apiDao();
    public abstract classifier_dao classifierDao();
    public abstract detected_faces_dao facesDao();
    public abstract known_ppl_dao knownDao();
    public abstract misc_info_dao miscDao();
    public abstract user_login_dao userDao();

    @VisibleForTesting
    public static final String DATABASE_NAME = "local-andlit-database";

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
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
        INSTANCE = null;
    }
}
