package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.Date;

import albocoder.github.com.facedetector.database.entities.Classifier;

@Dao
public interface classifier_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertClassifier(Classifier key);

    @Query("select * from classifier")
    Classifier getClassifier();

    // TODO: implements more functions!
    @Query("select `hash` from classifier")
    String getHash();

    @Query("select `path` from classifier")
    String getPath();

    @Query("select `last_update` from classifier")
    Date getLastModified();

    @Query("delete from classifier")
    void deleteClassifier();
}
